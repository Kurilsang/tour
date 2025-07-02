package com.tour.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByIdRequest;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.tour.model.WxPaymentRequest;
import com.tour.model.WxPaymentResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author kuril
 * @Description 微信支付工具类
 */
@Slf4j
@Component
public class WxPaymentUtil {

    @Value("${wechat.pay.merchant-id}")
    private String merchantId;

    @Value("${wechat.pay.private-key-path}")
    private String privateKeyPath;

    @Value("${wechat.pay.merchant-serial-number}")
    private String merchantSerialNumber;

    @Value("${wechat.pay.public-key-id}")
    private String publicKeyId;

    @Value("${wechat.pay.api-v3-key}")
    private String apiV3Key;
    
    @Value("${wechat.pay.appid}")
    private String appid;

    @Value("${wechat.pay.public-key-path}")
    private String publicKeyPath;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    // 通知解析器，懒加载
    private NotificationParser notificationParser;

    /**
     * 获取微信支付预支付信息
     * 
     * @param request 支付请求参数
     * @return 预支付响应
     */
    public WxPaymentResponseVO wxGetPrePayment(WxPaymentRequest request) {
        try {
            log.info("开始调用微信支付预支付接口，total: {}, openid: {}, description: {}", 
                    request.getTotal(), request.getOpenid(), request.getDescription());
            
            // 先使用JsapiService获取prepayId
            Config config = getConfig();
            JsapiService jsapiService = new JsapiService.Builder().config(config).build();
            
            // 构建请求
            PrepayRequest prepayRequest = new PrepayRequest();
            Amount amount = new Amount();
            amount.setTotal(request.getTotal());
            prepayRequest.setAmount(amount);
            prepayRequest.setAppid(appid);
            prepayRequest.setMchid(merchantId);
            prepayRequest.setDescription(request.getDescription());
            prepayRequest.setNotifyUrl(request.getNotifyUrl());

            
            // 使用传入的商户订单号，如果为空则生成一个
            String outTradeNo = request.getOutTradeNo();
            if (!StringUtils.hasText(outTradeNo)) {
                outTradeNo = generateOutTradeNo();
            }
            prepayRequest.setOutTradeNo(outTradeNo);
            
            Payer payer = new Payer();
            payer.setOpenid(request.getOpenid());
            prepayRequest.setPayer(payer);

            // 先获取prepayId
            PrepayResponse prepayResponse = jsapiService.prepay(prepayRequest);
            String prepayId = prepayResponse.getPrepayId();
            
            log.info("获取到prepayId: {}", prepayId);
            
            // 再使用JsapiServiceExtension获取支付所需参数
            JsapiServiceExtension serviceExtension = new JsapiServiceExtension.Builder().config(config).build();
            PrepayWithRequestPaymentResponse response = serviceExtension.prepayWithRequestPayment(prepayRequest);
            
            log.info("微信支付预支付接口调用成功，outTradeNo: {}", outTradeNo);

            // 构造返回数据
            WxPaymentResponseVO responseVO = new WxPaymentResponseVO();
            responseVO.setTimeStamp(response.getTimeStamp());
            responseVO.setNonceStr(response.getNonceStr());
            responseVO.setPackageStr(response.getPackageVal());
            responseVO.setSignType(response.getSignType());
            responseVO.setPaySign(response.getPaySign());
            responseVO.setOrderNo(outTradeNo);
            responseVO.setPrepayId(prepayId); // 使用先前获取的prepayId
            
            log.info("微信支付参数构建完成: {}", responseVO);
            
            return responseVO;
        } catch (Exception e) {
            log.error("调用微信支付预支付接口异常", e);
            throw new RuntimeException("微信支付预支付接口调用失败", e);
        }
    }
    
    /**
     * 使用云托管环境进行统一下单
     * 
     * @param request 支付请求参数，包含云托管环境信息
     * @return 预支付响应
     */
    public WxPaymentResponseVO wxGetPrePaymentWithCloudEnv(WxPaymentRequest request) {
        try {
            log.info("开始调用微信支付云托管统一下单接口，total: {}, openid: {}, description: {}, envId: {}", 
                    request.getTotal(), request.getOpenid(), request.getDescription(), request.getEnvId());
            
            // 构建请求参数
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("body", request.getDescription());
            requestMap.put("openid", request.getOpenid());
            
            // 使用传入的商户订单号，如果为空则生成一个
            String outTradeNo = request.getOutTradeNo();
            if (!StringUtils.hasText(outTradeNo)) {
                outTradeNo = generateOutTradeNo();
            }
            requestMap.put("out_trade_no", outTradeNo);
            
            // IP地址，可以是固定值或从请求中获取
            requestMap.put("spbill_create_ip", "127.0.0.1");
            
            // 云托管环境设置
            requestMap.put("env_id", request.getEnvId());
            Integer totalFee = request.getTotal();
            log.info("total_fee值为{}",totalFee);
            requestMap.put("total_fee", totalFee);
            requestMap.put("callback_type", request.getCallbackType());
            requestMap.put("sub_mch_id",merchantId);

            if (request.getContainer() != null) {
                Map<String, String> container = new HashMap<>();
                container.put("service", request.getContainer().getService());
                container.put("path", request.getContainer().getPath());
                requestMap.put("container", container);
            }
            
            // 设置HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 创建请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);
            
            // 发送POST请求
            log.info("发送post请求{}", requestMap);
            log.info("post请求全体信息{}",requestEntity);
            String apiUrl = "http://api.weixin.qq.com/_/pay/unifiedOrder";
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);
            log.info("返回内容{}",responseEntity);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = responseEntity.getBody();
                log.info("微信支付云托管统一下单成功: {}", responseBody);
                
                // 检查返回码
                Integer errcode = (Integer) responseBody.get("errcode");
                if (errcode != null && errcode == 0) {
                    // 获取支付参数
                    Map<String, Object> respData = (Map<String, Object>) responseBody.get("respdata");
                    Map<String, Object> payment = (Map<String, Object>) respData.get("payment");
                    
                    // 构建返回对象
                    WxPaymentResponseVO responseVO = new WxPaymentResponseVO();
                    responseVO.setTimeStamp((String) payment.get("timeStamp"));
                    responseVO.setNonceStr((String) payment.get("nonceStr"));
                    responseVO.setPackageStr((String) payment.get("package"));
                    responseVO.setSignType((String) payment.get("signType"));
                    responseVO.setPaySign((String) payment.get("paySign"));
                    responseVO.setOrderNo(outTradeNo);
                    responseVO.setPrepayId((String) respData.get("prepay_id"));
                    
                    log.info("微信支付云托管统一下单参数构建完成: {}", responseVO);
                    return responseVO;
                } else {
                    String errmsg = (String) responseBody.get("errmsg");
                    log.error("微信支付云托管统一下单API返回错误: {}, {}", errcode, errmsg);
                    throw new RuntimeException("微信支付云托管统一下单失败: " + errmsg);
                }
            } else {
                log.error("微信支付云托管统一下单API请求失败: {}", responseEntity.getStatusCode());
                throw new RuntimeException("微信支付云托管统一下单请求失败: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            log.error("调用微信支付云托管统一下单接口异常", e);
            throw new RuntimeException("微信支付云托管统一下单接口调用失败", e);
        }
    }
    
    /**
     * 查询订单状态（通过微信支付订单号）
     * 
     * @param transactionId 微信支付订单号
     * @return 交易信息
     */
    public Transaction queryOrderById(String transactionId) {
        log.info("开始查询微信支付订单状态, transactionId: {}", transactionId);
        
        try {
            Config config = getConfig();
            JsapiService service = new JsapiService.Builder().config(config).build();
            
            QueryOrderByIdRequest queryRequest = new QueryOrderByIdRequest();
            queryRequest.setMchid(merchantId);
            queryRequest.setTransactionId(transactionId);
            Transaction result = service.queryOrderById(queryRequest);
            log.info("订单查询成功, 订单状态: {}, 交易类型: {}", result.getTradeState(), result.getTradeType());
            return result;
        } catch (ServiceException e) {
            log.error("查询订单失败, 错误码: {}, 错误信息: {}, 响应体: {}", 
                    e.getErrorCode(), e.getErrorMessage(), e.getResponseBody());
            throw e;
        } catch (Exception e) {
            log.error("查询订单异常", e);
            throw new RuntimeException("查询微信支付订单状态失败", e);
        }
    }
    
    /**
     * 查询订单状态（通过商户订单号）
     * 
     * @param outTradeNo 商户订单号
     * @return 交易信息
     */
    public Transaction queryOrderByOutTradeNo(String outTradeNo) {
        log.info("开始查询微信支付订单状态, outTradeNo: {}", outTradeNo);
        
        try {
            Config config = getConfig();
            JsapiService service = new JsapiService.Builder().config(config).build();
            
            QueryOrderByOutTradeNoRequest queryRequest = new QueryOrderByOutTradeNoRequest();
            queryRequest.setMchid(merchantId);
            queryRequest.setOutTradeNo(outTradeNo);
            Transaction result = service.queryOrderByOutTradeNo(queryRequest);
            log.info("订单查询成功, 订单状态: {}, 交易类型: {}", result.getTradeState(), result.getTradeType());
            return result;
        } catch (ServiceException e) {
            log.error("查询订单失败, 错误码: {}, 错误信息: {}, 响应体: {}", 
                    e.getErrorCode(), e.getErrorMessage(), e.getResponseBody());
            throw e;
        } catch (Exception e) {
            log.error("查询订单异常", e);
            throw new RuntimeException("查询微信支付订单状态失败", e);
        }
    }
    
    /**
     * 关闭订单
     * 
     * @param outTradeNo 商户订单号
     * @return 是否关闭成功
     */
    public boolean closeOrder(String outTradeNo) {
        log.info("开始关闭微信支付订单, outTradeNo: {}", outTradeNo);
        
        try {
            Config config = getConfig();
            JsapiService service = new JsapiService.Builder().config(config).build();
            
            CloseOrderRequest closeRequest = new CloseOrderRequest();
            closeRequest.setMchid(merchantId);
            closeRequest.setOutTradeNo(outTradeNo);
            
            // 方法没有返回值，意味着成功时API返回204 No Content
            service.closeOrder(closeRequest);
            log.info("关闭订单成功, outTradeNo: {}", outTradeNo);
            return true;
        } catch (ServiceException e) {
            log.error("关闭订单失败, 错误码: {}, 错误信息: {}, 响应体: {}", 
                    e.getErrorCode(), e.getErrorMessage(), e.getResponseBody());
            return false;
        } catch (Exception e) {
            log.error("关闭订单异常", e);
            throw new RuntimeException("关闭微信支付订单失败", e);
        }
    }
    
    /**
     * 解析微信支付回调通知
     * 
     * @param requestBody 通知请求体
     * @param request HTTP请求对象
     * @return 解析后的交易信息
     */
    public Transaction parsePayNotify(String requestBody, HttpServletRequest request) {
        try {
            // 构造请求参数
            log.info("支付回调包体{}",requestBody);
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(request.getHeader("Wechatpay-Serial"))
                    .nonce(request.getHeader("Wechatpay-Nonce"))
                    .signature(request.getHeader("Wechatpay-Signature"))
                    .timestamp(request.getHeader("Wechatpay-Timestamp"))
                    .body(requestBody)
                    .build();
            
            // 懒加载获取通知解析器
            if (notificationParser == null) {
                synchronized (this) {
                    if (notificationParser == null) {
                        NotificationConfig config = new RSAPublicKeyConfig.Builder()
                                .merchantId(merchantId)
                                .privateKeyFromPath(privateKeyPath)
                                .publicKeyFromPath(publicKeyPath)
                                .publicKeyId(publicKeyId)
                                .merchantSerialNumber(merchantSerialNumber)
                                .apiV3Key(apiV3Key)
                                .build();
                        notificationParser = new NotificationParser(config);
                    }
                }
            }
            
            // 验签并解析请求体
            return notificationParser.parse(requestParam, Transaction.class);
        } catch (Exception e) {
            log.error("解析微信支付通知异常: {}", e.getMessage(), e);
            throw new RuntimeException("解析微信支付通知失败", e);
        }
    }
    
    /**
     * 验证微信支付通知的商户号
     * 
     * @param transaction 交易信息
     * @return 是否验证通过
     */
    public boolean verifyNotifyMerchantId(Transaction transaction) {
        return merchantId.equals(transaction.getMchid());
    }
    
    /**
     * 创建标准的成功响应
     * 
     * @return 成功响应
     */
    public Map<String, String> createSuccessResponse() {
        Map<String, String> result = new HashMap<>();
        result.put("code", "SUCCESS");
        result.put("message", "成功");
        return result;
    }
    
    /**
     * 创建标准的失败响应
     * 
     * @param errorMessage 错误信息
     * @return 失败响应
     */
    public Map<String, String> createFailResponse(String errorMessage) {
        Map<String, String> result = new HashMap<>();
        result.put("code", "FAIL");
        result.put("message", errorMessage);
        return result;
    }
    
    /**
     * 获取微信支付配置
     * 
     * @return 配置对象
     */
    private Config getConfig() {
        return new RSAPublicKeyConfig.Builder()
                .merchantId(merchantId)
                .privateKeyFromPath(privateKeyPath)
                .publicKeyFromPath(publicKeyPath)
                .publicKeyId(publicKeyId)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3Key)
                .build();
    }
    
    /**
     * 生成商户订单号
     * 
     * @return 商户订单号
     */
    private String generateOutTradeNo() {
        return "out_trade_no_" + System.currentTimeMillis();
    }
} 
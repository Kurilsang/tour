package com.tour.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.exception.HttpException;
import com.wechat.pay.java.core.exception.MalformedMessageException;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author kuril
 * @Description 微信支付退款工具类
 */
@Slf4j
@Component
public class WxRefundUtil {

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
    
    @Value("${wechat.pay.cloud.env-id:}")
    private String cloudEnvId;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 退款申请
     * 
     * @param outTradeNo 商户订单号
     * @param outRefundNo 商户退款单号
     * @param totalAmount 订单总金额，单位为分
     * @param refundAmount 退款金额，单位为分
     * @param reason 退款原因
     * @return 退款结果
     */
    public Refund createRefund(String outTradeNo, String outRefundNo, int totalAmount, int refundAmount, String reason) {
        log.info("开始申请退款，商户订单号: {}, 商户退款单号: {}, 订单金额: {}, 退款金额: {}, 退款原因: {}", 
                outTradeNo, outRefundNo, totalAmount, refundAmount, reason);
        
        try {
            // 初始化服务
            RefundService service = new RefundService.Builder().config(getConfig()).build();
            
            // 构建退款请求
            CreateRequest request = new CreateRequest();
            request.setOutTradeNo(outTradeNo);
            request.setOutRefundNo(outRefundNo);
            
            // 设置退款金额信息
            AmountReq amountReq = new AmountReq();
            amountReq.setTotal((long) totalAmount);
            amountReq.setRefund((long) refundAmount);
            log.info("总金额{},退款金额{}",totalAmount,refundAmount);
            amountReq.setCurrency("CNY");
            request.setAmount(amountReq);
            
            // 设置退款原因
            if (reason != null && !reason.isEmpty()) {
                request.setReason(reason);
            }
            
            // 调用退款接口
            Refund refund = service.create(request);
            log.info("退款申请成功，商户退款单号: {}, 微信退款单号: {}, 退款状态: {}", 
                    refund.getOutRefundNo(), refund.getRefundId(), refund.getStatus());
            
            return refund;
        } catch (HttpException e) {
            log.error("发送退款请求失败: {}, 状态码: {}", e.getMessage(), e.getMessage());
            throw new RuntimeException("申请退款网络请求失败", e);
        } catch (ServiceException e) {
            log.error("退款服务异常: {}, 错误码: {}, 错误信息: {}", 
                    e.getMessage(), e.getErrorCode(), e.getErrorMessage());
            throw new RuntimeException("申请退款服务异常: " + e.getErrorMessage(), e);
        } catch (MalformedMessageException e) {
            log.error("解析退款响应失败: {}", e.getMessage());
            throw new RuntimeException("解析退款响应失败", e);
        } catch (Exception e) {
            log.error("申请退款异常: {}", e.getMessage());
            throw new RuntimeException("申请退款失败", e);
        }
    }
    
    /**
     * 在云托管环境下申请退款
     * 
     * @param outTradeNo 商户订单号
     * @param outRefundNo 商户退款单号
     * @param totalAmount 订单总金额，单位为分
     * @param refundAmount 退款金额，单位为分
     * @param reason 退款原因
     * @param containerService 云托管服务名称
     * @param containerPath 云托管路径
     * @return 退款结果
     */
    public Refund createRefundWithCloudEnv(String outTradeNo, String outRefundNo, int totalAmount, 
                                          int refundAmount, String reason, 
                                          String containerService, String containerPath) {
        log.info("开始在云托管环境下申请退款，商户订单号: {}, 商户退款单号: {}, 订单金额: {}, 退款金额: {}, 退款原因: {}", 
                outTradeNo, outRefundNo, totalAmount, refundAmount, reason);
        
        try {
            // 构建请求参数
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("out_trade_no", outTradeNo);
            requestMap.put("out_refund_no", outRefundNo);
            requestMap.put("sub_mch_id", merchantId);
            requestMap.put("total_fee", totalAmount);
            requestMap.put("refund_fee", refundAmount);
            
            // 设置退款原因
            if (reason != null && !reason.isEmpty()) {
                requestMap.put("refund_desc", reason);
            }
            
            // 设置云托管环境信息
            requestMap.put("env_id", cloudEnvId);
            requestMap.put("callback_type", 2); // 2表示使用云托管回调
            
            // 设置回调容器信息
            Map<String, String> container = new HashMap<>();
            container.put("service", containerService);
            container.put("path", containerPath);
            requestMap.put("container", container);
            
            // 设置HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 创建请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);
            
            // 发送POST请求
            log.info("发送微信支付云托管退款请求: {}", requestMap);
            String apiUrl = "http://api.weixin.qq.com/_/pay/refund";
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);
            
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = responseEntity.getBody();
                log.info("微信支付云托管退款API响应: {}", responseBody);
                
                // 检查返回码
                Integer errcode = (Integer) responseBody.get("errcode");
                if (errcode != null && errcode == 0) {
                    // 获取退款结果
                    Map<String, Object> respData = (Map<String, Object>) responseBody.get("respdata");

                    // 手动构建Refund对象
                    Refund refund = new Refund();
                    refund.setOutTradeNo(outTradeNo);
                    refund.setOutRefundNo(outRefundNo);
                    refund.setRefundId((String) respData.get("refund_id"));
                    refund.setTransactionId((String) respData.get("transaction_id"));
                    refund.setStatus(Status.PROCESSING); // 默认为处理中
                    
                    // 设置金额信息
                    com.wechat.pay.java.service.refund.model.Amount amount = 
                            new com.wechat.pay.java.service.refund.model.Amount();
                    amount.setTotal(Long.valueOf(totalAmount));
                    amount.setRefund(Long.valueOf(refundAmount));
                    amount.setCurrency("CNY");
                    refund.setAmount(amount);
                    
                    // 设置时间信息
                    String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
                    refund.setCreateTime(currentTime);
                    
                    log.info("退款申请成功，商户退款单号: {}, 微信退款单号: {}", 
                            refund.getOutRefundNo(), refund.getRefundId());
                    
                    return refund;
                } else {
                    String errmsg = (String) responseBody.get("errmsg");
                    log.error("微信支付云托管退款API返回错误: {}, {}", errcode, errmsg);
                    throw new RuntimeException("微信支付云托管退款失败: " + errmsg);
                }
            } else {
                log.error("微信支付云托管退款API请求失败: {}", responseEntity.getStatusCode());
                throw new RuntimeException("微信支付云托管退款请求失败: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            log.error("在云托管环境下申请退款异常", e);
            throw new RuntimeException("在云托管环境下申请退款失败", e);
        }
    }
    
    /**
     * 查询退款状态（通过商户退款单号）
     * 
     * @param outRefundNo 商户退款单号
     * @return 退款信息
     */
    public Refund queryRefundByOutRefundNo(String outRefundNo) {
        log.info("开始查询退款状态，商户退款单号: {}", outRefundNo);
        
        try {
            // 初始化服务
            RefundService service = new RefundService.Builder().config(getConfig()).build();
            
            // 构建查询请求
            QueryByOutRefundNoRequest request = new QueryByOutRefundNoRequest();
            request.setOutRefundNo(outRefundNo);


            // 调用查询接口
            Refund refund = service.queryByOutRefundNo(request);
            log.info("查询退款成功，商户退款单号: {}, 微信退款单号: {}, 退款状态: {}", 
                    refund.getOutRefundNo(), refund.getRefundId(), refund.getStatus());
            
            return refund;
        } catch (HttpException e) {
            log.error("发送查询退款请求失败: {}, 状态码: {}", e.getMessage(), e.getMessage());
            throw new RuntimeException("查询退款网络请求失败", e);
        } catch (ServiceException e) {
            log.error("查询退款服务异常: {}, 错误码: {}, 错误信息: {}", 
                    e.getMessage(), e.getErrorCode(), e.getErrorMessage());
            throw new RuntimeException("查询退款服务异常: " + e.getErrorMessage(), e);
        } catch (MalformedMessageException e) {
            log.error("解析查询退款响应失败: {}", e.getMessage());
            throw new RuntimeException("解析查询退款响应失败", e);
        } catch (Exception e) {
            log.error("查询退款异常: {}", e.getMessage());
            throw new RuntimeException("查询退款失败", e);
        }
    }
    
    /**
     * 生成商户退款单号
     * 格式：refund_ + 时间戳 + 5位随机数字
     * 
     * @return 商户退款单号
     */
    public String generateOutRefundNo() {
        return "refund_" + System.currentTimeMillis() + String.format("%05d", (int)(Math.random() * 100000));
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
     * 在云托管环境下查询退款状态
     * 
     * @param outTradeNo 商户订单号
     * @return 退款信息
     */
    public Refund queryRefundWithCloudEnv(String outTradeNo) {
        log.info("开始在云托管环境下查询退款状态，商户订单号: {}", outTradeNo);
        
        try {
            // 构建请求参数
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("out_trade_no", outTradeNo);
            requestMap.put("sub_mch_id", merchantId);
            
            // 设置HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 创建请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);
            
            // 发送POST请求
            log.info("发送微信支付云托管退款查询请求: {}", requestMap);
            String apiUrl = "http://api.weixin.qq.com/_/pay/queryrefund";
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);
            
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = responseEntity.getBody();
                log.info("微信支付云托管退款查询API响应: {}", responseBody);
                
                // 检查返回码
                Integer errcode = (Integer) responseBody.get("errcode");
                if (errcode != null && errcode == 0) {
                    // 获取退款结果
                    Map<String, Object> respData = (Map<String, Object>) responseBody.get("respdata");
                    
                    // 检查业务返回码
                    String returnCode = (String) respData.get("return_code");
                    String resultCode = (String) respData.get("result_code");
                    
                    if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
                        String returnMsg = (String) respData.get("return_msg");
                        String errCodeDes = (String) respData.get("err_code_des");
                        log.error("微信支付云托管退款查询业务失败: return_code={}, return_msg={}, result_code={}, err_code_des={}", 
                                returnCode, returnMsg, resultCode, errCodeDes);
                        
                        throw new RuntimeException("微信支付云托管退款查询业务失败: " + (errCodeDes != null ? errCodeDes : returnMsg));
                    }
                    
                    // 判断是否有退款记录
                    Integer refundCount = (Integer) respData.get("refund_count");
                    if (refundCount == null || refundCount == 0) {
                        log.info("商户订单号[{}]未找到退款记录", outTradeNo);
                        return null;
                    }
                    
                    // 手动构建Refund对象，这里只返回最新的一条退款记录（如有多条）
                    Refund refund = new Refund();
                    refund.setOutTradeNo(outTradeNo);


                    // 获取商户退款单号和微信退款单号（如果有）
                    List<String> outRefundNoList = (List<String>) respData.get("out_refund_no_list");
                    List<String> refundIdList = (List<String>) respData.get("refund_id_list");
                    
                    if (outRefundNoList != null && !outRefundNoList.isEmpty()) {
                        refund.setOutRefundNo(outRefundNoList.get(0));
                    }
                    
                    if (refundIdList != null && !refundIdList.isEmpty()) {
                        refund.setRefundId(refundIdList.get(0));
                    }
                    
                    // 设置退款状态
                    List<String> refundStatusList = (List<String>) respData.get("refund_status_list");
//                    String refundStatus = (String)respData.get("refund_status_$n");
//                    log.info("refund_status_$n:{}",refundStatus);
                    if (refundStatusList != null && !refundStatusList.isEmpty()) {
                        String statusStr = refundStatusList.get(0);

                        if ("SUCCESS".equals(statusStr)) {
                            refund.setStatus(Status.SUCCESS);
                        } else if ("PROCESSING".equals(statusStr)) {
                            refund.setStatus(Status.PROCESSING);
                        } else if ("ABNORMAL".equals(statusStr)) {
                            refund.setStatus(Status.ABNORMAL);
                        } else if ("CLOSED".equals(statusStr)) {
                            refund.setStatus(Status.CLOSED);
                        } else {
                            refund.setStatus(Status.PROCESSING); // 默认处理中
                        }
                    } else {
                        refund.setStatus(Status.PROCESSING); // 默认处理中
                    }
                    
                    // 设置退款成功时间
                    List<String> refundSuccessTimeList = (List<String>) respData.get("refund_success_time_list");
                    if (refundSuccessTimeList != null && !refundSuccessTimeList.isEmpty() && refundSuccessTimeList.get(0) != null) {
                        refund.setSuccessTime(refundSuccessTimeList.get(0));
                    }
                    
                    // 设置金额信息
                    Integer totalFee = (Integer) respData.get("total_fee");
                    List<Integer> refundFeeList = (List<Integer>) respData.get("refund_fee_list");
                    com.wechat.pay.java.service.refund.model.Amount amount =
                            new com.wechat.pay.java.service.refund.model.Amount();
//                    amount.setTotal(Long.valueOf(totalFee));

//                    amount.setRefund(Long.valueOf(refundFeeList.get(0)));
//                    amount.setCurrency("CNY");
//                    refund.setAmount(amount);
                    if (totalFee != null && refundFeeList != null && !refundFeeList.isEmpty()) {
                        amount =
                                new com.wechat.pay.java.service.refund.model.Amount();
                        amount.setTotal(Long.valueOf(totalFee));
                        amount.setRefund(Long.valueOf(refundFeeList.get(0)));
                        amount.setCurrency("CNY");
                        refund.setAmount(amount);
                    }
                    
                    log.info("退款查询成功，商户订单号: {}, 退款状态: {}", outTradeNo, refund.getStatus());
                    
                    return refund;
                } else {
                    String errmsg = (String) responseBody.get("errmsg");
                    log.error("微信支付云托管退款查询API返回错误: {}, {}", errcode, errmsg);
                    throw new RuntimeException("微信支付云托管退款查询失败: " + errmsg);
                }
            } else {
                log.error("微信支付云托管退款查询API请求失败: {}", responseEntity.getStatusCode());
                throw new RuntimeException("微信支付云托管退款查询请求失败: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            log.error("在云托管环境下查询退款异常", e);
            throw new RuntimeException("在云托管环境下查询退款失败", e);
        }
    }
    
    /**
     * 将数据库记录转换为Refund对象
     * 
     * @param refundRecord 微信退款记录
     * @return 微信支付SDK的Refund对象
     */
    public Refund convertDbRecordToRefund(com.tour.model.WxRefundRecord refundRecord) {
        if (refundRecord == null) {
            return null;
        }
        
        try {
            // 创建Refund对象
            Refund refund = new Refund();
            refund.setOutTradeNo(refundRecord.getOutTradeNo());
            refund.setOutRefundNo(refundRecord.getOutRefundNo());
            refund.setRefundId(refundRecord.getRefundId());
            
            // 设置状态
            String statusStr = refundRecord.getRefundStatus();
            if ("SUCCESS".equals(statusStr)) {
                refund.setStatus(Status.SUCCESS);
            } else if ("PROCESSING".equals(statusStr)) {
                refund.setStatus(Status.PROCESSING);
            } else if ("ABNORMAL".equals(statusStr)) {
                refund.setStatus(Status.ABNORMAL);
            } else if ("CLOSED".equals(statusStr)) {
                refund.setStatus(Status.CLOSED);
            } else {
                refund.setStatus(Status.PROCESSING); // 默认处理中
            }
            
            // 设置金额信息
            com.wechat.pay.java.service.refund.model.Amount amount = 
                    new com.wechat.pay.java.service.refund.model.Amount();
            
            // 将元转换为分
            if (refundRecord.getTotalAmount() != null) {
                long totalAmountFen = refundRecord.getTotalAmount().multiply(new java.math.BigDecimal(100)).longValue();
                amount.setTotal(totalAmountFen);
            }
            
            if (refundRecord.getRefundAmount() != null) {
                long refundAmountFen = refundRecord.getRefundAmount().multiply(new java.math.BigDecimal(100)).longValue();
                amount.setRefund(refundAmountFen);
            }
            
            amount.setCurrency("CNY");
            refund.setAmount(amount);
            
            // 设置退款成功时间
            if (refundRecord.getSuccessTime() != null) {
                // 格式化时间为ISO格式
                refund.setSuccessTime(refundRecord.getSuccessTime().toString());
            }
            
            return refund;
        } catch (Exception e) {
            log.error("转换退款记录至Refund对象失败: {}", e.getMessage());
            throw new RuntimeException("转换退款记录失败", e);
        }
    }
} 
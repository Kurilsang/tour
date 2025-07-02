package com.tour.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tour.common.constant.Constants;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.WxPaymentUtil;
import com.tour.dto.WxProdOrderQueryResponseDTO;
import com.tour.enums.WxPaymentStatusEnum;
import com.tour.service.WxPaymentService;
import com.tour.vo.WxPaymentOrderVO;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author kuril
 * @Description 微信支付服务实现类
 */
@Service
@Slf4j
public class WxPaymentServiceImpl implements WxPaymentService {

    private final WxPaymentUtil wxPaymentUtil;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.profiles.active}")
    private String activeProfile;
    
    @Value("${wechat.pay.merchant-id}")
    private String merchantId;

    @Autowired
    public WxPaymentServiceImpl(WxPaymentUtil wxPaymentUtil, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.wxPaymentUtil = wxPaymentUtil;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 通过商户订单号查询订单
     *
     * @param outTradeNo 商户订单号
     * @return 订单查询结果
     */
    @Override
    public WxPaymentOrderVO queryOrderByOutTradeNo(String outTradeNo) {
        if (outTradeNo == null || outTradeNo.isEmpty()) {
            throw new ServiceException("商户订单号不能为空");
        }

        WxPaymentOrderVO result = new WxPaymentOrderVO();
        result.setOutTradeNo(outTradeNo);
        
        try {
            // 根据环境不同选择调用的方式
            if ("prod".equals(activeProfile)) {
                // 生产环境使用 API 接口文档中的方式
                return queryOrderByOutTradeNoInProd(outTradeNo);
            } else {
                // 开发环境使用原有的方式
                // 调用微信支付API查询订单
                Transaction transaction = wxPaymentUtil.queryOrderByOutTradeNo(outTradeNo);
                
                // 转换结果
                convertTransactionToVO(transaction, result);
                result.setSuccess(true);
                
                return result;
            }
        } catch (com.wechat.pay.java.core.exception.ServiceException e) {
            // 处理微信支付API返回的业务错误
            log.error("查询订单业务错误: {}, {}", e.getErrorCode(), e.getErrorMessage());
            
            result.setSuccess(false);
            result.setErrorCode(e.getErrorCode());
            result.setErrorMessage(e.getErrorMessage());
            
            // 特殊处理订单不存在的情况
            if (Constants.WX_PAY_ORDER_NOT_EXISTS.equals(e.getErrorCode())) {
                result.setTradeState(WxPaymentStatusEnum.NOTPAY.getCode());
                result.setTradeStateDesc("订单不存在");
            }
            
            return result;
        } catch (Exception e) {
            // 处理其他异常
            log.error("查询订单异常: {}", e.getMessage());
            
            result.setSuccess(false);
            result.setErrorCode("SYSTEM_ERROR");
            result.setErrorMessage("系统错误: " + e.getMessage());
            
            return result;
        }
    }

    /**
     * 使用微信云托管环境下的API查询订单（prod环境使用）
     * 
     * @param outTradeNo 商户订单号
     * @return 订单查询结果
     */
    private WxPaymentOrderVO queryOrderByOutTradeNoInProd(String outTradeNo) {
        log.info("使用微信云托管API查询订单，outTradeNo: {}", outTradeNo);
        WxPaymentOrderVO result = new WxPaymentOrderVO();
        result.setOutTradeNo(outTradeNo);
        
        try {
            // 构建请求
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("out_trade_no", outTradeNo);
            requestMap.put("sub_mch_id", merchantId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);
            
            // 发送请求
            String apiUrl = "http://api.weixin.qq.com/_/pay/queryorder";
            ResponseEntity<WxProdOrderQueryResponseDTO> responseEntity = 
                    restTemplate.postForEntity(apiUrl, requestEntity, WxProdOrderQueryResponseDTO.class);
            
            log.debug("微信支付订单查询返回: {}", responseEntity.getBody());
            
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                WxProdOrderQueryResponseDTO responseDTO = responseEntity.getBody();
                
                // 检查返回码
                if (responseDTO.getErrcode() != 0) {
                    log.error("微信支付订单查询失败: code={}, msg={}", 
                            responseDTO.getErrcode(), responseDTO.getErrmsg());
                    
                    result.setSuccess(false);
                    result.setErrorCode(String.valueOf(responseDTO.getErrcode()));
                    result.setErrorMessage(responseDTO.getErrmsg());
                    return result;
                }
                
                WxProdOrderQueryResponseDTO.OrderResponseData respData = responseDTO.getRespdata();
                
                // 检查API返回状态码
                if (!"SUCCESS".equals(respData.getReturn_code())) {
                    log.error("微信支付订单查询返回失败: {}", respData.getReturn_msg());
                    
                    result.setSuccess(false);
                    result.setErrorCode(respData.getReturn_code());
                    result.setErrorMessage(respData.getReturn_msg());
                    return result;
                }
                
                // 检查业务结果码
                if (!"SUCCESS".equals(respData.getResult_code())) {
                    log.error("微信支付订单查询业务结果失败: code={}, msg={}", 
                            respData.getErr_code(), respData.getErr_code_des());
                    
                    result.setSuccess(false);
                    result.setErrorCode(respData.getErr_code());
                    result.setErrorMessage(respData.getErr_code_des());
                    
                    // 特殊处理订单不存在的情况
                    if ("ORDERNOTEXIST".equals(respData.getErr_code())) {
                        result.setTradeState(WxPaymentStatusEnum.NOTPAY.getCode());
                        result.setTradeStateDesc("订单不存在");
                    }
                    
                    return result;
                }
                
                // 转换结果到VO
                result.setSuccess(true);
                result.setOutTradeNo(respData.getOut_trade_no());
                result.setTransactionId(respData.getTransaction_id());
                result.setTradeState(respData.getTrade_state());
                result.setTradeStateDesc(respData.getTrade_state_desc());
                result.setBankType(respData.getBank_type());
                result.setTradeType(respData.getTrade_type());
                result.setAttach(respData.getAttach());
                result.setOpenid(respData.getSub_openid() != null ? respData.getSub_openid() : respData.getOpenid());
                
                // 转换金额：分 -> 元
                if (respData.getTotal_fee() != null) {
                    result.setTotalAmount(
                            new BigDecimal(respData.getTotal_fee())
                            .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
                    );
                }
                
                // 转换时间格式：yyyyMMddHHmmss -> ISO格式
                if (respData.getTime_end() != null && !respData.getTime_end().isEmpty()) {
                    try {
                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                        LocalDateTime dateTime = LocalDateTime.parse(respData.getTime_end(), inputFormatter);
                        result.setSuccessTime(dateTime);
                    } catch (Exception e) {
                        log.warn("解析支付时间异常: {}", e.getMessage());
                    }
                }
                
                return result;
            } else {
                log.error("微信支付订单查询请求失败: {}", responseEntity.getStatusCode());
                
                result.setSuccess(false);
                result.setErrorCode("API_ERROR");
                result.setErrorMessage("API调用失败: " + responseEntity.getStatusCode());
                return result;
            }
        } catch (Exception e) {
            log.error("微信支付查询订单异常", e);
            
            result.setSuccess(false);
            result.setErrorCode("SYSTEM_ERROR");
            result.setErrorMessage("系统错误: " + e.getMessage());
            return result;
        }
    }

    /**
     * 通过预支付交易会话标识查询订单
     * 注：微信支付没有直接通过prepayId查询的接口，
     * 此处实现是通过查询数据库中与prepayId关联的outTradeNo，然后再查询订单
     *
     * @param prepayId 预支付交易会话标识
     * @return 订单查询结果
     */
    @Override
    public WxPaymentOrderVO queryOrderByPrepayId(String prepayId) {
        if (prepayId == null || prepayId.isEmpty()) {
            throw new ServiceException("预支付交易会话标识不能为空");
        }
        
        // 创建结果对象
        WxPaymentOrderVO result = new WxPaymentOrderVO();
        result.setPrepayId(prepayId);
        
        try {
            // 此处简化实现，直接返回错误
            // 实际项目中应该从数据库查询prepayId对应的outTradeNo，然后再查询订单
            result.setSuccess(false);
            result.setErrorCode("PREPAY_ID_QUERY_NOT_SUPPORTED");
            result.setErrorMessage("暂不支持直接通过prepayId查询订单，请使用商户订单号查询");
            
            return result;
        } catch (DataAccessException e) {
            log.error("查询prepayId对应订单数据库错误: {}", e.getMessage());
            
            result.setSuccess(false);
            result.setErrorCode("DATABASE_ERROR");
            result.setErrorMessage("数据库错误: " + e.getMessage());
            
            return result;
        } catch (Exception e) {
            log.error("查询prepayId对应订单异常: {}", e.getMessage());
            
            result.setSuccess(false);
            result.setErrorCode("SYSTEM_ERROR");
            result.setErrorMessage("系统错误: " + e.getMessage());
            
            return result;
        }
    }

    /**
     * 关闭订单
     *
     * @param outTradeNo 商户订单号
     * @return 是否关闭成功
     */
    @Override
    public boolean closeOrder(String outTradeNo) {
        if (outTradeNo == null || outTradeNo.isEmpty()) {
            throw new ServiceException("商户订单号不能为空");
        }
        
        try {
            // 调用微信支付API关闭订单
            return wxPaymentUtil.closeOrder(outTradeNo);
        } catch (Exception e) {
            log.error("关闭订单异常: {}", e.getMessage());
            throw new ServiceException("关闭订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 将Transaction对象转换为WxPaymentOrderVO
     *
     * @param transaction 微信支付交易信息
     * @param vo 待填充的视图对象
     */
    private void convertTransactionToVO(Transaction transaction, WxPaymentOrderVO vo) {
        if (transaction == null) {
            return;
        }
        
        // 基本信息
        vo.setOutTradeNo(transaction.getOutTradeNo());
        vo.setTransactionId(transaction.getTransactionId());
        
        // 交易状态 - 转换枚举为字符串
        if (transaction.getTradeState() != null) {
            vo.setTradeState(transaction.getTradeState().name());
        }
        
        vo.setTradeStateDesc(transaction.getTradeStateDesc());
        vo.setBankType(transaction.getBankType());
        
        // 交易类型 - 转换枚举为字符串
        if (transaction.getTradeType() != null) {
            vo.setTradeType(transaction.getTradeType().name());
        }
        
        vo.setAttach(transaction.getAttach());
        
        // 用户信息
        if (transaction.getPayer() != null) {
            vo.setOpenid(transaction.getPayer().getOpenid());
        }
        
        // 时间信息
        if (transaction.getSuccessTime() != null) {
            vo.setSuccessTime(LocalDateTime.parse(
                    transaction.getSuccessTime(),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
            ).atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        // 金额信息
        if (transaction.getAmount() != null && transaction.getAmount().getTotal() != null) {
            // 微信支付金额单位是分，需要转换为元
            vo.setTotalAmount(
                    new BigDecimal(transaction.getAmount().getTotal())
                    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
            );
        }
    }
} 
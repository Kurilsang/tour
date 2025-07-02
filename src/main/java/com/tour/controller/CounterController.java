package com.tour.controller;

import com.tour.common.Result;
import com.tour.common.util.WxPaymentUtil;
import com.tour.model.WxPaymentRequest;
import com.wechat.pay.java.core.*;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.exception.HttpException;
import com.wechat.pay.java.core.exception.MalformedMessageException;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tour.config.ApiResponse;
import com.tour.dto.CounterRequest;
import com.tour.model.Counter;
import com.tour.service.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.HttpException;
import com.wechat.pay.java.core.exception.MalformedMessageException;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.jsapi.model.CloseOrderRequest;
//import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByIdRequest;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;

/**
 * counter控制器
 */
@RestController

public class CounterController {

  final CounterService counterService;
  final Logger logger;
  private final WxPaymentUtil wxPaymentUtil;


  @PostMapping(value = "/test/wxGetPrePayment")
  Result testWxGetPrePayment(@RequestBody WxPaymentRequest wxPaymentRequest)  {

   return Result.success( wxPaymentUtil.wxGetPrePayment(wxPaymentRequest));
  }
  @GetMapping(value = "/test/queryOrderById/{outTradeNo}")
  Result testQueryOrderById(@PathVariable String outTradeNo)  {

    return Result.success( wxPaymentUtil.queryOrderByOutTradeNo(outTradeNo));
  }
  @GetMapping(value = "/test/wxGetPrePayment/{outTradeNo}")
  Result testCloseOrder(@PathVariable String outTradeNo)  {

    return Result.success( wxPaymentUtil.closeOrder(outTradeNo));
  }




  public CounterController(@Autowired CounterService counterService, WxPaymentUtil wxPaymentUtil) {
    this.counterService = counterService;
    this.logger = LoggerFactory.getLogger(CounterController.class);
    this.wxPaymentUtil = wxPaymentUtil;
  }


  /**
   * 获取当前计数
   * @return API response json
   */
  @GetMapping(value = "/api/count")
  ApiResponse get() {
    logger.info("/api/count get request");
    Optional<Counter> counter = counterService.getCounter(1);
    Integer count = 0;
    if (counter.isPresent()) {
      count = counter.get().getCount();
    }

    return ApiResponse.ok(count);
  }


  /**
   * 更新计数，自增或者清零
   * @param request {@link CounterRequest}
   * @return API response json
   */
  @PostMapping(value = "/api/count")
  ApiResponse create(@RequestBody CounterRequest request) {
    logger.info("/api/count post request, action: {}", request.getAction());

    Optional<Counter> curCounter = counterService.getCounter(1);
    if (request.getAction().equals("inc")) {
      Integer count = 1;
      if (curCounter.isPresent()) {
        count += curCounter.get().getCount();
      }
      Counter counter = new Counter();
      counter.setId(1);
      counter.setCount(count);
      counterService.upsertCount(counter);
      return ApiResponse.ok(count);
    } else if (request.getAction().equals("clear")) {
      if (!curCounter.isPresent()) {
        return ApiResponse.ok(0);
      }
      counterService.clearCount(1);
      return ApiResponse.ok(0);
    } else {
      return ApiResponse.error("参数action错误");
    }
  }
  /**
   * 使用微信支付公钥验证签名
   * @param publicKeyPem 微信支付公钥PEM格式字符串
   * @param signatureBase64 待验证的签名(Base64编码)
   * @param signData 验签名串
   * @return 验证结果，true表示通过，false表示失败
   * @throws CertificateException 证书异常
   * @throws NoSuchAlgorithmException 算法不存在异常
   * @throws InvalidKeyException 无效密钥异常
   * @throws SignatureException 签名异常
   * @throws IOException IO异常
   */
  public static boolean verifySignature(String publicKeyPem, String signatureBase64, String signData)
          throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {

    // 1. 解析原始RSA公钥（关键修改）
    String cleanPublicKey = publicKeyPem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", ""); // 移除所有空白字符
    byte[] publicKeyBytes = Base64.getDecoder().decode(cleanPublicKey);

    // 2. 使用KeyFactory生成PublicKey（而非CertificateFactory）
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
    PublicKey publicKey = keyFactory.generatePublic(keySpec);

    // 3. 解码签名
    byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

    // 4. 验签逻辑不变
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initVerify(publicKey);
    signature.update(signData.getBytes(StandardCharsets.UTF_8));
    return signature.verify(signatureBytes);
  }


}
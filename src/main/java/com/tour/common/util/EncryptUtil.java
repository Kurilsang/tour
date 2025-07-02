package com.tour.common.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * 加解密工具类
 *
 * @Author Abin
 */
@Component
public class EncryptUtil {

    @Value("${tour.encrypt.key:tourDefaultKey12345}")
    private String encryptKey;

    private static AES aes;

    @PostConstruct
    public void init() {
        // 初始化AES，使用encryptKey生成16位密钥
        byte[] key = SecureUtil.md5(encryptKey).substring(0, 16).getBytes(StandardCharsets.UTF_8);
        aes = SecureUtil.aes(key);
    }

    /**
     * 加密字符串
     *
     * @param content 待加密的内容
     * @return 加密后的内容
     */
    public String encrypt(String content) {
        if (content == null) {
            return null;
        }
        return aes.encryptHex(content);
    }

    /**
     * 解密字符串
     *
     * @param encrypted 已加密的内容
     * @return 解密后的内容
     */
    public String decrypt(String encrypted) {
        if (encrypted == null) {
            return null;
        }
        return aes.decryptStr(encrypted);
    }
} 
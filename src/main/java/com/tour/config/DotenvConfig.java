package com.tour.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * .env文件配置类
 * 只在dev环境下加载.env文件
 */
@Configuration
@Profile("dev") // 仅在dev环境激活
@PropertySource(value = "file:.env", ignoreResourceNotFound = true)
public class DotenvConfig {
    // spring-dotenv会自动将.env文件中的变量加载到环境中
    // 无需额外代码
} 
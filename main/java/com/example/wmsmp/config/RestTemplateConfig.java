package com.example.wmsmp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @Author:xhc
 * @Description 调用外部接口使用
 * @DateTime 2024/6/13 10:13
 * @Params
 * @Return
 */

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
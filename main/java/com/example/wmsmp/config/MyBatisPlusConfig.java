package com.example.wmsmp.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.*;

/**
 * MyBatisPlus配置类
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatisPlus拦截器（用于分页）
     */
    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        //定义拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //添加具体的分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.KINGBASE_ES));
        return interceptor;
    }
}
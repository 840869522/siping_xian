package com.example.wmsmp.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author:xhc
 * @Description 自动填充创建时间&更新时间
 * 重点！！！********************update(T t,Wrapper updateWrapper)时t不能为空,否则自动填充失效**********************
 * @DateTime 2024/1/30 8:57
 * @Params
 * @Return
 */

@Configuration
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        //插入时填充创建时间和更新时间
        this.strictInsertFill(metaObject, "create_time", Date.class, new Date());
        this.strictInsertFill(metaObject, "update_time", Date.class, new Date());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        //更新时填充更新时间
        this.strictUpdateFill(metaObject, "update_time", Date.class, new Date());
    }
}

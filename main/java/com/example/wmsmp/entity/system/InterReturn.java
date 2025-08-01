package com.example.wmsmp.entity.system;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * 接口返回类
 */
@ApiModel(value = "InterReturn 接口返回类")
@Data
public class InterReturn {
    String message;
    boolean status;
    Date timestamp;
    Object result;

}


package com.example.wmsmp.annotation;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 业务操作类型
 */
@Getter
public enum BusinessType {

    /**
     * 增加
     */
    ADD(1, "增加"),

    /**
     * 增加
     */
    LOSS(2, "减少"),

    /**
     * 增加并且减少
     */
    ADD_AND_LOSS(3, "移动"),

    /**
     * 其它
     */
    OTHER(99, "其它");

    @EnumValue
    @JsonValue
    private final int code;
    private final String info;

    BusinessType(int code, String info) {
        this.code = code;
        this.info = info;
    }

    public int getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

}

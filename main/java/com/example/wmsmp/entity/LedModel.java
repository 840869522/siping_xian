package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * led显示内容 wlixun
 *
 * @TableName t_led
 */
@TableName(value = "t_led")
@Data
public class LedModel implements Serializable {
    /**
     * led主键
     */
    @TableId(value = "led_id")
    private Integer ledId;

    /**
     * led内容
     */
    @TableField(value = "led_text")
    private String ledText;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

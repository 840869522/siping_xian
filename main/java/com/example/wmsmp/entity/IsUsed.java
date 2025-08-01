package com.example.wmsmp.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 三位态势使用
 */
@Data
public class IsUsed {


    /**
     * 已用
     */
    private String used;

    /**
     * 未用
     */
    private String unused;

}

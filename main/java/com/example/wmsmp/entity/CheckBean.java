package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @TableName t_check
 */
@TableName(value ="t_check")
@Data
public class CheckBean implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId(value = "check_id")
    private Integer checkId;

    /**
     * 盘点单号
     */
    @TableField(value = "check_code")
    private String checkCode;

    /**
     * 核算科目
     */
    @TableField(value = "he_suan_ke_mu")
    private String heSuanKeMu;

    /**
     * 库房号
     */
    @TableField(value = "warehouse_name")
    private String warehouseName;

    /**
     * 盘库依据
     */
    @TableField(value = "pan_ku_yi_ju")
    private String panKuYiJu;

    /**
     * 状态
     */
    @TableField(value = "status")
    private String status;

    /**
     * 状态描述用于转义
     */
    @TableField(exist = false)
    private String statusDescribe;

    /**
     *
     */
    @TableField(value = "udf01")
    private String udf01;

    /**
     *
     */
    @TableField(value = "udf02")
    private String udf02;

    /**
     *
     */
    @TableField(value = "udf03")
    private String udf03;

    /**
     *
     */
    @TableField(value = "udf04")
    private String udf04;

    /**
     *
     */
    @TableField(value = "udf05")
    private Object udf05;

    /**
     *
     */
    @TableField(value = "creator")
    private String creator;

    /**
     *
     */
    @TableField(value = "create_time")
    private String createTime;

    /**
     *
     */
    @TableField(value = "updater")
    private String updater;

    /**
     *
     */
    @TableField(value = "update_time")
    private String updateTime;

    /**
     * 制单单位
     */
    @TableField(value = "company")
    private String company;

    @TableField(value = "type")
    private Integer type;


    /**
     * 制单单位代码
     */
    @TableField(value = "company_code")
    private String companyCode;

    /**
     * 签发日期
     */
    @TableField(value = "open_time")
    private String openTime;


    /**
     * 有效日期
     */
    @TableField(value = "close_time")
    private String closeTime;


    /**
     * 上流对接使用
     */
    @TableField(exist = false)
    private List<CheckDetailBean> data;

}

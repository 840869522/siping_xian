package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @TableName t_check_detail
 */
@TableName(value ="t_check_detail")
@Data
public class CheckDetailBean implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private String heSuanKeMu;

    @TableField(exist = false)
    private String warehouseName;

    @TableField(exist = false)
    private String panKuYiJu;

    @TableField(exist = false)
    private String checkStatus;

    /**
     * 主键
     */
    @TableId(value = "t_check_detail_id")
    private Integer checkDetailId;

    /**
     * 盘点单号
     */
    @TableField(value = "check_code")
    private String checkCode;

    /**
     * 器材编号
     */
    @TableField(value = "material_code")
    private String materialCode;

    /**
     * 器材名称
     */
    @TableField(value = "material_name")
    private String materialName;

    /**
     * 价格
     */
    @TableField(value = "material_price")
    private String materialPrice;

    /**
     * 质量等级
     */
    @TableField(value = "zhi_liang_deng_ji")
    private String zhiLiangDengJi;

    /**
     * 批次
     */
    @TableField(value = "batch")
    private String batch;

    /**
     * 库存数量
     */
    @TableField(value = "inventory_count")
    private int inventoryCount;

    @TableField(value = "pick_count")
    private int pick_count;

    @TableField(value = "actual_count")
    private int actual_count;

    /**
     * 盘点数量
     */
    @TableField(value = "check_count")
    private int checkCount;

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
    private Object udf02;

    /**
     *
     */
    @TableField(value = "udf03")
    private Object udf03;

    /**
     *
     */
    @TableField(value = "udf04")
    private String udf04;

    /**
     *
     */
    @TableField(value = "udf05")
    private String udf05;

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

}

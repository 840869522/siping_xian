package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 出库明细表
 *
 * @TableName t_out_order_detail
 */
@TableName(value = "t_out_order_detail")
@Data
public class OutOrderDetail implements Serializable {
    /**
     * 出库单明细ID
     */
    @TableId(value = "out_order_detail_id", type = IdType.AUTO)
    private Integer out_order_detail_id;

    /**
     * 出库单ID
     */
    @TableField(value = "out_order_id")
    private String out_order_id;

    /**
     * 物料编号
     */
    @TableField(value = "material_code")
    private String material_code;

    /**
     * 物料名称
     */
    @TableField(value = "material_name")
    private String material_name;

    /**
     * 批次
     */
    @TableField(value = "batch")
    private String batch;

    /**
     * 订单数量
     */
    @TableField(value = "order_count")
    private Integer order_count;

    /**
     * 已出库数量
     */
    @TableField(value = "actual_count")
    private Integer actual_count;

    /**
     * 已配盘数量
     */
    @TableField(value = "pick_count")
    private Integer pick_count;

    /**
     * 状态
     * 1执行中
     * 2已完成
     */
    @TableField(value = "status")
    private String status;

    /**
     * 可用状态_显示值
     */
    @TableField(exist = false, value = "status_display_value")
    private String status_display_value;

    /**
     *
     */
    @TableField(value = "udf01")
    private String udf01;

    /**
     * 使用备用2 旧物料编号
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
    private String udf05;

    /**
     * 创建人
     */
    @TableField(value = "creator")
    private String creator;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date create_time;

    /**
     * 更新人
     */
    @TableField(value = "updater")
    private String updater;

    /**
     * 更新时间
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Date update_time;

    /**
     * 质量等级
     */
    @TableField(value = "zhiliangdengji")
    private String zhiliangdengji;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
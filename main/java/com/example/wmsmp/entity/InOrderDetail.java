package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 入库单明细
 * @TableName t_in_order_detail
 */
@TableName(value ="t_in_order_detail")
@Data
public class InOrderDetail implements Serializable {
    /**
     * 入库单明细ID
     */
    @TableId(value = "in_order_detail_id", type = IdType.AUTO)
    private Integer in_order_detail_id;

    /**
     * 入库单ID
     */
    @TableField(value = "in_order_id")
    private String in_order_id;

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
     * 订单数量
     */
    @TableField(value = "order_count")
    private Integer order_count;

    /**
     * 已收货数量
     */
    @TableField(value = "actual_count")
    private Integer actual_count;

    /**
     * 批次
     */
    @TableField(value = "batch")
    private String batch;

    /**
     * 状态
     * 1执行中
     * 2已完成
     */
    @TableField(value = "status")
    private String status;

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
     * 单价
     */
    @TableField(value = "material_price")
    private BigDecimal material_price;

    /**
     * 质量等级
     */
    @TableField(value = "zhiliangdengji")
    private String zhiliangdengji;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
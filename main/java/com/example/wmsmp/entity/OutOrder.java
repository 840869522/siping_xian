package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 出库单
 * @TableName t_out_order
 */
@ApiModel(value = "OutOrder 出库表头")
@TableName(value ="t_out_order")
@Data
public class OutOrder implements Serializable {
    /**
     * 出库单号
     */
    @TableId(value = "out_order_id",type = IdType.INPUT)
    private String out_order_id;

    /**
     * 订单类型
     */
    @TableField(value = "order_type")
    private String order_type;

    /**
     * 车牌号
     */
    @TableField(value = "car_no")
    private String car_no;

    /**
     * 原始单号
     */
    @TableField(value = "org_order")
    private String org_order;

    /**
     * 仓库名称
     */
    @TableField(value = "warehouse_name")
    private String warehouse_name;

    /**
     * 状态
     * 1创建中
     * 2待审核
     * 2已审核
     * 3已配盘
     * 4已完成
     */
    @TableField(value = "status")
    private String status;

    /**
     * 可用状态_显示值
     */
    @TableField(exist = false,value = "status_display_value")
    private String status_display_value;

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
     * 核算科目
     */
    @TableField(value = "hesuankemu")
    private String hesuankemu;

    /**
     * 收物单位
     */
    @TableField(value = "shouwudanwei")
    private String shouwudanwei;

    /**
     * 分队请领编号
     */
    @TableField(value = "fenduiqinglingbianhao")
    private String fenduiqinglingbianhao;

    /**
     * 调拨单号
     */
    @TableField(value = "diaobodanhao")
    private String diaobodanhao;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
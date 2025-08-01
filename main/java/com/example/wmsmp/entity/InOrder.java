package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 入库单
 * @TableName t_in_order
 */
@TableName(value ="t_in_order")
@Data
public class InOrder implements Serializable {
    /**
     * 入库单号
     */
    @TableId(value = "in_order_id")
    private String in_order_id;

    /**
     * 仓库名称
     */
    @TableField(value = "warehouse_name")
    private String warehouse_name;

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
     * 单据状态（文本）
     * 1创建中
     * 2已审核
     * 3收货中
     * 4已完成
     */
    @TableField(value = "status")
    private String status;

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
     * 库房号
     */
    @TableField(value = "kufangmingcheng")
    private String kufangmingcheng;

    /**
     * 发物单位
     */
    @TableField(value = "fawudanwei")
    private String fawudanwei;

    /**
     * 合同编号
     */
    @TableField(value = "hetongbianhao")
    private String hetongbianhao;

    /**
     * 调拨单号
     */
    @TableField(value = "diaobodanhao")
    private String diaobodanhao;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
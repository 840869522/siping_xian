package com.example.wmsmp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 出库Excel类 无数据库表
 */
@ApiModel(value = "OutOrderExcel 出库Excel类")
@Data
public class OutOrderExcel implements Serializable {
    /**
     * 出库单号
     */
    @TableId(value = "out_order_id", type = IdType.INPUT)
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

/*
 * 以下为明细
 */


    /**
     * 出库单明细ID
     */
    @TableId(value = "out_order_detail_id", type = IdType.AUTO)
    private Integer out_order_detail_id;

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
     * 质量等级
     */
    @TableField(value = "zhiliangdengji")
    private String zhiliangdengji;

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
     * 创建人
     */
    @TableField(value = "creator")
    private String creator;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date create_time;

    /**
     * 更新人
     */
    @TableField(value = "updater")
    private String updater;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date update_time;
}

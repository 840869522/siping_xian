package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 出库配盘
 * @TableName t_out_pick
 */
@TableName(value ="t_out_pick")
@Data
public class OutPick implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     * 拣选方式
     */
    @TableField(value = "out_pick_type")
    private String out_pick_type;

    /**
     * 载具编号
     */
    @TableField(value = "pallet_code")
    private String pallet_code;

    /**
     * 载具格子号
     */
    @TableField(value = "cell_id")
    private String cell_id;

    /**
     * 配盘数量
     */
    @TableField(value = "pick_qty")
    private Integer pick_qty;

    /**
     * 出库单号
     */
    @TableField(value = "out_order_id")
    private String out_order_id;

    /**
     * 出库单明细ID
     */
    @TableField(value = "out_order_detail_id")
    private Integer out_order_detail_id;

    /**
     * 库存id
     */
    @TableField(value = "inventory_id" )
    private Integer inventory_id;

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
     * 拣货台编号
     */
    @TableField(value = "pickstation_no")
    private String pickstation_no;

    /**
     * 拣货目标编号
     */
    @TableField(value = "target_no")
    private String target_no;

    /**
     * 批次
     */
    @TableField(value = "batch")
    private String batch;

    /**
     * 状态
     * 1待执行
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
     *
     */
    @TableField(value = "creator")
    private String creator;

    /**
     * 为流水服务 并未参与实际业务
     */
    @TableField(exist = false)
    private String palletCode;

    /**
     *
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date create_time;

    /**
     *
     */
    @TableField(value = "updater")
    private String updater;

    /**
     *
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Date update_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

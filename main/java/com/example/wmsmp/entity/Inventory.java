package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 托盘物料绑定
 * @TableName t_inventory
 */
@TableName(value ="t_inventory")
@Data
public class Inventory implements Serializable {
    /**
     * id
     */
    @TableId(value = "inventory_id", type = IdType.AUTO)
    private Integer inventory_id;

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
     * 货位号
     */
//    @TableField(value = "location_code")
//    private String location_code;

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
     * 库存数量
     */
    @TableField(value = "inventory_count")
    private Integer inventory_count;

    /**
     * 冻结数量
     */
    @TableField(value = "frozen_count")
    private Integer frozen_count;

    /**
     * 出库订单号
     */
    @TableField(value = "out_order_id")
    private String out_order_id;

    /**
     * 出库明细ID
     */
    @TableField(value = "out_order_detail_id")
    private Integer out_order_detail_id;

    /**
     * 状态
     */
    @TableField(value = "status")
    private String status;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 使用备用1 字段 合并物料编号和批次号
     */
    @TableField(value = "udf01")
    private String udf01;

    /**
     * 使用备用2 旧器材代码
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
     * 修改人
     */
    @TableField(value = "updater")
    private String updater;

    /**
     * 修改时间
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Date update_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}
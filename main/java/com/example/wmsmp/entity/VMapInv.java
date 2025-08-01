package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;

/**
 * @TableName v_inven_localtion_material
 */
@TableName(value = "v_inven_localtion_material")
@Data
public class VMapInv implements Serializable {
    /**
     *
     */
    @TableField(value = "id")
    private Integer id;

    /**
     *
     */
    @TableField(value = "warehouse")
    private String warehouse;

    /**
     *
     */
    @TableField(value = "location_code")
    private String location_code;

    /**
     *
     */
    @TableField(value = "location_area")
    private Integer location_area;

    /**
     *
     */
    @TableField(value = "inventory_id")
    private Integer inventory_id;

    /**
     *
     */
    @TableField(value = "cell_id")
    private String cell_id;

    /**
     *
     */
    @TableField(value = "pallet_code")
    private String pallet_code;

    /**
     *
     */
    @TableField(value = "material_code")
    private String material_code;

    /**
     *
     */
    @TableField(value = "material_name")
    private String material_name;

    /**
     *
     */
    @TableField(value = "batch")
    private String batch;

    /**
     *
     */
    @TableField(value = "inventory_count")
    private Integer inventory_count;

    /**
     *
     */
    @TableField(value = "frozen_count")
    private Integer frozen_count;

    /**
     *
     */
    @TableField(value = "status")
    private String status;


    /**
     *
     */
    @TableField(exist = false, value = "udf01")
    private String udf01;


}
package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 库位托盘关系表
 *
 * @TableName t_location_map
 */
@TableName(value = "t_location_map")
@Data
public class LocationMap implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     * 货位编号
     */
    @TableField(value = "location_code")
    private String location_code;

    /**
     * x
     */
    @TableField(value = "location_code_x")
    private Integer location_code_x;

    /**
     * y
     */
    @TableField(value = "location_code_y")
    private Integer location_code_y;

    /**
     * z
     */
    @TableField(value = "location_code_z")
    private Integer location_code_z;

    /**
     * d
     */
    @TableField(value = "location_code_d")
    private Integer location_code_d;

    /**
     * 区域
     */
    @TableField(value = "location_area")
    private Integer location_area;

    /**
     * 载具号
     */
    @TableField(value = "pallet_code")
    private String pallet_code;

    /**
     * 所属仓库
     */
    @TableField(value = "warehouse")
    private String warehouse;

    /**
     * 货位类型
     */
    @TableField(value = "warehouse_type")
    private String warehouse_type;

    /**
     * 货位高度
     */
    @TableField(value = "height")
    private Integer height;

    /**
     * 已用容量
     */
    @TableField(value = "volume")
    private Integer volume;

    /**
     * 可用状态
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
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date create_time;

    /**
     * 更新人
     */
    @TableField(value = "updater")
    private String updater;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date update_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
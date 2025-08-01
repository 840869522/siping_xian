package com.example.wmsmp.entity.base;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.sun.org.apache.xpath.internal.operations.Equals;
import lombok.Data;

/**
 * 设备基础数据
 * @TableName base_equipment
 */
@TableName(value ="base_equipment")
@Data
public class BaseEquipment implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    /**
     * 设备编号
     */
    @TableField(value = "equipment_code")
    private String equipment_code;

    /**
     * 设备名称
     */
    @TableField(value = "equipment_name")
    private String equipment_name;

    /**
     * 维护保养周期
     */
    @TableField(value = "period")
    private String period;

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
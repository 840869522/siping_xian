package com.example.wmsmp.entity.base;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

/**
 * 物料基础表
 * @TableName base_material
 */
@TableName(value ="base_material")
@Data
public class BaseMaterial implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

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
     * 类别
     */
    @TableField(value = "material_type")
    private String material_type;

    /**
     * 供应商
     */
    @TableField(value = "supplier")
    private String supplier;

    /**
     * 规格
     */
    @TableField(value = "material_spec")
    private String material_spec;

    /**
     * 单位
     */
    @TableField(value = "material_unit")
    private String material_unit;

    /**
     * 长度，单位cm
     */
    @TableField(value = "lengths")
    private Integer lengths;

    /**
     * 宽度，单位cm
     */
    @TableField(value = "wide")
    private Integer wide;

    /**
     * 高度，单位cm
     */
    @TableField(value = "hight")
    private Integer hight;

    /**
     * 重量，单位g
     */
    @TableField(value = "weight")
    private Integer weight;

    /**
     * 存储区域
     */
    @TableField(value = "material_storage_area")
    private String material_storage_area;

    /**
     * 图号
     */
    @TableField(value = "draw_no")
    private String draw_no;

    /**
     * 合格证
     */
    @TableField(value = "certificate")
    private String certificate;

    /**
     * 保质期（天）
     */
    @TableField(value = "quality")
    private Integer quality;

    /**
     * 最小库存
     */
    @TableField(value = "min_stock")
    private Integer min_stock;

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



    /**
     * 装备代码
     */
    @TableField(value = "equip_code")
    private String equip_code;

    /**
     * 装备名称
     */
    @TableField(value = "equip_name")
    private String equip_name;

    /**
     * 原品种标识码
     */
    @TableField(value = "org_material_type")
    private String org_material_type;

    /**
     * 旧器材代码
     */
    @TableField(value = "org_material_code")
    private String org_material_code;

    /**
     * 是否单品
     */
    @TableField(value = "issingle")
    private Boolean issingle;

    /**
     * 器材价格
     */
    @TableField(value = "material_price")
    private BigDecimal material_price;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
package com.example.wmsmp.entity.base;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 电子标签 料箱绑定
 * @TableName base_e_tag
 */
@TableName(value ="base_e_tag")
@Data
public class BaseETag implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    /**
     * 电子标签编号
     */
    @TableField(value = "tag_code")
    private String tag_code;

    /**
     * 电子标签位置
     */
    @TableField(value = "tag_location")
    private String tag_location;

    /**
     * 料箱号
     */
    @TableField(value = "pallet_code")
    private String pallet_code;

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
     * 1.1号台 2.2号台 3.3号台 4.机器人 5.二楼人工
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
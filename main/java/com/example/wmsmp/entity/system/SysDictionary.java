package com.example.wmsmp.entity.system;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 
 * @TableName sys_dictionary
 */
@ApiModel(value = "SysDictionary 字典实体")
@TableName(value ="sys_dictionary")
@Data
public class SysDictionary implements Serializable {
    /**
     * 数据ID
     */
    @TableId(value = "dic_id")
    private int dic_id;

    /**
     * 表名
     */
    @TableField(value = "dic_name")
    private String dic_name;

    /**
     * 数据类别
     */
    @TableField(value = "dic_type")
    private String dic_type;

    /**
     * 显示值
     */
    @TableField(value = "dic_display_value")
    private String dic_display_value;

    /**
     * 实际值
     */
    @TableField(value = "dic_value")
    private int dic_value;

    /**
     * 状态信息
     */
    @TableField(value = "status")
    private int status;

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
     * lcy:表对应的前端页面展示名，所有的下拉类标况前端都传该值
     */
    @TableField(value = "dic_display_name")
    private String dic_display_name;




    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("dic_id=").append(dic_id);
        sb.append(", dic_name=").append(dic_name);
        sb.append(", dic_type=").append(dic_type);
        sb.append(", dic_display_value=").append(dic_display_value);
        sb.append(", dic_value=").append(dic_value);
        sb.append(", status=").append(status);
        sb.append(", remark=").append(remark);
        sb.append(", udf01=").append(udf01);
        sb.append(", udf02=").append(udf02);
        sb.append(", udf03=").append(udf03);
        sb.append(", udf04=").append(udf04);
        sb.append(", udf05=").append(udf05);
        sb.append(", creator=").append(creator);
        sb.append(", create_time=").append(create_time);
        sb.append(", updater=").append(updater);
        sb.append(", dic_display_name=").append(dic_display_name);
        sb.append(", update_time=").append(update_time);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
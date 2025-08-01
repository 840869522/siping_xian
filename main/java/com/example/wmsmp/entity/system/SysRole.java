package com.example.wmsmp.entity.system;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 
 * @TableName sys_role
 */
@ApiModel(value = "SysRole 权限实体")
@TableName(value ="sys_role")
@Data
public class SysRole implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "role_id")
    private Integer role_id;

    /**
     * 角色名
     */
    @TableField(value = "role_name")
    private String role_name;

    /**
     * 菜单名称
     */
    @TableField(value = "title")
    private String title;

    /**
     * 组件ID
     */
    @TableField(value = "component_id")
    private String component_id;

    /**
     * 组件名
     */
    @TableField(value = "component")
    private String component;

    /**
     * 组件名称
     */
    @TableField(value = "component_name")
    private String component_name;

    /**
     * 图标
     */
    @TableField(value = "icon")
    private String icon;

    /**
     * 父级菜单ID
     */
    @TableField(value = "parent_id")
    private Integer parent_id;

    /**
     * 状态
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 可用状态_显示值
     */
    @TableField(exist = false,value = "status_display_value")
    private String status_display_value;

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


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    List<SysRole> children;//菜单列表 数据库表中无此属性 用于回传前端

    public List<SysRole> getChildren() {
        return children;
    }

    public void setChildren(List<SysRole> children) {
        this.children = children;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("role_id=").append(role_id);
        sb.append(", role_name=").append(role_name);
        sb.append(", title=").append(title);
        sb.append(", component_id=").append(component_id);
        sb.append(", component=").append(component);
        sb.append(", component_name=").append(component_name);
        sb.append(", icon=").append(icon);
        sb.append(", parent_id=").append(parent_id);
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
        sb.append(", update_time=").append(update_time);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
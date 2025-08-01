package com.example.wmsmp.entity.system;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @TableName sys_users
 */
@ApiModel(value = "SysUser 用户实体")
@TableName(value = "sys_users")
@Data
public class SysUser implements Serializable {
    /**
     * 登录名
     */
    @TableId(value = "login_name")
    private String login_name;

    /**
     * 用户姓名
     */
    @TableField(value = "user_name")
    private String user_name;

    /**
     * 密码
     */
    @TableField(value = "password",select = false)
    private String password;

    /**
     * 角色名称
     */
    @TableField(value = "role_name")
    private String role_name;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("login_name=").append(login_name);
        sb.append(", user_name=").append(user_name);
        sb.append(", password=").append(password);
        sb.append(", role_name=").append(role_name);
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
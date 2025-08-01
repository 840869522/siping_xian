package com.example.wmsmp.entity.task.D;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 大件库任务表
 * @TableName t_task_d
 */
@TableName(value ="t_task_d")
@Data
public class TaskD implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 任务号
     */
    @TableField(value = "task_no")
    private String task_no;

    /**
     * 库名
     */
    @TableField(value = "warehouse")
    private String warehouse;

    /**
     * 货位号
     */
    @TableField(value = "location_code")
    private String location_code;

    /**
     * 任务类型
     */
    @TableField(value = "task_type")
    private String task_type;

    /**
     * 载具编号
     */
    @TableField(value = "pallet_code")
    private String pallet_code;

    /**
     * 站台编号
     */
    @TableField(value = "prot_no")
    private String prot_no;

    /**
     * 
     */
    @TableField(value = "status")
    private String status;

    /**
     * 货位的组号
     */
    @TableField(value = "udf01")
    private Integer udf01;

    /**
     * 
     */
    @TableField(value = "udf02")
    private String udf02;

    /**
     * 原货位（移库起点）
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

    /**
     * 
     */
    @TableField(value = "car_no")
    private String car_no;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;



}
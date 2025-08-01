package com.example.wmsmp.entity.task.AGV;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * agv任务
 * @TableName t_task_agv
 */
@ApiModel(value = "AgvTask AGV任务")
@TableName(value ="t_task_agv")
@Data
public class AgvTask implements Serializable {
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
     * 载具编号
     */
    @TableField(value = "pallet_code")
    private String pallet_code;

    /**
     * 任务类型
     */
    @TableField(value = "task_type")
    private String task_type;

    /**
     * 起始位置
     */
    @TableField(value = "org_position")
    private String org_position;

    /**
     * 目标位置
     */
    @TableField(value = "target_position")
    private String target_position;

    /**
     * 任务状态
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}
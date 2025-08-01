package com.example.wmsmp.entity.task.CTU;


import lombok.Data;

@Data
public class CtuTaskStatus {

    String taskId;//任务id
    Integer status;//该任务执行状态    Status:1正在执行,2走出储位,3任务完成
    String ctuNo;//执行该任务的ctu  30975 30976
    String updateTime;//更新时间
}

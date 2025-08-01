package com.example.wmsmp.entity.task.CTU;


import lombok.Data;

@Data
public class CtuStatus {

    String robotCode;//CTU编号
    String battery;//电量
    boolean online;//是否在线

    String status;//状态码
    String taskId;//任务号
}

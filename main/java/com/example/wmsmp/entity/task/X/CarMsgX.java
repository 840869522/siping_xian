package com.example.wmsmp.entity.task.X;

import lombok.Data;

@Data
public class CarMsgX {

    //用于查询车辆状态接口接收
    //仅取相关信息  详细信息字段见接口协议

    Integer shuttleId;//车号 3001或3002
    Integer onlineStatus;//联机状态 1联机 2遥控
    Integer equipStatus;//设备状态 0空闲 1作业中 2报警
    Integer currentBattery;//当前电量 0-100

    Integer currentTaskId;//穿梭车当前任务号
    Integer postTaskId;//穿梭车过账指令号
    Integer wmsTaskId;//wms任务号

    String error;//错误信息
}

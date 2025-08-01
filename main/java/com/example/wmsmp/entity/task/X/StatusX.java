package com.example.wmsmp.entity.task.X;

import lombok.Data;

@Data
public class StatusX {

    Integer shuttleId;//
    Integer onlineStatus;//联机状态 1练级  2遥控器
    Integer equipStatus;//0空闲 1作业中 2报警
    Integer currentBattery;//电量
    Integer  powerStatus;//1电量正常 2馈电状态

    Integer  onRailway;//1在轨 0不在轨
    Integer  currentTaskMode;//0初始 1左出库 2右出库 3左入库 4右入库 5左移库 6右移库 7近点 8远点

    Integer  currentTaskId;//当前任务号
    Integer  postTaskId;//过账任务号
    Integer  postTaskStatus;//
    Integer  taskStatus;//0空闲 1指令执行完成 2指令删除 3指令执行中
    Integer  palletNumber;//盘点结果数量

    Integer  isEnable;//1启用 0禁用

    String error;//错误信息
}

package com.example.wmsmp.entity.task.AGV;


import lombok.Data;

import java.util.Date;

@Data
public class AgvStatus {
    Integer id;//Agv唯一编号
    String name;//名称
    String status;//状态(待机，运行，异常，离线)
    String abnormal;//异常信息
    Integer x;//X坐标
    Integer y;//Y坐标
    Integer angle;//姿态角度(0-360)
    Integer soc;//电量(0-100)
    String taskId;//当前执行任务id
    String receiveTime;//获取时间

}

package com.example.wmsmp.entity.task.AGV;


import lombok.Data;

@Data
public class AgvTaskStatus {


    String taskId;//任务id
    Integer status;//该任务执行状态    Status:0尚未开始执行,1正在执行,2已完成或不存在
    Integer agvNo;//执行该任务的agv名称     agvNo:车编号
    Integer faultCode;//任务异常说明      faultCode:0:无异常 ,1.没有叉到货物 ,2.起始点不存在,3.终点不存在,4.任务异常
    String updateTime;//更新时间
}

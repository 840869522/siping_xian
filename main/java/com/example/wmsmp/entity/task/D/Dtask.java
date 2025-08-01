package com.example.wmsmp.entity.task.D;

import lombok.Data;

@Data
public class Dtask {
    //用于5任务状态上报
    String taskId;//任务号
    String reportTime;//时间
    Integer taskStatus;//任务状态  0已接收 1已开始 3任务中断 4放货完成 8任务结束
    String gridId;//目标货位
}

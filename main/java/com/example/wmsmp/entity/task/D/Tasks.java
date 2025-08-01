package com.example.wmsmp.entity.task.D;

import lombok.Data;

@Data
public class Tasks {
    //用于1接受任务接口

    String taskId;//任务号
    Integer taskType;//任务类型 0入库 1出库 2移库
    String startNode;//任务起点
    String endNode;//任务终点
    String barCode;//托盘码
    Integer order;//排序   值小在前，大的在后
}

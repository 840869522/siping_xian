package com.example.wmsmp.entity.task.D;

import lombok.Data;

import java.util.List;

@Data
public class DtaskReceive {
//用于1接受任务接口

    String groupId;//组号
    String msgTime;//时间
    Integer priorityCode;//优先级
    String warehouse;//仓库编码

    List<Tasks> tasks;//任务组
}

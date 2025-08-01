package com.example.wmsmp.entity.task.AGV;


import lombok.Data;

import java.util.Date;

@Data
public class AgvTaskSend {


    String id;//任务唯一id
    Integer type;//任务类型 1托盘 2木箱子
    Integer prio;//优先级 0最后
    String source;//取货地址
    String target;//卸货地址
    String createTime;//创建时间

}

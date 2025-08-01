package com.example.wmsmp.entity.task.CTU;


import lombok.Data;

@Data
public class CtuTaskSend {


    String id;//任务唯一id
    Integer type;
    Integer boxType;//箱子类型 默认1
    Integer prio;//优先级 0最后
    String source;//取货地址
    String target;//卸货地址
    String createTime;//创建时间
}

package com.example.wmsmp.entity.task.Z;

import lombok.Data;

import java.util.List;

@Data
public class ZTaskSorter1 {

    //交叉带分拣机任务下发 第一层
    String order_no;//单据号
    String task_type;//任务类型
    String task_type_desc;//任务描述
    String create_time;
    List<ZTaskSorter2> list;


}

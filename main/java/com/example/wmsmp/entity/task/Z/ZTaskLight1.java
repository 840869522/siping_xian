package com.example.wmsmp.entity.task.Z;

import lombok.Data;

import java.util.List;

@Data
public class ZTaskLight1 {
    //电子标签亮灯任务下发 第一层
    String uuid;
    String wall_no;//墙号
    String task_type;//任务类型 tag_light
    String task_type_desc;//任务描述 电子标签亮灯
    String create_time;
    List<ZTaskLight2> data_list;


}

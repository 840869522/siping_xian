package com.example.wmsmp.entity.task.Z;

import lombok.Data;


@Data
public class ZTag {

    //电子标签 拍下反馈入参
    String uuid;
    String tag_code;
    String num;
    String task_type;//任务类型 tag_light_off_result
    String task_type_desc;//任务描述 电子标签灭灯
    String create_time;

}

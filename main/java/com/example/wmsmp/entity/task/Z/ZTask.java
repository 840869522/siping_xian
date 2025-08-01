package com.example.wmsmp.entity.task.Z;

import lombok.Data;

@Data
public class ZTask {

    //任务下发接口入参
    String task_id;
    String barcode;
    String start_dest;
    String end_dest;
    String task_type;
    String task_type_desc;
    String create_time;
    String mfc_out_priority;
    String mfc_level;


}

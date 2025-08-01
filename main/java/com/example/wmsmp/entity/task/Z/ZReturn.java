package com.example.wmsmp.entity.task.Z;

import lombok.Data;
@Data
public class ZReturn {

    //与wcs交互接口出参
    String task_id;
    String code;//200成功 其他失败
    String message;

}

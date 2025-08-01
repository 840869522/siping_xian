package com.example.wmsmp.entity.task.D;

import lombok.Data;
@Data
public class DReturn {


    Integer returnStatus;//0成功 1失败
    String returnInfo;
    String msgTime;


}

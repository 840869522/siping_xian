package com.example.wmsmp.entity.UpSystem;

import lombok.Data;

@Data
public class ReturnMark {


    String returnMark;//"success"正确       "fail"错误

    String errorMsg;//信息
    String errorCode;//代码
}

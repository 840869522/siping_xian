package com.example.wmsmp.entity.system;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "ResReturn 查询回传数据类")
@Data
public class ResReturn implements Serializable {

    int pageNo;
    int pageSize;
    int totalCount;
    int totalPage;
    Object Anything;
}


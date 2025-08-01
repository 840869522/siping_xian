package com.example.wmsmp.entity.UpSystem;


import lombok.Data;

import java.util.List;

@Data
public class ReturnBack {


    String ruKuDanHao;//入库单号
    String chuKuDanHao;//出库单号
    String panKuDanHao;//盘库单号
    List<DataObject> data;

}

package com.example.wmsmp.entity.task.Z;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ZTaskSorter2 {

    //交叉带分拣机任务下发 第二层
    String box_id;//箱子序号
    String barcode;//物料编码
    String batch;//批次
    Integer num;//此箱个数

    Integer detail_id;//明细ID（wms用）

}

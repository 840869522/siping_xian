package com.example.wmsmp.service;

import com.example.wmsmp.entity.Flow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author win
* @description 针对表【t_flow(作业流水)】的数据库操作Service
* @createDate 2023-08-29 17:13:03
*/
public interface FlowService extends IService<Flow> {

    Integer queryInventoryCount(Flow flow);
}

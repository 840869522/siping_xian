package com.example.wmsmp.mapper;

import com.example.wmsmp.entity.Flow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author win
* @description 针对表【t_flow(作业流水)】的数据库操作Mapper
* @createDate 2023-08-29 17:13:03
* @Entity com.example.wmsmp.entity.Flow
*/
public interface FlowMapper extends BaseMapper<Flow> {

    Integer queryInventoryCount(Flow flow);
}





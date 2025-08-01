package com.example.wmsmp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wmsmp.entity.Flow;
import com.example.wmsmp.service.FlowService;
import com.example.wmsmp.mapper.FlowMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author win
* @description 针对表【t_flow(作业流水)】的数据库操作Service实现
* @createDate 2023-08-29 17:13:03
*/
@Service
public class FlowServiceImpl extends ServiceImpl<FlowMapper, Flow>
    implements FlowService{

    @Resource
    private FlowMapper flowMapper;

    @Override
    public Integer queryInventoryCount(Flow flow) {
        return flowMapper.queryInventoryCount(flow);
    }
}





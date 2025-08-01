package com.example.wmsmp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wmsmp.entity.OutOrder;
import com.example.wmsmp.service.OutOrderService;
import com.example.wmsmp.mapper.OutOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author xhc
* @description 针对表【t_out_order(出库单)】的数据库操作Service实现
* @createDate 2023-04-28 09:43:41
*/
@Service
public class OutOrderServiceImpl extends ServiceImpl<OutOrderMapper, OutOrder>
    implements OutOrderService{

}





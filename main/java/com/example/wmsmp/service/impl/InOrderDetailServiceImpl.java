package com.example.wmsmp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wmsmp.entity.InOrderDetail;
import com.example.wmsmp.service.InOrderDetailService;
import com.example.wmsmp.mapper.InOrderDetailMapper;
import org.springframework.stereotype.Service;

/**
* @author 16
* @description 针对表【t_in_order_detail(入库单明细)】的数据库操作Service实现
* @createDate 2023-07-19 14:59:16
*/
@Service
public class InOrderDetailServiceImpl extends ServiceImpl<InOrderDetailMapper, InOrderDetail>
    implements InOrderDetailService{

}





package com.example.wmsmp.mapper;

import com.example.wmsmp.entity.OutOrder;
import com.example.wmsmp.entity.OutOrderDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
* @author xhc
* @description 针对表【t_out_order_detail(出库明细表)】的数据库操作Mapper
* @createDate 2023-04-28 11:04:54
* @Entity com.example.wmsmp.entity.OutOrderDetail
*/
public interface OutOrderDetailMapper extends BaseMapper<OutOrderDetail> {
}





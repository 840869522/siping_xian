package com.example.wmsmp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wmsmp.entity.CheckBean;
import com.example.wmsmp.entity.CheckDetailBean;

import java.util.List;

/**
* @author 王力勋
* @description 针对表【t_check_detail】的数据库操作Mapper
* @createDate 2024-08-07 11:42:23
* @Entity com.example.wmsmp.entity.CheckDetailBean
*/
public interface CheckDetailMapper extends BaseMapper<CheckDetailBean> {

    Long queryCount(CheckBean check);

    Long update4Check(CheckDetailBean check);

    List<CheckDetailBean> queryMaterial(CheckDetailBean checkDetail);

    Long update2checkCount(CheckDetailBean checkDetailBean);
}





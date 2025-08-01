package com.example.wmsmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wmsmp.entity.CheckBean;
import com.example.wmsmp.entity.CheckDetailBean;
import com.example.wmsmp.entity.system.InterReturn;

/**
* @author win
* @description 针对表【t_agv_location_map】的数据库操作Service
* @createDate 2024-07-26 11:42:23
*/
public interface CheckDetailService extends IService<CheckDetailBean> {

    InterReturn queryPage(CheckDetailBean check, int pageNo, int pageSize);

    Long queryCount(CheckBean check);

    InterReturn queryMaterial(CheckDetailBean checkDetail);


    Long update2checkCount(CheckDetailBean checkDetailBean);
}

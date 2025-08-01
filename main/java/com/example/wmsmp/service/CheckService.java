package com.example.wmsmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wmsmp.entity.CheckBean;
import com.example.wmsmp.entity.system.InterReturn;

/**
* @author win
* @description 针对表【t_agv_location_map】的数据库操作Service
* @createDate 2024-07-26 11:42:23
*/
public interface CheckService extends IService<CheckBean> {

    InterReturn queryPage(CheckBean check, int pageNo, int pageSize);

    InterReturn checkUpdate(CheckBean check);
}

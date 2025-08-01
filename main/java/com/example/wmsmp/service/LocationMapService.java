package com.example.wmsmp.service;

import com.example.wmsmp.entity.LocationMap;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wmsmp.mapper.LocationMapMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
* @author 16
* @description 针对表【t_location_map(库位托盘关系表)】的数据库操作Service
* @createDate 2023-07-19 11:28:54
*/
public interface LocationMapService extends IService<LocationMap> {

  //  @Autowired
//    LocationMapMapper locationMapMapper;
//
//    public void executeProcedure() {
//        String outputParam = "";
//        locationMapMapper.callYourProcedure("B库", 123, 123, 123, outputParam);
//        System.out.println("Output param: " + outputParam);
//    }



}

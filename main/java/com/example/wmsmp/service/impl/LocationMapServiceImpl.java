package com.example.wmsmp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wmsmp.entity.LocationMap;
import com.example.wmsmp.service.LocationMapService;
import com.example.wmsmp.mapper.LocationMapMapper;
import org.springframework.stereotype.Service;

/**
* @author 16
* @description 针对表【t_location_map(库位托盘关系表)】的数据库操作Service实现
* @createDate 2023-07-19 11:28:54
*/
@Service
public class LocationMapServiceImpl extends ServiceImpl<LocationMapMapper, LocationMap>
    implements LocationMapService{

}





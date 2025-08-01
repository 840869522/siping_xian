package com.example.wmsmp.mapper;

import com.example.wmsmp.entity.LocationMap;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author 16
 * @description 针对表【t_location_map(库位托盘关系表)】的数据库操作Mapper
 * @createDate 2023-07-19 11:28:54
 * @Entity com.example.wmsmp.entity.LocationMap
 */
public interface LocationMapMapper extends BaseMapper<LocationMap> {

    @Select("{call pro_auto_distribution_location(#{warehosename, jdbcType=VARCHAR}, " +
            "#{localtion_row,   jdbcType=INTEGER}, " +
            "#{localtion_col,  jdbcType=INTEGER}," +
            " #{localtion_layer,   jdbcType=INTEGER}, " +
            "#{store_localtion, mode=OUT, jdbcType=VARCHAR})}")
    void callYourProcedure(@Param("warehosename") String warehosename,
                           @Param("localtion_row") Integer localtion_row,
                           @Param("localtion_col") Integer localtion_col,
                           @Param("localtion_layer") Integer localtion_layer,
                           @Param("store_localtion") String store_localtion);
}






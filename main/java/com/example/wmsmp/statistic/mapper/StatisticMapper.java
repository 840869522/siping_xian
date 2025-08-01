package com.example.wmsmp.statistic.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
* @author wlixun
* @createDate 2024-08-06 09:42:23
*/
@Mapper
public interface StatisticMapper{

    List<Map<String, Object>> materialChartOut(Map<String, Object> param);

    List<Map<String, Object>> materialChartIn(Map<String, Object> param);

    Long taskCountP(Map<String, Object> query);

    Long taskCountD(Map<String, Object> query);

    Long taskCountX(Map<String, Object> query);

    Long taskCountB(Map<String, Object> query);

    Long queryCount(Map<String, Object> param);

    List<Map<String, Object>> queryInventoryCount(Map<String, Object> param);

    List<Map<String, Object>> pieSuccesP(Map<String, Object> param);

    List<Map<String, Object>> pieSuccesD(Map<String, Object> param);

    List<Map<String, Object>> pieSuccesX(Map<String, Object> param);

    List<Map<String, Object>> pieSuccesB(Map<String, Object> param);
}





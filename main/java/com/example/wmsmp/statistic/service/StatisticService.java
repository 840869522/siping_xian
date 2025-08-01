package com.example.wmsmp.statistic.service;

import com.example.wmsmp.entity.system.InterReturn;

import java.util.Map;

/**
 * 测试adminService接口
 *
 * @author wlixun
 * @date 2024-07-19
 */
public interface StatisticService{

    InterReturn materialChart(Map<String, Object> param);

    InterReturn libraryStatement(Map<String, Object> param);

    InterReturn pieChart(Map<String, Object> param);

    InterReturn dayChart(Map<String, Object> param);

    InterReturn successPie(Map<String, Object> param);

    InterReturn materialRanking(Map<String, Object> param);

    InterReturn inventoryUse(Map<String, Object> param);

    InterReturn taskWeek(Map<String, Object> param);

    InterReturn taskType(Map<String, Object> param);
}

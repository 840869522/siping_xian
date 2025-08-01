package com.example.wmsmp.statistic.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.mapper.*;
import com.example.wmsmp.statistic.mapper.StatisticMapper;
import com.example.wmsmp.statistic.service.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试adminService业务层处理
 *
 * @author wlixun
 * @date 2024-07-19
 */
@Service
@Slf4j
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class StatisticServiceImpl implements StatisticService {

    @Resource
    private OutOrderDetailMapper outOrderDetailMapper;

    @Resource
    private InOrderDetailMapper inOrderDetailMapper;

    @Resource
    private TaskPMapper taskPMapper;

    @Resource
    private TaskDMapper taskDMapper;

    @Resource
    private TaskBMapper taskBMapper;

    @Resource
    private TaskXMapper taskXMapper;

    @Resource
    private LocationMapMapper locationMapMapper;

    @Resource
    private InventoryMapper inventoryMapper;

    @Resource
    private StatisticMapper statisticMapper;

    @Override
    public InterReturn materialChart(Map<String, Object> param) {
        //回调信息
        InterReturn interReturn = new InterReturn();
        //查询排行榜
        List<Map<String, Object>> callback = new ArrayList<>();
        if (param.containsKey("type")) {
            Integer type = Integer.valueOf(param.get("type").toString());
            if (type == 0) {
                callback = statisticMapper.materialChartOut(param);
                interReturn.setStatus(true);
                interReturn.setMessage("检索成功！");
                interReturn.setResult(callback);
            } else if (type == 1) {
                callback = statisticMapper.materialChartIn(param);
                interReturn.setStatus(true);
                interReturn.setMessage("检索成功！");
                interReturn.setResult(callback);
            }
        } else {
            interReturn.setStatus(false);
            interReturn.setMessage("请选择出入库！");
        }
        return interReturn;
    }

    @Override
    public InterReturn libraryStatement(Map<String, Object> param) {
        //回调信息
        InterReturn interReturn = new InterReturn();
        List<Map<String, Object>> callback = new ArrayList<>();
        //获取统计所需的x轴及查询条件数据
        Map<String, Object> formatTimeMap = formatTime(DateUtil.date());
        //在回调中插入x轴数据
        JSONArray xArray = JSONUtil.parseArray(formatTimeMap.get("x"));
        //开始查询y轴数据
        JSONArray times = JSONUtil.parseArray(formatTimeMap.get("times"));
        //准备Y轴数据list 并查询Y轴数据
        for (int i = 0; i < times.size(); i++) {
            Map<String, Object> chartMap = new HashMap<>();
            //插入X轴数据
            chartMap.put("x", xArray.get(i));
            Map<String, Object> query = JSONUtil.parse(times.get(i)).toBean(Map.class);
            query.put("type", param.get("type"));
            String materialStorageArea = param.get("material_storage_area").toString();
            Long count = 0L;
            if (materialStorageArea.equals("P库")) count = statisticMapper.taskCountP(query);
            else if (materialStorageArea.equals("D库")) count = statisticMapper.taskCountD(query);
            else if (materialStorageArea.equals("X库")) count = statisticMapper.taskCountX(query);
            else if (materialStorageArea.equals("B库")) count = statisticMapper.taskCountB(query);
            chartMap.put("y", count);
            callback.add(chartMap);
        }
        interReturn.setStatus(true);
        interReturn.setMessage("检索成功！");
        interReturn.setResult(callback);
        return interReturn;
    }

    @Override
    public InterReturn pieChart(Map<String, Object> param) {
        //回调信息
        InterReturn interReturn = new InterReturn();
        List<Map<String, Object>> callback = new ArrayList<>();
        //查询库位被占用数量 0 未占用 1 已占用
        param.put("type", 1);
        Long notEmptyCount = statisticMapper.queryCount(param);
        Map<String, Object> notMap = new HashMap<>();
        notMap.put("item", "已占用库位");
        notMap.put("count", notEmptyCount);
        callback.add(notMap);
        //未占用
        param.put("type", 0);
        Long emptyCount = statisticMapper.queryCount(param);
        Map<String, Object> map = new HashMap<>();
        map.put("item", "空闲库位");
        map.put("count", emptyCount);
        callback.add(map);
        interReturn.setStatus(true);
        interReturn.setMessage("检索成功！");
        interReturn.setResult(callback);
        return interReturn;
    }

    @Override
    public InterReturn dayChart(Map<String, Object> param) {
        //回调信息
        InterReturn interReturn = new InterReturn();
        //查询出入库排行榜
        List<Map<String, Object>> callback = statisticMapper.queryInventoryCount(param);
        for (int i = 0; i < callback.size(); i++) {
            Map<String, Object> stringObjectMap = callback.get(i);
            stringObjectMap.put("index", i + 1);
        }
        interReturn.setStatus(true);
        interReturn.setMessage("检索成功！");
        interReturn.setResult(callback);
        return interReturn;
    }

    @Override
    public InterReturn successPie(Map<String, Object> param) {
        String materialStorageArea = param.get("material_storage_area").toString();
        //回调信息
        InterReturn interReturn = new InterReturn();
        //查询任务执行情况
        List<Map<String, Object>> callback = new ArrayList<>();
        if (materialStorageArea.equals("P库")) callback = statisticMapper.pieSuccesP(param);
        else if (materialStorageArea.equals("D库")) callback = statisticMapper.pieSuccesD(param);
        else if (materialStorageArea.equals("X库")) callback = statisticMapper.pieSuccesX(param);
        else if (materialStorageArea.equals("B库")) callback = statisticMapper.pieSuccesB(param);
        interReturn.setStatus(true);
        interReturn.setMessage("检索成功！");
        interReturn.setResult(callback);
        return interReturn;
    }

    @Override
    public InterReturn materialRanking(Map<String, Object> param) {
        //回调信息
        InterReturn interReturn = new InterReturn();
        param.put("limit", 5);
        List<Map<String, Object>> maps = statisticMapper.queryInventoryCount(param);
        //封装数据
        List<Map<String, Object>> callback = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("name", map.get("materialname"));
            hashMap.put("value", map.get("num"));
            callback.add(hashMap);
        }
        interReturn.setStatus(true);
        interReturn.setMessage("检索成功！");
        interReturn.setResult(callback);
        return interReturn;
    }

    @Override
    public InterReturn inventoryUse(Map<String, Object> param) {
        InterReturn interReturn = new InterReturn();
        List<Map<String, Object>> callback = new ArrayList<>();
        //已使用
        param.put("status", "启用");
        param.put("type", 1);
        useRatio(param, "已使用");
        callback.add(useRatio(param, "已使用"));
        //未使用
        param.put("status", "启用");
        param.put("type", 0);
        callback.add(useRatio(param, "未使用"));
        //禁用
        param.put("status", "禁用");
        param.remove("type");
        callback.add(useRatio(param, "禁用"));
        interReturn.setStatus(true);
        interReturn.setMessage("检索成功！");
        interReturn.setResult(callback);
        return interReturn;
    }

    @Override
    public InterReturn taskWeek(Map<String, Object> param) {
        InterReturn interReturn = new InterReturn();
        List<Long> callback = new ArrayList<>();
        //获取统计所需的查询条件数据
        List<Map<String, Object>> weekMap = formatTime4Week(DateUtil.date());
        //准备Y轴数据list 并查询Y轴数据
        for (Map<String, Object> query : weekMap) {
            String materialStorageArea = param.get("material_storage_area").toString();
            Long count = 0L;
            if (materialStorageArea.equals("P库")) count = statisticMapper.taskCountP(query);
            else if (materialStorageArea.equals("D库")) count = statisticMapper.taskCountD(query);
            else if (materialStorageArea.equals("X库")) count = statisticMapper.taskCountX(query);
            else if (materialStorageArea.equals("B库")) count = statisticMapper.taskCountB(query);
            callback.add(count);
        }
        interReturn.setStatus(true);
        interReturn.setMessage("检索成功！");
        interReturn.setResult(callback);
        return interReturn;
    }

    @Override
    public InterReturn taskType(Map<String, Object> param) {
        InterReturn interReturn = new InterReturn();
        Map<String, Object> callback = new HashMap<>();
        //获取统计所需的查询条件数据
        DateTime date = DateUtil.date();
        String startTime = DateUtil.format(DateUtil.beginOfWeek(date), "yyyy-MM-dd HH:mm:ss");
        String endTime = DateUtil.format(DateUtil.endOfWeek(date), "yyyy-MM-dd HH:mm:ss");
        param.put("startTime", startTime);
        param.put("endTime", endTime);
        //准备Y轴数据list 并查询Y轴数据
        String materialStorageArea = param.get("material_storage_area").toString();
        List<String> xList;
        if (materialStorageArea.equals("P库")) {
            xList = ListUtil.list(false, "入库", "回库", "出库", "载货台入库");
            callback.put("x", xList);
            List<Long> countList = new ArrayList<>();
            for (String taskType : xList) {
                param.put("taskType", taskType);
                countList.add(statisticMapper.taskCountP(param));
            }
            callback.put("y", countList);
        } else if (materialStorageArea.equals("D库")) {
            xList = ListUtil.list(false, "入库", "出库", "移库", "满入入库");
            callback.put("x", xList);
            List<Long> countList = new ArrayList<>();
            for (String taskType : xList) {
                param.put("taskType", taskType);
                countList.add(statisticMapper.taskCountD(param));
            }
            callback.put("y", countList);
        } else if (materialStorageArea.equals("X库")) {
            xList = ListUtil.list(false, "入库", "出库");
            callback.put("x", xList);
            List<Long> countList = new ArrayList<>();
            for (String taskType : xList) {
                param.put("taskType", taskType);
                countList.add(statisticMapper.taskCountX(param));
            }
            callback.put("y", countList);
        } else if (materialStorageArea.equals("B库")) {
            xList = ListUtil.list(false, "入库", "回库", "出库", "移库", "满箱给agv", "满入入库");
            callback.put("x", xList);
            List<Long> countList = new ArrayList<>();
            for (String taskType : xList) {
                param.put("taskType", taskType);
                countList.add(statisticMapper.taskCountB(param));
            }
            callback.put("y", countList);
        }
        interReturn.setStatus(true);
        interReturn.setMessage("检索成功！");
        interReturn.setResult(callback);
        return interReturn;
    }

    /**
     * 查询利用率
     *
     * @param param
     * @param name  回调名称
     * @return
     */
    private Map<String, Object> useRatio(Map<String, Object> param, String name) {
        Map<String, Object> callbackMap = new HashMap<>();
        Long count = statisticMapper.queryCount(param);
        callbackMap.put("name", name);
        callbackMap.put("value", count);
        return callbackMap;
    }

    /**
     * 今日昨日任务数曲线图
     *
     * @param param 库位
     * @return
     */
    private Map<String, Object> statisticTaskGraph(Map<String, Object> param) {
        Map<String, Object> callback = new HashMap<>();
        String materialStorageArea = param.get("material_storage_area").toString();
        callback.put("today", taskGraph(DateUtil.date(), materialStorageArea));
        callback.put("yesterday", taskGraph(DateUtil.yesterday(), materialStorageArea));
        return callback;
    }

    /**
     * 曲线图查询
     *
     * @param data
     * @param materialStorageArea
     * @return
     */
    private Map<String, Object> taskGraph(DateTime data, String materialStorageArea) {
        Map<String, Object> callback = new HashMap<>();
        //获取今日能耗曲线图
        Map<String, Object> todayTimeMap = hourTime(DateUtil.date());
        //查询曲线图y轴数据
        JSONArray times = JSONUtil.parseArray(todayTimeMap.get("times"));
        //准备Y轴数据list 并查询Y轴数据
        List<Long> yList = new ArrayList<>();
        for (Object time : times) {
            Map<String, Object> query = JSONUtil.parse(time).toBean(Map.class);
            Long count = 0L;
            if (materialStorageArea.equals("P库")) count = statisticMapper.taskCountP(query);
            else if (materialStorageArea.equals("D库")) count = statisticMapper.taskCountD(query);
            else if (materialStorageArea.equals("X库")) count = statisticMapper.taskCountX(query);
            else if (materialStorageArea.equals("B库")) count = statisticMapper.taskCountB(query);
            yList.add(count);
        }
        callback.put("x", todayTimeMap.get("x"));
        callback.put("y", yList);
        return callback;
    }

    /**
     * 格式化查询条件
     *
     * @param time
     * @param param
     */
    private void dayTime(DateTime time, Map<String, Object> param) {
        param.put("startTime", DateUtil.format(DateUtil.beginOfDay(time), "yyyy-MM-dd HH:mm:ss"));
        param.put("endTime", DateUtil.format(DateUtil.endOfDay(time), "yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 某日小时颗粒度查询条件
     *
     * @param date
     */
    private Map<String, Object> hourTime(DateTime date) {
        Map<String, Object> callback = new HashMap<>();
        //x轴数据 时间
        List<String> timeList = new ArrayList<>();
        DateTime startTime = DateUtil.beginOfDay(date);
        //time查询数据
        List<Map<String, String>> queryMap = new ArrayList<>();
        for (int i = 0; i < DateUtil.hour(date, true) + 1; i++) {
            DateTime dateTime = DateUtil.offsetHour(startTime, i);
            String format = DateUtil.format(dateTime, "HH时");
            timeList.add(format);
            //time查询条件
            Map<String, String> time = new HashMap<>();
            time.put("startTime", DateUtil.format(DateUtil.beginOfHour(dateTime), "yyyy-MM-dd HH:mm:ss"));
            time.put("endTime", DateUtil.format(DateUtil.endOfHour(dateTime), "yyyy-MM-dd HH:mm:ss"));
            queryMap.add(time);
        }
        callback.put("x", timeList);
        callback.put("times", queryMap);
        return callback;
    }

    /**
     * 格式化查询条件 月
     *
     * @param date 需要格式化的年化时间
     * @return
     */
    private Map<String, Object> formatTime(DateTime date) {
        Map<String, Object> callback = new HashMap<>();
        //x轴数据 时间
        List<String> timeList = new ArrayList<>();
        DateTime startTime = DateUtil.beginOfYear(date);
        //time查询数据
        List<Map<String, String>> queryMap = new ArrayList<>();
        for (int i = 0; i < DateUtil.month(date) + 1; i++) {
            DateTime dateTime = DateUtil.offsetMonth(startTime, i);
            String format = DateUtil.format(dateTime, "MM月");
            timeList.add(format);
            //time查询条件
            Map<String, String> time = new HashMap<>();
            time.put("startTime", DateUtil.format(DateUtil.beginOfMonth(dateTime), "yyyy-MM-dd HH:mm:ss"));
            time.put("endTime", DateUtil.format(DateUtil.endOfMonth(dateTime), "yyyy-MM-dd HH:mm:ss"));
            queryMap.add(time);
        }
        callback.put("x", timeList);
        callback.put("times", queryMap);
        return callback;
    }

    /**
     * 格式化查询条件周
     *
     * @param date
     * @return
     */
    private List<Map<String, Object>> formatTime4Week(DateTime date) {
        DateTime startTime = DateUtil.beginOfWeek(date);
        //time查询数据
        List<Map<String, Object>> queryMap = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            DateTime dateTime = DateUtil.offsetDay(startTime, i);
            //time查询条件
            Map<String, Object> time = new HashMap<>();
            time.put("startTime", DateUtil.format(DateUtil.beginOfDay(dateTime), "yyyy-MM-dd HH:mm:ss"));
            time.put("endTime", DateUtil.format(DateUtil.endOfDay(dateTime), "yyyy-MM-dd HH:mm:ss"));
            queryMap.add(time);
        }
        return queryMap;
    }
}




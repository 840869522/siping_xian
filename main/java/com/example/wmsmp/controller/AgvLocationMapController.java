package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.entity.task.AGV.AgvLocationMap;
import com.example.wmsmp.service.AgvLocationMapService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Api(value = "123", tags = "MAP-AGV")
@RestController
@CrossOrigin
public class AgvLocationMapController {

    @Autowired
    AgvLocationMapService agvlocationMapService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    private static Logger logger = Logger.getLogger(AgvLocationMapController.class);

    /**
     * @Author:xhc
     * @Description 按Page查询agv货位关系
     * @DateTime 2024/8/11 14:29
     * @Params
     * @Return
     */
    @ApiOperation(value = "按Page查询agv货位关系", notes = "参数：AgvLocationMap类(货位号、托盘号、占用状态)、pageNo、pageSize")
    @PostMapping("/AgvLocationMap/GetAGVMapByPage")
    @CrossOrigin
    public InterReturn GetAGVMapByPage(@RequestBody AgvLocationMap agvLocationMap, int pageNo, int pageSize) {
        // logger.info("进入 GetAGVMapByPage");
        // logger.info("接收到参数：" + agvLocationMap);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<AgvLocationMap> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<AgvLocationMap> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(agvLocationMap.getLocation_code() != null && !agvLocationMap.getLocation_code().isEmpty(),
                    AgvLocationMap::getLocation_code, agvLocationMap.getLocation_code());//货位号
            wrapper.eq(agvLocationMap.getPallet_code() != null && !agvLocationMap.getPallet_code().isEmpty(),
                    AgvLocationMap::getPallet_code, agvLocationMap.getPallet_code());//载具编号
            wrapper.eq(agvLocationMap.getStatus() != null && !agvLocationMap.getStatus().isEmpty(),
                    AgvLocationMap::getStatus, agvLocationMap.getStatus());//占用状态
            wrapper.orderByAsc(AgvLocationMap::getId);//顺序
            //需要按时间查询时 放开此行              wrapper.between(AgvTask::getCreate_time, startTime, endTime);//时间在这两个时间中间

            List<AgvLocationMap> agvLocationMaps = agvlocationMapService.page(page, wrapper).getRecords();
            if (agvLocationMaps == null || agvLocationMaps.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找agv库区信息！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询agv库区信息成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(agvLocationMaps);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询agv库区信息出错：" + e.getCause());
            System.out.println("查询agv库区信息出错：" + e.getMessage());
            logger.error("查询agv库区信息出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        //  logger.info("GetAGVMapByPage 返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:xhc
     * @Description 按货位号更新
     * @DateTime 2024/8/11 14:39
     * @Params
     * @Return
     */
    @ApiOperation(value = "按货位号更新", notes = "参数：AgvLocationMap类(货位号、托盘号、占用状态)")
    @PostMapping("/AgvLocationMap/UpdateAGVMapByLocation")
    @CrossOrigin
    public InterReturn UpdateAGVMapByLocation(@RequestBody AgvLocationMap agvLocationMap) {
        logger.info("进入 UpdateAGVMapByLocation");
        logger.info("接收到参数：" + agvLocationMap);
        InterReturn interReturn = new InterReturn();
        try {
            if (agvLocationMap.getLocation_code() == null || agvLocationMap.getLocation_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("位置号为空");
                System.out.println("位置号为空");
                logger.info("UpdateAGVMapByLocation 返回：" + interReturn);
                return interReturn;
            }

            LambdaUpdateWrapper<AgvLocationMap> wrapperUpdate = new LambdaUpdateWrapper<>();
            wrapperUpdate.eq(AgvLocationMap::getLocation_code, agvLocationMap.getLocation_code());//位置号
            AgvLocationMap agvLocationMap1 = new AgvLocationMap();
            agvLocationMap1.setPallet_code(agvLocationMap.getPallet_code());//托盘号
            if (agvLocationMap.getStatus() == null || agvLocationMap.getStatus().length() == 0) {
                //不更新
            } else {
                //更新
                agvLocationMap1.setStatus(agvLocationMap.getStatus());//状态
            }
            boolean boolUpdate = agvlocationMapService.update(agvLocationMap1, wrapperUpdate);
            if (boolUpdate) {
                interReturn.setStatus(true);
                interReturn.setMessage("agv库区信息更新成功");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("agv库区信息更新失败");
                logger.info("UpdateAGVMapByLocation 返回：" + interReturn);
                return interReturn;
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("agv库区信息更新出错：" + e.getCause());
            System.out.println("agv库区信息更新出错：" + e.getMessage());
            logger.error("agv库区信息更新出错：" + e.getMessage());
        }
        logger.info("UpdateAGVMapByLocation 返回：" + interReturn);
        return interReturn;
    }
}

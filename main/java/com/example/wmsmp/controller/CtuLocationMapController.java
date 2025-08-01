package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.entity.task.CTU.CtuLocationMap;
import com.example.wmsmp.service.CtuLocationMapService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "123", tags = "MAP-CTU")
@RestController
@CrossOrigin
public class CtuLocationMapController {
    @Autowired
    CtuLocationMapService ctulocationMapService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    private static Logger logger = Logger.getLogger(CtuLocationMapController.class);


    /**
     * @Author:xhc
     * @Description 按Page查询ctu货位关系
     * @DateTime 2024/8/11 14:43
     * @Params
     * @Return
     */
    @ApiOperation(value = "按Page查询ctu货位关系", notes = "参数：CtuLocationMap类(货位号、托盘号、占用状态)、pageNo、pageSize")
    @PostMapping("/CtuLocationMap/GetCTUMapByPage")
    @CrossOrigin
    public InterReturn GetCTUMapByPage(@RequestBody CtuLocationMap ctuLocationMap, int pageNo, int pageSize) {
        // logger.info("进入 GetCTUMapByPage");
        // logger.info("接收到参数：" + ctuLocationMap);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<CtuLocationMap> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<CtuLocationMap> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ctuLocationMap.getLocation_code() != null && !ctuLocationMap.getLocation_code().isEmpty(),
                    CtuLocationMap::getLocation_code, ctuLocationMap.getLocation_code());//货位号
            wrapper.eq(ctuLocationMap.getPallet_code() != null && !ctuLocationMap.getPallet_code().isEmpty(),
                    CtuLocationMap::getPallet_code, ctuLocationMap.getPallet_code());//载具编号
            wrapper.eq(ctuLocationMap.getStatus() != null && !ctuLocationMap.getStatus().isEmpty(),
                    CtuLocationMap::getStatus, ctuLocationMap.getStatus());//占用状态
            wrapper.orderByAsc(CtuLocationMap::getId);//顺序
            //需要按时间查询时 放开此行              wrapper.between(AgvTask::getCreate_time, startTime, endTime);//时间在这两个时间中间

            List<CtuLocationMap> ctuLocationMaps = ctulocationMapService.page(page, wrapper).getRecords();
            if (ctuLocationMaps == null || ctuLocationMaps.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找ctu库区信息！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询ctu库区信息成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(ctuLocationMaps);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询ctu库区信息出错：" + e.getCause());
            System.out.println("查询ctu库区信息出错：" + e.getMessage());
            logger.error("查询ctu库区信息出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        //  logger.info("GetCTUMapByPage 返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:xhc
     * @Description 按货位号更新
     * @DateTime 2024/8/11 14:45
     * @Params
     * @Return
     */
    @ApiOperation(value = "按货位号更新", notes = "参数：CtuLocationMap类(货位号、托盘号、占用状态)")
    @PostMapping("/CtuLocationMap/UpdateCTUMapByLocation")
    @CrossOrigin
    public InterReturn UpdateCTUMapByLocation(@RequestBody CtuLocationMap ctuLocationMap) {
        logger.info("进入 UpdateCTUMapByLocation");
        logger.info("接收到参数：" + ctuLocationMap);
        InterReturn interReturn = new InterReturn();
        try {
            if (ctuLocationMap.getLocation_code() == null || ctuLocationMap.getLocation_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("位置号为空");
                System.out.println("位置号为空");
                logger.info("UpdateCTUMapByLocation 返回：" + interReturn);
                return interReturn;
            }

            LambdaUpdateWrapper<CtuLocationMap> wrapperUpdate = new LambdaUpdateWrapper<>();
            wrapperUpdate.eq(CtuLocationMap::getLocation_code, ctuLocationMap.getLocation_code());//位置号
            CtuLocationMap ctuLocationMap1 = new CtuLocationMap();
            ctuLocationMap1.setPallet_code(ctuLocationMap.getPallet_code());//托盘号
            if (ctuLocationMap.getStatus() == null || ctuLocationMap.getStatus().length() == 0) {
                //不更新
            } else {
                //更新
                ctuLocationMap1.setStatus(ctuLocationMap.getStatus());//状态
            }
            boolean boolUpdate = ctulocationMapService.update(ctuLocationMap1, wrapperUpdate);
            if (boolUpdate) {
                interReturn.setStatus(true);
                interReturn.setMessage("ctu库区信息更新成功");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("ctu库区信息更新失败");
                logger.info("UpdateCTUMapByLocation 返回：" + interReturn);
                return interReturn;
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("ctu库区信息更新出错：" + e.getCause());
            System.out.println("ctu库区信息更新出错：" + e.getMessage());
            logger.error("ctu库区信息更新出错：" + e.getMessage());
        }
        logger.info("UpdateCTUMapByLocation 返回：" + interReturn);
        return interReturn;
    }
}

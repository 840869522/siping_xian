package com.example.wmsmp.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.wmsmp.entity.Flow;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.service.FlowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.List;

/**
 * @Author:lJy
 * @Description :作业流水
 * @DateTime 2023/7/19 10:49
 * @Params
 * @Return
 */

@Api(value = "123", tags = "作业流水")
@RestController
@CrossOrigin

public class FlowController {
    @Autowired
    FlowService flowService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    private static Logger logger = Logger.getLogger(BaseMaterialController.class);


    @ApiOperation(value = "条件查询作业流水", notes = "Flow类 pageNo pageSize startTime endTime")
    @PostMapping("/Flow/GetFlowBy")
    @CrossOrigin
    public InterReturn GetFlowBy(@RequestBody Flow flow, int pageNo, int pageSize, String startTime, String endTime) {
        logger.info("进入 GetFlowBy");
        logger.info("接收到参数：" + flow);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值

            //mybatisplus
            Page<Flow> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<Flow> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(StrUtil.isNotBlank(flow.getFlow_title()), Flow::getFlow_title, flow.getFlow_title());
            wrapper.eq(StrUtil.isNotBlank(flow.getMaterial_code()), Flow::getMaterial_code, flow.getMaterial_code());
            wrapper.like(StrUtil.isNotBlank(flow.getMaterial_name()), Flow::getMaterial_name, flow.getMaterial_name());
            wrapper.eq(StrUtil.isNotBlank(flow.getBatch()), Flow::getBatch, flow.getPallet_code());
            wrapper.eq(StrUtil.isNotBlank(flow.getPallet_code()), Flow::getPallet_code, flow.getBatch());
            wrapper.eq(StrUtil.isNotBlank(flow.getOrder_id()), Flow::getOrder_id, flow.getOrder_id());
//            wrapper.between(StrUtil.isNotBlank(startTime) && StrUtil.isNotBlank(endTime), Flow::getCreate_time, startTime, endTime);//时间在这两个时间中间
            wrapper.between(StrUtil.isNotBlank(flow.getStartTime()) && StrUtil.isNotBlank(flow.getEndTime()), Flow::getCreate_time, flow.getStartTime(), flow.getEndTime());//时间在这两个时间中间
            wrapper.orderByDesc(Flow::getCreate_time);
            List<Flow> flows = flowService.page(page, wrapper).getRecords();
            if (flows == null || flows.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到作业流水！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询作业流水成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(flows);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询作业流水出错：" + e.getCause());
            System.out.println("查询作业流水出错：" + e.getMessage());
            logger.error("查询作业流水出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        logger.info("GetFlowBy 返回：" + interReturn);
        return interReturn;
    }


    //    @ApiOperation(value = "新增作业流水")
//    @PostMapping("/Flow/AddFlow")
//    @CrossOrigin
    public InterReturn AddFlow(@RequestBody Flow flow) {
        logger.info("进入 AddFlow");
        logger.info("接收到参数：" + flow);
        InterReturn interReturn = new InterReturn();
        try {
            if (flow.getFlow_type() == null || flow.getFlow_type().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("作业类别异常或类别名为空");
                System.out.println("作业类别异常或类别名为空");
                return interReturn;
            } else if (flow.getOrder_id() == null || flow.getOrder_id().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("订单号异常或订单号为空");
                System.out.println("订单号异常或订单号为空");
                return interReturn;
            }

            flow.setCreate_time(Calendar.getInstance().getTime());
            boolean boolSave = flowService.save(flow);
            if (boolSave) {
                interReturn.setStatus(true);
                interReturn.setMessage("OK");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("添加作业流水数据条数为0");
            }

        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("新增作业流水出错：" + e.getCause());
            System.out.println("新增作业流水出错：" + e.getMessage());
            logger.error("新增作业流水出错：" + e.getMessage());
        }
        logger.info("AddFlow返回：" + interReturn);
        return interReturn;
    }

}

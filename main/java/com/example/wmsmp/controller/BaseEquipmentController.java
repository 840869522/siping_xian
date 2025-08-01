package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.base.BaseEquipment;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.service.BaseEquipmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.List;

@Api(value = "123", tags = "设备信息")
@RestController
@CrossOrigin
public class BaseEquipmentController {
    @Autowired
    BaseEquipmentService baseEquipmentService;
    private static Logger logger = Logger.getLogger(BaseEquipmentController.class);

    /**
     * @Author:lcy
     * @Description
     * @DateTime 2023/8/15 9:48
     * @Params
     * @Return
     */

    @ApiOperation(value = "条件查询设备接口")
    @PostMapping("/BaseEquipment/GetBaseEquipmentBy")
    @CrossOrigin
    public InterReturn GetBaseEquipmentBy(@RequestBody BaseEquipment baseEquipment, int pageNo, int pageSize) {
//        logger.info("进入到GetBaseEquipmentBy方法");
//        logger.info("接收到参数：" + baseEquipment);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<BaseEquipment> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<BaseEquipment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(baseEquipment.getEquipment_name() != null && !baseEquipment.getEquipment_name().isEmpty(),
                    BaseEquipment::getEquipment_name, baseEquipment.getEquipment_name());
            wrapper.eq(baseEquipment.getEquipment_code() != null && !baseEquipment.getEquipment_code().isEmpty(),
                    BaseEquipment::getEquipment_code, baseEquipment.getEquipment_code());
            wrapper.eq(baseEquipment.getStatus() != null && !baseEquipment.getStatus().isEmpty(),
                    BaseEquipment::getStatus, baseEquipment.getStatus());
            wrapper.orderByDesc(BaseEquipment::getUpdate_time);
            List<BaseEquipment> equipment = baseEquipmentService.page(page, wrapper).getRecords();

            if (equipment == null || equipment.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到设备信息！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询到设备信息成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(equipment);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错：" + e.getCause());
            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
//        logger.info("GetBaseEquipmentBy 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:lcy
     * @Description
     * @DateTime 2023/8/15 9:48
     * @Params
     * @Return
     */

    @ApiOperation(value = "添加设备")
    @PostMapping("/BaseEquipment/AddBaseEquipment")
    @CrossOrigin
    public InterReturn AddBaseEquipment(@RequestBody BaseEquipment baseEquipment) {
        logger.info("进入 AddBaseEquipment");
        logger.info("接收到参数：" + baseEquipment);
        InterReturn interReturn = new InterReturn();
        try {
            if (baseEquipment.getEquipment_code() == null || baseEquipment.getEquipment_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("设备编号为空！");
                System.out.println("设备编号为空！");
                return interReturn;
            } else if (baseEquipment.getEquipment_name() == null || baseEquipment.getEquipment_name().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("设备名称为空！");
                System.out.println("设备名称为空！");
                return interReturn;
            } else if (baseEquipment.getPeriod() == null || baseEquipment.getPeriod().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("保养期限为空！");
                System.out.println("保养期限为空！");
                return interReturn;
            } else {
                baseEquipment.setCreate_time(Calendar.getInstance().getTime());//创建时间赋值
                baseEquipment.setUpdate_time(Calendar.getInstance().getTime());//更新时间赋值
                boolean boolSave = baseEquipmentService.save(baseEquipment);
                if (boolSave) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("添加设备成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("添加数据条数为0！");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("新增出错：" + e.getCause());
            System.out.println("新增出错：" + e.getMessage());
            logger.error("新增出错：" + e.getMessage());
        }
        logger.info("AddBaseEquipment 返回：" + interReturn);
        return interReturn;
    }
}


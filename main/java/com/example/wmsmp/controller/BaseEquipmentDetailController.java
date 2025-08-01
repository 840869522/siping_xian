package com.example.wmsmp.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.base.BaseEquipmentDetail;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.service.BaseEquipmentDetailService;
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

@Api(value = "123", tags = "设备保养/维修信息")
@RestController
@CrossOrigin
public class BaseEquipmentDetailController {
    @Autowired
    BaseEquipmentDetailService baseEquipmentDetailService;
    private static Logger logger = Logger.getLogger(BaseEquipmentDetailController.class);

    @ApiOperation(value = "条件查询新增保养/维修记录")
    @PostMapping("/BaseEquipmentDetail/GetBaseEquipmentDetailBy")
    @CrossOrigin
    public InterReturn GetBaseEquipmentBy(@RequestBody BaseEquipmentDetail baseEquipmentDetail, int pageNo, int pageSize) {
//        logger.info("进入到GetBaseEquipmentDetailBy方法");
//        logger.info("接收到参数：" + baseEquipmentDetail);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<BaseEquipmentDetail> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<BaseEquipmentDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(baseEquipmentDetail.getEquipment_code() != null && !baseEquipmentDetail.getEquipment_code().isEmpty(),
                    BaseEquipmentDetail::getEquipment_code, baseEquipmentDetail.getEquipment_code());
            wrapper.orderByDesc(BaseEquipmentDetail::getUpdate_time);
            List<BaseEquipmentDetail> equipment = baseEquipmentDetailService.page(page, wrapper).getRecords();
            if (equipment == null || equipment.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到设备保养信息！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询到设备保养信息成功！");
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
     //   logger.info("GetBaseEquipmentDetailBy 返回：" + interReturn);
        return interReturn;
    }


    @ApiOperation(value = "添加设备保养/维修信息")
    @PostMapping("/BaseEquipmentDetail/AddBaseEquipmentDetail")
    @CrossOrigin
    public InterReturn AddBaseEquipmentDetail(@RequestBody BaseEquipmentDetail baseEquipmentDetail) {
        logger.info("进入 AddBaseEquipmentDetail");
        logger.info("接收到参数：" + baseEquipmentDetail);
        InterReturn interReturn = new InterReturn();
        try {
            if (baseEquipmentDetail.getEquipment_code() == null || baseEquipmentDetail.getEquipment_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("设备编号为空！");
                System.out.println("设备编号为空！");
                return interReturn;
            } else if (baseEquipmentDetail.getEquipment_name() == null || baseEquipmentDetail.getEquipment_name().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("设备名称为空！");
                System.out.println("设备名称为空！");
                return interReturn;
            } else if (baseEquipmentDetail.getMaintenance_content() == null || baseEquipmentDetail.getMaintenance_content().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("保养/维修内容为空！");
                System.out.println("保养/维修内容为空！");
                return interReturn;
            } else if (baseEquipmentDetail.getMaintenance_person() == null || baseEquipmentDetail.getMaintenance_person().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("保养/维修日期为空！");
                System.out.println("保养/维修日期为空！");
                return interReturn;
            } else if (baseEquipmentDetail.getMaintenance_date() == null || baseEquipmentDetail.getMaintenance_date().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("保养/维修时间为空！");
                System.out.println("保养/维修时间为空！");
                return interReturn;
            } else {
                baseEquipmentDetail.setCreate_time(Calendar.getInstance().getTime());//创建时间赋值
                baseEquipmentDetail.setUpdate_time(Calendar.getInstance().getTime());
                boolean boolSave = baseEquipmentDetailService.save(baseEquipmentDetail);
                if (boolSave) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("添加设备保养/维修信息成功！");
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
        logger.info("AddBaseEquipmentDetail 返回：" + interReturn);
        return interReturn;
    }
}


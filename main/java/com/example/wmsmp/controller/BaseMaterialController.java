package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.base.BaseMaterial;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.mapper.BaseMaterialMapper;
import com.example.wmsmp.service.BaseMaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Api(value = "123", tags = "基础物料")
@RestController
@CrossOrigin
public class BaseMaterialController {
    @Autowired
    BaseMaterialService baseMaterialService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    private static Logger logger = Logger.getLogger(BaseMaterialController.class);

    /**
     * @Author:lcy
     * @Description: 模糊查询物料信息，兼容基础物料界面查询
     * @DateTime: 2023/7/4 14:30
     * @Params:
     */
    @ApiOperation(value = "模糊查询物料信息")
    @PostMapping("/BaseMaterial/GetMaterialByFuzzy")
    @CrossOrigin
    public InterReturn GetMaterialByFuzzy(@RequestBody BaseMaterial baseMaterial, int pageNo, int pageSize, String startTime, String endTime) {
//        logger.info("进入到GetMaterialByFuzzy方法");
//        logger.info("接收到参数" + baseMaterial);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {

            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<BaseMaterial> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<BaseMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(baseMaterial.getMaterial_code() != null, BaseMaterial::getMaterial_code, baseMaterial.getMaterial_code());
            lambdaQueryWrapper.eq(baseMaterial.getOrg_material_code() != null, BaseMaterial::getOrg_material_code, baseMaterial.getOrg_material_code());
            //模糊查询
            lambdaQueryWrapper.like(baseMaterial.getMaterial_name() != null && !baseMaterial.getMaterial_name().isEmpty(), BaseMaterial::getMaterial_name, baseMaterial.getMaterial_name());
            lambdaQueryWrapper.eq(baseMaterial.getStatus() != null && !baseMaterial.getStatus().isEmpty(), BaseMaterial::getStatus, baseMaterial.getStatus());
            lambdaQueryWrapper.between(BaseMaterial::getCreate_time, startTime, endTime);//时间在这两个时间中间
            lambdaQueryWrapper.orderByDesc(BaseMaterial::getUpdate_time);
            List<BaseMaterial> materials = baseMaterialService.page(page, lambdaQueryWrapper).getRecords();

            if (materials == null || materials.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找物料信息！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询物料信息成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(materials);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询物料信息出错：" + e.getCause());
            System.out.println("查询物料信息出错：" + e.getMessage());
            logger.error("查询物料信息出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        // logger.info("GetMaterialByFuzzy返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description: 精确查询物料信息
     * @DateTime: 2024/3/5 10:35
     * @Params:
     */
    @ApiOperation(value = "精确查询物料信息")
    @PostMapping("/BaseMaterial/GetMaterialBy")
    @CrossOrigin
    public InterReturn GetMaterialBy(@RequestBody BaseMaterial baseMaterial, int pageNo, int pageSize, String startTime, String endTime) {

        logger.info("进入到GetMaterialBy方法");
        logger.info("接收到参数" + baseMaterial);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {

            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<BaseMaterial> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<BaseMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(baseMaterial.getMaterial_code() != null, BaseMaterial::getMaterial_code, baseMaterial.getMaterial_code());
            //精确查询
            lambdaQueryWrapper.eq(baseMaterial.getMaterial_name() != null && !baseMaterial.getMaterial_name().isEmpty(), BaseMaterial::getMaterial_name, baseMaterial.getMaterial_name());
            lambdaQueryWrapper.eq(baseMaterial.getStatus() != null && !baseMaterial.getStatus().isEmpty(), BaseMaterial::getStatus, baseMaterial.getStatus());
            lambdaQueryWrapper.between(BaseMaterial::getCreate_time, startTime, endTime);//时间在这两个时间中间
            lambdaQueryWrapper.orderByDesc(BaseMaterial::getUpdate_time);
            List<BaseMaterial> materials = baseMaterialService.page(page, lambdaQueryWrapper).getRecords();

            if (materials == null || materials.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找物料信息！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询物料信息成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(materials);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询物料信息出错：" + e.getCause());
            System.out.println("查询物料信息出错：" + e.getMessage());
            logger.error("查询物料信息出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        logger.info("GetMaterialBy返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:lcy
     * @Description :添加物料
     * @DateTime 2023/7/19 10:49
     * @Params
     * @Return
     */
    @ApiOperation(value = "添加物料")
    @PostMapping("/BaseMaterial/AddMaterial")
    @CrossOrigin
    public InterReturn AddMaterial(@RequestBody BaseMaterial baseMaterial) {
        logger.info("进入 AddMaterial");
        logger.info("接收到参数：" + baseMaterial);
        InterReturn interReturn = new InterReturn();
        try {
            if (baseMaterial.getMaterial_code() == null) {
                interReturn.setStatus(false);
                interReturn.setMessage("物料编码空！");
                System.out.println("物料编码空！");
                return interReturn;
            } else if (baseMaterial.getMaterial_name() == null || baseMaterial.getMaterial_name().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("物料名称为空！");
                System.out.println("物料名称为空！");
                return interReturn;

//            } else if (baseMaterial.getStatus() == null || baseMaterial.getStatus().length() == 0) {
//                interReturn.setStatus(false);
//                interReturn.setMessage("物料状态为空！");
//                System.out.println("物料状态为空！");
//                return interReturn;


            } else {
                baseMaterial.setCreate_time(Calendar.getInstance().getTime());//创建时间赋值
                baseMaterial.setUpdate_time(Calendar.getInstance().getTime());//更新时间赋值
                //mybatisplus
                boolean boolSave = baseMaterialService.save(baseMaterial);
                if (boolSave) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("添加物料成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("添加数据条数为0");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("新增出错：" + e.getCause());
            System.out.println("新增出错：" + e.getMessage());
            logger.error("新增出错：" + e.getMessage());
        }
        logger.info("AddMaterial 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 批量添加物料
     * @DateTime 2024/8/11 9:53
     * @Params
     * @Return
     */
    @ApiOperation(value = "批量添加物料")
    @PostMapping("/BaseMaterial/AddMaterialBatch")
    @CrossOrigin
    public InterReturn AddMaterialBatch(@RequestBody List<BaseMaterial> baseMaterialList) {
        logger.info("进入 AddMaterialBatch");
        logger.info("接收到参数：" + baseMaterialList);
        InterReturn interReturn = new InterReturn();
        try {
            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            BaseMaterialMapper baseMaterialMapper = sqlSession.getMapper(BaseMaterialMapper.class);//获取对应Mapper

            Date time = Calendar.getInstance().getTime();//创建时间赋值

            for (BaseMaterial baseMaterial : baseMaterialList) {
                {
                    //判断物料编号是否为空
                    if (baseMaterial.getMaterial_code() == null || baseMaterial.getMaterial_code().trim().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("物料编号有空值");
                        System.out.println("物料编号有空值");
                        return interReturn;
                    }
                    //判断物料名称是否为空
                    if (baseMaterial.getMaterial_name() == null || baseMaterial.getMaterial_name().trim().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("物料名称有空值");
                        System.out.println("物料名称有空值");
                        return interReturn;
                    }
//   价格不是必填项    //判断物料单价是否小于等于0
//                    if (baseMaterial.getMaterial_price().compareTo(BigDecimal.ZERO) <= 0) {
//                        interReturn.setStatus(false);
//                        interReturn.setMessage("物料单价异常");
//                        System.out.println("物料单价异常");
//                        return interReturn;
//                    }
                    //判断旧物料编号是否为空
                    if (baseMaterial.getOrg_material_code() == null || baseMaterial.getOrg_material_code().trim().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("旧物料编号有空值");
                        System.out.println("旧物料编号有空值");
                        return interReturn;
                    }
                }


                baseMaterial.setCreate_time(time);
                baseMaterial.setUpdate_time(time);
                baseMaterial.setCreator("批量添加");
                baseMaterial.setStatus("启用");
//                baseMaterial.setUpdater( );
                baseMaterialMapper.insert(baseMaterial);//准备执行sql
            }

            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("批量新增物料成功！");
            } catch (Exception e) {
                logger.error("批量新增物料异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("批量新增物料异常，事务回滚:" + e.getCause());
            } finally {
                sqlSession.close();
            }


        } catch (Exception ee) {
            interReturn.setStatus(false);
            interReturn.setMessage("批量新增物料出错：" + ee.getCause());
            System.out.println("批量新增物料出错：" + ee.getMessage());
            logger.error("批量新增物料出错：" + ee.getMessage());
        }
        logger.info("AddMaterialBatch 返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:lcy
     * @Description:更新物料接口，除了物料编号外都可以更新
     * @DateTime 2023/8/10 8:24
     * @Params
     * @Return
     */

    @ApiOperation(value = "更新物料")
    @PostMapping("/BaseMaterial/UpdateMaterial")
    @CrossOrigin
    public InterReturn UpdateMaterial(@RequestBody BaseMaterial baseMaterial) {
        logger.info("进入 UpdateMaterial");
        logger.info("接收到参数：" + baseMaterial);
        InterReturn interReturn = new InterReturn();
        try {
            if (baseMaterial.getMaterial_code() == null || baseMaterial.getMaterial_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或物料编码为空");
                System.out.println("参数异常或物料编码为空");
                return interReturn;
            }
            //mybatisplus
            LambdaUpdateWrapper<BaseMaterial> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(BaseMaterial::getMaterial_code, baseMaterial.getMaterial_code());
            wrapper.set(baseMaterial.getMaterial_name() != null, BaseMaterial::getMaterial_name, baseMaterial.getMaterial_name());
            wrapper.set(baseMaterial.getMaterial_type() != null, BaseMaterial::getMaterial_type, baseMaterial.getMaterial_type());
            wrapper.set(baseMaterial.getSupplier() != null, BaseMaterial::getSupplier, baseMaterial.getSupplier());
            wrapper.set(baseMaterial.getMaterial_spec() != null, BaseMaterial::getMaterial_spec, baseMaterial.getMaterial_spec());
            wrapper.set(baseMaterial.getMaterial_unit() != null, BaseMaterial::getMaterial_unit, baseMaterial.getMaterial_unit());
            wrapper.set(baseMaterial.getLengths() != null, BaseMaterial::getLengths, baseMaterial.getLengths());
            wrapper.set(baseMaterial.getWide() != null, BaseMaterial::getWide, baseMaterial.getWide());
            wrapper.set(baseMaterial.getHight() != null, BaseMaterial::getHight, baseMaterial.getHight());
            wrapper.set(baseMaterial.getWeight() != null, BaseMaterial::getWeight, baseMaterial.getWeight());
            wrapper.set(baseMaterial.getMaterial_storage_area() != null, BaseMaterial::getMaterial_storage_area, baseMaterial.getMaterial_storage_area());
            wrapper.set(baseMaterial.getDraw_no() != null, BaseMaterial::getDraw_no, baseMaterial.getDraw_no());
            wrapper.set(baseMaterial.getCertificate() != null, BaseMaterial::getCertificate, baseMaterial.getCertificate());
            wrapper.set(baseMaterial.getQuality() != null, BaseMaterial::getQuality, baseMaterial.getQuality());
            wrapper.set(baseMaterial.getMin_stock() != null, BaseMaterial::getMin_stock, baseMaterial.getMin_stock());
            wrapper.set(baseMaterial.getStatus() != null, BaseMaterial::getStatus, baseMaterial.getStatus());
            wrapper.set(baseMaterial.getUpdater() != null, BaseMaterial::getUpdater, baseMaterial.getUpdater());
            wrapper.set(BaseMaterial::getUpdate_time, LocalDateTime.now());
            boolean bool = baseMaterialService.update(null, wrapper);
            if (bool) {
                interReturn.setStatus(true);
                interReturn.setMessage("更新成功！");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("无更新！");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("更新出错：" + e.getCause());
            System.out.println("更新出错：" + e.getCause());
            logger.error("更新出错：" + e.getMessage());
        }
        logger.info("UpdateMaterial 返回：" + interReturn);
        return interReturn;
    }


}


package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.base.BasePallet;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.mapper.BasePalletMapper;
import com.example.wmsmp.service.BasePalletService;
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Api(value = "123", tags = "载具信息")
@RestController
@CrossOrigin
public class BasePalletController {
    @Autowired
    BasePalletService basePalletService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    private static Logger logger = Logger.getLogger(BasePalletController.class);

    /**
     * @Author:lcy
     * @Description:模糊查询载具
     * @DateTime 2023/7/28 14:47
     * @Params
     * @Return
     */
    @ApiOperation(value = "模糊查询载具")
    @PostMapping("/BasePallet/GetPalletByFuzzy")
    @CrossOrigin
    public InterReturn GetPalletBy(@RequestBody BasePallet basePallet, int pageNo, int pageSize) {
//        logger.info("进入到GetPalletByFuzzy方法");
//        logger.info("接收到参数" + basePallet);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<BasePallet> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<BasePallet> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            // 模糊查询
            lambdaQueryWrapper.like(basePallet.getPallet_code() != null && !basePallet.getPallet_code().isEmpty(), BasePallet::getPallet_code, basePallet.getPallet_code());
            lambdaQueryWrapper.eq(basePallet.getStatus() != null && !basePallet.getStatus().isEmpty(),
                    BasePallet::getStatus, basePallet.getStatus());
            lambdaQueryWrapper.orderByAsc(BasePallet::getId);
            List<BasePallet> palletInfo = basePalletService.page(page, lambdaQueryWrapper).getRecords();
            if (palletInfo == null || palletInfo.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找载具信息！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询载具信息成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(palletInfo);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询载具信息出错：" + e.getCause());
            System.out.println("查询载具信息出错：" + e.getMessage());
            logger.error("查询载具信息出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        //   logger.info("GetPalletByFuzzy 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 批量添加载具（未优化速度凑活用）
     * @DateTime 2024/8/24 13:12
     * @Params
     * @Return
     */
    @ApiOperation(value = "批量添加载具（未优化速度凑活用）", notes = "起始数字，结束数字")
    @PostMapping("/BasePallet/AddPalletBetween")
    @CrossOrigin
    public InterReturn AddPalletBetween(int low, int high) {
        InterReturn interReturn = new InterReturn();
        int size = high - low + 1;
        for (int i = 0; i < size; i++) {
            BasePallet basePallet = new BasePallet();
            String newNoStr = String.format("%06d", low);//'%04d'的定义:0代表前面要补的字符 6代表字符串长度 d表示参数为整数类型
            basePallet.setPallet_code("P" + newNoStr);
            interReturn = AddPallet(basePallet);
            if (interReturn.isStatus()) {
                interReturn.setStatus(true);
                interReturn.setMessage("P" + newNoStr + "完成");
            } else {
                return interReturn;
            }
            low++;
        }
        return interReturn;
    }

    /**
     * @Author:lcy
     * @Description:生成(添加）载具
     * @DateTime 2023/7/28 14:48
     * @Params
     * @Return
     */
    @ApiOperation(value = "添加载具")
    @PostMapping("/BasePallet/AddPallet")
    @CrossOrigin
    public InterReturn AddPallet(@RequestBody BasePallet basePallet) {
//        logger.info("进入 AddPallet");
//        logger.info("接收到参数：" + basePallet);
        InterReturn interReturn = new InterReturn();
        try {
            if (basePallet.getPallet_code() == null || basePallet.getPallet_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("载具编码空！");
                System.out.println("载具编码空！");
                return interReturn;
            } else {
                //lcy:判断载具是否存在，存在提示
                LambdaQueryWrapper<BasePallet> palletQueryWrapper = new LambdaQueryWrapper<>();
                palletQueryWrapper.eq(BasePallet::getPallet_code, basePallet.getPallet_code());

                List<BasePallet> basePallets = basePalletService.list(palletQueryWrapper);
                if (basePallets.size() > 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage(basePallet.getPallet_code() + "载具已存在！");
                } else {
                    basePallet.setStatus("可用");//是否可用
                    basePallet.setIsprint("否");//是否已打印
                    basePallet.setCreate_time(Calendar.getInstance().getTime());//创建时间赋值
                    basePallet.setUpdate_time(Calendar.getInstance().getTime());//更新时间赋值
                    //mybatisplus
                    boolean boolSave = basePalletService.save(basePallet);
                    if (boolSave) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("添加载具成功！");
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("添加数据条数为0");
                    }
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("新增出错：" + e.getCause());
            System.out.println("新增出错：" + e.getMessage());
            logger.error("新增出错：" + e.getMessage());
        }
        //logger.info("AddPallet 返回：" + interReturn);
        return interReturn;
    }


    @ApiOperation(value = "批量载具更新打印状态")
    @PostMapping("/BasePallet/UpdatePalletIsPrintByCodes")
    @CrossOrigin
    public InterReturn UpdatePalletIsPrintByCodes(@RequestBody List<BasePallet> basePalletList) {
        //logger.info("进入 UpdatePalletIsPrintByCodes");
        //logger.info("接收到参数：" + basePalletList);
        InterReturn interReturn = new InterReturn();
        interReturn.setStatus(false);

        try {
            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            BasePalletMapper mapMapper = sqlSession.getMapper(BasePalletMapper.class);//获取对应Mapper

            Date time = Calendar.getInstance().getTime();//更新时间赋值
            for (BasePallet basePallet : basePalletList) {
                LambdaUpdateWrapper<BasePallet> updateWrapperPallet = new LambdaUpdateWrapper<>();
                updateWrapperPallet.eq(BasePallet::getPallet_code, basePallet.getPallet_code());
                BasePallet newBasePallet = new BasePallet();
                newBasePallet.setUpdate_time(time);
                newBasePallet.setIsprint("是");
                mapMapper.update(newBasePallet, updateWrapperPallet);//准备执行sql
            }

            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("批量更新打印状态成功！");
            } catch (Exception e) {
                logger.error("批量更新打印状态异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("批量更新打印状态异常，事务回滚:" + e.getCause());
            } finally {
                sqlSession.close();
            }
            return interReturn;

        } catch (Exception ee) {
            interReturn.setMessage("生成出错：" + ee.getCause());
            return interReturn;
        }
    }
}
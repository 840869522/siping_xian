package com.example.wmsmp.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.CheckBean;
import com.example.wmsmp.entity.CheckDetailBean;
import com.example.wmsmp.entity.Inventory;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.mapper.CheckDetailMapper;
import com.example.wmsmp.service.CheckDetailService;
import com.example.wmsmp.service.CheckService;
import com.example.wmsmp.service.InventoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Api(value = "123", tags = "库位盘点明细")
@RestController
@CrossOrigin
@Slf4j
public class CheckDetailController {
    @Autowired
    private CheckService checkService;

    @Autowired
    private CheckDetailService checkDetailService;

    @Resource
    private CheckDetailMapper detailMapper;


    @Resource
    private InventoryService inventoryService;

    @ApiOperation(value = "条件查询盘点明细", notes = "CheckDetailBean类（明细ID，盘点单号，物料编号，物料名称） pageNo pageSize")
    @PostMapping("/check/detail/page")
    @CrossOrigin
    public InterReturn GetCheckDetailBeanBy(@RequestBody CheckDetailBean checkDetailBean, int pageNo, int pageSize) {
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<CheckDetailBean> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<CheckDetailBean> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(checkDetailBean.getCheckDetailId() != null, CheckDetailBean::getCheckDetailId, checkDetailBean.getCheckDetailId());//明细ID
            wrapper.eq(StrUtil.isNotBlank(checkDetailBean.getCheckCode()), CheckDetailBean::getCheckCode, checkDetailBean.getCheckCode());//出库单ID
            wrapper.eq(StrUtil.isNotBlank(checkDetailBean.getMaterialCode()), CheckDetailBean::getMaterialCode, checkDetailBean.getMaterialCode());//物料编号
            wrapper.eq(StrUtil.isNotBlank(checkDetailBean.getMaterialName()), CheckDetailBean::getMaterialName, checkDetailBean.getMaterialName());//物料名称
            wrapper.eq(StrUtil.isNotBlank(checkDetailBean.getStatus()), CheckDetailBean::getStatus, checkDetailBean.getStatus());//状态
            wrapper.orderByDesc(CheckDetailBean::getUpdateTime);
            List<CheckDetailBean> inOrderDetails = checkDetailService.page(page, wrapper).getRecords();
            if (inOrderDetails == null || inOrderDetails.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到明细！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询明细成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(inOrderDetails);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询明细出错：" + e.getCause());
            System.out.println("查询明细出错：" + e.getMessage());
            log.error("查询明细出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        return interReturn;
    }

    @ApiOperation(value = "查询物料数据", notes = "checkDetail pageNo pageSize startTime endTime")
    @PostMapping("/check/query/material")
    @CrossOrigin
    public InterReturn queryMaterial(@RequestBody CheckDetailBean checkDetail) {
        return checkDetailService.queryMaterial(checkDetail);
    }


    @ApiOperation(value = "更新盘点明细", notes = "根据OrderId更新用户 传CheckDetailBean类")
    @PostMapping("/check/detail/update")
    @CrossOrigin
    public InterReturn UpdateCheckDetailBeanByOrderId(@RequestBody CheckDetailBean checkDetailBean) {
        InterReturn interReturn = new InterReturn();
        try {
            if (StrUtil.isBlank(checkDetailBean.getMaterialCode())) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或器材编号为空");
                System.out.println("参数异常或器材编号为空");
                return interReturn;
            }
            if (StrUtil.isBlank(checkDetailBean.getBatch())) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或批次号为空");
                System.out.println("参数异常或批次号为空");
                return interReturn;
            }
            if (StrUtil.isBlank(checkDetailBean.getUpdater())) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或更新人为空");
                System.out.println("参数异常或更新人为空");
                return interReturn;
            }
            checkDetailBean.setUpdateTime(DateUtil.now());
            Long bool = checkDetailService.update2checkCount(checkDetailBean);
            //校验是否全部盘点完成 全部完成后 修改表头状态
            LambdaQueryWrapper<CheckDetailBean> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CheckDetailBean::getCheckCode, checkDetailBean.getCheckCode());
            queryWrapper.ne(CheckDetailBean::getStatus, "已完成");
            long count = checkDetailService.count(queryWrapper);
            //物料盘点后回滚库存锁定状态
            callbackInventory(checkDetailBean);
            if (count == 0) {
                LambdaUpdateWrapper<CheckBean> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(CheckBean::getCheckCode, checkDetailBean.getCheckCode());
                updateWrapper.set(CheckBean::getStatus, "已完成");
                checkService.update(updateWrapper);
            }
            if (bool > 0) {
                interReturn.setStatus(true);
                interReturn.setMessage("OK");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("无更新");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("更新出错：" + e.getCause());
            System.out.println("更新出错：" + e.getCause());
            log.error("更新出错：" + e.getMessage());
        }
        return interReturn;
    }


    @ApiOperation(value = "批量添加盘点明细", notes = "传CheckDetailBean类List")
    @PostMapping("/check/detail/Adds")
    @CrossOrigin
    @Transactional
    public InterReturn Adds(@RequestBody List<CheckDetailBean> checkDetailBeans) {
        log.info("进入 Adds");
        log.info("接收到参数：出库明细列表：" + checkDetailBeans);
        InterReturn interReturn = new InterReturn();
        interReturn.setStatus(false);
        try {
            for (CheckDetailBean CheckDetailBean : checkDetailBeans) {
                CheckDetailBean.setCreateTime(DateUtil.now());
                Boolean b = saveDetail(CheckDetailBean);
            }
            interReturn.setStatus(true);
            interReturn.setMessage("生成成功！");
            return interReturn;
        } catch (Exception ee) {
            interReturn.setMessage("生成出错：" + ee.getCause());
            return interReturn;
        }

    }

    /**
     * @Author:wlixun
     * @Description:新增盘点明细
     * @DateTime 2023/8/26 15:18
     * @Params
     * @Return
     */
    @ApiOperation(value = "新增盘点单明细")
    @PostMapping("/check/detail/add")
    @CrossOrigin
    public InterReturn AddCheckDetailBean(@RequestBody CheckDetailBean checkDetailBean) {
        InterReturn interReturn = new InterReturn();
        try {
            if (StrUtil.isBlank(checkDetailBean.getMaterialCode())) {
                interReturn.setStatus(false);
                interReturn.setMessage("物料编号异常或物料编号为空！");
                return interReturn;
            } else if (StrUtil.isBlank(checkDetailBean.getCheckCode())) {
                interReturn.setStatus(false);
                interReturn.setMessage("盘点单头号为空!");
                return interReturn;
            } else if (StrUtil.isBlank(checkDetailBean.getMaterialName())) {
                interReturn.setStatus(false);
                interReturn.setMessage("物料名称异常或物料名称为空!");
                return interReturn;
            } else if (checkDetailBean.getInventoryCount() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("库存数量为0！");
                return interReturn;
            }
            //1.判断表头是否能插入
            LambdaQueryWrapper<CheckBean> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CheckBean::getCheckCode, checkDetailBean.getCheckCode());
            List<CheckBean> onOrders = checkService.list(wrapper);
            if (onOrders.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("盘点单不存在：" + checkDetailBean.getCheckCode());
                System.out.println("盘点单不存在：" + checkDetailBean.getCheckCode());
                return interReturn;
            }
            //只有创建中可以添加
            if (!onOrders.get(0).getStatus().equals("草稿")) {
                interReturn.setStatus(false);
                interReturn.setMessage("盘点单状态异常：" + onOrders.get(0).getStatus());
                System.out.println("盘点单状态异常：" + onOrders.get(0).getStatus());
                return interReturn;
            }
            //获取物料库存数量
            //2.插入明细
            checkDetailBean.setCreateTime(DateUtil.now());
            checkDetailBean.setUpdateTime(DateUtil.now());
            checkDetailBean.setStatus("草稿");
            Boolean boolSave = saveDetail(checkDetailBean);
            if (boolSave) {
                interReturn.setStatus(true);
                interReturn.setMessage("添加出库单明细成功！");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("暂无库存！");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("新增出库单明细出错：" + e.getCause());
            System.out.println("新增出库单明细出错：" + e.getMessage());
            log.error("新增出库单明细出错：" + e.getMessage());
        }
        return interReturn;
    }

    private Boolean saveDetail(CheckDetailBean checkDetailBean) {
        //如果物料已添加则不在添加
        LambdaQueryWrapper<CheckDetailBean> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(CheckDetailBean::getCheckCode, checkDetailBean.getCheckCode());
        countWrapper.eq(CheckDetailBean::getBatch, checkDetailBean.getBatch());
        countWrapper.eq(CheckDetailBean::getMaterialCode, checkDetailBean.getMaterialCode());
        long count = checkDetailService.count(countWrapper);
        if (count > 0) return false;
        //获取库存数量
        List<CheckDetailBean> list = detailMapper.queryMaterial(checkDetailBean);
        //当没有物料及库存为0时都不添加明细数据
        if (list.size() == 0) return false;
        CheckDetailBean detailBean = list.get(0);
        if (detailBean.getInventoryCount() == 0) return false;
        checkDetailBean.setInventoryCount(detailBean.getInventoryCount());
        checkDetailBean.setActual_count(0);
        checkDetailBean.setPick_count(0);
        checkDetailBean.setCreateTime(DateUtil.now());
        checkDetailBean.setUpdateTime(DateUtil.now());
        checkDetailBean.setStatus("草稿");
        boolean boolSave = checkDetailService.save(checkDetailBean);
        return boolSave;
    }

    /**
     * @Author:wlixun
     * @Description :删除盘点明细
     * @DateTime 2023/8/26 15:20
     * @Params
     * @Return
     */
    @ApiOperation(value = "删除盘点明细")
    @PostMapping("/check/detail/delete")
    @CrossOrigin
    public InterReturn DelCheckDetailBean(@RequestBody CheckDetailBean checkDetailBean) {
        InterReturn interReturn = new InterReturn();
        try {
            if (checkDetailBean.getCheckDetailId() == null) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或明细单号为空");
                System.out.println("参数异常或明细单号为空");
                return interReturn;
            } else {
                boolean bool = checkDetailService.removeById(checkDetailBean);
                if (bool) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("删除明细成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("删除数据条数为0！");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("删除出库明细出错：" + e.getCause());
            System.out.println("删除出库明细出错：" + e.getMessage());
            log.error("删除出库明细出错：" + e.getMessage());
        }
        return interReturn;
    }

    /**
     * @Author:wlixun
     * @Description :编辑盘点明细接口
     * @DateTime 2023/8/26 15:26
     * @Params
     * @Return
     */
    @ApiOperation(value = "编辑盘点明细")
    @PostMapping("/check/detail/edit")
    @CrossOrigin
    @Transactional
    public InterReturn EditCheckDetailBean(@RequestBody CheckDetailBean checkDetailBean) {
        InterReturn interReturn = new InterReturn();
        try {
            if (checkDetailBean.getCheckDetailId() == null) {
                interReturn.setStatus(false);
                interReturn.setMessage("主键为空");
            }
            checkDetailBean.setStatus("已完成");
            boolean bool = checkDetailService.updateById(checkDetailBean);
            //校验是否全部盘点完成 全部完成后 修改表头状态
            LambdaQueryWrapper<CheckDetailBean> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CheckDetailBean::getCheckCode, checkDetailBean.getCheckCode());
            wrapper.ne(CheckDetailBean::getStatus, "已完成");
            long count = checkDetailService.count(wrapper);
            //物料盘点后回滚库存锁定状态
            callbackInventory(checkDetailBean);
            if (count == 0) {
                LambdaUpdateWrapper<CheckBean> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(CheckBean::getCheckCode, checkDetailBean.getCheckCode());
                updateWrapper.set(CheckBean::getStatus, "已完成");
                checkService.update(updateWrapper);
            }
            if (bool) {
                interReturn.setStatus(true);
                interReturn.setMessage("盘点成功！");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("盘点失败");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("盘点出错：" + e.getCause());
            log.error("盘点出错：" + e.getMessage());
        }
        return interReturn;
    }

    /**
     * 盘点结束后回滚锁定状态
     * @param checkDetailBean
     */
    private void callbackInventory(CheckDetailBean checkDetailBean){
        //物料全部完成后 解锁物料库存
        LambdaUpdateWrapper<Inventory> wrapperUpdate = new LambdaUpdateWrapper<>();
        wrapperUpdate.eq(Inventory::getMaterial_code, checkDetailBean.getMaterialCode());//id
        wrapperUpdate.eq(Inventory::getBatch, checkDetailBean.getBatch());//id
        wrapperUpdate.set(Inventory::getFrozen_count, 0);//增量
        wrapperUpdate.set(Inventory::getUpdater, checkDetailBean.getUpdater());
        wrapperUpdate.set(Inventory::getUpdate_time, LocalDateTime.now());
        inventoryService.update(wrapperUpdate);
    }


    @ApiOperation(value = "查询返回Excel类", notes = "返回Excel类")
    @PostMapping("/check/detail/GetOutOrderExcel")
    @CrossOrigin
    public InterReturn GetOutOrderExcel(@RequestBody List<CheckBean> outOrders) {
        InterReturn interReturn = new InterReturn();
        List<CheckDetailBean> outOrderExcels = new ArrayList<>();//用于导出Excel
        try {
            if (outOrders == null || outOrders.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("传入表头为空");
                log.info("GetOutOrderExcel返回：" + interReturn);
                return interReturn;
            }
            //去重
            List<CheckBean> newOutOrderList = outOrders.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(CheckBean::getCheckCode))), ArrayList::new));
            for (CheckBean item : newOutOrderList) {
                if (StrUtil.isBlank(item.getCheckCode())) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("传入单据号有空");
                    log.info("GetOutOrderExcel返回：" + interReturn);
                    return interReturn;
                }
            }
            for (CheckBean outOrder : newOutOrderList) {
                LambdaQueryWrapper<CheckDetailBean> wrapperCheckDetailBean = new LambdaQueryWrapper<>();
                wrapperCheckDetailBean.eq(CheckDetailBean::getCheckCode, outOrder.getCheckCode());
                List<CheckDetailBean> CheckDetailBeans = checkDetailService.list(wrapperCheckDetailBean);
                if (CheckDetailBeans.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("“" + outOrder.getCheckCode() + "”单据无明细");
                    log.info("GetOutOrderExcel返回：" + interReturn);
                    return interReturn;
                }
                //拼Excel类
                for (CheckDetailBean CheckDetailBean : CheckDetailBeans) {
                    CheckDetailBean.setCheckCode(outOrder.getCheckCode());//单号
                    CheckDetailBean.setHeSuanKeMu(outOrder.getHeSuanKeMu());//核算科目
                    CheckDetailBean.setPanKuYiJu(outOrder.getPanKuYiJu());//盘库依据
                    CheckDetailBean.setCheckStatus(outOrder.getStatus());//状态
                    CheckDetailBean.setWarehouseName(outOrder.getWarehouseName());//库房
                    outOrderExcels.add(CheckDetailBean);
                }
            }
            interReturn.setStatus(true);
            interReturn.setMessage("查询成功");
            interReturn.setResult(outOrderExcels);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询返回Excel类出错：" + e.getCause());
            System.out.println("查询返回Excel类出错：" + e.getMessage());
            log.error("查询返回Excel类出错：" + e.getMessage());
        }
        return interReturn;
    }

    /**
     * @Author:wlixun
     * @Description 盘点导入 参数Excel类
     * @DateTime 2024/1/27 14:15
     * @Params
     * @Return
     */
    @ApiOperation(value = "导入Excel类", notes = "传Excel类 返回导入结果")
    @PostMapping("/CheckDetailBean/ImportOutOrderExcel")
    @CrossOrigin
    @Transactional
    public InterReturn ImportOutOrderExcel(@RequestBody List<CheckDetailBean> outOrderExcels) {
        InterReturn interReturn = new InterReturn();
        try {
            //判空
            if (outOrderExcels == null || outOrderExcels.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("传入Excel信息为空");
                log.info("ImportOutOrderExcel返回：" + interReturn);
                return interReturn;
            }
            //判单据号空
            for (CheckDetailBean outOrderExcel : outOrderExcels) {
                if (outOrderExcel.getCheckCode() == null || outOrderExcel.getCheckCode().isEmpty()) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("传入单据号有空");
                    log.info("ImportOutOrderExcel返回：" + interReturn);
                    return interReturn;
                }
            }
            //去重
            List<CheckDetailBean> newOutOrderExcelList = outOrderExcels.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(CheckDetailBean::getCheckCode))), ArrayList::new));
            for (CheckDetailBean orderExcel : newOutOrderExcelList) {
                CheckBean outOrder = new CheckBean();
                outOrder.setCheckCode(orderExcel.getCheckCode());//出库单号
                outOrder.setHeSuanKeMu(orderExcel.getHeSuanKeMu());//车牌号
                outOrder.setPanKuYiJu(orderExcel.getPanKuYiJu());//原始单号
                outOrder.setWarehouseName(orderExcel.getWarehouseName());//仓库名称
                outOrder.setStatus("草稿");//单据状态
                outOrder.setCreateTime(DateUtil.now());
                outOrder.setUpdateTime(DateUtil.now());
                checkService.save(outOrder);
            }
            for (CheckDetailBean outOrderExcel : outOrderExcels) {
                outOrderExcel.setStatus("草稿");//明细状态
                outOrderExcel.setCreateTime(DateUtil.now());
                outOrderExcel.setUpdateTime(DateUtil.now());
                outOrderExcel.setActual_count(0);
                outOrderExcel.setPick_count(0);
                checkDetailService.save(outOrderExcel);
            }
            interReturn.setStatus(true);
            interReturn.setMessage("生成成功！");
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("导入Excel类出错：" + e.getCause());
            System.out.println("导入Excel类出错：" + e.getMessage());
            log.error("导入Excel类出错：" + e.getMessage());
        }
        return interReturn;
    }
}

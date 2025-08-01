package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.*;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.mapper.OutOrderDetailMapper;
import com.example.wmsmp.mapper.OutOrderMapper;
import com.example.wmsmp.service.OutOrderDetailService;
import com.example.wmsmp.service.OutOrderService;
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
import java.util.*;
import java.util.stream.Collectors;

@Api(value = "123", tags = "出库明细")
@RestController
@CrossOrigin
public class OutOrderDetailController {
    @Autowired
    OutOrderService outOrderService;

    @Autowired
    OutOrderDetailService outOrderDetailService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    private static Logger logger = Logger.getLogger(OutOrderDetailController.class);

    @ApiOperation(value = "条件查询出库明细", notes = "OutOrderDetail类（出库单明细ID，出库单ID，物料编号，物料名称） pageNo pageSize")
    @PostMapping("/OutOrderDetail/GetOutOrderDetailBy")
    @CrossOrigin
    public InterReturn GetOutOrderDetailBy(@RequestBody OutOrderDetail outOrderDetail, int pageNo, int pageSize) {
//        logger.info("进入 GetOutOrderDetailBy");
//        logger.info("接收到参数：" + outOrderDetail);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<OutOrderDetail> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<OutOrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(outOrderDetail.getOut_order_detail_id() != null && !outOrderDetail.getOut_order_detail_id().equals(0),
                    OutOrderDetail::getOut_order_detail_id, outOrderDetail.getOut_order_detail_id());//出库单明细ID
            wrapper.eq(outOrderDetail.getOut_order_id() != null && !outOrderDetail.getOut_order_id().isEmpty(),
                    OutOrderDetail::getOut_order_id, outOrderDetail.getOut_order_id());//出库单ID
            wrapper.eq(outOrderDetail.getMaterial_code() != null && !outOrderDetail.getMaterial_code().isEmpty(),
                    OutOrderDetail::getMaterial_code, outOrderDetail.getMaterial_code());//物料编号
            wrapper.eq(outOrderDetail.getMaterial_name() != null && !outOrderDetail.getMaterial_name().isEmpty(),
                    OutOrderDetail::getMaterial_name, outOrderDetail.getMaterial_name());//物料名称
            wrapper.orderByDesc(OutOrderDetail::getUpdate_time);
            wrapper.eq(outOrderDetail.getUdf02() != null && !outOrderDetail.getUdf02().isEmpty(),
                    OutOrderDetail::getUdf02, outOrderDetail.getUdf02());//旧物料编号
//需要按时间查询时 放开此行   wrapper.between(OutOrderDetail::getCreate_time, startTime, endTime);//时间在这两个时间中间

            List<OutOrderDetail> inOrderDetails = outOrderDetailService.page(page, wrapper).getRecords();

            if (inOrderDetails == null || inOrderDetails.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到出库明细！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询出库明细成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(inOrderDetails);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出库明细出错：" + e.getCause());
            System.out.println("查询出库明细出错：" + e.getMessage());
            logger.error("查询出库明细出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        //  logger.info("GetOutOrderDetailBy 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "更新出库明细", notes = "根据OrderId更新用户 传OutOrderDetail类")
    @PostMapping("/OutOrderDetail/UpdateOutOrderDetailByOrderId")
    @CrossOrigin
    public InterReturn UpdateOutOrderDetailByOrderId(@RequestBody OutOrderDetail outOrderDetail) {
//        logger.info("进入 UpdateOutOrderDetailByOrderId");
//        logger.info("接收到参数：" + outOrderDetail);
        InterReturn interReturn = new InterReturn();
        try {
            if (outOrderDetail.getOut_order_id() == null || outOrderDetail.getOut_order_id().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或出库表头id为空");
                System.out.println("参数异常或出库表头id为空");
                return interReturn;
            }
            if (outOrderDetail.getStatus() == null) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或出库明细状态为空");
                System.out.println("参数异常或出库明细状态为空");
                return interReturn;
            }
            if (outOrderDetail.getUpdater() == null || outOrderDetail.getUpdater().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或更新人为空");
                System.out.println("参数异常或更新人为空");
                return interReturn;
            }
            //mybatisplus
            LambdaUpdateWrapper<OutOrderDetail> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(OutOrderDetail::getOut_order_id, outOrderDetail.getOut_order_id());
            wrapper.set(outOrderDetail.getStatus() != null, OutOrderDetail::getStatus, outOrderDetail.getStatus());
            wrapper.set(OutOrderDetail::getUpdater, outOrderDetail.getUpdater());
            wrapper.set(OutOrderDetail::getUpdate_time, LocalDateTime.now());
            boolean bool = outOrderDetailService.update(null, wrapper);
            if (bool) {
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
            logger.error("更新出错：" + e.getMessage());
        }
//        logger.info("UpdateOutOrderDetailByOrderId 返回：" + interReturn);
        return interReturn;
    }


    @ApiOperation(value = "批量添加出库明细(仅上游调用)", notes = "传OutOrderDetail类List")
    @PostMapping("/OutOrderDetail/Adds")
    @CrossOrigin
    public InterReturn Adds(@RequestBody List<OutOrderDetail> outOrderDetails) {
        logger.info("进入 Adds");
        logger.info("接收到参数：出库明细列表：" + outOrderDetails);
        InterReturn interReturn = new InterReturn();
        interReturn.setStatus(false);

        try {
            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            OutOrderDetailMapper mapMapper = sqlSession.getMapper(OutOrderDetailMapper.class);//获取对应Mapper

            Date time = Calendar.getInstance().getTime();//创建时间赋值
            for (OutOrderDetail outOrderDetail : outOrderDetails) {
                {
                    //判断批次是否为空
                    if (outOrderDetail.getBatch() == null || outOrderDetail.getBatch().trim().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("批次号有空值");
                        System.out.println("批次号有空值");
                        return interReturn;
                    }
                    //判断入库单号是否为空
                    if (outOrderDetail.getOut_order_id() == null || outOrderDetail.getOut_order_id().trim().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("出库单号有空值");
                        System.out.println("出库单号有空值");
                        return interReturn;
                    }
                    //判断物料编号是否为空
                    if (outOrderDetail.getMaterial_code() == null || outOrderDetail.getMaterial_code().trim().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("物料编号有空值");
                        System.out.println("物料编号有空值");
                        return interReturn;
                    }
                    //判断物料名称是否为空
                    if (outOrderDetail.getMaterial_name() == null || outOrderDetail.getMaterial_name().trim().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("物料名称有空值");
                        System.out.println("物料名称有空值");
                        return interReturn;
                    }
                    //判断计划数量是否小于等于0
                    if (outOrderDetail.getOrder_count() <= 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("计划数量异常");
                        System.out.println("计划数量有异常");
                        return interReturn;
                    }
                    //判断质量等级是否为空
                    if (outOrderDetail.getZhiliangdengji() == null || outOrderDetail.getZhiliangdengji().trim().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("质量等级有空值");
                        System.out.println("质量等级有空值");
                    }
                }


                outOrderDetail.setCreate_time(time);
                outOrderDetail.setCreator("上游下发");
                outOrderDetail.setActual_count(0);
                outOrderDetail.setPick_count(0);
                outOrderDetail.setStatus("执行中");
                mapMapper.insert(outOrderDetail);//准备执行sql
            }

            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("出库明细生成成功！");
            } catch (Exception e) {
                logger.error("批量插入出库明细异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("批量插入出库明细异常，事务回滚:" + e.getCause());
            } finally {
                sqlSession.close();
            }
            return interReturn;

        } catch (Exception ee) {
            interReturn.setMessage("生成出错：" + ee.getCause());
            return interReturn;
        }

    }

    /**
     * @Author:徐浩铖
     * @Description:新增出库单明细
     * @DateTime 2023/8/26 15:18
     * @Params
     * @Return
     */
    @ApiOperation(value = "新增出库单明细")
    @PostMapping("/OutOrderDetail/AddOutOrderDetail")
    @CrossOrigin
    public InterReturn AddOutOrderDetail(@RequestBody OutOrderDetail outOrderDetail) {
//        logger.info("进入 AddOutOrderDetail");
//        logger.info("接收到参数：" + outOrderDetail);
        InterReturn interReturn = new InterReturn();
        try {
            if (outOrderDetail.getMaterial_code() == null || outOrderDetail.getMaterial_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("物料编号异常或物料编号为空！");
                System.out.println("物料编号异常或物料编号为空！");
                return interReturn;
            } else if (outOrderDetail.getOut_order_id() == null || outOrderDetail.getOut_order_id().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("出库单头号为空!");
                System.out.println("出库单头号为空!");
                return interReturn;
            } else if (outOrderDetail.getMaterial_name() == null || outOrderDetail.getMaterial_name().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("物料名称异常或物料名称为空!");
                System.out.println("物料名称异常或物料名称为空!");
                return interReturn;
            } else if (outOrderDetail.getOrder_count() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("出库数量为0！");
                System.out.println("出库数量为0！");
                return interReturn;
//            } else if (outOrderDetail.getBatch() == null || outOrderDetail.getBatch().length() == 0) {
//                interReturn.setStatus(false);
//                interReturn.setMessage("批次为空！");
//                System.out.println("批次为空！");
//                return interReturn;
            }


            //1.判断表头是否能插入
            LambdaQueryWrapper<OutOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OutOrder::getOut_order_id, outOrderDetail.getOut_order_id());
            List<OutOrder> onOrders = outOrderService.list(wrapper);
            if (onOrders.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("出库单不存在：" + outOrderDetail.getOut_order_id());
                System.out.println("出库单不存在：" + outOrderDetail.getOut_order_id());
                return interReturn;
            }

            //只有创建中可以添加
            if (!"创建中".equals(onOrders.get(0).getStatus())) {
                interReturn.setStatus(false);
                interReturn.setMessage("出库单状态异常：" + onOrders.get(0).getStatus());
                System.out.println("出库单状态异常：" + onOrders.get(0).getStatus());
                return interReturn;
            }

            //2.插入明细
            outOrderDetail.setCreate_time(Calendar.getInstance().getTime());
            outOrderDetail.setUpdate_time(Calendar.getInstance().getTime());
            outOrderDetail.setStatus("执行中");
            boolean boolSave = outOrderDetailService.save(outOrderDetail);
            if (boolSave) {
                interReturn.setStatus(true);
                interReturn.setMessage("添加出库单明细成功！");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("添加出库单明细数据条数为0！");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("新增出库单明细出错：" + e.getCause());
            System.out.println("新增出库单明细出错：" + e.getMessage());
            logger.error("新增出库单明细出错：" + e.getMessage());
        }
//        logger.info("AddOutOrderDetail返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:徐浩铖
     * @Description :删除出库明细
     * @DateTime 2023/8/26 15:20
     * @Params
     * @Return
     */
    @ApiOperation(value = "删除出库明细")
    @PostMapping("/OutOrderDetail/DelOutOrderDetail")
    @CrossOrigin
    public InterReturn DelOutOrderDetail(@RequestBody OutOrderDetail outOrderDetail) {
//        logger.info("进入 DelOutOrderDetail");
//        logger.info("接收到参数：" + outOrderDetail);
        InterReturn interReturn = new InterReturn();
        try {
            if (outOrderDetail.getOut_order_detail_id() == null) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或出库明细单号为空");
                System.out.println("参数异常或出库明细单号为空");
                return interReturn;
            } else {
                //mybatisplus
                boolean bool = outOrderDetailService.removeById(outOrderDetail);
                if (bool) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("删除出库明细成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("删除数据条数为0！");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("删除出库明细出错：" + e.getCause());
            System.out.println("删除出库明细出错：" + e.getMessage());
            logger.error("删除出库明细出错：" + e.getMessage());
        }
//        logger.info("DelOutOrderDetail返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:徐浩铖
     * @Description :编辑出库明细接口
     * @DateTime 2023/8/26 15:26
     * @Params
     * @Return
     */
    @ApiOperation(value = "编辑出库明细")
    @PostMapping("/OutOrderDetail/EditOutOrderDetail")
    @CrossOrigin
    public InterReturn EditOutOrderDetail(@RequestBody OutOrderDetail outOrderDetail) {
//        logger.info("进入EditOutOrderDetail");
//        logger.info("接收到参数：" + outOrderDetail);
        InterReturn interReturn = new InterReturn();
        try {
            LambdaUpdateWrapper<OutOrderDetail> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(OutOrderDetail::getOut_order_detail_id, outOrderDetail.getOut_order_detail_id());
            wrapper.set(outOrderDetail.getMaterial_code() != null && !outOrderDetail.getMaterial_code().isEmpty(),
                    OutOrderDetail::getMaterial_code, outOrderDetail.getMaterial_code());
            wrapper.set(outOrderDetail.getMaterial_name() != null && !outOrderDetail.getMaterial_name().isEmpty(),
                    OutOrderDetail::getMaterial_name, outOrderDetail.getMaterial_name());
            wrapper.set(outOrderDetail.getBatch() != null && !outOrderDetail.getBatch().isEmpty(), OutOrderDetail::getBatch, outOrderDetail.getBatch());
            wrapper.set(outOrderDetail.getOrder_count() != 0, OutOrderDetail::getOrder_count, outOrderDetail.getOrder_count());

            boolean bool = outOrderDetailService.update(null, wrapper);
            if (bool) {
                interReturn.setStatus(true);
                interReturn.setMessage("编辑出库明细成功！");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("无更新");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("编辑出库明细出错：" + e.getCause());
            System.out.println("编辑出库明细出错：" + e.getMessage());
            logger.error("编辑出库明细出错：" + e.getMessage());
        }
//        logger.info("EditOutOrderDetail 返回：" + interReturn);
        return interReturn;
    }


    @ApiOperation(value = "查询返回Excel类", notes = "返回Excel类")
    @PostMapping("/OutOrderDetail/GetOutOrderExcel")
    @CrossOrigin
    public InterReturn GetOutOrderExcel(@RequestBody List<OutOrder> outOrders) {
//        logger.info("进入GetOutOrderExcel");
//        logger.info("接收到参数：" + outOrders);
        InterReturn interReturn = new InterReturn();
        List<OutOrderExcel> outOrderExcels = new ArrayList<>();//用于导出Excel
        try {
            if (outOrders == null || outOrders.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("传入表头为空");
                logger.info("GetOutOrderExcel返回：" + interReturn);
                return interReturn;
            }

            //去重
            List<OutOrder> newOutOrderList = outOrders.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(OutOrder::getOut_order_id))),
                            ArrayList::new));

            for (OutOrder item : newOutOrderList) {
                if (item.getOut_order_id() == null || item.getOut_order_id().isEmpty()) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("传入单据号有空");
                    logger.info("GetOutOrderExcel返回：" + interReturn);
                    return interReturn;
                }
            }

            for (OutOrder outOrder : newOutOrderList) {
                LambdaQueryWrapper<OutOrderDetail> wrapperOutOrderDetail = new LambdaQueryWrapper<>();
                wrapperOutOrderDetail.eq(OutOrderDetail::getOut_order_id, outOrder.getOut_order_id());
                List<OutOrderDetail> outOrderDetails = outOrderDetailService.list(wrapperOutOrderDetail);
                if (outOrderDetails.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("“" + outOrder.getOut_order_id() + "”单据无明细");
                    logger.info("GetOutOrderExcel返回：" + interReturn);
                    return interReturn;
                }

                //拼Excel类
                for (OutOrderDetail outOrderDetail : outOrderDetails) {
                    OutOrderExcel outOrderExcel = new OutOrderExcel();
                    outOrderExcel.setOut_order_id(outOrder.getOut_order_id());//出库单号
                    outOrderExcel.setOrder_type(outOrder.getOrder_type());//订单类型
                    outOrderExcel.setCar_no(outOrder.getCar_no());//车牌号
                    outOrderExcel.setOrg_order(outOrder.getOrg_order());//原始单号
                    outOrderExcel.setWarehouse_name(outOrder.getWarehouse_name());//仓库名称
                    outOrderExcel.setHesuankemu(outOrder.getHesuankemu());//核算科目
                    outOrderExcel.setShouwudanwei(outOrder.getShouwudanwei());//收物单位
                    outOrderExcel.setFenduiqinglingbianhao(outOrder.getFenduiqinglingbianhao());//分队请领编号
                    outOrderExcel.setDiaobodanhao(outOrder.getDiaobodanhao());//调拨单号
                    //明细
                    //outOrderExcel.setOut_order_detail_id();//明细ID
                    outOrderExcel.setMaterial_code(outOrderDetail.getMaterial_code());//物料编号
                    outOrderExcel.setMaterial_name(outOrderDetail.getMaterial_name());//物料名称
                    outOrderExcel.setBatch(outOrderDetail.getBatch());//批次
                    outOrderExcel.setZhiliangdengji(outOrderDetail.getZhiliangdengji());//质量等级
                    outOrderExcel.setOrder_count(outOrderDetail.getOrder_count());//订单数量
                    outOrderExcel.setActual_count(outOrderDetail.getActual_count());//已出库数量
                    outOrderExcel.setPick_count(outOrderDetail.getPick_count());//已配盘数量
                    outOrderExcel.setCreator(outOrderDetail.getCreator());//创建人
                    outOrderExcel.setCreate_time(outOrderDetail.getCreate_time());//创建时间
                    outOrderExcel.setUpdater(outOrderDetail.getUpdater());//更新人
                    outOrderExcel.setUpdate_time(outOrderDetail.getUpdate_time());//更新时间
                    outOrderExcels.add(outOrderExcel);
                }
            }
            interReturn.setStatus(true);
            interReturn.setMessage("查询成功");
            interReturn.setResult(outOrderExcels);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询返回Excel类出错：" + e.getCause());
            System.out.println("查询返回Excel类出错：" + e.getMessage());
            logger.error("查询返回Excel类出错：" + e.getMessage());
        }
//        logger.info("GetOutOrderExcel返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 出库单导入 参数Excel类
     * @DateTime 2024/1/27 14:15
     * @Params
     * @Return
     */
    @ApiOperation(value = "导入Excel类", notes = "传Excel类 返回导入结果")
    @PostMapping("/OutOrderDetail/ImportOutOrderExcel")
    @CrossOrigin
    public InterReturn ImportOutOrderExcel(@RequestBody List<OutOrderExcel> outOrderExcels) {
//        logger.info("进入ImportOutOrderExcel");
//        logger.info("接收到参数：" + outOrderExcels);
        InterReturn interReturn = new InterReturn();
        try {
            //判空
            if (outOrderExcels == null || outOrderExcels.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("传入Excel信息为空");
                logger.info("ImportOutOrderExcel返回：" + interReturn);
                return interReturn;
            }

            //判单据号空
            for (OutOrderExcel outOrderExcel : outOrderExcels) {
                if (outOrderExcel.getOut_order_id() == null || outOrderExcel.getOut_order_id().isEmpty()) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("传入单据号有空");
                    logger.info("ImportOutOrderExcel返回：" + interReturn);
                    return interReturn;
                }
            }
            //去重
            List<OutOrderExcel> newOutOrderExcelList = outOrderExcels.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(OutOrderExcel::getOut_order_id))),
                            ArrayList::new));

            List<OutOrder> outOrders = new ArrayList<>();//用于insert
            for (OutOrderExcel orderExcel : newOutOrderExcelList) {
                OutOrder outOrder = new OutOrder();
                outOrder.setOut_order_id(orderExcel.getOut_order_id());//出库单号
                outOrder.setOrder_type(orderExcel.getOrder_type());//订单类型
                outOrder.setCar_no(orderExcel.getCar_no());//车牌号
                outOrder.setOrg_order(orderExcel.getOrg_order());//原始单号
                outOrder.setWarehouse_name(orderExcel.getWarehouse_name());//仓库名称
                outOrder.setHesuankemu(orderExcel.getHesuankemu());//核算科目
                outOrder.setShouwudanwei(orderExcel.getShouwudanwei());//收物单位
                outOrder.setFenduiqinglingbianhao(orderExcel.getFenduiqinglingbianhao());//分队请领编号
                outOrder.setDiaobodanhao(orderExcel.getDiaobodanhao());//调拨单号

                outOrder.setStatus("创建中");//单据状态
                outOrders.add(outOrder);
            }


            List<OutOrderDetail> outOrderDetails = new ArrayList<>();//用于insert
            for (OutOrderExcel outOrderExcel : outOrderExcels) {
                OutOrderDetail outOrderDetail = new OutOrderDetail();
                outOrderDetail.setOut_order_id(outOrderExcel.getOut_order_id());//出库单号
                outOrderDetail.setMaterial_code(outOrderExcel.getMaterial_code());//物料编号
                outOrderDetail.setMaterial_name(outOrderExcel.getMaterial_name());//物料名称
                outOrderDetail.setBatch(outOrderExcel.getBatch());//批次
                outOrderDetail.setZhiliangdengji(outOrderExcel.getZhiliangdengji());//质量等级
                outOrderDetail.setOrder_count(outOrderExcel.getOrder_count());//订单数量
                outOrderDetail.setActual_count(outOrderExcel.getActual_count());//已出库数量
                outOrderDetail.setPick_count(outOrderExcel.getPick_count());//已配盘数量
//                outOrderDetail.setCreator(outOrderExcel.getCreator());//创建人
//                outOrderDetail.setCreate_time(outOrderExcel.getCreate_time());//创建时间
//                outOrderDetail.setUpdater(outOrderExcel.getUpdater());//更新人
//                outOrderDetail.setUpdate_time(outOrderExcel.getUpdate_time());//更新时间
                outOrderDetail.setStatus("执行中");//明细状态
                outOrderDetails.add(outOrderDetail);
            }

            //批量添加两个表 事务

            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            OutOrderMapper outOrderMapper = sqlSession.getMapper(OutOrderMapper.class);//获取对应Mapper
            OutOrderDetailMapper outOrderDetailMapper = sqlSession.getMapper(OutOrderDetailMapper.class);//获取对应Mapper

            Date time = Calendar.getInstance().getTime();//创建时间赋值
            for (OutOrder outOrder : outOrders) {
                outOrder.setCreate_time(time);
                outOrder.setUpdate_time(time);
//                outOrder.setCreator( );
//                outOrder.setUpdater( );
                outOrderMapper.insert(outOrder);//准备执行sql
            }
            for (OutOrderDetail outOrderDetail : outOrderDetails) {
                outOrderDetail.setCreate_time(time);
                outOrderDetail.setUpdate_time(time);
//                outOrderDetail.setCreator( );
//                outOrderDetail.setUpdater( );
                outOrderDetailMapper.insert(outOrderDetail);//准备执行sql
            }

            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("生成成功！");
            } catch (Exception e) {
                logger.error("批量插入出库单表头&明细异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("批量插入出库单表头&明细异常，事务回滚:" + e.getCause());
            } finally {
                sqlSession.close();
            }


        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("导入Excel类出错：" + e.getCause());
            System.out.println("导入Excel类出错：" + e.getMessage());
            logger.error("导入Excel类出错：" + e.getMessage());
        }


//        logger.info("ImportOutOrderExcel返回：" + interReturn);
        return interReturn;
    }
}

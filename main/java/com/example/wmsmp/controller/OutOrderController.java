package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.*;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
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

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Api(value = "123", tags = "出库表头")
@RestController
@CrossOrigin
public class OutOrderController {
    @Autowired
    OutOrderService outOrderService;
    @Autowired
    OutOrderDetailService outOrderDetailService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    private static Logger logger = Logger.getLogger(OutOrderController.class);

    @ApiOperation(value = "模糊查询出库表头", notes = "OutOrder类（出库单号，订单类型，车牌号，原始单号，单据状态） pageNo pageSize")
    @PostMapping("/OutOrder/GetOutOrderByFuzzy")
    @CrossOrigin
    public InterReturn GetOutOrderByFuzzy(@RequestBody OutOrder outOrder, int pageNo, int pageSize) {
//        logger.info("进入 GetOutOrderByFuzzy");
//        logger.info("接收到参数：" + outOrder);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<OutOrder> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<OutOrder> wrapper = new LambdaQueryWrapper<>();

            wrapper.eq(outOrder.getWarehouse_name() != null && !outOrder.getWarehouse_name().isEmpty(),
                    OutOrder::getWarehouse_name, outOrder.getWarehouse_name());//库名
            // 模糊查询
            wrapper.like(outOrder.getOut_order_id() != null && !outOrder.getOut_order_id().isEmpty(),
                    OutOrder::getOut_order_id, outOrder.getOut_order_id());

            wrapper.eq(outOrder.getOrder_type() != null && !outOrder.getOrder_type().isEmpty(),
                    OutOrder::getOrder_type, outOrder.getOrder_type());//订单类型
            wrapper.eq(outOrder.getCar_no() != null && !outOrder.getCar_no().isEmpty(),
                    OutOrder::getCar_no, outOrder.getCar_no());//车牌号
            wrapper.eq(outOrder.getOrg_order() != null && !outOrder.getOrg_order().isEmpty(),
                    OutOrder::getOrg_order, outOrder.getOrg_order());//原始单号
            wrapper.eq(outOrder.getStatus() != null && !outOrder.getStatus().isEmpty(),
                    OutOrder::getStatus, outOrder.getStatus());//单据状态
            wrapper.orderByDesc(OutOrder::getUpdate_time);
//需要按时间查询时 放开此行              wrapper.between(OutOrder::getCreate_time, startTime, endTime);//时间在这两个时间中间

            List<OutOrder> outOrders = outOrderService.page(page, wrapper).getRecords();

            if (outOrders == null || outOrders.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找出库单表头记录！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询出库单表头成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(outOrders);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出库单表头出错：" + e.getCause());
            System.out.println("查询出库单表头出错：" + e.getMessage());
            logger.error("查询出库单表头出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        //  logger.info("GetOutOrderByFuzzy 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "更新出库表头", notes = "根据OrderId更新用户 传OutOrder类")
    @PostMapping("/OutOrder/UpdateOutOrderByOrderId")
    @CrossOrigin
    public InterReturn UpdateOutOrderByOrderId(@RequestBody OutOrder outOrder) {
//        logger.info("进入 UpdateOutOrderByOrderId");
//        logger.info("接收到参数：" + outOrder);
        InterReturn interReturn = new InterReturn();
        try {
            if (outOrder.getOut_order_id() == null || outOrder.getOut_order_id().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或出库表头id为空");
                System.out.println("参数异常或出库表头id为空");
                return interReturn;
            }
            if (outOrder.getStatus() == null) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或出库表头状态为空");
                System.out.println("参数异常或出库表头状态为空");
                return interReturn;
            }
            if (outOrder.getUpdater() == null || outOrder.getUpdater().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或更新人为空");
                System.out.println("参数异常或更新人为空");
                return interReturn;
            }
            //mybatisplus
            LambdaUpdateWrapper<OutOrder> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(OutOrder::getOut_order_id, outOrder.getOut_order_id());
            wrapper.set(outOrder.getStatus() != null, OutOrder::getStatus, outOrder.getStatus());
            wrapper.set(OutOrder::getUpdater, outOrder.getUpdater());
            wrapper.set(OutOrder::getUpdate_time, LocalDateTime.now());
            boolean bool = outOrderService.update(null, wrapper);
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
//        logger.info("UpdateOutOrderByOrderId 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:徐浩铖
     * @Description :编辑出库单头接口
     * @DateTime 2023/8/26 14:43
     * @Params
     * @Return
     */
    @ApiOperation(value = "编辑出库单头接口")
    @PostMapping("/OutOrder/EditOutOrder")
    @CrossOrigin
    public InterReturn EditOutOrder(@RequestBody OutOrder outOrder) {
//        logger.info("进入EditOutOrder");
//        logger.info("接收到参数：" + outOrder);
        InterReturn interReturn = new InterReturn();
        try {
            if (outOrder.getOut_order_id() == null) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或出库单号为空！");
                System.out.println("参数异常或出库单号为空！");
                return interReturn;
            } else {
                LambdaUpdateWrapper<OutOrder> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(OutOrder::getOut_order_id, outOrder.getOut_order_id());
                wrapper.set(outOrder.getOrder_type() != null && !outOrder.getOrder_type().isEmpty(), OutOrder::getOrder_type, outOrder.getOrder_type());
                wrapper.set(outOrder.getStatus() != null, OutOrder::getStatus, outOrder.getStatus());
                wrapper.set(outOrder.getCar_no() != null && !outOrder.getCar_no().isEmpty(), OutOrder::getCar_no, outOrder.getCar_no());
                wrapper.set(outOrder.getOrg_order() != null && !outOrder.getOrg_order().isEmpty(), OutOrder::getOrg_order, outOrder.getOrg_order());
                wrapper.set(OutOrder::getUpdater, outOrder.getUpdater());
                wrapper.set(OutOrder::getUpdate_time, LocalDateTime.now());
                boolean bool = outOrderService.update(null, wrapper);
                if (bool) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("更新成功");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("无更新");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("更新出错：" + e.getCause());
            System.out.println("更新出错：" + e.getMessage());
            logger.error("更新出错：" + e.getMessage());
        }
//        logger.info("EditOutOrder返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:徐浩铖
     * @Description :新增出库单表头
     * @DateTime 2023/8/26 14:33
     * @Params
     * @Return
     */
    @ApiOperation(value = "新增出库单表头")
    @PostMapping("/OutOrder/AddOutOrder")
    @CrossOrigin
    public InterReturn AddOutOrder(@RequestBody OutOrder outOrder) {
//        logger.info("进入 AddOutOrder");
//        logger.info("接收到参数：" + outOrder);
        InterReturn interReturn = new InterReturn();
        try {
            if (outOrder.getOrder_type() == null || outOrder.getOrder_type().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("单据类别异常或单据名为空");
                System.out.println("单据类别异常或单据名为空");
                return interReturn;
//            } else if (outOrder.getCar_no() == null || outOrder.getCar_no().length() == 0) {
//                interReturn.setStatus(false);
//                interReturn.setMessage("车牌号异常或车牌号为空");
//                System.out.println("车牌号异常或车牌号为空");
//                return interReturn;
            } else if (outOrder.getOrg_order() == null || outOrder.getOrg_order().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("来源单号为空");
                System.out.println("来源单号异常或来源单号为空");
                return interReturn;
            }

            InterReturn interReturnOutOrderNo = GenerateOutOrderNo();//生成单据号
            if (interReturnOutOrderNo.isStatus()) {
                interReturn.setMessage("出库号生成成功");
            } else {
                interReturn.setMessage("出库号生成出错：" + interReturnOutOrderNo.getMessage());
                return interReturn;
            }

            outOrder.setOut_order_id(interReturnOutOrderNo.getResult().toString());
            outOrder.setStatus("创建中");
            outOrder.setCreate_time(Calendar.getInstance().getTime());
            outOrder.setUpdate_time(Calendar.getInstance().getTime());
            boolean boolSave = outOrderService.save(outOrder);
            if (boolSave) {
                interReturn.setStatus(true);
                interReturn.setMessage("添加出库单表头成功");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("添加出库单头数据条数为0");
            }

        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("新增出库单头出错：" + e.getCause());
            System.out.println("新增出库单头出错：" + e.getMessage());
            logger.error("新增出库单头出错：" + e.getMessage());
        }
//        logger.info("AddOutOrder返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "批量添加出库表头(仅上游调用)", notes = "传OutOrder类List")
    @PostMapping("/OutOrder/Adds")
    @CrossOrigin
    public InterReturn Adds(@RequestBody List<OutOrder> outOrders) {
        logger.info("进入 Adds");
        logger.info("接收到参数：出库表头列表：" + outOrders);
        InterReturn interReturn = new InterReturn();
        interReturn.setStatus(false);
        try {
            Set<String> warehouseList = new HashSet<>();
            warehouseList.add("P库");
            warehouseList.add("B库");
            warehouseList.add("H库");
            warehouseList.add("D库");
            warehouseList.add("X库");


            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            OutOrderMapper mapMapper = sqlSession.getMapper(OutOrderMapper.class);//获取对应Mapper

            Date time = Calendar.getInstance().getTime();//创建时间赋值
            for (OutOrder outOrder : outOrders) {
                //判断单据号是否为空
                if (outOrder.getOut_order_id() == null || outOrder.getOut_order_id().trim().length() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("单据号有空值");
                    System.out.println("单据号有空值");
                    return interReturn;
                }
                //判断库名是否合法
                if (!warehouseList.contains(outOrder.getWarehouse_name())) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("未识别的库名：" + outOrder.getWarehouse_name());
                    System.out.println("未识别的库名：" + outOrder.getWarehouse_name());
                    return interReturn;
                }
                outOrder.setCreate_time(time);
                outOrder.setStatus("已审核");
                outOrder.setCreator("上游下发");
                mapMapper.insert(outOrder);//准备执行sql
            }

            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("出库表头生成成功！");
            } catch (Exception e) {
                logger.error("批量插入出库表头异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("批量插入出库表头异常，事务回滚:" + e.getCause());
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
     * @Description :提交/申鹤单据，查找入库单头是否有对应的明细，有的话可以，无的话给出提示
     * @DateTime 2023/8/26 13:42
     * @Params
     * @Return
     */
    @ApiOperation(value = "提交、审核出库单表头接口", notes = "单据类的状态传待审核、已审核")
    @PostMapping("/OutOrder/SubmitOutOrder")
    @CrossOrigin
    public InterReturn SubmitOutOrder(@RequestBody OutOrder outOrder) {
//        logger.info("进入SubmitInOrder");
//        logger.info("接收到参数：" + outOrder);
        InterReturn interReturn = new InterReturn();
        try {
            String orderState = outOrder.getStatus();
            //单据状态 不为待审核或已审核
            if (!orderState.equals("待审核") && !orderState.equals("已审核")) {
                interReturn.setStatus(false);
                interReturn.setMessage("单据新状态异常：" + orderState);
                return interReturn;
            }


            if (outOrder.getOut_order_id() == null) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或出库单号为空！");
                System.out.println("参数异常或出库单号为空！");
                return interReturn;
            } else {
                LambdaUpdateWrapper<OutOrderDetail> detailWrapper = new LambdaUpdateWrapper<>();
                detailWrapper.eq(OutOrderDetail::getOut_order_id, outOrder.getOut_order_id());
                List<OutOrderDetail> orderDetails = outOrderDetailService.list(detailWrapper);
                if (orderDetails.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("无出库单头明细，不能提交出库单头！");
                } else {
                    LambdaUpdateWrapper<OutOrder> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(OutOrder::getOut_order_id, outOrder.getOut_order_id());
                    wrapper.set(OutOrder::getStatus, orderState);
                    wrapper.set(OutOrder::getUpdate_time, LocalDateTime.now());
                    boolean bool = outOrderService.update(null, wrapper);
                    if (bool) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("出库单表头提交成功！");
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("出库单表头提交失败！");
                    }
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("出库单表头提交出错：" + e.getCause());
            System.out.println("出库单表头提交出错：" + e.getMessage());
            logger.error("出库单表头提交出错：" + e.getMessage());
        }
//        logger.info("SubmitOutOrder 返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:徐浩铖
     * @Description 删除出库单头接口，删除出库单头时对应的明细也需要删除
     * @DateTime 2023/8/26 14:05
     * @Params
     * @Return
     */
    @ApiOperation(value = "删除出库单表头 及对应明细", notes = "提交后的单据不可删除 仅编辑中可删")
    @PostMapping("/OutOrder/DelOutOrder")
    @CrossOrigin
    public InterReturn DelOutOrder(@RequestBody OutOrder outOrder) {
//        logger.info("进入 DelOutOrder");
//        logger.info("接收到参数：" + outOrder);
        InterReturn interReturn = new InterReturn();
        try {
            if (outOrder.getOut_order_id() == null || outOrder.getOut_order_id().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或出库单号为空");
                System.out.println("参数异常或出库单号为空");
                return interReturn;
            } else {
                //mybatisplus
                boolean bool = outOrderService.removeById(outOrder);
                if (bool) {
                    LambdaQueryWrapper<OutOrderDetail> wrapperOutDetail = new LambdaQueryWrapper<>();
                    wrapperOutDetail.eq(OutOrderDetail::getOut_order_id, outOrder.getOut_order_id());
                    boolean boolDetail = outOrderDetailService.remove(wrapperOutDetail);
                    interReturn.setStatus(true);
                    interReturn.setMessage("删除出库单表头成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("删除表头为0条");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("删除出库单表头出错：" + e.getCause());
            System.out.println("删除出库单表头出错：" + e.getMessage());
            logger.error("删除出库单头出错：" + e.getMessage());
        }
//        logger.info("DelOutOrder 返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:lcy
     * @Description :生成出库单号
     * @DateTime 2023/9/28
     * @Params
     * @Return
     */

    @ApiOperation(value = "生成出库单号")
    @PostMapping("/OutOrder/GenerateOutOrderNo")
    @CrossOrigin
    public InterReturn GenerateOutOrderNo() {
//        logger.info("进入 GenerateOutOrderNo");
        InterReturn interReturn = new InterReturn();
        String outOrderNo;
        try {
            LambdaQueryWrapper<OutOrder> outOrderWrapper = new LambdaQueryWrapper<>();
            List<OutOrder> outOrders = outOrderService.list(outOrderWrapper);
            String outOrderMax = "";
            //取出通过stream里的方法，取出outorders里面最大的订单号
            if (outOrders.size() > 0)
                outOrderMax = outOrders.stream().max(Comparator.comparing(OutOrder::getOut_order_id)).get().getOut_order_id();
            //获取当前订单日期
            LocalDate currentData = LocalDate.now();
            String currentOutOrder = String.valueOf(currentData);
            currentOutOrder = currentOutOrder.replace("-", "");
            if (outOrderMax.equals((""))) outOrderMax = "HO" + currentOutOrder + "0000";
            //取出入库单整数部分，用int长度不够，所以的用BigInteger
            BigInteger outOrderNoTemp = BigInteger.valueOf(Long.parseLong(outOrderMax.substring(2)));

            if (outOrderMax.substring(2, outOrderMax.length() - 4).equals(currentOutOrder)) {
                //BigInteger型加1
                outOrderNoTemp = outOrderNoTemp.add(BigInteger.valueOf(1));
                //组成字符型入库单号，返回给前端
                outOrderNo = "HO" + outOrderNoTemp;

            } else {
                outOrderNo = "HO" + currentOutOrder + "0001";
            }
            interReturn.setResult(outOrderNo);
            interReturn.setStatus(true);

        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错：" + e.getCause());
            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
        }
        interReturn.setMessage("生成出库单号！");
//        logger.info("GenerateOutOrderNo 返回：" + interReturn);
        return interReturn;
    }
}
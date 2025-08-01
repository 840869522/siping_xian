package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.InOrder;
import com.example.wmsmp.entity.InOrderDetail;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.mapper.InOrderMapper;
import com.example.wmsmp.service.InOrderDetailService;
import com.example.wmsmp.service.InOrderService;
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

@Api(value = "123", tags = "入库表头")
@RestController
@CrossOrigin
public class InOrderController {
    @Autowired
    InOrderService inOrderService;

    @Autowired
    OutOrderService outOrderService;

    @Autowired
    OutOrderDetailService outOrderDetailService;
    @Autowired
    InOrderDetailService inOrderDetailService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    private static Logger logger = Logger.getLogger(InOrderController.class);


    /**
     * @Author:lcy
     * @Description 带上时间查询的条件
     * @DateTime 2023/7/1 10:34
     * @Params
     * @Return
     */
    @ApiOperation(value = "模糊查询入库单表头", notes = "InOrder类(库名，入库单号，订单类型，车牌号，原始单号，单据状态) pageNo pageSize startTime endTime")
    @PostMapping("/InOrder/GetInOrderByFuzzy")
    @CrossOrigin
    public InterReturn GetInOrderByFuzzy(@RequestBody InOrder inOrder, int pageNo, int pageSize, String startTime, String endTime) {
//        logger.info("进入 GetInOrderByFuzzy");
//        logger.info("接收到参数：" + inOrder);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<InOrder> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<InOrder> wrapper = new LambdaQueryWrapper<>();

            wrapper.eq(inOrder.getWarehouse_name() != null && !inOrder.getWarehouse_name().isEmpty(),
                    InOrder::getWarehouse_name, inOrder.getWarehouse_name());//库名
            // 模糊查询
            wrapper.like(inOrder.getIn_order_id() != null && !inOrder.getIn_order_id().isEmpty(), InOrder::getIn_order_id, inOrder.getIn_order_id());

            wrapper.eq(inOrder.getOrder_type() != null && !inOrder.getOrder_type().isEmpty(),
                    InOrder::getOrder_type, inOrder.getOrder_type());//订单类型
            wrapper.eq(inOrder.getCar_no() != null && !inOrder.getCar_no().isEmpty(),
                    InOrder::getCar_no, inOrder.getCar_no());//车牌号
            wrapper.eq(inOrder.getOrg_order() != null && !inOrder.getOrg_order().isEmpty(),
                    InOrder::getOrg_order, inOrder.getOrg_order());//原始单号
            wrapper.eq(inOrder.getStatus() != null && !inOrder.getStatus().isEmpty(),
                    InOrder::getStatus, inOrder.getStatus());//单据状态
            wrapper.between(InOrder::getCreate_time, startTime, endTime);//时间在这两个时间中间
            wrapper.orderByDesc(InOrder::getUpdate_time);

            List<InOrder> inOrders = inOrderService.page(page, wrapper).getRecords();

            if (inOrders == null || inOrders.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找入库单表头记录！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询入库单表头成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(inOrders);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询入库单表头出错：" + e.getCause());
            System.out.println("查询入库单表头出错：" + e.getMessage());
            logger.error("查询入库单表头出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        //logger.info("GetInOrderByFuzzy 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "更新入库表头", notes = "根据OrderId更新用户传InOrder类")
    @PostMapping("/InOrder/UpdateInOrderByOrderId")
    @CrossOrigin
    public InterReturn UpdateInOrderByOrderId(@RequestBody InOrder inOrder) {
        logger.info("进入 UpdateInOrderByOrderId");
        logger.info("接收到参数：" + inOrder);
        InterReturn interReturn = new InterReturn();
        try {
            if (inOrder.getIn_order_id() == null || inOrder.getIn_order_id().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或入库表头id为空");
                System.out.println("参数异常或入库表头id为空");
                return interReturn;
            }
            if (inOrder.getStatus() == null) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或入库表头状态为空");
                System.out.println("参数异常或入库表头状态为空");
                return interReturn;
            }
            if (inOrder.getUpdater() == null || inOrder.getUpdater().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或更新人为空");
                System.out.println("参数异常或更新人为空");
                return interReturn;
            }
            //mybatisplus
            LambdaUpdateWrapper<InOrder> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(InOrder::getIn_order_id, inOrder.getIn_order_id());
            wrapper.set(inOrder.getStatus() != null, InOrder::getStatus, inOrder.getStatus());
            wrapper.set(InOrder::getUpdater, inOrder.getUpdater());
            wrapper.set(InOrder::getUpdate_time, LocalDateTime.now());
            boolean bool = inOrderService.update(null, wrapper);
            if (bool) {
                interReturn.setStatus(true);
                interReturn.setMessage("更新成功");
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
        logger.info("UpdateInOrderByOrderId 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "批量添加入库单表头(仅上游调用)", notes = "传InOrder类List")
    @PostMapping("/InOrder/Adds")
    @CrossOrigin
    public InterReturn Adds(@RequestBody List<InOrder> inOrders) {
        logger.info("进入 Adds");
        logger.info("接收到参数：入库单表头列表：" + inOrders);
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
            InOrderMapper mapMapper = sqlSession.getMapper(InOrderMapper.class);//获取对应Mapper

            Date time = Calendar.getInstance().getTime();//创建时间赋值
            for (InOrder inOrder : inOrders) {
                //判断单据号是否为空
                if (inOrder.getIn_order_id() == null || inOrder.getIn_order_id().trim().length() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("单据号有空值");
                    System.out.println("单据号有空值");
                    return interReturn;
                }
                //判断库名是否合法
                if (!warehouseList.contains(inOrder.getWarehouse_name())) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("未识别的库名：" + inOrder.getWarehouse_name());
                    System.out.println("未识别的库名：" + inOrder.getWarehouse_name());
                    return interReturn;
                }
                inOrder.setCreate_time(time);
                inOrder.setStatus("已审核");
                inOrder.setCreator("上游下发");
                mapMapper.insert(inOrder);//准备执行sql
            }
            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("入库表头生成成功！");
            } catch (Exception e) {
                logger.error("批量插入入库单表头异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("批量插入入库单表头异常，事务回滚:" + e.getCause());
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
     * @Author:lcy
     * @Description :提交/申鹤单据，查找入库单头是否有对应的明细，有的话可以，无的话给出提示
     * @DateTime 2023/7/1 13:55
     * @Params
     * @Return
     */
    @ApiOperation(value = "提交、审核入库单表头接口", notes = "单据类的状态传待审核、已审核")
    @PostMapping("/InOrder/SubmitInOrder")
    @CrossOrigin
    public InterReturn SubmitInOrder(@RequestBody InOrder inOrder) {
//        logger.info("进入SubmitInOrder");
//        logger.info("接收到参数：" + inOrder);
        InterReturn interReturn = new InterReturn();
        try {
            String orderState = inOrder.getStatus();
            //单据状态 不为待审核或已审核
            if (!"待审核".equals(orderState) && !"已审核".equals(orderState)) {
                interReturn.setStatus(false);
                interReturn.setMessage("单据新状态异常：" + orderState);
                return interReturn;
            }


            if (inOrder.getIn_order_id() == null) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或入库单号为空！");
                System.out.println("参数异常或入库单号为空！");
                return interReturn;
            } else {
                //同时通过入库表头的单号，查找到是否有明细，无明细不能删除
                LambdaQueryWrapper<InOrderDetail> detailWrapper = new LambdaQueryWrapper<>();
                detailWrapper.eq(InOrderDetail::getIn_order_id, inOrder.getIn_order_id());
                List<InOrderDetail> orderDetails = inOrderDetailService.list(detailWrapper);
                if (orderDetails.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("无入库单头明细，不能提交入库单头！");
                } else {
                    LambdaUpdateWrapper<InOrder> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(InOrder::getIn_order_id, inOrder.getIn_order_id());
                    wrapper.set(InOrder::getStatus, orderState);
                    wrapper.set(InOrder::getUpdate_time, LocalDateTime.now());
                    boolean bool = inOrderService.update(null, wrapper);
                    if (bool) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("入库单表头提交成功！");
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("入库单表头提交失败！");
                    }
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("入库单表头提交出错：" + e.getCause());
            System.out.println("入库单表头提交出错：" + e.getMessage());
            logger.error("入库单表头提交出错：" + e.getMessage());
            return interReturn;
        }
//        logger.info("SubmitInOrder 返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:lcy
     * @Description 删除入库单头接口，删除入库单头时对应的明细也需要删除
     * @DateTime 2023/7/1 11:47
     * @Params
     * @Return
     */
    @ApiOperation(value = "删除入库单表头 及对应明细", notes = "提交后的单据不可删除 仅编辑中可删")
    @PostMapping("/InOrder/DelInOrder")
    @CrossOrigin
    public InterReturn DelInOrder(@RequestBody InOrder inOrder) {
//        logger.info("进入 DelInOrder");
//        logger.info("接收到参数：" + inOrder);
        InterReturn interReturn = new InterReturn();
        try {
            if (inOrder.getIn_order_id() == null || inOrder.getIn_order_id().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或入库单号为空");
                System.out.println("参数异常或入库单号为空");
                return interReturn;
            } else {
                //mybatisplus
                boolean bool = inOrderService.removeById(inOrder);
                if (bool) {
                    LambdaQueryWrapper<InOrderDetail> wrapperIndetail = new LambdaQueryWrapper<>();
                    wrapperIndetail.eq(InOrderDetail::getIn_order_id, inOrder.getIn_order_id());
                    boolean boolDetail = inOrderDetailService.remove(wrapperIndetail);
                    interReturn.setStatus(true);
                    interReturn.setMessage("入库单表头删除成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("删除表头为0条！");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("删除入库单表头出错：" + e.getCause());
            System.out.println("删除入库单表头出错：" + e.getMessage());
            logger.error("删除入库单头出错：" + e.getMessage());
            return interReturn;
        }
//        logger.info("DelInOrder 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:lcy
     * @Description :自动生成入库单号
     * @DateTime 2023/7/1 12:18
     * @Params
     * @Return
     */
    //  @ApiOperation(value = "生成订单号")
    // @PostMapping("/InOrder/GenerateInOrderNo")
    //  @CrossOrigin
    public InterReturn GenerateInOrderNo() {
//        logger.info("进入 GenerateInOrderNo");
        InterReturn interReturn = new InterReturn();
        String inOrderNo;
        try {
            LambdaQueryWrapper<InOrder> inOrderWrapper = new LambdaQueryWrapper<>();
            List<InOrder> inOrders = inOrderService.list(inOrderWrapper);
            String inOrderMax = "";
            //取出通过stream里的方法，取出inorders里面最大的订单号
            if (inOrders.size() > 0)
                inOrderMax = inOrders.stream().max(Comparator.comparing(InOrder::getIn_order_id)).get().getIn_order_id();
            //获取当前订单日期
            LocalDate currentData = LocalDate.now();
            String currentInorder = String.valueOf(currentData);
            currentInorder = currentInorder.replace("-", "");
            if (inOrderMax.equals((""))) inOrderMax = "UO" + currentInorder + "0000";
            //取出入库单整数部分，用int长度不够，所以的用BigInteger
            BigInteger inOrderNoTemp = BigInteger.valueOf(Long.parseLong(inOrderMax.substring(2)));

            if (inOrderMax.substring(2, inOrderMax.length() - 4).equals(currentInorder)) {
                //BigInteger型加1
                inOrderNoTemp = inOrderNoTemp.add(BigInteger.valueOf(1));
                //组成字符型入库单号，返回给前端
                inOrderNo = "UO" + inOrderNoTemp;

            } else {
                inOrderNo = "UO" + currentInorder + "0001";
            }
            interReturn.setStatus(true);
            interReturn.setResult(inOrderNo);
            interReturn.setMessage("生成入库单号成功！");
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错：" + e.getCause());
            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
            return interReturn;
        }
//        logger.info("GenerateInOrderNo 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:lcy
     * @Description 新增入库单头
     * @DateTime 2023/7/1 12:53
     * @Params
     * @Return
     */

    @ApiOperation(value = "新增入库单表头")
    @PostMapping("/InOrder/AddInOrder")
    @CrossOrigin
    public InterReturn AddInOrder(@RequestBody InOrder inOrder) {
//        logger.info("进入 AddInOrder");
//        logger.info("接收到参数：" + inOrder);
        InterReturn interReturn = new InterReturn();
        try {
            if (inOrder.getOrder_type() == null || inOrder.getOrder_type().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("单据类别异常或单据名为空");
                System.out.println("单据类别异常或单据名为空");
                return interReturn;
//            } else if (inOrder.getCar_no() == null || inOrder.getCar_no().length() == 0) {
//                interReturn.setStatus(false);
//                interReturn.setMessage("车牌号异常或车牌号为空");
//                System.out.println("车牌号异常或车牌号为空");
//                return interReturn;
            } else if (inOrder.getOrg_order() == null || inOrder.getOrg_order().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("来源单号为空");
                System.out.println("来源单号异常或来源单号为空");
                return interReturn;
            }

            InterReturn interReturnInOrderNo = GenerateInOrderNo();//生成单据号
            if (interReturnInOrderNo.isStatus()) {
                interReturn.setMessage("入库号生成成功");
            } else {
                interReturn.setMessage("入库号生成出错：" + interReturnInOrderNo.getMessage());
                return interReturn;
            }
            inOrder.setIn_order_id(interReturnInOrderNo.getResult().toString());
            inOrder.setStatus("创建中");
            inOrder.setCreate_time(Calendar.getInstance().getTime());
            inOrder.setUpdate_time(Calendar.getInstance().getTime());

            boolean boolSave = inOrderService.save(inOrder);
            if (boolSave) {
                interReturn.setStatus(true);
                interReturn.setMessage("添加入库单头数据成功！");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("添加入库单头数据条数为0！");
            }

        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("新增入库单头出错：" + e.getCause());
            System.out.println("新增入库单头出错：" + e.getMessage());
            logger.error("新增入库单头出错：" + e.getMessage());
            return interReturn;
        }
        interReturn.setTimestamp(Calendar.getInstance().getTime());
//        logger.info("AddInOrder返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:lcy
     * @Description :编辑入库单头接口
     * @DateTime 2023/7/4 9:55
     * @Params
     * @Return
     */
    @ApiOperation(value = "编辑入库单头接口")
    @PostMapping("/InOrder/EditInOrder")
    @CrossOrigin
    public InterReturn EditInOrder(@RequestBody InOrder inOrder) {
//        logger.info("进入EditInOrder");
//        logger.info("接收到参数：" + inOrder);
        InterReturn interReturn = new InterReturn();
        try {
            if (inOrder.getIn_order_id() == null || "".equals(inOrder.getIn_order_id().trim())) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或入库单号为空！");
                System.out.println("参数异常或入库单号为空！");
                return interReturn;
            } else {
                LambdaUpdateWrapper<InOrder> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(InOrder::getIn_order_id, inOrder.getIn_order_id());
                wrapper.set(inOrder.getOrder_type() != null && !inOrder.getOrder_type().isEmpty(), InOrder::getOrder_type, inOrder.getOrder_type());
                wrapper.set(inOrder.getStatus() != null, InOrder::getStatus, inOrder.getStatus());
                wrapper.set(inOrder.getCar_no() != null && !inOrder.getCar_no().isEmpty(), InOrder::getCar_no, inOrder.getCar_no());
                wrapper.set(inOrder.getOrg_order() != null && !inOrder.getOrg_order().isEmpty(), InOrder::getOrg_order, inOrder.getOrg_order());
                wrapper.set(inOrder.getHesuankemu() != null && !inOrder.getHesuankemu().isEmpty(), InOrder::getHesuankemu, inOrder.getHesuankemu());
                wrapper.set(inOrder.getKufangmingcheng() != null && !inOrder.getKufangmingcheng().isEmpty(), InOrder::getKufangmingcheng, inOrder.getKufangmingcheng());
                wrapper.set(inOrder.getFawudanwei() != null && !inOrder.getFawudanwei().isEmpty(), InOrder::getFawudanwei, inOrder.getFawudanwei());
                wrapper.set(inOrder.getHetongbianhao() != null && !inOrder.getHetongbianhao().isEmpty(), InOrder::getHetongbianhao, inOrder.getHetongbianhao());
                wrapper.set(inOrder.getDiaobodanhao() != null && !inOrder.getDiaobodanhao().isEmpty(), InOrder::getDiaobodanhao, inOrder.getDiaobodanhao());
                wrapper.set(InOrder::getUpdater, inOrder.getUpdater());
                wrapper.set(InOrder::getUpdate_time, LocalDateTime.now());
                boolean bool = inOrderService.update(null, wrapper);
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
            return interReturn;
        }
        logger.info("EditInOrder返回：" + interReturn);
        return interReturn;
    }

}

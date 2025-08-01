package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.InOrder;
import com.example.wmsmp.entity.InOrderDetail;
import com.example.wmsmp.entity.InOrderExcel;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.mapper.InOrderDetailMapper;
import com.example.wmsmp.mapper.InOrderMapper;
import com.example.wmsmp.service.InOrderDetailService;
import com.example.wmsmp.service.InOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Api(value = "123", tags = "入库明细")
@RestController
@CrossOrigin
public class InOrderDetailController {
    @Autowired
    InOrderService inOrderService;

    @Autowired
    InOrderDetailService inOrderDetailService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    private static Logger logger = Logger.getLogger(InOrderDetailController.class);

    /**
     * @Author:lcy
     * @Description :前端传递过来入库单明细的入库单头号
     * @DateTime 2023/7/3 11:34
     * @Params
     * @Return
     */
    @ApiOperation(value = "条件查询入库明细", notes = "InOrderDetail类（入库单明细ID，入库单ID，物料编号，物料名称） pageNo pageSize")
    @PostMapping("/InOrderDetail/GetInOrderDetailBy")
    @CrossOrigin
    public InterReturn GetInOrderDetailBy(@RequestBody InOrderDetail inOrderDetail, int pageNo, int pageSize) {
//        logger.info("进入 GetInOrderDetailBy");
//        logger.info("接收到参数：" + inOrderDetail);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<InOrderDetail> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<InOrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(inOrderDetail.getIn_order_detail_id() != null && !inOrderDetail.getIn_order_detail_id().equals(0),
                    InOrderDetail::getIn_order_detail_id, inOrderDetail.getIn_order_detail_id());//入库单明细ID
            wrapper.eq(inOrderDetail.getIn_order_id() != null && !inOrderDetail.getIn_order_id().isEmpty(),
                    InOrderDetail::getIn_order_id, inOrderDetail.getIn_order_id());//入库单ID
            wrapper.eq(inOrderDetail.getMaterial_code() != null && !inOrderDetail.getMaterial_code().isEmpty(),
                    InOrderDetail::getMaterial_code, inOrderDetail.getMaterial_code());//物料编号
            wrapper.eq(inOrderDetail.getMaterial_name() != null && !inOrderDetail.getMaterial_name().isEmpty(),
                    InOrderDetail::getMaterial_name, inOrderDetail.getMaterial_name());//物料名称

            wrapper.eq(inOrderDetail.getUdf02() != null && !inOrderDetail.getUdf02().isEmpty(),
                    InOrderDetail::getUdf02, inOrderDetail.getUdf02());//旧物料编号

//需要按时间查询时 放开此行   wrapper.between(InOrderDetail::getCreate_time, startTime, endTime);//时间在这两个时间中间
            wrapper.orderByDesc(InOrderDetail::getUpdate_time);
            List<InOrderDetail> inOrderDetails = inOrderDetailService.page(page, wrapper).getRecords();

            if (inOrderDetails == null || inOrderDetails.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到入库明细！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询入库明细成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(inOrderDetails);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询入库明细出错：" + e.getCause());
            System.out.println("查询入库明细出错：" + e.getMessage());
            logger.error("查询入库明细出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        //logger.info("GetInOrderDetailBy 返回：" + interReturn);
        return interReturn;
    }


    @ApiOperation(value = "更新入库明细状态", notes = "根据OrderId更新用户 传InOrderDetail类")
    @PostMapping("/InOrderDetail/UpdateInOrderDetailByOrderId")
    @CrossOrigin
    public InterReturn UpdateInOrderDetailByOrderId(@RequestBody InOrderDetail inOrderDetail) {
//        logger.info("进入 UpdateInOrderDetailByOrderId");
//        logger.info("接收到参数：" + inOrderDetail);
        InterReturn interReturn = new InterReturn();
        try {
            if (inOrderDetail.getIn_order_id() == null || inOrderDetail.getIn_order_id().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或入库表头id为空");
                System.out.println("参数异常或入库表头id为空");
                return interReturn;
            }
            if (inOrderDetail.getStatus() == null) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或入库明细状态为空");
                System.out.println("参数异常或入库明细状态为空");
                return interReturn;
            }
            if (inOrderDetail.getUpdater() == null || inOrderDetail.getUpdater().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或更新人为空");
                System.out.println("参数异常或更新人为空");
                return interReturn;
            }
            //mybatisplus
            LambdaUpdateWrapper<InOrderDetail> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(InOrderDetail::getIn_order_id, inOrderDetail.getIn_order_id());
            wrapper.set(inOrderDetail.getStatus() != null, InOrderDetail::getStatus, inOrderDetail.getStatus());
            wrapper.set(InOrderDetail::getUpdater, inOrderDetail.getUpdater());
            wrapper.set(InOrderDetail::getUpdate_time, LocalDateTime.now());
            boolean bool = inOrderDetailService.update(null, wrapper);
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
            return interReturn;
        }
//        logger.info("UpdateInOrderDetailByOrderId 返回：" + interReturn);
        return interReturn;
    }


    @ApiOperation(value = "批量添加入库明细(仅上游调用)", notes = "传InOrderDetail类List")
    @PostMapping("/InOrderDetail/Adds")
    @CrossOrigin
    public InterReturn Adds(@RequestBody List<InOrderDetail> inOrderDetails) {
        logger.info("进入 Adds");
        logger.info("接收到参数：入库明细列表：" + inOrderDetails);
        InterReturn interReturn = new InterReturn();
        interReturn.setStatus(false);
        try {
            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            InOrderDetailMapper mapMapper = sqlSession.getMapper(InOrderDetailMapper.class);//获取对应Mapper

            Date time = Calendar.getInstance().getTime();//创建时间赋值
            for (InOrderDetail inOrderDetail : inOrderDetails) {
                {
                    //判断批次是否为空
                    if (inOrderDetail.getBatch() == null || inOrderDetail.getBatch().trim().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("批次号有空值");
                        System.out.println("批次号有空值");
                        return interReturn;
                    }
                    //判断入库单号是否为空
                    if (inOrderDetail.getIn_order_id() == null || inOrderDetail.getIn_order_id().trim().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("入库单号有空值");
                        System.out.println("入库单号有空值");
                        return interReturn;
                    }
                    //判断物料编号是否为空
                    if (inOrderDetail.getMaterial_code() == null || inOrderDetail.getMaterial_code().trim().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("物料编号有空值");
                        System.out.println("物料编号有空值");
                        return interReturn;
                    }
                    //判断物料名称是否为空
                    if (inOrderDetail.getMaterial_name() == null || inOrderDetail.getMaterial_name().trim().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("物料名称有空值");
                        System.out.println("物料名称有空值");
                        return interReturn;
                    }
//   价格不是必填项    //判断物料单价是否小于等于0
//                    if (inOrderDetail.getMaterial_price().compareTo(BigDecimal.ZERO) <= 0) {
//                        interReturn.setStatus(false);
//                        interReturn.setMessage("物料单价异常");
//                        System.out.println("物料单价异常");
//                        return interReturn;
//                    }
                    //判断物料数量是否小于等于0
                    if (inOrderDetail.getOrder_count() <= 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("计划数量异常");
                        System.out.println("计划数量有异常");
                        return interReturn;
                    }
                    //判断质量等级是否为空
                    if (inOrderDetail.getZhiliangdengji() == null || inOrderDetail.getZhiliangdengji().trim().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("质量等级有空值");
                        System.out.println("质量等级有空值");
                        return interReturn;
                    }
                }
                inOrderDetail.setCreate_time(time);
                inOrderDetail.setCreator("上游下发");
                inOrderDetail.setActual_count(0);
                inOrderDetail.setStatus("执行中");
                mapMapper.insert(inOrderDetail);//准备执行sql
            }
            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("入库明细生成成功！");
            } catch (Exception e) {
                logger.error("批量插入入库明细异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("批量插入入库明细异常，事务回滚:" + e.getCause());
            } finally {
                sqlSession.close();
            }
            return interReturn;
        } catch (
                Exception ee) {
            interReturn.setMessage("生成出错：" + ee.getCause());
            return interReturn;
        }

    }

    /**
     * @Author:lcy
     * @Description:新增入库单明细
     * @DateTime 2023/7/1 13:30
     * @Params
     * @Return
     */
    @ApiOperation(value = "新增入库明细")
    @PostMapping("/InOrderDetail/AddInOrderDetail")
    @CrossOrigin
    public InterReturn AddInOrderDetail(@RequestBody InOrderDetail inOrderDetail) {
//        logger.info("进入 AddInOrderDetail");
//        logger.info("接收到参数：" + inOrderDetail);
        InterReturn interReturn = new InterReturn();
        try {
            if (inOrderDetail.getMaterial_code() == null || inOrderDetail.getMaterial_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("物料编号异常或物料编号为空！");
                System.out.println("物料编号异常或物料编号为空！");
                return interReturn;
            } else if (inOrderDetail.getIn_order_id() == null || inOrderDetail.getIn_order_id().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("入库单头号为空!");
                System.out.println("入库单头号为空!");
                return interReturn;
            } else if (inOrderDetail.getMaterial_name() == null || inOrderDetail.getMaterial_name().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("物料名称异常或物料名称为空!");
                System.out.println("物料名称异常或物料名称为空!");
                return interReturn;
            } else if (inOrderDetail.getOrder_count() <= 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("订单数量不能小于等于0！");
                System.out.println("订单数量不能小于等于0！");
                return interReturn;
            } else if (inOrderDetail.getBatch() == null || inOrderDetail.getBatch().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("批次为空！");
                System.out.println("批次为空！");
                return interReturn;
            }

            //1.判断表头是否能插入
            LambdaQueryWrapper<InOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(InOrder::getIn_order_id, inOrderDetail.getIn_order_id());
            List<InOrder> inOrders = inOrderService.list(wrapper);
            if (inOrders.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("入库单不存在：" + inOrderDetail.getIn_order_id());
                System.out.println("入库单不存在：" + inOrderDetail.getIn_order_id());
                return interReturn;
            }

            //只有创建中可以添加
            if (!inOrders.get(0).getStatus().equals("创建中")) {
                interReturn.setStatus(false);
                interReturn.setMessage("入库单状态异常：" + inOrders.get(0).getStatus());
                System.out.println("入库单状态异常：" + inOrders.get(0).getStatus());
                return interReturn;
            }

            //2.插入明细
            inOrderDetail.setCreate_time(Calendar.getInstance().getTime());
            inOrderDetail.setUpdate_time(Calendar.getInstance().getTime());
            inOrderDetail.setStatus("执行中");
            boolean boolSave = inOrderDetailService.save(inOrderDetail);
            if (boolSave) {
                interReturn.setStatus(true);
                interReturn.setMessage("添加入库单明细成功！");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("添加入库单明细数据条数为0！");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("新增出错：" + e.getCause());
            System.out.println("新增出错：" + e.getMessage());
            logger.error("新增出错：" + e.getMessage());
            return interReturn;
        }
//        logger.info("AddInOrderDetail返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:lcy
     * @Description :删除入库明细
     * @DateTime 2023/7/1 13:33
     * @Params
     * @Return
     */
    @ApiOperation(value = "删除入库明细")
    @PostMapping("/InOrderDetail/DelInOrderDetail")
    @CrossOrigin
    public InterReturn DelInOrderDetail(@RequestBody InOrderDetail tInOrderDetail) {
//        logger.info("进入 DelInOrderDetail");
//        logger.info("接收到参数：" + tInOrderDetail);
        InterReturn interReturn = new InterReturn();
        try {
            if (tInOrderDetail.getIn_order_detail_id() == null) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或入库明细单号为空");
                System.out.println("参数异常或入库明细单号为空");
                return interReturn;
            } else {
                //mybatisplus
                boolean bool = inOrderDetailService.removeById(tInOrderDetail);
                if (bool) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("删除入库明细成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("删除数据条数为0！");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("删除入库明细出错：" + e.getCause());
            System.out.println("删除入库明细出错：" + e.getMessage());
            logger.error("删除入库明细出错：" + e.getMessage());
        }
//        logger.info("DelInOrderDetail返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:lcy
     * @Description :编辑入库明细接口，待确定下来完善
     * @DateTime 2023/7/4 9:55
     * @Params
     * @Return
     */
    @ApiOperation(value = "编辑入库明细")
    @PostMapping("/InOrderDetail/EditInOrderDetail")
    @CrossOrigin
    public InterReturn EditInOrderDetail(@RequestBody InOrderDetail inOrderDetail) {
//        logger.info("进入EditInOrderDetail");
//        logger.info("接收到参数：" + inOrderDetail);
        InterReturn interReturn = new InterReturn();
        try {
            LambdaUpdateWrapper<InOrderDetail> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(InOrderDetail::getIn_order_detail_id, inOrderDetail.getIn_order_detail_id());
            wrapper.set(inOrderDetail.getMaterial_code() != null && !inOrderDetail.getMaterial_code().isEmpty(),
                    InOrderDetail::getMaterial_code, inOrderDetail.getMaterial_code());
            wrapper.set(inOrderDetail.getMaterial_name() != null && !inOrderDetail.getMaterial_name().isEmpty(),
                    InOrderDetail::getMaterial_name, inOrderDetail.getMaterial_name());
            wrapper.set(inOrderDetail.getBatch() != null && !inOrderDetail.getBatch().isEmpty(),
                    InOrderDetail::getBatch, inOrderDetail.getBatch());
            wrapper.set(inOrderDetail.getOrder_count() != 0,
                    InOrderDetail::getOrder_count, inOrderDetail.getOrder_count());
            wrapper.set(inOrderDetail.getUpdate_time() != null, InOrderDetail::getUpdate_time, LocalDateTime.now());
            wrapper.set(inOrderDetail.getCreator() != null, InOrderDetail::getCreator, inOrderDetail.getCreator());
            wrapper.set(inOrderDetail.getCreator() != null, InOrderDetail::getCreator, inOrderDetail.getCreator());
            wrapper.set(InOrderDetail::getUpdate_time, LocalDateTime.now());
            boolean bool = inOrderDetailService.update(null, wrapper);
            if (bool) {
                interReturn.setStatus(true);
                interReturn.setMessage("编辑入库明细成功！");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("无更新!");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("编辑入库明细出错：" + e.getCause());
            System.out.println("编辑入库明细出错：" + e.getMessage());
            logger.error("编辑入库明细出错：" + e.getMessage());
            return interReturn;
        }
//        logger.info("EditInOrderDetail 返回：" + interReturn);
        return interReturn;
    }


    @ApiOperation(value = "查询返回Excel类", notes = "返回Excel类")
    @PostMapping("/InOrderDetail/GetInOrderExcel")
    @CrossOrigin
    public InterReturn GetInOrderExcel(@RequestBody List<InOrder> inOrders) {
//        logger.info("进入GetInOrderExcel");
//        logger.info("接收到参数：" + inOrders);
        InterReturn interReturn = new InterReturn();
        List<InOrderExcel> inOrderExcels = new ArrayList<>();//用于导出Excel
        try {
            if (inOrders == null || inOrders.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("传入表头为空");
                logger.info("GetInOrderExcel返回：" + interReturn);
                return interReturn;
            }

            //去重
            List<InOrder> newInOrderList = inOrders.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(InOrder::getIn_order_id))),
                            ArrayList::new));

            for (InOrder item : newInOrderList) {
                if (item.getIn_order_id() == null || item.getIn_order_id().isEmpty()) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("传入单据号有空");
                    logger.info("GetInOrderExcel返回：" + interReturn);
                    return interReturn;
                }
            }

            for (InOrder inOrder : newInOrderList) {
                LambdaQueryWrapper<InOrderDetail> wrapperInOrderDetail = new LambdaQueryWrapper<>();
                wrapperInOrderDetail.eq(InOrderDetail::getIn_order_id, inOrder.getIn_order_id());
                List<InOrderDetail> inOrderDetails = inOrderDetailService.list(wrapperInOrderDetail);
                if (inOrderDetails.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("“" + inOrder.getIn_order_id() + "”单据无明细");
                    logger.info("GetInOrderExcel返回：" + interReturn);
                    return interReturn;
                }

                //拼Excel类
                for (InOrderDetail inOrderDetail : inOrderDetails) {
                    InOrderExcel inOrderExcel = new InOrderExcel();
                    inOrderExcel.setIn_order_id(inOrder.getIn_order_id());//入库单号
                    inOrderExcel.setWarehouse_name(inOrder.getWarehouse_name());//仓库名称
                    inOrderExcel.setOrder_type(inOrder.getOrder_type());//订单类型
                    inOrderExcel.setCar_no(inOrder.getCar_no());//车牌号
                    inOrderExcel.setOrg_order(inOrder.getOrg_order());//原始单号
                    inOrderExcel.setHesuankemu(inOrder.getHesuankemu());//核算科目
                    inOrderExcel.setKufangmingcheng(inOrder.getKufangmingcheng());//库房号
                    inOrderExcel.setFawudanwei(inOrder.getFawudanwei());//发物单位
                    inOrderExcel.setHetongbianhao(inOrder.getHetongbianhao());//合同编号
                    inOrderExcel.setDiaobodanhao(inOrder.getDiaobodanhao());//调拨单号
                    //明细
                    //inOrderExcel.setIn_order_detail_id();//明细ID
                    inOrderExcel.setMaterial_code(inOrderDetail.getMaterial_code());//物料编号
                    inOrderExcel.setMaterial_name(inOrderDetail.getMaterial_name());//物料名称
                    inOrderExcel.setOrder_count(inOrderDetail.getOrder_count());//订单数量
                    inOrderExcel.setActual_count(inOrderDetail.getActual_count());//收货数量
                    inOrderExcel.setBatch(inOrderDetail.getBatch());//批次
                    inOrderExcel.setCreator(inOrderDetail.getCreator());//创建人
                    inOrderExcel.setCreate_time(inOrderDetail.getCreate_time());//创建时间
                    inOrderExcel.setUpdater(inOrderDetail.getUpdater());//更新人
                    inOrderExcel.setUpdate_time(inOrderDetail.getUpdate_time());//更新时间
                    inOrderExcel.setMaterial_price(inOrderDetail.getMaterial_price());//单价
                    inOrderExcel.setZhiliangdengji(inOrderDetail.getZhiliangdengji());//质量等级

                    inOrderExcels.add(inOrderExcel);
                }
            }
            interReturn.setStatus(true);
            interReturn.setMessage("查询成功");
            interReturn.setResult(inOrderExcels);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询返回Excel类出错：" + e.getCause());
            System.out.println("查询返回Excel类出错：" + e.getMessage());
            logger.error("查询返回Excel类出错：" + e.getMessage());
        }
//        logger.info("GetInOrderExcel返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 入库单导入 参数Excel类
     * @DateTime 2024/1/27 14:14
     * @Params
     * @Return
     */
    @ApiOperation(value = "导入Excel类", notes = "传Excel类 返回导入结果")
    @PostMapping("/InOrderDetail/ImportInOrderExcel")
    @CrossOrigin
    public InterReturn ImportInOrderExcel(@RequestBody List<InOrderExcel> inOrderExcels) {
//        logger.info("进入ImportInOrderExcel");
//        logger.info("接收到参数：" + inOrderExcels);
        InterReturn interReturn = new InterReturn();
        try {
            //判空
            if (inOrderExcels == null || inOrderExcels.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("传入Excel信息为空");
                logger.info("ImportInOrderExcel返回：" + interReturn);
                return interReturn;
            }

            //判单据号空
            for (InOrderExcel inOrderExcel : inOrderExcels) {
                if (inOrderExcel.getIn_order_id() == null || inOrderExcel.getIn_order_id().isEmpty()) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("传入单据号有空");
                    logger.info("ImportInOrderExcel返回：" + interReturn);
                    return interReturn;
                }
            }
            //去重
            List<InOrderExcel> newInOrderExcelList = inOrderExcels.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(InOrderExcel::getIn_order_id))),
                            ArrayList::new));

            List<InOrder> inOrders = new ArrayList<>();//用于insert
            for (InOrderExcel inOrderExcel : newInOrderExcelList) {
                InOrder inOrder = new InOrder();
                inOrder.setIn_order_id(inOrderExcel.getIn_order_id());//入库单号
                inOrder.setWarehouse_name(inOrderExcel.getWarehouse_name());//仓库名称
                inOrder.setOrder_type(inOrderExcel.getOrder_type());//订单类型
                inOrder.setCar_no(inOrderExcel.getCar_no());//车牌号
                inOrder.setOrg_order(inOrderExcel.getOrg_order());//原始单号
                inOrder.setHesuankemu(inOrderExcel.getHesuankemu());//核算科目
                inOrder.setKufangmingcheng(inOrderExcel.getKufangmingcheng());//库房号
                inOrder.setFawudanwei(inOrderExcel.getFawudanwei());//发物单位
                inOrder.setHetongbianhao(inOrderExcel.getHetongbianhao());//合同编号
                inOrder.setDiaobodanhao(inOrderExcel.getDiaobodanhao());//调拨单号

                inOrder.setStatus("创建中");//单据状态
                inOrders.add(inOrder);
            }


            List<InOrderDetail> inOrderDetails = new ArrayList<>();//用于insert
            for (InOrderExcel inOrderExcel : inOrderExcels) {
                InOrderDetail inOrderDetail = new InOrderDetail();
                inOrderDetail.setIn_order_id(inOrderExcel.getIn_order_id());//入库单号
                inOrderDetail.setMaterial_code(inOrderExcel.getMaterial_code());//物料编号
                inOrderDetail.setMaterial_name(inOrderExcel.getMaterial_name());//物料名称
                inOrderDetail.setOrder_count(inOrderExcel.getOrder_count());//订单数量
                inOrderDetail.setActual_count(inOrderExcel.getActual_count());//收货数量
                inOrderDetail.setBatch(inOrderExcel.getBatch());//批次
                inOrderDetail.setStatus("执行中");//明细状态
                inOrderDetail.setMaterial_price(inOrderExcel.getMaterial_price());//单价
                inOrderDetail.setZhiliangdengji(inOrderExcel.getZhiliangdengji());//质量等级
                inOrderDetails.add(inOrderDetail);
            }

            //批量添加两个表 事务

            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            InOrderMapper inOrderMapper = sqlSession.getMapper(InOrderMapper.class);//获取对应Mapper
            InOrderDetailMapper inOrderDetailMapper = sqlSession.getMapper(InOrderDetailMapper.class);//获取对应Mapper

            Date time = Calendar.getInstance().getTime();//创建时间赋值
            for (InOrder inOrder : inOrders) {
                inOrder.setCreate_time(time);
                inOrder.setUpdate_time(time);
//                inOrder.setCreator( );
//                inOrder.setUpdater( );
                inOrderMapper.insert(inOrder);//准备执行sql
            }
            for (InOrderDetail inOrderDetail : inOrderDetails) {
                inOrderDetail.setCreate_time(time);
                inOrderDetail.setUpdate_time(time);
//                inOrderDetail.setCreator( );
//                inOrderDetail.setUpdater( );
                inOrderDetailMapper.insert(inOrderDetail);//准备执行sql
            }

            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("插入成功！");
            } catch (Exception e) {
                logger.error("批量插入入库单表头&明细异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("批量插入入库单表头&明细异常，事务回滚:" + e.getCause());
            } finally {
                sqlSession.close();
            }


        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("导入Excel类出错：" + e.getCause());
            System.out.println("导入Excel类出错：" + e.getMessage());
            logger.error("导入Excel类出错：" + e.getMessage());
        }

//        logger.info("ImportInOrderExcel返回：" + interReturn);
        return interReturn;
    }
}

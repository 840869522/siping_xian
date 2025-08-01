package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.annotation.BusinessType;
import com.example.wmsmp.annotation.FlowAnnotation;
import com.example.wmsmp.entity.*;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.mapper.OutOrderMapper;
import com.example.wmsmp.service.InventoryService;
import com.example.wmsmp.service.OutOrderDetailService;
import com.example.wmsmp.service.OutPickService;
import com.example.wmsmp.service.VMapInvService;
import com.example.wmsmp.util.OutHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Api(value = "123", tags = "配盘信息")
@RestController
@CrossOrigin
public class OutPickController {

    @Autowired
    OutPickService outPickService;

    @Autowired
    InventoryService inventoryService;

    @Autowired
    VMapInvService vMapInvService;

    @Autowired
    OutOrderDetailService outOrderDetailService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    private static Logger logger = Logger.getLogger(OutPickController.class);

    /**
     * @Author:徐浩铖
     * @Description :条件查询配盘信息
     * @DateTime 2023/8/28 19:50
     * @Params
     * @Return
     */
    @ApiOperation(value = "条件查询配盘信息", notes = "参数OutPick类（拣选方式，载具编号，物料编号，出库单号，状态） pageNo pageSize")
    @PostMapping("/OutPick/GetOutPickBy")
    @CrossOrigin
    public InterReturn GetOutPickBy(@RequestBody OutPick outPick, int pageNo, int pageSize) {
        //public InterReturn GetOutPickBy(@RequestBody OutPick outPick, int pageNo, int pageSize, String startTime, String endTime) {
//        logger.info("进入 GetOutPickBy");
//        logger.info("接收到参数：" + outPick);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<OutPick> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<OutPick> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(outPick.getOut_pick_type() != null && !outPick.getOut_pick_type().isEmpty(),
                    OutPick::getOut_pick_type, outPick.getOut_pick_type());//拣选方式
            wrapper.eq(outPick.getPallet_code() != null && !outPick.getPallet_code().isEmpty(),
                    OutPick::getPallet_code, outPick.getPallet_code());//载具编号
            wrapper.eq(outPick.getMaterial_code() != null && !outPick.getMaterial_code().isEmpty(),
                    OutPick::getMaterial_code, outPick.getMaterial_code());//物料编号
            wrapper.eq(outPick.getOut_order_id() != null && !outPick.getOut_order_id().isEmpty(),
                    OutPick::getOut_order_id, outPick.getOut_order_id());//出库单号
            wrapper.eq(outPick.getStatus() != null && !outPick.getStatus().isEmpty(),
                    OutPick::getStatus, outPick.getStatus());//状态
            wrapper.orderByDesc(OutPick::getId);
            //  wrapper.between(outPick.getCreate_time() != null, OutPick::getCreate_time, startTime, endTime);//时间在这两个时间中间
            List<OutPick> outPicks = outPickService.page(page, wrapper).getRecords();

            if (outPicks == null || outPicks.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找配盘信息！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询配盘信息成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(outPicks);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询配盘信息出错：" + e.getCause());
            System.out.println("查询配盘信息出错：" + e.getMessage());
            logger.error("查询配盘信息出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        //logger.info("GetOutPickBy 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:徐浩铖
     * @Description :编辑配盘信息
     * @DateTime 2023/8/28 20:26
     * @Params
     * @Return
     */
    @ApiOperation(value = "编辑配盘信息", notes = "根据out_pick_id更新 传OutPick类")
    @PostMapping("/OutPick/UpdateOutPickByID")
    @CrossOrigin
    public InterReturn UpdateOutPickByID(@RequestBody OutPick outPick) {
        logger.info("进入UpdateOutPickByID");
        logger.info("接收到参数：" + outPick);
        InterReturn interReturn = new InterReturn();
        try {
            if (outPick.getMaterial_code() == null || outPick.getMaterial_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或配盘信息ID为空！");
                System.out.println("参数异常或配盘信息ID为空！");
                logger.info("UpdateOutPickByID返回：" + interReturn);
                return interReturn;
            } else {
                LambdaUpdateWrapper<OutPick> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(OutPick::getMaterial_code, outPick.getMaterial_code());
                wrapper.set(outPick.getPick_qty() != null && !outPick.getPick_qty().equals(0),
                        OutPick::getPick_qty, outPick.getPick_qty());
                wrapper.set(OutPick::getUpdater, outPick.getUpdater());
                wrapper.set(OutPick::getUpdate_time, LocalDateTime.now());
                boolean bool = outPickService.update(null, wrapper);
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
            interReturn.setMessage("更新配盘信息出错：" + e.getCause());
            logger.error("更新配盘信息出错：" + e.getMessage());
        }
        logger.info("UpdateOutPickByID返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:徐浩铖
     * @Description 删除配盘信息
     * @DateTime 2023/8/28 20:03
     * @Params
     * @Return
     */
    @ApiOperation(value = "删除配盘信息", notes = "根据out_pick_id删除 传OutPick类")
    @PostMapping("/OutPick/DelOutPickByID")
    @CrossOrigin
    public InterReturn DelOutPickByID(@RequestBody OutPick outPick) {
        logger.info("进入 DelOutPick");
        logger.info("接收到参数：" + outPick);
        InterReturn interReturn = new InterReturn();
        try {
            if (outPick.getMaterial_code() == null || outPick.getMaterial_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或配盘信息ID为空");
                System.out.println("参数异常或配盘信息ID为空");
                logger.info("DelOutPickByID 返回：" + interReturn);
                return interReturn;
            } else {
                //mybatisplus
                boolean bool = outPickService.removeById(outPick);
                if (bool) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("删除配盘信息成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("删除数据条数为0");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("删除配盘信息出错：" + e.getCause());
            System.out.println("删除配盘信息出错：" + e.getMessage());
            logger.error("删除配盘信息出错：" + e.getMessage());
        }
        logger.info("DelOutPickByID 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:徐浩铖
     * @Description 出库配盘 0表头→明细 参数2024年8月5日改为单个表头和出库口
     * @DateTime 2023/10/26 9:33
     * @Params 出库单头List
     * @Return
     */
    @ApiOperation(value = "表头配盘", notes = "出库配盘 0表头→明细")
    @PostMapping("/OutPick/OrderPick")
    @CrossOrigin
    public InterReturn OrderPick(@RequestBody OutOrder outOrderOld, String[] ports) {
        InterReturn interReturn = new InterReturn();
        List<OutOrderDetail> detailListAll = new ArrayList<>();
        logger.info("进入 OrderPick");
        logger.info("接收到参数：" + outOrderOld);
        try {
            //数据校验
            if (ports.length == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("出库配盘出错：出库口集合为空！");
                logger.info("OrderPick 返回：" + interReturn);
                return interReturn;
            }
            List<OutOrder> orderList = new ArrayList<>();
            orderList.add(outOrderOld);
            for (OutOrder order : orderList) {
                if (order.getOut_order_id() == null || order.getOut_order_id().length() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("出库配盘出错：单头id错误！");
                    logger.info("OrderPick 返回：" + interReturn);
                    return interReturn;
                }
                if (!order.getStatus().equals("已审核")) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("出库配盘出错：" + order.getOut_order_id() + "单据(" + order.getStatus() + ")未审核或已配盘！");
                    logger.info("OrderPick 返回：" + interReturn);
                    return interReturn;
                }
            }

            //0.按表头查明细
            for (OutOrder outOrder : orderList) {
                Map<String, Object> columnMap = new HashMap<>();
                columnMap.put("out_order_id", outOrder.getOut_order_id());
                List<OutOrderDetail> detailListOne = outOrderDetailService.listByMap(columnMap);
                detailListAll.addAll(detailListOne);
            }
            interReturn = DetailPick(detailListAll, ports);//明细配盘

            //配盘成功 则更新表头状态为“已配盘”
            if (interReturn.isStatus()) {
                //mybatisplus
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
                OutOrderMapper mapMapper = sqlSession.getMapper(OutOrderMapper.class);//获取对应Mapper

                Date time = Calendar.getInstance().getTime();//创建时间赋值
                for (OutOrder outOrder : orderList) {
                    outOrder.setStatus("已配盘");
                    outOrder.setUpdater("配盘人");//待改
                    outOrder.setUpdate_time(time);
                    mapMapper.updateById(outOrder);//准备执行sql
                }

                try {
                    sqlSession.commit();//执行sql
                    interReturn.setStatus(true);
                    interReturn.setMessage("批量更新出库表头状态成功！");
                } catch (Exception e) {
                    logger.error("批量更新出库表头状态异常，事务回滚", e);
                    sqlSession.rollback();
                    interReturn.setMessage("批量更新出库表头状态异常，事务回滚:" + e.getCause());
                } finally {
                    sqlSession.close();
                }
                return interReturn;
            } else {
                //配盘出错
            }

        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("出库配盘出错：" + e.getCause());
            System.out.println("出库配盘出错：" + e.getMessage());
            logger.error("出库配盘出错：" + e.getMessage());
        }
        logger.info("OrderPick 返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:徐浩铖
     * @Description 出库配盘 1插入配盘 2更新明细已配盘 3更新库存冻结
     * @DateTime 2023/9/20 11:03
     * @Params 出库单明细List
     * @Return
     */
    @ApiOperation(value = "明细配盘", notes = "传入出库单明细List<OutOrderDetail>,出库口集合  1插入配盘 2更新明细已配盘 3更新库存冻结")
    //  @PostMapping("/OutPick/DetailPick")
    @CrossOrigin
    public InterReturn DetailPick(@RequestBody List<OutOrderDetail> details, String[] ports) {
        InterReturn interReturn = new InterReturn();
        logger.info("进入 DetailPick");
        logger.info("接收到参数：" + details);
        try {
            interReturn = OutPickDetails(details, ports);//生成 出库单据明细配盘数据
            if (interReturn.isStatus()) {
                List<OutPick> pickList = (List<OutPick>) interReturn.getResult();
                for (OutPick item : pickList) {
                    //1.配盘表insert
                    item.setCreate_time(Calendar.getInstance().getTime());
                    item.setUpdate_time(Calendar.getInstance().getTime());
                    boolean boolSave = outPickService.save(item);
                    if (boolSave) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("配盘" + item.getId() + "的新增成功");
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("配盘" + item.getId() + "的新增失败");
                        logger.info("DetailPick 返回：" + interReturn);
                        return interReturn;
                    }

                    //2.单据配盘数&状态 update增 Status已配盘
                    LambdaUpdateWrapper<OutOrderDetail> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(OutOrderDetail::getOut_order_detail_id, item.getOut_order_detail_id());
                    wrapper.setSql("pick_count=pick_count+" + item.getPick_qty());//增量
                    wrapper.set(OutOrderDetail::getStatus, "已配盘");//出库单明细的状态改为已配盘
                    wrapper.set(OutOrderDetail::getUpdater, item.getUpdater());
                    wrapper.set(OutOrderDetail::getUpdate_time, LocalDateTime.now());
                    boolean bool = outOrderDetailService.update(wrapper);
                    if (bool) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("配盘" + item.getId() + "的单据更新成功");
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("配盘" + item.getId() + "的单据更新失败");
                        logger.info("DetailPick 返回：" + interReturn);
                        return interReturn;
                    }

                    //3.库存表update增
                    LambdaUpdateWrapper<Inventory> wrapperUpdate = new LambdaUpdateWrapper<>();
                    wrapperUpdate.setSql("frozen_count=frozen_count+" + item.getPick_qty());//增量
                    wrapperUpdate.eq(Inventory::getInventory_id, item.getInventory_id());//id

                    wrapperUpdate.set(Inventory::getUpdater, item.getUpdater());
                    wrapperUpdate.set(Inventory::getUpdate_time, LocalDateTime.now());
                    boolean boolUpdate = inventoryService.update(wrapperUpdate);
                    if (boolUpdate) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("配盘" + item.getId() + "的库存更新成功");
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("配盘" + item.getId() + "的库存更新失败");
                        logger.info("DetailPick 返回：" + interReturn);
                        return interReturn;
                    }
                }
            } else {
                logger.info("DetailPick 返回：" + interReturn);
                return interReturn;
            }
            interReturn.setMessage("配盘成功");

        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("出库明细配盘出错：" + e.getCause());
            System.out.println("出库明细配盘出错：" + e.getMessage());
            logger.error("出库明细配盘出错：" + e.getMessage());
        }
        logger.info("DetailPick 返回：" + interReturn);
        return interReturn;
    }

    //生成 出库单据明细配盘数据
    public InterReturn OutPickDetails(@RequestBody List<OutOrderDetail> details, String[] ports) {
        logger.info("进入 OutPickDetails");
        logger.info("接收到参数：" + details);
        InterReturn interReturn = new InterReturn();
        List<OutOrderDetail> newDetails = new ArrayList<>();//更新此次应配的数量
        try {
            if (details.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("出库配盘出错：参数异常or出库明细为空！");
                logger.info("OutPickDetails 返回：" + interReturn);
                return interReturn;
            }

            //整理明细 筛选待配订单&待配数量赋值给订单数
            for (OutOrderDetail detail : details) {
                if (detail.getActual_count() >= detail.getOrder_count()) {
                    //已出库数量>=订单数量
                    //已出库完成
                    continue;
                }
                if (detail.getPick_count() >= detail.getOrder_count() - detail.getActual_count()) {
                    //已配盘数量>=订单数量-已出库数量
                    //已经全配
                    continue;
                } else {
                    //订单数量-已出库数量-已配盘数量>0
                    //还有未配
                    Integer lastNum = detail.getOrder_count() - detail.getActual_count() - detail.getPick_count();
                    detail.setOrder_count(lastNum);
                }
                newDetails.add(detail);
            }
            if (newDetails.size() < 1) {
                interReturn.setStatus(false);
                interReturn.setMessage("出库配盘出错：无可配盘单据！");
                logger.info("OutPickDetails 返回：" + interReturn);
                return interReturn;
            }

            //多条明细对多条库存
            interReturn = GetPickPalletResult(newDetails, ports);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("出库配盘出错：" + e.getCause());
            logger.error("出库配盘出错：" + e.getMessage());
        }
        logger.info("OutPickDetails 返回：" + interReturn);
        return interReturn;
    }

    //单据明细分组求和
    public List<OutOrderDetail> detailsGroup(@RequestBody List<OutOrderDetail> details) {
        //使用备用1 字段 合并物料编号和批次号
        for (OutOrderDetail detail : details) {
            detail.setUdf01(detail.getMaterial_code() + "__" + detail.getBatch());
        }

        List<OutOrderDetail> result = new ArrayList<>();
        for (OutOrderDetail order : details) {
            OutOrderDetail newOrder = new OutOrderDetail();
            BeanUtils.copyProperties(order, newOrder);//复制类  源头→目标
            result.add(newOrder);
        }

        //分组求和
        result = new ArrayList<>(result.stream()
                // 表示name为key，接着如果有重复的，那么从Pool对象o1与o2中筛选出一个，这里选择o1，
                // 并把name重复，需要将value与o1进行合并的o2, 赋值给o1，最后返回o1
                .collect(Collectors.toMap(OutOrderDetail::getUdf01,
                        a -> a, (o1, o2) -> {
                            o1.setOrder_count(o1.getOrder_count() + o2.getOrder_count());
                            return o1;
                        })).values());
        return result;
    }

    // 库存分组求值
    public List<Inventory> inventoryGroup(@RequestBody List<Inventory> details) {
        //使用备用1 字段 合并物料编号和批次号
        for (Inventory detail : details) {
            detail.setUdf01(detail.getMaterial_code() + "__" + detail.getBatch());
        }

        List<Inventory> result = new ArrayList<>();
        for (Inventory inventory : details) {
            Inventory newinventory = new Inventory();
            BeanUtils.copyProperties(inventory, newinventory);//复制类  源头→目标
            result.add(newinventory);
        }


        //分组求和
        result = new ArrayList<>(result.stream()
                // 表示name为key，接着如果有重复的，那么从Pool对象o1与o2中筛选出一个，这里选择o1，
                // 并把name重复，需要将value与o1进行合并的o2, 赋值给o1，最后返回o1
                .collect(Collectors.toMap(Inventory::getUdf01,
                        a -> a, (o1, o2) -> {
                            o1.setInventory_count(o1.getInventory_count() + o2.getInventory_count() -
                                    o1.getFrozen_count() - o2.getFrozen_count()
                            );//可用库存
                            return o1;
                        })).values());
        return result;
    }

    // 联查的库存分组求值 2024年8月4日
    public List<VMapInv> vMapInvGroup(@RequestBody List<VMapInv> details) {
        //使用备用1 字段 合并物料编号和批次号
        for (VMapInv detail : details) {
            detail.setUdf01(detail.getMaterial_code() + "__" + detail.getBatch());
        }

        List<VMapInv> result = new ArrayList<>();
        for (VMapInv inv : details) {
            VMapInv vMapInv = new VMapInv();
            BeanUtils.copyProperties(inv, vMapInv);//复制类  源头→目标
            result.add(vMapInv);
        }


        //分组求和
        result = new ArrayList<>(result.stream()
                // 表示name为key，接着如果有重复的，那么从Pool对象o1与o2中筛选出一个，这里选择o1，
                // 并把name重复，需要将value与o1进行合并的o2, 赋值给o1，最后返回o1
                .collect(Collectors.toMap(VMapInv::getUdf01,
                        a -> a, (o1, o2) -> {
                            o1.setInventory_count(o1.getInventory_count() + o2.getInventory_count()
                                    - o1.getFrozen_count() - o2.getFrozen_count());//可用库存
                            o1.setFrozen_count(0);//冻结数量清空
                            return o1;
                        })).values());
        return result;
    }


    //多条明细对多条库存  2024年8月4日，由库存类改为带货位关系的联查库存类
    private InterReturn GetPickPalletResult(@RequestBody List<OutOrderDetail> newDetails, String[] ports) {
        List<OutOrderDetail> detailsGroup = detailsGroup(newDetails);//出库明细 分组求和

        InterReturn returnIsEnough = new InterReturn();
        List<VMapInv> thisDetailsAllVMapInvs = new ArrayList<>();//这些明细对应的库存

        //判断可用库存是否够用
        {
            try {
                //找出对应库存(已改为联查库存)
                for (OutOrderDetail outOrderDetail : detailsGroup) {
                    LambdaQueryWrapper<VMapInv> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(VMapInv::getMaterial_code, outOrderDetail.getMaterial_code());//物料编号
                    wrapper.eq(VMapInv::getBatch, outOrderDetail.getBatch());//批次号
                    List<VMapInv> vMapInvList = vMapInvService.list(wrapper);
                    //无此物料
                    if (vMapInvList.size() < 1) {
                        returnIsEnough.setStatus(false);
                        returnIsEnough.setMessage("无此物料！\n物料编号：" + outOrderDetail.getMaterial_code()
                                + "\n批次号：" + outOrderDetail.getBatch());
                        logger.error("判断库存是否足够出错：" + returnIsEnough);
                        return returnIsEnough;
                    }
                    thisDetailsAllVMapInvs.addAll(vMapInvList);
                }

                //对应库存 分组求和
                List<VMapInv> vMapInvsGroup = vMapInvGroup(thisDetailsAllVMapInvs);

                //处理后的单据和库存 比大小
                for (OutOrderDetail itemO : detailsGroup) {
                    for (VMapInv itemI : vMapInvsGroup) {
                        if (itemO.getUdf01().equals(itemI.getUdf01())) {//getUdf01 物料编号与批次的拼接

                            //旧写法
                            //if (itemI.getInventory_count() - itemI.getFrozen_count() < itemO.getOrder_count()) {//库存-冻结<单据出库数量

                            //新写法
                            if (itemI.getInventory_count() < itemO.getOrder_count()) {//可用库存 <单据出库数量  分组时已经计算可用库存
                                returnIsEnough.setStatus(false);
                                returnIsEnough.setMessage("此物料不足\n物料编号：" + itemO.getMaterial_code() + "\n批次号：" + itemO.getBatch()
                                        + "\n需出：" + itemO.getOrder_count() + "\n可用：" + (itemI.getInventory_count() - itemI.getFrozen_count()));
                                logger.error("判断库存是否足够出错：" + returnIsEnough);
                                return returnIsEnough;
                            }
                        }
                    }
                }
                returnIsEnough.setStatus(true);
                returnIsEnough.setMessage("物料足够配盘");
            } catch (Exception e) {
                returnIsEnough.setStatus(false);
                returnIsEnough.setMessage("判断库存是否足够出错：" + e.getCause());
                logger.error("判断库存是否足够出错：" + e.getMessage());
            }
            if (!returnIsEnough.isStatus()) {
                return returnIsEnough;
            }
        }

        InterReturn interReturn = new InterReturn();
        List<OutPick> outPickAll = new ArrayList<>();
        try {
            //将thisDetailsAllVMapInvs按location_area(组号)排序
            thisDetailsAllVMapInvs.sort(Comparator.comparingInt(VMapInv::getLocation_area));
            //每一条明细 全部相关库存
            int i = 0;//取出库口数量

            for (OutOrderDetail newDetail : newDetails) {
                //重点 一条单据→配盘明细    //配盘类赋值
                {
                    List<OutPick> outPickList = new ArrayList<>();
                    Integer countEnd = newDetail.getOrder_count();//需要配盘的数量
                    for (VMapInv item : thisDetailsAllVMapInvs) {
                        if (newDetail.getMaterial_code().equals(item.getMaterial_code())//同物料则配盘
                                && newDetail.getBatch().equals(item.getBatch())) {

                            OutPick outPick = new OutPick();
                            outPick.setOut_pick_type("拣选方式");//拣选方式
                            outPick.setPallet_code(item.getPallet_code());//载具编号
                            outPick.setCell_id(item.getCell_id());//盒子编号
                            outPick.setOut_order_id(newDetail.getOut_order_id());//出库单号
                            outPick.setOut_order_detail_id(newDetail.getOut_order_detail_id());//出库单明细ID
                            outPick.setInventory_id(item.getInventory_id());//库存ID
                            outPick.setMaterial_code(item.getMaterial_code());//物料编号
                            outPick.setMaterial_name(item.getMaterial_name());//物料名称

                            if (i >= ports.length) {
                                i = 0;
                            }

                            outPick.setPickstation_no(ports[i]);//拣货台编号
                            OutHelper outHelper = new OutHelper();
                            int random = new Random().nextInt(2) + 1;//随机数 1-2间的整数
                            outPick.setTarget_no(outHelper.getDownPortB(ports[i]).get(random).toString());//拣货目标编号
                            outPick.setBatch(item.getBatch());//批次
                            outPick.setStatus("待执行");//状态
                            i++;
                            //此托可用数量
                            int validQty = item.getInventory_count() - item.getFrozen_count();//全部-冻结

                            if (validQty > 0) {//有可用物料
                                if (validQty < countEnd)//可用数量 < 需要配盘的数量
                                {//此托盘不够出
                                    outPick.setPick_qty(validQty);
                                    outPickList.add(outPick);
                                    countEnd -= validQty;
                                    item.setInventory_count(0);
                                } else {//此托盘够出
                                    outPick.setPick_qty(countEnd);
                                    outPickList.add(outPick);
                                    item.setInventory_count(item.getInventory_count() - countEnd);//待改 此处有错，两条同物料的明细 第二次查到的库存未更新
                                    break;
                                }
                            }


                        }
                    }


                    if (outPickList.size() > 0) {
                        outPickAll.addAll(outPickList);
                    }
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("多对多配盘出错：" + e.getCause());
            logger.error("多对多配盘出错：" + e.getMessage());
        }
        interReturn.setStatus(true);
        interReturn.setMessage("多对多配盘成功");
        interReturn.setResult(outPickAll);
        return interReturn;
    }


//    private List<OutPick> detailGetPick(OutOrderDetail outOrderDetail, List<VMapInv> inventorys, List<String> ports) {
//
//
//        List<OutPick> outPickList = new ArrayList<>();
//        Integer countEnd = outOrderDetail.getOrder_count();//需要配盘的数量
//        for (VMapInv item : inventorys) {
//            if (outOrderDetail.getMaterial_code().equals(item.getMaterial_code())//同物料则配盘
//                    && outOrderDetail.getBatch().equals(item.getBatch())) {
//
//                OutPick outPick = new OutPick();
//                outPick.setOut_pick_type("拣选方式");//拣选方式
//                outPick.setPallet_code(item.getPallet_code());//载具编号
//                outPick.setCell_id(item.getCell_id());//盒子编号
//                outPick.setOut_order_id(outOrderDetail.getOut_order_id());//出库单号
//                outPick.setOut_order_detail_id(outOrderDetail.getOut_order_detail_id());//出库单明细ID
//                outPick.setInventory_id(item.getInventory_id());//库存ID
//                outPick.setMaterial_code(item.getMaterial_code());//物料编号
//                outPick.setMaterial_name(item.getMaterial_name());//物料名称
//                outPick.setPickstation_no("拣货台编号");//拣货台编号
//                outPick.setTarget_no("拣货目标编号");//拣货目标编号
//                outPick.setBatch(item.getBatch());//批次
//                outPick.setStatus("待执行");//状态
//
//                //此托可用数量
//                int validQty = item.getInventory_count() - item.getFrozen_count();//全部-冻结
//
//                if (validQty > 0) {//有可用物料
//                    if (validQty < countEnd)//可用数量 < 需要配盘的数量
//                    {//此托盘不够出
//                        outPick.setPick_qty(validQty);
//                        outPickList.add(outPick);
//                        countEnd -= validQty;
//                        item.setInventory_count(0);
//                    } else {//此托盘够出
//                        outPick.setPick_qty(countEnd);
//                        outPickList.add(outPick);
//                        item.setInventory_count(item.getInventory_count() - countEnd);//待改 此处有错，两条同物料的明细 第二次查到的库存未更新
//                        break;
//                    }
//                }
//            }
//        }
//
//        return outPickList;
//    }

    /**
     * @Author:徐浩铖
     * @Description 拣货 入托盘完成
     * @DateTime 2023/9/30 13:26
     * @Params
     * @Return
     */
    @ApiOperation(value = "拣货 入托盘完成", notes = "配盘信息 装入的订单箱号")
    @PostMapping("/OutPick/PickPallet")
    @CrossOrigin
    @Transactional
    @FlowAnnotation(title = "拣选入托盘完成", businessType = BusinessType.ADD_AND_LOSS)
    public InterReturn PickPallet(@RequestBody OutPick outPick, String palletCode) {
        //配盘表 状态已完成
        //原料箱 数量和已配盘数量减or删
        //订单箱 数量增 填入出库明细ID
        logger.info("进入 PickPallet");
        logger.info("接收到参数：" + palletCode + "和" + outPick);
        InterReturn interReturn = new InterReturn();
        try {
            if (palletCode == null || palletCode.length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("订单箱号为空!");
                System.out.println("订单箱号为空!");
                logger.info("PickPallet 返回：" + interReturn);
                return interReturn;
            }
            interReturn = PickPallet23(outPick);//拣货23 改配盘状态&更新原料箱
            if (!interReturn.isStatus()) {
                logger.info("PickPallet 返回：" + interReturn);
                return interReturn;
            }
            interReturn = PickPallet33(outPick, palletCode);//拣货33 装入订单箱
            if (!interReturn.isStatus()) {
                logger.info("PickPallet 返回：" + interReturn);
                return interReturn;
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("拣货出错：" + e.getCause());
            System.out.println("拣货出错：" + e.getMessage());
            logger.error("拣货出错：" + e.getMessage());
            logger.info("PickPallet 返回：" + interReturn);
            return interReturn;
        }
        interReturn.setStatus(true);
        interReturn.setMessage("拣货成功！");
        logger.info("PickPallet 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:徐浩铖
     * @Description 拣货23 改配盘状态&更新原料箱
     * @DateTime 2023/10/18 11:26
     * @Params
     * @Return
     */
    @ApiOperation(value = "拣货23 改配盘状态&更新原料箱", notes = "配盘信息")
    @PostMapping("/OutPick/PickPallet23")
    @CrossOrigin
    @Transactional
    @FlowAnnotation(title = "拣选23", businessType = BusinessType.LOSS)
    public InterReturn PickPallet23(@RequestBody OutPick outPick) {
        logger.info("进入 PickPallet23");
        logger.info("接收到参数：" + outPick);
        InterReturn interReturn = new InterReturn();
        try {
            //1.配盘表 状态已完成
            LambdaUpdateWrapper<OutPick> wrapper1 = new LambdaUpdateWrapper<>();
            wrapper1.eq(OutPick::getId, outPick.getId());//配盘id
            OutPick outPick1 = new OutPick();
            outPick1.setStatus("已完成");//状态
            boolean bool1 = outPickService.update(outPick1, wrapper1);
            if (bool1) {
                interReturn.setStatus(true);
                interReturn.setMessage("更新配盘表状态成功！");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("更新配盘表状态 数量为0！");
                logger.info("PickPallet23 返回：" + interReturn);
                return interReturn;
            }

            //2.原料箱 数量和已配盘数量减or删
            LambdaUpdateWrapper<Inventory> wrapper2 = new LambdaUpdateWrapper<>();
            wrapper2.eq(Inventory::getInventory_id, outPick.getInventory_id());//库存的id
            List<Inventory> inventoryList = inventoryService.list(wrapper2);
            if (inventoryList.size() == 0) {
                //原料箱无此条配盘数据
                interReturn.setStatus(false);
                interReturn.setMessage("原料箱无此条配盘物料数据");
                logger.info("PickPallet23 返回：" + interReturn);
                return interReturn;
            }

            if (inventoryList.get(0).getInventory_count().equals(outPick.getPick_qty())) {
                //2.1 相等 删除库存
                boolean bool21 = inventoryService.removeById(outPick.getInventory_id());//库存的id
                if (bool21) {
                    interReturn.setMessage("相等，删除原料箱库存成功！");
                    System.out.println("相等，删除原料箱库存成功！");
                    logger.info("相等，删除原料箱库存成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("相等，删除原料箱库存出错！");
                    logger.info("PickPallet23 返回：" + interReturn);
                    return interReturn;
                }
            } else {
                //2.2 不等 更新（减少库存和冻结）
                LambdaUpdateWrapper<Inventory> wrapper22 = new LambdaUpdateWrapper<>();
                wrapper22.eq(Inventory::getInventory_id, outPick.getInventory_id());//库存的id
                wrapper22.setSql("inventory_count=inventory_count-" + outPick.getPick_qty());//库存数量
                wrapper22.setSql("frozen_count=frozen_count-" + outPick.getPick_qty());//冻结数量
                wrapper22.set(Inventory::getUpdate_time, LocalDateTime.now());//更新时间
                boolean update22 = inventoryService.update(wrapper22);
                if (update22) {
                    interReturn.setMessage("不等，扣减原料箱库存成功！");
                    System.out.println("不等，扣减原料箱库存成功！");
                    logger.info("不等，扣减原料箱库存成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("不等，扣减原料箱库存成功！");
                    logger.info("PickPallet23 返回：" + interReturn);
                    return interReturn;
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("拣货23出错：" + e.getCause());
            System.out.println("拣货23出错：" + e.getMessage());
            logger.error("拣货23出错：" + e.getMessage());
            logger.info("PickPallet23 返回：" + interReturn);
            return interReturn;
        }
        interReturn.setStatus(true);
        interReturn.setMessage("拣货23成功！");
        logger.info("PickPallet23 返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:徐浩铖
     * @Description 拣货33 装入订单箱
     * @DateTime 2023/10/18 11:26
     * @Params
     * @Return
     */
    @ApiOperation(value = "拣货33 装入订单箱", notes = "配盘信息 装入的订单箱号")
    @PostMapping("/OutPick/PickPallet33")
    @CrossOrigin
    @Transactional
    @FlowAnnotation(title = "拣选33", businessType = BusinessType.ADD)
    public InterReturn PickPallet33(@RequestBody OutPick outPick, String palletCode) {
        logger.info("进入 PickPallet33");
        logger.info("接收到参数：" + palletCode + "和" + outPick);
        InterReturn interReturn = new InterReturn();
        try {
            //palletCode判空
            if (palletCode == null || "".equals(palletCode.trim()) || "noread".equalsIgnoreCase(palletCode.trim())) {
                interReturn.setStatus(false);
                interReturn.setMessage("装入订单箱出错,料箱号为空:" + palletCode);
                System.out.println("装入订单箱出错,料箱号为空:" + palletCode);
                logger.error("装入订单箱出错,料箱号为空:" + palletCode);
                return interReturn;
            }

            //3.订单箱 数量增 填入出库明细ID

            //查订单箱有无此物料
            LambdaUpdateWrapper<Inventory> wrapper3 = new LambdaUpdateWrapper<>();
            wrapper3.eq(Inventory::getPallet_code, palletCode);//载具编号 订单箱
            wrapper3.eq(Inventory::getMaterial_code, outPick.getMaterial_code());//物料编号
            wrapper3.eq(Inventory::getBatch, outPick.getBatch());//批次
            List<Inventory> inventoryListNew = inventoryService.list(wrapper3);
            if (inventoryListNew.size() == 0) {
                //3.1无 添加数据
                Inventory inventory = new Inventory();
                inventory.setPallet_code(palletCode);//载具编号 订单箱
                inventory.setCell_id("1/1");//载具格子号
                inventory.setMaterial_code(outPick.getMaterial_code());//物料编号
                inventory.setMaterial_name(outPick.getMaterial_name());//物料名称
                inventory.setBatch(outPick.getBatch());//批次
                inventory.setInventory_count(outPick.getPick_qty());//库存数量
                inventory.setFrozen_count(outPick.getPick_qty());//冻结数量
                inventory.setOut_order_id(outPick.getOut_order_id());//出库订单号
                inventory.setOut_order_detail_id(outPick.getOut_order_detail_id());//出库明细ID
                inventory.setCreator(outPick.getCreator());//创建人
                inventory.setCreate_time(Calendar.getInstance().getTime());//创建时间
                if ("执行中".equals(inventory.getStatus())) {
                    logger.info("此处库存状态执行中：" + inventory.getStatus());
                }
                boolean bool31 = inventoryService.save(inventory);
                if (bool31) {
                    interReturn.setMessage("无，订单箱新增成功！");
                    System.out.println("无，订单箱新增成功！");
                    logger.info("无，订单箱新增成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("无，订单箱添加出错！");
                    logger.info("PickPallet33 返回：" + interReturn);
                    return interReturn;
                }
            } else {
                //3.2有 更新数量
                LambdaUpdateWrapper<Inventory> wrapper32 = new LambdaUpdateWrapper<>();
                wrapper32.eq(Inventory::getInventory_id, inventoryListNew.get(0).getInventory_id());//库存id
                Inventory inventory = new Inventory();
                inventory.setUpdater(outPick.getCreator());//更新人取入参的创建人
                wrapper32.setSql("inventory_count=inventory_count+" + outPick.getPick_qty());//库存数量
                wrapper32.setSql("frozen_count=frozen_count+" + outPick.getPick_qty());//冻结数量
                boolean update32 = inventoryService.update(inventory, wrapper32);
                if (update32) {
                    interReturn.setMessage("有，订单箱更新成功！");
                    System.out.println("有，订单箱更新成功！");
                    logger.info("有，订单箱更新成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("订单箱更新出错！");
                    logger.info("PickPallet33 返回：" + interReturn);
                    return interReturn;
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("装入订单箱出错：" + e.getCause());
            System.out.println("装入订单箱出错：" + e.getMessage());
            logger.error("装入订单箱出错：" + e.getMessage());
            logger.info("PickPallet33 返回：" + interReturn);
            return interReturn;
        }
        interReturn.setStatus(true);
        interReturn.setMessage("装入订单箱成功！");
        logger.info("PickPallet33 返回：" + interReturn);
        return interReturn;
    }


    @ApiOperation(value = "拣货33 德玛落包使用", notes = "装完的信息")
    @PostMapping("/OutPick/PickPallet33forDM")
    @CrossOrigin
    @FlowAnnotation(title = "分拣机落包", businessType = BusinessType.ADD)
    public InterReturn PickPallet33forDM(@RequestBody OutPick outPick) {
        logger.info("进入 PickPallet33forDM");
        logger.info("接收到参数：" + outPick);
        String palletCode = outPick.getPallet_code();
        //     outPick.setPallet_code(null);//流水需要暂时注释
        return PickPallet33(outPick, palletCode);//给德马转换了一下
    }

    /**
     * @Author:徐浩铖
     * @Description 确认出库
     * @DateTime 2023/9/30 14:42
     * @Params
     * @Return
     */
    @ApiOperation(value = "确认出库", notes = "参数：托盘信息 操作：1明细出库数量增 2订单箱删")
    @PostMapping("/OutPick/Checkout")
    @CrossOrigin
    @Transactional
    @FlowAnnotation(title = "出库", businessType = BusinessType.LOSS)
    public InterReturn Checkout(@RequestBody Inventory inventory) {
        logger.info("进入 Checkout");
        logger.info("接收到参数：" + inventory);
        InterReturn interReturn = new InterReturn();
        try {
            if (inventory.getInventory_id() == null || inventory.getInventory_id() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("库存ID为空!");
                System.out.println("库存ID为空!");
                logger.info("Checkout 返回：" + interReturn);
                return interReturn;
            }
            if (inventory.getOut_order_detail_id() == null || inventory.getOut_order_detail_id() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("出库明细ID为空!");
                System.out.println("出库明细ID为空!");
                logger.info("Checkout 返回：" + interReturn);
                return interReturn;
            }

            //1.明细 出库数量增
            //判断出库明细中的出库数量和订单数量是否相同？
            // 相同的话，明细状态改为已完成；
            // 订单数量小于出库数量给出提示；
            // 订单数量大于出库数量 更新数量 明细状态（不变）为执行中
            LambdaQueryWrapper<OutOrderDetail> wrapperOutdetail = new LambdaQueryWrapper<>();
            wrapperOutdetail.eq(OutOrderDetail::getOut_order_detail_id, inventory.getOut_order_detail_id());//明细id
            List<OutOrderDetail> outOrderDetailList = outOrderDetailService.list(wrapperOutdetail);

            if (outOrderDetailList.get(0).getOrder_count() == outOrderDetailList.get(0).getActual_count() + inventory.getInventory_count()) {
                LambdaUpdateWrapper<OutOrderDetail> wrapperDetailUpdate = new LambdaUpdateWrapper<>();
                wrapperDetailUpdate.eq(OutOrderDetail::getOut_order_detail_id, inventory.getOut_order_detail_id());//明细id
                wrapperDetailUpdate.setSql("actual_count=actual_count+" + inventory.getInventory_count());//增量
                wrapperDetailUpdate.set(OutOrderDetail::getUpdater, inventory.getUpdater());
                wrapperDetailUpdate.set(OutOrderDetail::getStatus, "已完成");
                boolean boolDetailUpdate = outOrderDetailService.update(wrapperDetailUpdate);
                if (boolDetailUpdate) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("更新明细已出库数量成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("更新明细已出库数量为0！");
                    logger.info("Checkout 返回：" + interReturn);
                    return interReturn;
                }
            } else if (outOrderDetailList.get(0).getOrder_count() < outOrderDetailList.get(0).getActual_count() + inventory.getInventory_count()) {
                interReturn.setStatus(false);
                interReturn.setMessage("订单数量不能小于出库数量！");
                return interReturn;
            } else {
                LambdaUpdateWrapper<OutOrderDetail> wrapperDetailUpdate = new LambdaUpdateWrapper<>();
                wrapperDetailUpdate.eq(OutOrderDetail::getOut_order_detail_id, inventory.getOut_order_detail_id());//明细id
                wrapperDetailUpdate.setSql("actual_count=actual_count+" + inventory.getInventory_count());//增量
                wrapperDetailUpdate.set(OutOrderDetail::getUpdater, inventory.getUpdater());
                boolean boolDetailUpdate = outOrderDetailService.update(wrapperDetailUpdate);
                if (boolDetailUpdate) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("更新明细已出库数量成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("更新明细已出库数量为0！");
                    logger.info("Checkout 返回：" + interReturn);
                    return interReturn;
                }
            }

            //2.订单箱 删
            boolean boolDel = inventoryService.removeById(inventory.getInventory_id());
            if (boolDel) {
                interReturn.setStatus(true);
                interReturn.setMessage("删除订单箱明细成功！");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("删除订单箱明细数量为0！");
                logger.info("Checkout 返回：" + interReturn);
                return interReturn;
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("确认出库出错：" + e.getCause());
            System.out.println("确认出库出错：" + e.getMessage());
            logger.error("确认出库出错：" + e.getMessage());
            logger.info("Checkout 返回：" + interReturn);
            return interReturn;
        }
        interReturn.setStatus(true);
        interReturn.setMessage("确认出库成功！");
        logger.info("Checkout 返回：" + interReturn);
        return interReturn;
    }
}

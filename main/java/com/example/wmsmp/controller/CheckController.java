package com.example.wmsmp.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.*;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.mapper.CheckDetailMapper;
import com.example.wmsmp.service.*;
import com.example.wmsmp.util.OutHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Api(value = "123", tags = "库位盘点")
@RestController
@CrossOrigin
@Slf4j
public class CheckController {
    @Autowired
    private CheckService checkService;
    @Autowired
    private CheckDetailService checkDetailService;

    @Value("${check.up}")
    private String upApi;

    @Resource
    private CheckDetailMapper checkDetailMapper;

    @Resource
    private VMapInvService vMapInvService;

    @Autowired
    OutPickService outPickService;

    @Autowired
    InventoryService inventoryService;

    @ApiOperation(value = "模糊查询盘点表头", notes = "CheckBean类（盘点单号，盘点类型，盘点依据，盘点状态，核算科目） pageNo pageSize")
    @PostMapping("/check/page")
    @CrossOrigin
    public InterReturn GetOutOrderByFuzzy(@RequestBody CheckBean checkBean, int pageNo, int pageSize) {
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<CheckBean> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<CheckBean> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StrUtil.isNotBlank(checkBean.getCheckCode()), CheckBean::getCheckCode, checkBean.getCheckCode());//盘点单号
            wrapper.eq(StrUtil.isNotBlank(checkBean.getHeSuanKeMu()), CheckBean::getHeSuanKeMu, checkBean.getHeSuanKeMu());//核算科目
            wrapper.eq(StrUtil.isNotBlank(checkBean.getStatus()), CheckBean::getStatus, checkBean.getStatus());//盘点状态
            wrapper.like(StrUtil.isNotBlank(checkBean.getPanKuYiJu()), CheckBean::getPanKuYiJu, checkBean.getPanKuYiJu());//盘库依据
            wrapper.orderByDesc(CheckBean::getUpdateTime);

            List<CheckBean> outOrders = checkService.page(page, wrapper).getRecords();

            if (outOrders == null || outOrders.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找盘点单表头记录！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询盘点单表头成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(outOrders);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询盘点单表头出错：" + e.getCause());
            log.error("查询盘点单表头出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        return interReturn;
    }

    @ApiOperation(value = "更新盘点表头", notes = "根据主键更新用户 传CheckBean类")
    @PostMapping("/check/update")
    @CrossOrigin
    @Transactional
    public InterReturn UpdateCheckByCheckCode(@RequestBody CheckBean checkBean) {
        InterReturn interReturn = new InterReturn();
        try {
            if (StrUtil.isBlank(checkBean.getCheckCode())) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或盘点编号为空");
                log.error("参数异常或盘点主键为空");
                return interReturn;
            }
            if (StrUtil.isBlank(checkBean.getStatus())) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或盘点表头状态为空");
                System.out.println("参数异常或盘点表头状态为空");
                return interReturn;
            }
            if (StrUtil.isBlank(checkBean.getUpdater())) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或更新人为空");
                System.out.println("参数异常或更新人为空");
                return interReturn;
            }
            //校验是否创建明细
            LambdaQueryWrapper<CheckDetailBean> query = new LambdaQueryWrapper<>();
            query.eq(CheckDetailBean::getCheckCode, checkBean.getCheckCode());
            long count = checkDetailService.count(query);
            if (count == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("明细还未创建");
                return interReturn;
            }
            boolean bool = checkService.updateById(checkBean);
            //变更明细表状态
            LambdaUpdateWrapper<CheckDetailBean> update = new LambdaUpdateWrapper<>();
            update.eq(CheckDetailBean::getCheckCode, checkBean.getCheckCode());
            update.set(CheckDetailBean::getStatus, checkBean.getStatus());
            checkDetailService.update(update);
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
            log.error("更新出错：" + e.getMessage());
        }
        return interReturn;
    }

    /**
     * 盘点配盘
     *
     * @param checkBean
     * @param ports     出口
     * @return
     */
    @ApiOperation(value = "盘点配盘", notes = "盘点配盘 0表头→明细")
    @PostMapping("/check/pick")
    @CrossOrigin
    public InterReturn OrderPick(@RequestBody CheckBean checkBean, String[] ports) {
        InterReturn interReturn = new InterReturn();
        log.info("进入 出库配盘");
        log.info("接收到参数：" + checkBean);
        //数据校验
        if (ports.length == 0) {
            interReturn.setStatus(false);
            interReturn.setMessage("出库配盘出错：出库口集合为空！");
            return interReturn;
        }
        if (StrUtil.isBlank(checkBean.getCheckCode())) {
            interReturn.setStatus(false);
            interReturn.setMessage("盘点单号为空！");
            return interReturn;
        }
        if (!checkBean.getStatus().equals("盘点中")) {
            interReturn.setStatus(false);
            interReturn.setMessage("盘点配盘出错：" + checkBean.getCheckCode() + "单据状态为(" + checkBean.getStatus() + ")不符合配盘条件！");
            return interReturn;
        }
        //0.按表头查明细
        LambdaQueryWrapper<CheckDetailBean> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CheckDetailBean::getCheckCode, checkBean.getCheckCode());
        List<CheckDetailBean> detailBeanList = checkDetailService.list(queryWrapper);
        //将明组装为 出库明细
        List<OutOrderDetail> outOrderDetails = new ArrayList<>();
        for (CheckDetailBean detailBean : detailBeanList) {
            OutOrderDetail outOrderDetail = new OutOrderDetail();
            outOrderDetail.setOut_order_detail_id(detailBean.getCheckDetailId());
            outOrderDetail.setOut_order_id(detailBean.getCheckCode());
            outOrderDetail.setMaterial_code(detailBean.getMaterialCode());
            outOrderDetail.setMaterial_name(detailBean.getMaterialName());
            outOrderDetail.setBatch(detailBean.getBatch());
            outOrderDetail.setOrder_count(detailBean.getInventoryCount());
            outOrderDetail.setActual_count(detailBean.getActual_count());
            outOrderDetail.setPick_count(detailBean.getPick_count());
            outOrderDetails.add(outOrderDetail);
        }
        InterReturn interReturn1 = DetailPick(outOrderDetails, ports);//明细配盘
        if (interReturn1.isStatus()) {
            //修改盘点状态
            checkBean.setStatus("已配盘");
            checkBean.setUpdateTime(DateUtil.now());
            checkService.updateById(checkBean);
            interReturn.setStatus(true);
            interReturn.setMessage("批量更新出库表头状态成功！");
        } else {
            interReturn.setStatus(false);
            interReturn.setMessage("请确认盘点物料库存最近是否有变动！");
        }
        return interReturn;
    }


    public InterReturn DetailPick(List<OutOrderDetail> details, String[] ports) {
        InterReturn interReturn = new InterReturn();
        log.info("进入 DetailPick");
        log.info("接收到参数：" + details);
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
                        log.info("DetailPick 返回：" + interReturn);
                        return interReturn;
                    }
                    //2.单据配盘数&状态 update增 Status已配盘
                    LambdaUpdateWrapper<CheckDetailBean> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(CheckDetailBean::getCheckDetailId, item.getOut_order_detail_id());
                    wrapper.setSql("pick_count=pick_count+" + item.getPick_qty());//增量
                    wrapper.set(CheckDetailBean::getStatus, "已配盘");//出库单明细的状态改为已配盘
                    wrapper.set(CheckDetailBean::getUpdater, item.getUpdater());
                    wrapper.set(CheckDetailBean::getUpdateTime, LocalDateTime.now());
                    boolean bool = checkDetailService.update(wrapper);
                    if (bool) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("配盘" + item.getId() + "的单据更新成功");
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("配盘" + item.getId() + "的单据更新失败");
                        log.info("DetailPick 返回：" + interReturn);
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
                        log.info("DetailPick 返回：" + interReturn);
                        return interReturn;
                    }
                }
            } else {
                log.info("DetailPick 返回：" + interReturn);
                return interReturn;
            }
            interReturn.setMessage("配盘成功");

        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("盘点明细配盘出错：" + e.getCause());
            log.error("盘点明细配盘出错：" + e.getMessage());
        }
        log.info("DetailPick 返回：" + interReturn);
        return interReturn;
    }

    public InterReturn OutPickDetails(List<OutOrderDetail> details, String[] ports) {
        log.info("进入 OutPickDetails");
        log.info("接收到参数：" + details);
        InterReturn interReturn = new InterReturn();
        List<OutOrderDetail> newDetails = new ArrayList<>();//更新此次应配的数量
        try {
            if (details.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("出库配盘出错：参数异常or出库明细为空！");
                log.info("OutPickDetails 返回：" + interReturn);
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
                log.info("OutPickDetails 返回：" + interReturn);
                return interReturn;
            }

            //多条明细对多条库存
            interReturn = GetPickPalletResult(newDetails, ports);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("出库配盘出错：" + e.getCause());
            log.error("出库配盘出错：" + e.getMessage());
        }
        log.info("OutPickDetails 返回：" + interReturn);
        return interReturn;
    }

    private InterReturn GetPickPalletResult(List<OutOrderDetail> newDetails, String[] ports) {
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
                        returnIsEnough.setMessage("无此物料！\n物料编号：" + outOrderDetail.getMaterial_code() + "\n批次号：" + outOrderDetail.getBatch());
                        log.error("判断库存是否足够出错：" + returnIsEnough);
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
                            if (itemI.getInventory_count() - itemI.getFrozen_count() < itemO.getOrder_count()) {//库存-冻结<单据出库数量
                                returnIsEnough.setStatus(false);
                                returnIsEnough.setMessage("此物料不足\n物料编号：" + itemO.getMaterial_code() + "\n批次号：" + itemO.getBatch() + "\n需出：" + itemO.getOrder_count() + "\n可用：" + (itemI.getInventory_count() - itemI.getFrozen_count()));
                                log.error("判断库存是否足够出错：" + returnIsEnough);
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
                log.error("判断库存是否足够出错：" + e.getMessage());
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
            log.error("多对多配盘出错：" + e.getMessage());
        }
        interReturn.setStatus(true);
        interReturn.setMessage("多对多配盘成功");
        interReturn.setResult(outPickAll);
        return interReturn;
    }

    public List<OutOrderDetail> detailsGroup(List<OutOrderDetail> details) {
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
                .collect(Collectors.toMap(OutOrderDetail::getUdf01, a -> a, (o1, o2) -> {
                    o1.setOrder_count(o1.getOrder_count() + o2.getOrder_count());
                    return o1;
                })).values());
        return result;
    }

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
                .collect(Collectors.toMap(VMapInv::getUdf01, a -> a, (o1, o2) -> {
                    o1.setInventory_count(o1.getInventory_count() + o2.getInventory_count() - o1.getFrozen_count() - o2.getFrozen_count());//可用库存
                    return o1;
                })).values());
        return result;
    }


    /**
     * @Author:wlixun
     * @Description :编辑
     * @DateTime 2023/8/26 14:43
     * @Params
     * @Return
     */
    @ApiOperation(value = "编辑盘点单头接口")
    @PostMapping("/check/edit")
    @CrossOrigin
    public InterReturn EditCheck(@RequestBody CheckBean checkBean) {
        InterReturn interReturn = new InterReturn();
        try {
            if (StrUtil.isBlank(checkBean.getCheckCode())) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或盘点单号为空！");
                log.error("参数异常或盘点单号为空");
                return interReturn;
            } else {
                LambdaUpdateWrapper<CheckBean> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(CheckBean::getCheckCode, checkBean.getCheckCode());
                wrapper.set(StrUtil.isNotBlank(checkBean.getPanKuYiJu()), CheckBean::getPanKuYiJu, checkBean.getPanKuYiJu());
                wrapper.set(StrUtil.isNotBlank(checkBean.getStatus()), CheckBean::getStatus, checkBean.getStatus());
                wrapper.set(StrUtil.isNotBlank(checkBean.getHeSuanKeMu()), CheckBean::getHeSuanKeMu, checkBean.getHeSuanKeMu());
                wrapper.set(CheckBean::getUpdater, checkBean.getUpdater());
                wrapper.set(CheckBean::getUpdateTime, DateUtil.date());
                boolean bool = checkService.update(wrapper);
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
            log.error("更新出错：" + e.getMessage());
        }
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
    @PostMapping("/check/add")
    @CrossOrigin
    public InterReturn AddOutOrder(@RequestBody CheckBean checkBean) {
        InterReturn interReturn = new InterReturn();
        try {
            if (StrUtil.isBlank(checkBean.getHeSuanKeMu())) {
                interReturn.setStatus(false);
                interReturn.setMessage("单据核算科目为空");
                System.out.println("单据核算科目为空");
                return interReturn;
            } else if (StrUtil.isBlank(checkBean.getPanKuYiJu())) {
                interReturn.setStatus(false);
                interReturn.setMessage("盘库依据为空");
                System.out.println("盘库依据为空");
                return interReturn;
            }
            String checkCode = generateCode();
            checkBean.setCheckCode(checkCode);
            checkBean.setStatus("草稿");
            checkBean.setCreateTime(DateUtil.now());
            checkBean.setUpdateTime(DateUtil.now());
            checkBean.setType(2);
            Boolean boolSave = checkAdd(checkBean);
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
            log.error("新增出库单头出错：" + e.getMessage());
        }
        return interReturn;
    }

    @ApiOperation(value = "批量添加盘点表头", notes = "传CheckBean类List")
    @PostMapping("/check/Adds")
    @CrossOrigin
    @Transactional
    public InterReturn Adds(@RequestBody List<CheckBean> outOrders) {
        log.info("进入 Adds");
        log.info("接收到参数：出库表头列表：" + outOrders);
        InterReturn interReturn = new InterReturn();
        interReturn.setStatus(false);
        try {
            for (CheckBean outOrder : outOrders) {
                outOrder.setCreateTime(DateUtil.now());
                outOrder.setStatus("草稿");
                outOrder.setType(2);
                checkAdd(outOrder);
            }
            interReturn.setStatus(true);
            interReturn.setMessage("生成成功！");
            return interReturn;
        } catch (Exception ee) {
            interReturn.setMessage("生成出错：" + ee.getCause());
            return interReturn;
        }
    }


    @ApiOperation(value = "上游系统数据下发", notes = "传CheckBean类List")
    @PostMapping("/check/down/add")
    @CrossOrigin
    @Transactional
    public JSON downAdd(@RequestBody List<CheckBean> checkBeanList) {
        log.info("===上游系统数据下发====>" + JSONUtil.parseArray(checkBeanList));
        JSON callback = JSONUtil.createObj();
        if (!(checkBeanList.size() > 0)) {
            callback.putByPath("result", 1);
            callback.putByPath("info", "盘点数据为空");
            return callback;
        }
        try {
            //保存盘点表头
            for (CheckBean checkBean : checkBeanList) {
                checkBean.setCreateTime(DateUtil.now());
                checkBean.setStatus("草稿");
                checkBean.setCreator("上游下发");
                checkBean.setType(1);
                Set<String> warehouseList = new HashSet<>();
                warehouseList.add("P库");
                warehouseList.add("B库");
                warehouseList.add("H库");
                warehouseList.add("D库");
                warehouseList.add("X库");
                //判断库名是否合法
                if (!warehouseList.contains(checkBean.getWarehouseName())) {
                    log.error("未识别的库名:" + checkBean.getWarehouseName());
                    callback.putByPath("result", 1);
                    callback.putByPath("info", "未识别的库名:" + checkBean.getWarehouseName());
                    return callback;
                }
                if (checkAdd(checkBean)) {
                    //保存盘点明细
                    List<CheckDetailBean> data = checkBean.getData();
                    if (data.size() > 0) {
                        for (CheckDetailBean checkDetail : data) {
                            checkDetail.setCheckCode(checkBean.getCheckCode());
                            checkDetail.setStatus("草稿");
                            checkDetail.setActual_count(0);
                            checkDetail.setPick_count(0);
                            checkDetailService.save(checkDetail);
                        }
                    }
                }else{
                    callback.putByPath("result", 1);
                    callback.putByPath("info", "数据重复!");
                }
            }
            callback.putByPath("result", 0);
            callback.putByPath("info", null);
            return callback;
        } catch (Exception ee) {
            callback.putByPath("result", 1);
            callback.putByPath("info", ee);
            return callback;
        }
    }

    private Boolean checkAdd(CheckBean checkBean) {
        LambdaUpdateWrapper<CheckBean> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CheckBean::getCheckCode, checkBean.getCheckCode());
        long count = checkService.count(wrapper);
        if (count > 0) return false;
        checkService.save(checkBean);
        return true;
    }

    @ApiOperation(value = "上游系统数据上传", notes = "传CheckBean类List")
    @PostMapping("/check/up/send")
    @CrossOrigin
    @Transactional
    public InterReturn sendUp() {
        InterReturn interReturn = new InterReturn();
        interReturn.setStatus(false);
        //获取需要回传的数据 仅回传已完成未回传的数据
        LambdaUpdateWrapper<CheckBean> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CheckBean::getType, 1);
        wrapper.eq(CheckBean::getStatus, "已完成");
        List<CheckBean> checkBeanList = checkService.list(wrapper);
        if (checkBeanList.size() > 0) {
            //回传参数
            JSONArray array = JSONUtil.createArray();
            for (CheckBean bean : checkBeanList) {
                //转义需回传的数据
                Map<String, Object> callback = new HashMap<>();
                callback.put("panKuDanHao", bean.getCheckCode());
                //获取明细
                List<Map<String, Object>> detailCallback = new ArrayList<>();
                LambdaUpdateWrapper<CheckDetailBean> wrapperDetail = new LambdaUpdateWrapper<>();
                wrapperDetail.eq(CheckDetailBean::getCheckCode, bean.getCheckCode());
                List<CheckDetailBean> detailList = checkDetailService.list(wrapperDetail);
                for (CheckDetailBean detailBean : detailList) {
                    Map<String, Object> detailMap = new HashMap<>();
                    detailMap.put("qiCaiDaiMa", detailBean.getMaterialCode());
                    detailMap.put("qiCaiMingCheng", detailBean.getMaterialName());
                    detailMap.put("zhiLiangDengJi", detailBean.getZhiLiangDengJi());
                    detailMap.put("piCiHao", detailBean.getBatch());
                    detailMap.put("shiJiShuLiang", detailBean.getCheckCount());
                    detailCallback.add(detailMap);
                }
                callback.put("data", detailCallback);
                array.add(callback);
            }
            log.info("===盘点数据回传===>" + array);
            //回传数据
            try {
//                String post = HttpUtil.post(upApi, array.toString());
                String post = HttpUtil.post(upApi, array.toString(), 3000);
                JSONObject parse = JSONUtil.parseObj(post);
                if (parse.containsKey("returnMark") && parse.get("returnMark").equals("success")) {
                    for (CheckBean checkBean : checkBeanList) {
                        //修改单据为已回传  只有成功才回修改状态
                        LambdaUpdateWrapper<CheckBean> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.eq(CheckBean::getCheckCode, checkBean.getCheckCode());
                        updateWrapper.set(CheckBean::getType, 3);
                        checkService.update(updateWrapper);
                    }
                    interReturn.setStatus(true);
                    interReturn.setMessage("回传成功！");
                } else {
                    if (parse.containsKey("errorMsg")) {
                        interReturn.setMessage("回传失败:" + parse.get("errorMsg"));
                    }
                    interReturn.setStatus(false);
                }
            } catch (Exception e) {
                interReturn.setStatus(false);
                interReturn.setMessage("与上游系统网络断开！");
                throw new RuntimeException(e);
            }
        } else {
            interReturn.setStatus(false);
            interReturn.setMessage("不存在需回传的数据！");
        }
        return interReturn;
    }

    /**
     * @Author:徐浩铖
     * @Description :提交/申鹤单据，查找入库单头是否有对应的明细，有的话可以，无的话给出提示
     * @DateTime 2023/8/26 13:42
     * @Params
     * @Return
     */
//    @ApiOperation(value = "提交、审核单据", notes = "单据类的状态传待审核、已审核")
//    @PostMapping("/check/SubmitOutOrder")
//    @CrossOrigin
//    public InterReturn SubmitOutOrder(@RequestBody OutOrder outOrder) {
//        InterReturn interReturn = new InterReturn();
//        try {
//            String orderState = outOrder.getStatus();
//            if (!orderState.equals("待审核") && !orderState.equals("已审核")) {
//                interReturn.setStatus(false);
//                interReturn.setMessage("单据新状态异常：" + orderState);
//                return interReturn;
//            }
//            if (outOrder.getOut_order_id() == null) {
//                interReturn.setStatus(false);
//                interReturn.setMessage("参数异常或出库单号为空！");
//                System.out.println("参数异常或出库单号为空！");
//                return interReturn;
//            } else {
//                LambdaUpdateWrapper<OutOrderDetail> detailWrapper = new LambdaUpdateWrapper<>();
//                detailWrapper.eq(OutOrderDetail::getOut_order_id, outOrder.getOut_order_id());
//                List<OutOrderDetail> orderDetails = outOrderDetailService.list(detailWrapper);
//                if (orderDetails.size() == 0) {
//                    interReturn.setStatus(false);
//                    interReturn.setMessage("无出库单头明细，不能提交出库单头！");
//                } else {
//                    LambdaUpdateWrapper<OutOrder> wrapper = new LambdaUpdateWrapper<>();
//                    wrapper.eq(OutOrder::getOut_order_id, outOrder.getOut_order_id());
//                    wrapper.set(OutOrder::getStatus, orderState);
//                    wrapper.set(OutOrder::getUpdate_time, LocalDateTime.now());
//                    boolean bool = outOrderService.update(null, wrapper);
//                    if (bool) {
//                        interReturn.setStatus(true);
//                        interReturn.setMessage("出库单表头提交成功！");
//                    } else {
//                        interReturn.setStatus(false);
//                        interReturn.setMessage("出库单表头提交失败！");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            interReturn.setStatus(false);
//            interReturn.setMessage("出库单表头提交出错：" + e.getCause());
//            System.out.println("出库单表头提交出错：" + e.getMessage());
//            logger.error("出库单表头提交出错：" + e.getMessage());
//        }
////        logger.info("SubmitOutOrder 返回：" + interReturn);
//        return interReturn;
//    }


    /**
     * @Author:徐浩铖
     * @Description 删除出库单头接口，删除出库单头时对应的明细也需要删除
     * @DateTime 2023/8/26 14:05
     * @Params
     * @Return
     */
    @ApiOperation(value = "删除出库单表头 及对应明细", notes = "提交后的单据不可删除 仅编辑中可删")
    @PostMapping("/check/delete")
    @CrossOrigin
    @Transactional
    public InterReturn DelOutOrder(@RequestBody CheckBean checkBean) throws Exception {
        InterReturn interReturn = new InterReturn();
        try {
            if (StrUtil.isBlank(checkBean.getCheckCode())) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或盘点单号为空");
                System.out.println("参数异常或盘点单号为空");
                return interReturn;
            } else {
                LambdaQueryWrapper<CheckBean> wrapperCheck = new LambdaQueryWrapper<>();
                wrapperCheck.eq(CheckBean::getCheckCode, checkBean.getCheckCode());
                boolean bool = checkService.remove(wrapperCheck);
                if (bool) {
                    LambdaQueryWrapper<CheckDetailBean> wrapperDetail = new LambdaQueryWrapper<>();
                    wrapperDetail.eq(CheckDetailBean::getCheckCode, checkBean.getCheckCode());
                    boolean boolDetail = checkDetailService.remove(wrapperDetail);
                    interReturn.setStatus(true);
                    interReturn.setMessage("删除出库单表头成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("删除表头为0条");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("删除盘点单表头出错：" + e.getCause());
            log.error("删除盘点单头出错：" + e.getMessage());
        }
        return interReturn;
    }

    private Long code = 1L;

    /**
     * 生成盘点单号
     *
     * @return
     */
    private String generateCode() {
        String time = DateUtil.format(DateUtil.date(), "yyyyMMdd");
        if (!code.toString().startsWith(time)) {
            time = time + "0001";
            code = Long.parseLong(time);
        } else {
            code++;
        }
        return "CH" + code;
    }

}

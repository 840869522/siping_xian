package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.annotation.BusinessType;
import com.example.wmsmp.annotation.FlowAnnotation;
import com.example.wmsmp.entity.InOrder;
import com.example.wmsmp.entity.InOrderDetail;
import com.example.wmsmp.entity.Inventory;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.service.InOrderDetailService;
import com.example.wmsmp.service.InOrderService;
import com.example.wmsmp.service.InventoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Api(value = "123", tags = "库存")
@RestController
@CrossOrigin
public class InventoryController {
    @Autowired
    InventoryService inventoryService;

    @Autowired
    InOrderDetailService inOrderDetailService;

    @Autowired
    InOrderService inOrderService;
    private static Logger logger = Logger.getLogger(InventoryController.class);

    /**
     * @Author:lcy
     * @Description :条件查询库存
     * @DateTime 2023/7/10 10:57
     * @Params
     * @Return
     */
    @ApiOperation(value = "条件查询库存")
    @PostMapping("/Inventory/GetInventoryBy")
    @CrossOrigin
    public InterReturn GetInventoryBy(@RequestBody Inventory inventory, int pageNo, int pageSize, String startTime, String endTime) {
        // logger.info("进入 GetInventoryBy");
        // logger.info("接收到参数：" + inventory);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值

            //mybatisplus
            Page<Inventory> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(inventory.getMaterial_code() != null && !inventory.getMaterial_code().isEmpty(),
                    Inventory::getMaterial_code, inventory.getMaterial_code());

            wrapper.eq(inventory.getMaterial_name() != null && !inventory.getMaterial_name().isEmpty(),
                    Inventory::getMaterial_name, inventory.getMaterial_name());

            wrapper.eq(inventory.getStatus() != null,
                    Inventory::getStatus, inventory.getStatus());

            wrapper.eq(inventory.getInventory_id() != null,
                    Inventory::getInventory_id, inventory.getInventory_id());

            wrapper.eq(inventory.getPallet_code() != null && !inventory.getPallet_code().isEmpty(),
                    Inventory::getPallet_code, inventory.getPallet_code());

//            wrapper.eq(inventory.getLocation_code() != null && !inventory.getLocation_code().isEmpty(),
//                    Inventory::getLocation_code, inventory.getLocation_code());

            wrapper.eq(inventory.getBatch() != null && !inventory.getBatch().isEmpty(),
                    Inventory::getBatch, inventory.getBatch());
            wrapper.between(Inventory::getCreate_time, startTime, endTime);//时间在这两个时间中间
            wrapper.orderByDesc(Inventory::getUpdate_time);
            List<Inventory> inventorys = inventoryService.page(page, wrapper).getRecords();

            if (inventorys == null || inventorys.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找库存记录！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询库存记录成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(inventorys);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("条件查询库存出错：" + e.getCause());
            System.out.println("条件查询库存出错：" + e.getMessage());
            logger.error("条件查询库存出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        // logger.info("GetInventoryBy 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:ljy、lcy修改该方法名，删除其他多余参数,查询参数写死,后续要加5
     * @Description :托盘与物料绑定
     * @DateTime 2023/8/10 10:57
     * @Params
     * @Return
     */
//    @ApiOperation(value = "托盘与物料绑定")
//    @PostMapping("/Inventory/BindPalletAndMaterial")
//    @CrossOrigin
//    public InterReturn BindPalletAndMaterial(@RequestBody Inventory tInventory, String tempPallet_code) {
//        logger.info("进入BindPalletAndMaterial");
//        //String tempPallet_code 暂存的托盘号 ，tInventory.pallent_code收货的托盘号
//        logger.info("接收到参数" + tInventory);
//        InterReturn interReturn = new InterReturn();
//
//        try {
//            if (tInventory.getPallet_code() == null || tInventory.getPallet_code().length() == 0) {
//                interReturn.setStatus(false);
//                interReturn.setMessage("托盘号为空");
//                System.out.println("托盘号为空");
//                return interReturn;
//            } else if (tInventory.getMaterial_code() == null || tInventory.getMaterial_code().length() == 0) {
//                interReturn.setStatus(false);
//                interReturn.setMessage("物料编号为空");
//                System.out.println("物料编号为空");
//                return interReturn;
//            } else if (tInventory.getBatch() == null || tInventory.getBatch().length() == 0) {
//                interReturn.setStatus(false);
//                interReturn.setMessage("批次为空");
//                System.out.println("批次为空");
//                return interReturn;
//            }
//            int addnum;
//            int delnum;
//            addnum = tInventory.getInventory_count();   // 收货数量
//            delnum = tInventory.getInventory_count();
//            InterReturn interReturnGetInventory = GetInventoryBy(tInventory, 1, 100, "2000-12-30", "2999-12-30");
//            List<Inventory> inventorys = (List<Inventory>) ((ResReturn) interReturnGetInventory.getResult()).getAnything();
//            if (inventorys == null || inventorys.size() == 0) {
//                //首先判断数据库里有无收货的托盘的记录，如果无收货的托盘将该托盘记录插入到数据库中
//                interReturn = AddInventory(tInventory);
//
//            } else {
//                //有收货托盘记录的话，更新该条记录，数据库里该托盘的数量在原有的基础上加上收货的数量
//                tInventory.setInventory_count(inventorys.get(0).getInventory_count() + addnum);//数据库该托盘内物料数量+收货数量
//                tInventory.setUpdate_time(Calendar.getInstance().getTime());
//                tInventory.setPallet_code(inventorys.get(0).getPallet_code());
//                tInventory.setMaterial_code(inventorys.get(0).getMaterial_code());
//                tInventory.setBatch(inventorys.get(0).getBatch());
//                interReturn = UpdateInventoryByPalletNo(tInventory);
//            }
//            //原有的暂存区或者托盘上的数量减少
//            LambdaQueryWrapper<Inventory> wrapperInventory = new LambdaQueryWrapper<>();
//            wrapperInventory.eq(Inventory::getPallet_code, tempPallet_code);
//            wrapperInventory.eq(Inventory::getMaterial_code, tInventory.getMaterial_code());
//            wrapperInventory.eq(Inventory::getBatch, tInventory.getBatch());
//            List<Inventory> inventoryTemp = inventoryService.list(wrapperInventory);
//
//            //库存的数量小于收货的数量大于，不让操作
//            if (inventoryTemp.get(0).getInventory_count() < delnum) {
//                interReturn.setStatus(false);
//                interReturn.setMessage("数量大于库存数量，无法操作！");
//                return interReturn;
//            }
//            //库存的数量等于收货数量时，删除
//            else if (inventoryTemp.get(0).getInventory_count() == delnum) {
//                //库存减完的情况下，删除tempPallet_code托盘的库存记录
//                LambdaQueryWrapper<Inventory> wrapperdel = new LambdaQueryWrapper<>();
//                wrapperdel.eq(Inventory::getPallet_code, tempPallet_code);
//                wrapperdel.eq(Inventory::getMaterial_code, tInventory.getMaterial_code());
//                wrapperdel.eq(Inventory::getBatch, tInventory.getBatch());
//                List<Inventory> inventoryDel = inventoryService.list(wrapperInventory);
//                boolean bool = inventoryService.removeById(inventoryDel.get(0));
//                if (bool) {
//                    interReturn.setStatus(true);
//                } else {
//                    interReturn.setStatus(false);
//                }
//            } else {
//                //库存数量大于收货数量时 ，减少库存
//
//                tInventory.setInventory_count(inventoryTemp.get(0).getInventory_count() - delnum);
//                tInventory.setUpdate_time(Calendar.getInstance().getTime());
//                tInventory.setPallet_code(tempPallet_code);
//                tInventory.setMaterial_code(tInventory.getMaterial_code());
//                tInventory.setBatch(tInventory.getBatch());
//                interReturn = UpdateInventoryByPalletNo(tInventory);
//            }
//        } catch (Exception e) {
//            interReturn.setStatus(false);
//            interReturn.setMessage("托盘与物料绑定出错：" + e.getCause());
//            System.out.println("托盘与物料绑定出错：" + e.getMessage());
//            logger.error("托盘与物料绑定出错：" + e.getMessage());
//            return interReturn;
//        }
//        logger.info("BindPalletAndMaterial 返回：" + interReturn);
//        return interReturn;
//    }

    /**
     * @Author:ljy
     * @Description :新增库存 托盘二楼码垛完成使用、手动绑定使用
     * @DateTime 2023/8/10 10:57
     * @Params
     * @Return
     */
    @ApiOperation(value = "新增库存 (托盘二楼码垛完成使用、手动绑定使用)")
    @PostMapping("/Inventory/AddInventory")
    @CrossOrigin
    @FlowAnnotation(title = "无单收货", businessType = BusinessType.ADD)
    public InterReturn AddInventory(@RequestBody Inventory tInventory) {
        logger.info("进入AddInventory");
        logger.info("接收到参数" + tInventory);
        InterReturn interReturn = new InterReturn();
        try {
            if (tInventory.getPallet_code() == null || tInventory.getPallet_code().trim().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("托盘号为空");
                System.out.println("托盘号为空");
                return interReturn;
            }
            //校验inventory的cellid在列表中
            Set<String> CellIds = new HashSet<>();
            CellIds.add("1/1");
            CellIds.add("1/6");
            CellIds.add("2/6");
            CellIds.add("3/6");
            CellIds.add("4/6");
            CellIds.add("5/6");
            CellIds.add("6/6");
            if (!CellIds.contains(tInventory.getCell_id())) {
                interReturn.setStatus(false);
                interReturn.setMessage("格子号格式错误：" + tInventory.getCell_id());
                return interReturn;
            } else if (tInventory.getBatch() == null || tInventory.getBatch().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("批次号为空");
                System.out.println("批次号为空");
                return interReturn;
            } else if (tInventory.getMaterial_code() == null || tInventory.getMaterial_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("物料编号为空");
                System.out.println("物料编号为空");
                return interReturn;
            } else if (tInventory.getCreator() == null || tInventory.getCreator().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("创建人为空");
                System.out.println("创建人为空");
                return interReturn;
            } else if (tInventory.getInventory_count() == null || tInventory.getInventory_count() <= 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("新增物料数量为空或者0");
                System.out.println("新增物料数量为空或者0");
                return interReturn;
            }
            tInventory.setCreate_time(Calendar.getInstance().getTime());
            tInventory.setUpdate_time(Calendar.getInstance().getTime());
            if ("执行中".equals(tInventory.getStatus())) {
                logger.info("此处库存状态执行中：" + tInventory.getStatus());
            }
            if ("执行中".equals(tInventory.getStatus())) {
                logger.info("此处库存状态执行中：" + tInventory.getStatus());
            }
            boolean boolSave = inventoryService.save(tInventory);
            if (boolSave) {
                interReturn.setStatus(true);
                interReturn.setMessage("添加成功");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("添加库存物料数据条数为0");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("新增入库物料出错：" + e.getCause());
            System.out.println("新增入库单头出错：" + e.getMessage());
            logger.error("新增入库单头出错：" + e.getMessage());
        }
        logger.info("AddInventory返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:lcy
     * @Description：调整库存数量
     * @DateTime 2023/8/1 15:44
     * @Params
     * @Return
     */
    @ApiOperation(value = "更改库存数量", notes = "调整库存数量接口")
    @PostMapping("/Inventory/UpdateInventory")
    @CrossOrigin
    public InterReturn UpdateInventory(@RequestBody Inventory inventory) {
        logger.info("进入 UpdateInventory");
        logger.info("接收到参数：" + inventory);
        InterReturn interReturn = new InterReturn();
        try {
            if (inventory.getUpdater() == null || "".equals(inventory.getUpdater())) {
                interReturn.setStatus(false);
                interReturn.setMessage("操作员为空");
                System.out.println("操作员为空");
                return interReturn;
            }


            LambdaUpdateWrapper<Inventory> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(inventory.getPallet_code() != null, Inventory::getPallet_code, inventory.getPallet_code());
            wrapper.eq(inventory.getMaterial_name() != null, Inventory::getMaterial_name, inventory.getMaterial_name());
            wrapper.eq(inventory.getMaterial_code() != null, Inventory::getMaterial_code, inventory.getMaterial_code());
            wrapper.eq(inventory.getInventory_id() != null, Inventory::getInventory_id, inventory.getInventory_id());
            wrapper.set(inventory.getInventory_count() != null, Inventory::getInventory_count, inventory.getInventory_count());
            wrapper.set(Inventory::getUpdater, inventory.getUpdater());
            wrapper.set(Inventory::getUpdate_time, LocalDateTime.now());
            boolean bool = inventoryService.update(null, wrapper);
            if (bool) {
                interReturn.setStatus(true);
                interReturn.setMessage("更新成功");
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
        logger.info("UpdateInventory 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description :入库单头修改状态为收货中，入库明细修改收货数量，库存表如果有该库存（物料编码、批次一样）只是增加数量，没有库存记录的话增加新的记录，后续优化，状态也要更新
     * @DateTime 2023/8/31 14:51
     * @Params
     * @Return
     */
    @ApiOperation(value = "PDA收货")
    @PostMapping("/Inventory/AddReceiveMaterial")
    @CrossOrigin
    @Transactional
    @FlowAnnotation(title = "PDA收货", businessType = BusinessType.ADD)
    public InterReturn AddReceiveMaterial(@RequestBody Inventory inventory, String inOrderId) {
        logger.info("进入到AddReceiveMaterial方法");
        logger.info("接收到参数" + inOrderId + "和" + inventory);
        InterReturn interReturn = new InterReturn();

        if (inOrderId == null || inOrderId.length() == 0) {
            interReturn.setStatus(false);
            interReturn.setMessage("入库表头ID为空");
            System.out.println("入库表头ID为空");
            return interReturn;
        }
        if (inventory.getPallet_code() == null || inventory.getPallet_code().length() == 0) {
            interReturn.setStatus(false);
            interReturn.setMessage("载具号为空");
            System.out.println("载具号为空");
            return interReturn;
        }
        if (inventory.getCell_id() == null || inventory.getCell_id().length() == 0) {
            interReturn.setStatus(false);
            interReturn.setMessage("格子号为空");
            System.out.println("格子号为空");
            return interReturn;
        }
        if (inventory.getCell_id().contains("/")) {


        }
        if (inventory.getMaterial_code() == null || inventory.getMaterial_code().length() == 0) {
            interReturn.setStatus(false);
            interReturn.setMessage("物料编号为空");
            System.out.println("物料编号为空");
            return interReturn;
        }
        if (inventory.getInventory_count() == null || inventory.getInventory_count() <= 0) {
            interReturn.setStatus(false);
            interReturn.setMessage("数量异常");
            System.out.println("数量异常");
            return interReturn;
        }
        if (inventory.getUpdater() == null || "".equals(inventory.getUpdater())) {
            interReturn.setStatus(false);
            interReturn.setMessage("更新人不能为空");
            System.out.println("更新人不能为空");
            return interReturn;
        }
        try {
            LambdaQueryWrapper<InOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(InOrder::getIn_order_id, inOrderId);
            List<InOrder> inOrder = inOrderService.list(wrapper);
            if (inOrder.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("无此订单号：" + inOrderId);
                System.out.println("无此订单号：" + inOrderId);
                return interReturn;
            }

            //20240119  lcy:判断单头状态，防止未审核收货，只有单头为已审核或者收货中的状态时才能收货
            if ("已审核".equals(inOrder.get(0).getStatus()) || "收货中".equals(inOrder.get(0).getStatus())) {
                //通过入库单号、物料代码、批次 查找该条明细记录，取出订单数量
                LambdaQueryWrapper<InOrderDetail> wrapperIndetail = new LambdaQueryWrapper<>();
                wrapperIndetail.eq(InOrderDetail::getIn_order_id, inOrderId);
                wrapperIndetail.eq(InOrderDetail::getMaterial_code, inventory.getMaterial_code());
                wrapperIndetail.eq(InOrderDetail::getBatch, inventory.getBatch());
                List<InOrderDetail> inorderDetail = inOrderDetailService.list(wrapperIndetail);
                if (inorderDetail.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("订单号：" + inOrderId + "无此物料编号：" + inventory.getMaterial_code() + "批次：" + inventory.getBatch());
                    System.out.println("订单号：" + inOrderId + "无此物料编号：" + inventory.getMaterial_code() + "批次：" + inventory.getBatch());
                    return interReturn;
                }

                //1.判断入库明细的收货数量（已收货数量+本次收货数量）现在和订单数量是否相同？
                //1.1 订单数量=收货数量，明细状态改为已完成
                //1.2 订单数量<收货数量，给出提示
                //1.3 订单数量>收货数量，更新数量
                if (inorderDetail.get(0).getOrder_count() == inorderDetail.get(0).getActual_count() + inventory.getInventory_count()) {
                    //1.1 订单数量=收货数量，明细状态改为已完成
                    LambdaUpdateWrapper<InOrderDetail> wrapperDetail = new LambdaUpdateWrapper<>();
                    wrapperDetail.eq(InOrderDetail::getIn_order_id, inOrderId);
                    wrapperDetail.eq(InOrderDetail::getMaterial_code, inventory.getMaterial_code());
                    wrapperDetail.eq(InOrderDetail::getBatch, inventory.getBatch());
                    wrapperDetail.setSql("actual_count=actual_count+" + inventory.getInventory_count());//增量
                    wrapperDetail.set(InOrderDetail::getUpdate_time, LocalDateTime.now());
                    wrapperDetail.set(InOrderDetail::getUpdater, inventory.getUpdater());
                    wrapperDetail.set(InOrderDetail::getStatus, "已完成");
                    boolean update2 = inOrderDetailService.update(wrapperDetail);
                    if (update2) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("更新收货数量成功！");
                        interReturn.setResult(inventory);
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("更新收货数量0条！");
                        return interReturn;
                    }
                } else if (inorderDetail.get(0).getOrder_count() < inorderDetail.get(0).getActual_count() + inventory.getInventory_count()) {
                    //1.2 订单数量<收货数量，给出提示
                    interReturn.setStatus(false);
                    interReturn.setMessage("收货数量大于订单数量，请检查！");
                    return interReturn;
                } else {
                    //1.3 订单数量>收货数量，更新数量
                    LambdaUpdateWrapper<InOrderDetail> wrapperDetail = new LambdaUpdateWrapper<>();
                    wrapperDetail.eq(InOrderDetail::getIn_order_id, inOrderId);
                    wrapperDetail.eq(InOrderDetail::getMaterial_code, inventory.getMaterial_code());
                    wrapperDetail.eq(InOrderDetail::getBatch, inventory.getBatch());
                    wrapperDetail.setSql("actual_count=actual_count+" + inventory.getInventory_count());//增量
                    //wrapperDetail.set(InOrderDetail::getStatus, "执行中");//无需更新 初始化时为"执行中"
                    wrapperDetail.set(InOrderDetail::getUpdate_time, LocalDateTime.now());
                    wrapperDetail.set(InOrderDetail::getUpdater, inventory.getUpdater());
                    boolean update3 = inOrderDetailService.update(wrapperDetail);
                    if (update3) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("更新明细收货数量成功！");
                        interReturn.setResult(inventory);
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("更新收货数量0条！");
                        return interReturn;
                    }
                }

                //2.1库存表查找此托盘内是否有同种物料（物料编码和批次一样），有的话库存数量增加
                LambdaQueryWrapper<Inventory> wrapperInventory = new LambdaQueryWrapper<>();
                wrapperInventory.eq(Inventory::getPallet_code, inventory.getPallet_code());
                wrapperInventory.eq(Inventory::getMaterial_code, inventory.getMaterial_code());
                wrapperInventory.eq(Inventory::getBatch, inventory.getBatch());
                List<Inventory> inventorys = inventoryService.list(wrapperInventory);
                if (inventorys.size() > 0) {
                    int addNum = inventorys.get(0).getInventory_count();
                    addNum += inventory.getInventory_count();
                    LambdaUpdateWrapper<Inventory> wrapperadd = new LambdaUpdateWrapper<>();
                    wrapperadd.eq(Inventory::getPallet_code, inventory.getPallet_code());
                    wrapperadd.eq(Inventory::getMaterial_code, inventory.getMaterial_code());
                    wrapperadd.eq(Inventory::getBatch, inventory.getBatch());
                    wrapperadd.set(Inventory::getInventory_count, addNum);
                    wrapperadd.set(Inventory::getUpdater, inventory.getUpdater());
                    wrapperadd.set(Inventory::getUpdate_time, LocalDateTime.now());
                    boolean add = inventoryService.update(wrapperadd);
                    if (add) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("库存数量更新成功！");
                        interReturn.setResult(inventory);
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("库存数量更新出错！");
                        return interReturn;
                    }
                }
                //2.2没有则插入记录
                else {
                    if ("执行中".equals(inventory.getStatus())) {
                        logger.info("此处库存状态执行中：" + inventory.getStatus());
                    }
                    boolean boolSave = inventoryService.save(inventory);
                    if (boolSave) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("添加库存成功！");
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("添加库存失败！");
                        return interReturn;
                    }
                }

                //3.查找所有明细状态查看是否为已完成，全部为已完成修改入库单头状态为已完成；明细状态存在执行中或者收货中的话修改入库单头状态为收货中
                LambdaQueryWrapper<InOrderDetail> wrapperInorderStatu = new LambdaQueryWrapper<>();
                wrapperInorderStatu.eq(InOrderDetail::getIn_order_id, inOrderId);
                List<InOrderDetail> inorderDetails = inOrderDetailService.list(wrapperInorderStatu);
                LambdaUpdateWrapper<InOrder> wrapperFinish = new LambdaUpdateWrapper<>();
                //查找list里是否存在状态为执行中或者收货中，如果存在则入库单头状态为收货中，不存在则为已完成
                boolean isStatu = inorderDetails.stream().anyMatch(item -> (item.getStatus().equals("执行中") || item.getStatus().equals("收货中")));
                if (isStatu) {
                    wrapperFinish.eq(InOrder::getIn_order_id, inOrderId);
                    wrapperFinish.set(InOrder::getStatus, "收货中");
                } else {
                    wrapperFinish.eq(InOrder::getIn_order_id, inOrderId);
                    wrapperFinish.set(InOrder::getStatus, "已完成");
                }
                boolean finish = inOrderService.update(wrapperFinish);
                if (finish) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("更新单头状态成功！");
                    interReturn.setResult(inventory);
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("更新单头状态出错！");
                    return interReturn;
                }
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("订单方未审核，当前为状态为：" + inOrder.get(0).getStatus());
                System.out.println("订单方未审核，当前为状态为：" + inOrder.get(0).getStatus());
                return interReturn;
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("收货出错：" + e.getCause());
            System.out.println("收货出错：" + e.getMessage());
            logger.error("收货出错：" + e.getMessage());
        }
//        interReturn.setStatus(true);
//        interReturn.setMessage("收货成功！");
        logger.info("AddReceiveMaterial 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:lcy
     * @Description :组托，未考虑事务，后续优化,String palletCodeOld原托盘号, inventory里是目标托盘号
     * palletCodeOld→放入inventory里
     * @DateTime 2023/9/28 14:34
     * @Params
     * @Return
     */
    @ApiOperation(value = "组托/拆托", notes = "新库存类，原托盘，原格子号")
    @PostMapping("/Inventory/BindPallet")
    @CrossOrigin
    @Transactional
    @FlowAnnotation(title = "组托/拆托", businessType = BusinessType.ADD_AND_LOSS)
    public InterReturn BindPallet(@RequestBody Inventory inventory, String palletCodeOld, String cellIdOld) {
        logger.info("进入到BindPallet方法");
        logger.info("接收到参数原托盘：" + palletCodeOld + "原格子号" + cellIdOld + "目标托盘：" + inventory);
        InterReturn interReturn = new InterReturn();
        //0.参数校验
        //1.查询原托盘的数量,如果输入的数量比原托盘少的话，原托盘减少；如果输入的数量等于原托盘，原托盘删除；大于暂存原托盘给出提示
        //2.查找目标托盘是否有此料，有的话，托盘数量增加更新，无的话托盘信息插入数量增加
        try {
            //0.参数校验
            {
                //0.1校验原托盘号palletCode非空
                if (palletCodeOld == null || "".equals(palletCodeOld)) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("原托盘号不能为空！");
                    return interReturn;
                }
                //0.2校验inventory的托盘号非空
                if (inventory.getPallet_code() == null || "".equals(inventory.getPallet_code())) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("目标托盘号不能为空！");
                    return interReturn;
                }
                //0.3校验inventory的物料编号非空
                if (inventory.getMaterial_code() == null || "".equals(inventory.getMaterial_code())) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("物料编号不能为空！");
                    return interReturn;
                }
                //0.4校验inventory的批次非空
                if (inventory.getBatch() == null || "".equals(inventory.getBatch())) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("批次不能为空！");
                    return interReturn;
                }
                //0.5校验inventory的数量非0
                if (inventory.getInventory_count() == null || inventory.getInventory_count() <= 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("数量异常！");
                    return interReturn;
                }
                //0.6校验inventory的更新人非空
                if (inventory.getUpdater() == null || "".equals(inventory.getUpdater())) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("更新人不能为空！");
                    return interReturn;
                }

                //0.7校验inventory的cellid在列表中
                Set<String> CellIds = new HashSet<>();

                CellIds.add("1/1");
                CellIds.add("1/6");
                CellIds.add("2/6");
                CellIds.add("3/6");
                CellIds.add("4/6");
                CellIds.add("5/6");
                CellIds.add("6/6");

                if (!CellIds.contains(inventory.getCell_id())) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("新格子号格式错误：" + inventory.getCell_id());
                    return interReturn;
                }
                //0.8校验新的cellid在列表中
                if (!CellIds.contains(cellIdOld)) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("旧格子号格式错误：" + cellIdOld);
                    return interReturn;
                }
            }

            //1.查询原托盘的数量,如果输入的数量比原托盘少的话，原托盘减少；如果输入的数量等于原托盘，原托盘删除；大于暂存原托盘给出提示
            LambdaQueryWrapper<Inventory> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Inventory::getPallet_code, palletCodeOld);
            lambdaQueryWrapper.eq(Inventory::getCell_id, cellIdOld);
            lambdaQueryWrapper.eq(Inventory::getMaterial_code, inventory.getMaterial_code());
            lambdaQueryWrapper.eq(Inventory::getBatch, inventory.getBatch());
            List<Inventory> inventorys = inventoryService.list(lambdaQueryWrapper);
            if (inventorys.size() > 0) {
                //原来的数量
                int areaNum = inventorys.get(0).getInventory_count();
                //输入的数量比原托盘数量少
                if (inventory.getInventory_count() < areaNum) {
                    LambdaUpdateWrapper<Inventory> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                    lambdaUpdateWrapper.eq(Inventory::getPallet_code, palletCodeOld);
                    lambdaQueryWrapper.eq(Inventory::getCell_id, cellIdOld);
                    lambdaUpdateWrapper.eq(Inventory::getMaterial_code, inventory.getMaterial_code());
                    lambdaUpdateWrapper.set(Inventory::getInventory_count, areaNum - inventory.getInventory_count());
                    lambdaUpdateWrapper.set(Inventory::getUpdater, inventory.getUpdater());
                    lambdaUpdateWrapper.set(Inventory::getUpdate_time, LocalDateTime.now());
                    boolean update = inventoryService.update(lambdaUpdateWrapper);
                    if (update) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("更新成功 原料箱数量减少");
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("更新0条!");
                        return interReturn;
                    }
                } else if (inventory.getInventory_count() == areaNum) {
                    boolean bool = inventoryService.removeById(inventorys.get(0));
                    if (bool) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("删除成功！");
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("删除数据条数为0");
                        return interReturn;
                    }
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("输入数量(" + inventory.getInventory_count() + ")不得大于现有数量(" + areaNum + ")");
                    return interReturn;
                }
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage(palletCodeOld + "中未绑定此物料");
                return interReturn;
            }

            //2.查找目标托盘是否有此料，有的话，托盘数量增加更新，无的话托盘信息插入数量增加
            LambdaUpdateWrapper<Inventory> pallentWrapper = new LambdaUpdateWrapper<>();
            pallentWrapper.eq(Inventory::getPallet_code, inventory.getPallet_code());
            pallentWrapper.eq(Inventory::getCell_id, inventory.getCell_id());
            pallentWrapper.eq(Inventory::getMaterial_code, inventory.getMaterial_code());
            pallentWrapper.eq(Inventory::getBatch, inventory.getBatch());
            List<Inventory> inventorysTemp = inventoryService.list(pallentWrapper);
            if (inventorysTemp.size() > 0) {
                int pallentNum = inventorysTemp.get(0).getInventory_count();
                //托盘物料数量增加
                pallentNum = pallentNum + inventory.getInventory_count();
                LambdaUpdateWrapper<Inventory> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(Inventory::getPallet_code, inventory.getPallet_code());
                wrapper.eq(Inventory::getMaterial_code, inventory.getMaterial_code());
                wrapper.set(Inventory::getInventory_count, pallentNum);
                wrapper.set(Inventory::getUpdater, inventory.getUpdater());
                wrapper.set(Inventory::getUpdate_time, LocalDateTime.now());
                boolean update = inventoryService.update(wrapper);
                if (update) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("托盘区数量更新成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("托盘区数量更新0条");
                    return interReturn;
                }
            } else {
                boolean boolSave = inventoryService.save(inventory);
                if (boolSave) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("添加库存数据成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("添加库存数据失败！");
                    return interReturn;
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("组托/拆托出错：" + e.getCause());
            System.out.println("组托/拆托出错：" + e.getMessage());
            logger.error("组托/拆托出错：" + e.getMessage());
            return interReturn;
        }
        interReturn.setStatus(true);
        interReturn.setMessage("组托/拆托成功！");
        logger.info("BindPallet返回：" + interReturn);
        return interReturn;
    }
}


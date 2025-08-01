package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.*;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.entity.task.D.TaskD;
import com.example.wmsmp.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Api(value = "123", tags = "库存预警")
@RestController
@CrossOrigin
public class InvenMaterialLocationmapController {
    Map<Integer, Integer> map = new ConcurrentHashMap<>();//键：层数 值：数量
    @Autowired
    InvenMaterialLocationmapService invenMaterialLocationmapService;

    @Autowired
    LocationMapService locationMapService;

    @Autowired
    VMapInvService vMapInvService;

    @Autowired
    InventoryService inventoryService;

    @Autowired
    VSelectDplocationService vSelectDplocationService;

    @Autowired
    TaskDService taskDService;

    private static Logger logger = Logger.getLogger(InvenMaterialLocationmapController.class);

    /**
     * @Author:lcy
     * @Description:库存预警
     * @DateTime 2023/8/21 14:59
     * @Params
     * @Return
     */
    @ApiOperation(value = "库存预警")
    @PostMapping("/InvenMaterialLocationmap/GetInvenMaterialLocationmapBy")
    @CrossOrigin
    public InterReturn GetInvenMaterialLocationmapBy(@RequestBody InvenMaterialLocationmap invenMaterialLocationmap, int pageNo, int pageSize, String startTime, String endTime) {
        logger.info("进入 GetInvenMaterialLocationmapBy");
        logger.info("接收到参数：" + invenMaterialLocationmap);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            //mybatisplus
            Page<InvenMaterialLocationmap> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<InvenMaterialLocationmap> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(invenMaterialLocationmap.getMaterial_code() != null && !invenMaterialLocationmap.getMaterial_code().isEmpty(),
                    InvenMaterialLocationmap::getMaterial_code, invenMaterialLocationmap.getMaterial_code());
            wrapper.eq(invenMaterialLocationmap.getMaterial_name() != null && !invenMaterialLocationmap.getMaterial_name().isEmpty(),
                    InvenMaterialLocationmap::getMaterial_name, invenMaterialLocationmap.getMaterial_name());
            wrapper.eq(invenMaterialLocationmap.getStatus() != null,
                    InvenMaterialLocationmap::getStatus, invenMaterialLocationmap.getStatus());

            wrapper.eq(invenMaterialLocationmap.getPallet_code() != null,
                    InvenMaterialLocationmap::getPallet_code, invenMaterialLocationmap.getPallet_code());

            wrapper.eq(invenMaterialLocationmap.getLocation_code() != null && !invenMaterialLocationmap.getLocation_code().isEmpty(),
                    InvenMaterialLocationmap::getLocation_code, invenMaterialLocationmap.getLocation_code());
            wrapper.between(InvenMaterialLocationmap::getCreate_time, startTime, endTime);//时间在这两个时间中间
            List<InvenMaterialLocationmap> inventorys = invenMaterialLocationmapService.page(page, wrapper).getRecords();

            if (inventorys == null || inventorys.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("位找到记录！");
            } else {
                for (InvenMaterialLocationmap inventory : inventorys) {
                    //获取当前订单日期
                    LocalDate currentData = LocalDate.now();
                    String batch = inventory.getBatch();
                    StringBuilder batchTemp = new StringBuilder(batch);
                    batchTemp.insert(4, "-");
                    batchTemp.insert(7, "-");

                    //当前日期和批次时间的日期差减去保质期的时间，算出是否过期日期
                    long finaTime = (currentData.toEpochDay() - (LocalDate.parse(batchTemp).toEpochDay())) - inventory.getQuality();

                    if (finaTime >= 10) {
                        inventory.setStatus("已过期");
                    } else if (finaTime > 0) {
                        inventory.setStatus("临期");
                    } else {
                        inventory.setStatus("正常");
                    }
                }
                interReturn.setStatus(true);
                interReturn.setMessage("查询记录成功！");
            }
            resReturn.setAnything(inventorys);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("条件查询出错：" + e.getCause());
            System.out.println("条件查询出错：" + e.getMessage());
            logger.error("条件查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);
        logger.info("GetInventoryLocationBy 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 联查
     * @DateTime 2024/7/25 12:02
     * @Params
     * @Return
     */
    @ApiOperation(value = "联查", notes = "库名、物料编号、物料名称、批次、托盘编号、库位编号")
    @PostMapping("/InvenMaterialLocationmap/GetBy")
    @CrossOrigin
    public InterReturn GetBy(@RequestBody VMapInv item, int pageNo, int pageSize) {
//        logger.info("进入 GetBy");
//        logger.info("接收到参数：" + item);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            //mybatisplus
            Page<VMapInv> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<VMapInv> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(item.getWarehouse() != null && !item.getWarehouse().isEmpty(),
                    VMapInv::getWarehouse, item.getWarehouse());//库名
            wrapper.eq(item.getMaterial_code() != null && !item.getMaterial_code().isEmpty(),
                    VMapInv::getMaterial_code, item.getMaterial_code());//物料编号
            wrapper.eq(item.getMaterial_name() != null && !item.getMaterial_name().isEmpty(),
                    VMapInv::getMaterial_name, item.getMaterial_name());//物料名称
            wrapper.eq(item.getBatch() != null && !item.getBatch().isEmpty(),
                    VMapInv::getBatch, item.getBatch());//批次
            wrapper.eq(item.getPallet_code() != null,
                    VMapInv::getPallet_code, item.getPallet_code());//托盘编号
            wrapper.eq(item.getLocation_code() != null && !item.getLocation_code().isEmpty(),
                    VMapInv::getLocation_code, item.getLocation_code());//库位编号
            List<VMapInv> inventorys = vMapInvService.page(page, wrapper).getRecords();
            if (inventorys == null || inventorys.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到库存记录！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询到库存记录！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(inventorys);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("条件查询出错：" + e.getCause());
            System.out.println("条件查询出错：" + e.getMessage());
            logger.error("条件查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);
        //  logger.info("GetBy 返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:xhc
     * @Description 按库名获取空托盘
     * @DateTime 2024/7/26 20:19
     * @Params
     * @Return
     */
    @ApiOperation(value = "按库名获取空托盘", notes = "库名")
    @PostMapping("/InvenMaterialLocationmap/GetEmptyPalletByWarehouse")
    @CrossOrigin
    public InterReturn GetEmptyPalletByWarehouse(@RequestBody VMapInv item, int pageNo, int pageSize) {
//        logger.info("进入 GetEmptyPalletByWarehouse");
//        logger.info("接收到参数：" + item);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            //判断库名为空
            if (item.getWarehouse() == null || item.getWarehouse().isEmpty()) {
                interReturn.setStatus(false);
                interReturn.setMessage("库名为空请检查！");
                return interReturn;
            }
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            //mybatisplus
            Page<VMapInv> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<VMapInv> wrapper = new LambdaQueryWrapper<>();

            wrapper.eq(VMapInv::getWarehouse, item.getWarehouse());//库名
            wrapper.isNull(VMapInv::getMaterial_code);//物料编号 为空
            wrapper.isNotNull(VMapInv::getPallet_code);//托盘编号 不为空
            wrapper.eq(VMapInv::getStatus, "启用");//状态 启用
            wrapper.orderByAsc(VMapInv::getLocation_area);//组号 顺序
            List<VMapInv> inventorys = vMapInvService.page(page, wrapper).getRecords();
            if (inventorys == null || inventorys.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到库存记录！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询到库存记录！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(inventorys);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("GetEmptyPalletByWarehouse条件查询出错：" + e.getCause());
            System.out.println("GetEmptyPalletByWarehouse条件查询出错：" + e.getMessage());
            logger.error("GetEmptyPalletByWarehouse条件查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);
//        logger.info("GetEmptyPalletByWarehouse 返回：" + interReturn);
        return interReturn;
    }




    //-------------上官青云---------------








    ///sgqy  大件库库位分配  根据托盘号分配一个合适的库位信息
    /**
     * @Author:sgqy
     * @Description 大件库分配一个入库位置
     * @DateTime 2024/7/27 9:20
     * @Params
     * @Return
     */
    @ApiOperation(value = "大件库分配库位", notes = "托盘编号")
    @PostMapping("/InvenMaterialLocationmap/GetLocationmap")
    @CrossOrigin
    public InterReturn GetLocationmap(String pallet_code) {
        InterReturn interReturn = new InterReturn();
        //根据托盘编号判断是否空托盘或绑定入库   1.空托盘按从左到右分配   2.绑定入库找到托盘绑定的物料信息查询，在同一个列未满就选择这个列，满的话就选择空列进行分配
        try {
            interReturn = GetMaterial(pallet_code);
            if (interReturn.isStatus()) {
                interReturn.setMessage(null);
            }
        } catch (Exception x) {
            logger.error("大件库自动分配异常：" + x);
        }
        return interReturn;
    }

    /**
     * 遍历所有层找到合适库位
     * @return
     */
    private InterReturn GetLocationCode() {
        InterReturn interReturn;
        interReturn = GetTaskDs();
        if (!interReturn.isStatus()) {
            return interReturn;
        }
//        for (int i = 0; i < map.size(); i++) {
        //通过层数拿到库位号
        interReturn = GetLocationmap((Object[]) interReturn.getResult());
        if (interReturn.isStatus() && interReturn.getResult() != null) {
            return interReturn;
        }
//        }
        return interReturn;
    }

    /**
     * 查询每层存在的任务数量
     * @return  可以下任务的层数
     */
    private InterReturn GetTaskDs(){
        InterReturn interReturn = new InterReturn();
//        Map<Integer,Integer> map = new ConcurrentHashMap<>();//键：层数 值：数量
        try {
            LambdaQueryWrapper<TaskD> wrapper = new LambdaQueryWrapper<>();
            wrapper.notIn(TaskD::getStatus, Arrays.asList("已完成", "已取消"));//任务状态
            //需要按时间查询时 放开此行              wrapper.between(TaskD::getCreate_time, startTime, endTime);//时间在这两个时间中间
            wrapper.orderByDesc(TaskD::getCreate_time);//倒序
            List<TaskD> taskDList = taskDService.list(wrapper);
            if (taskDList == null || taskDList.size() == 0) {
                interReturn.setStatus(true);
                interReturn.setResult("1");
                return interReturn;
            } else {
                interReturn.setStatus(true);
                for (int i = 1; i < 5; i++) {
                    //先将所有层添加入集合
                    map.put(i,0);
                    for (TaskD taskD :taskDList) {
                        //若任务层数和遍历层数一致需要对每层的数量加1
                        if(taskD.getLocation_code().substring(taskD.getLocation_code().length()-1).equals(String.valueOf(i))){
                            map.put(i,map.get(i).intValue()+1);
                        }
                    }
                }
                interReturn.setResult(map);
                //计算出最少任务  将任务数量从小到达排序
                Collection collection = map.values();
                Object[] objects = collection.toArray();
                Arrays.sort(objects);
                interReturn.setResult(objects);
                return interReturn;
//                //遍历集合拿到层数
//                for (Integer key :map.keySet()) {
//                    int val = map.get(key);
//                    if(val == Integer.parseInt(objects[0].toString()) && Integer.parseInt(objects[0].toString()) >= 0){
//                        interReturn.setResult(key);
//                        return interReturn;
//                    }
//                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询TaskD任务出错：" + e.getCause());
            logger.error("查询TaskD任务出错：" + e.getMessage());
        }
        return interReturn;
    }

    /**
     * 根据托盘编号查找同类型货位
     * @param pallet_code
     * @return
     */
    private InterReturn GetMaterial(String pallet_code) {
        InterReturn interReturn = new InterReturn();
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getPallet_code, pallet_code);
        List<Inventory> inventories = inventoryService.list(wrapper);
        if (inventories == null || inventories.size() == 0) {
            interReturn.setMessage("未找到GetMaterial信息！");
            //拿到任务最少的层数
            interReturn = GetTaskDs();
            if(!interReturn.isStatus()){
                return interReturn;
            }
            //根据层数获取合适库位
            interReturn = GetLocationmap((Object[]) interReturn.getResult());
            if (interReturn.isStatus() && interReturn.getResult() == null) {
                interReturn = GetLocationCode();
            }
            return interReturn;
        } else {
            interReturn.setStatus(true);
            //根据托盘编号查找到的库存信息，再寻找所有相同物料库存信息
            LambdaQueryWrapper<VSelectDplocation> vMapInvLambdaQueryWrapper = new LambdaQueryWrapper<>();
            vMapInvLambdaQueryWrapper.eq(VSelectDplocation::getMaterial_code, inventories.get(0).getMaterial_code());
            vMapInvLambdaQueryWrapper.eq(VSelectDplocation::getBatch, inventories.get(0).getBatch());
            List<VSelectDplocation> vSelectDplocations = vSelectDplocationService.list(vMapInvLambdaQueryWrapper);
            if(vSelectDplocations != null &&vSelectDplocations.size()>0){
                interReturn.setResult(vSelectDplocations.get(0));
                return interReturn;
            }
            //每个相同的都没前深位库位那必须的分配一个最内则的深度库位
            interReturn = GetTaskDs();
            if (!interReturn.isStatus()) {
                return interReturn;
            }
            //通过层数拿到库位号
            interReturn = GetLocationmap((Object[]) interReturn.getResult());
        }
        return interReturn;
    }

    /**
     * 根据库位编号获得一个合适的库位
     * @param location_code  从那个库位开始往后寻找
     * @param location_area  从那个库位开始往后寻找
     * @return  合适的库位
     */
    private InterReturn GetLocationmapBase(String location_code,String location_area){
        InterReturn interReturn = null;
        //获取最后一个深度
        int area = Integer.parseInt(location_area.substring(location_area.length()-1));
        for (int i = area; i >0 ; i = i-2) {
            interReturn = new InterReturn();
            LambdaQueryWrapper<LocationMap> wrapperLocationMap = new LambdaQueryWrapper<>();
            //拿到需要寻找的下一个前深位的库位
            wrapperLocationMap.eq(LocationMap::getLocation_area, location_area.substring(0,location_area.length()-1)+i);
            wrapperLocationMap.isNull(LocationMap::getPallet_code);
            wrapperLocationMap.eq(LocationMap::getWarehouse, "D库");
//            wrapperLocationMap.in(LocationMap::getLocation_code, "DP");
            wrapperLocationMap.eq(LocationMap::getStatus, "启用");
            List<LocationMap> locationMaps = locationMapService.list(wrapperLocationMap);
            if (locationMaps == null || locationMaps.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("库位号："+location_code+"未找到前深度的库位");
            }else {
                interReturn.setStatus(true);
                interReturn.setResult(locationMaps);
                return interReturn;
            }
        }
        return interReturn;
    }

    /**
     * 获得一个合适的库位
     * @param tier  任务比较少的设备和层数
     * @return  合适的库位
     */
    private InterReturn GetLocationmap(Object[] tier) {
        InterReturn interReturn = new InterReturn();
        try {
            //按任务数从小到大的寻找每层可用库位
            for (int t = 0; t < tier.length; t++) {
                LambdaQueryWrapper<LocationMap> wrapperLocationMap = new LambdaQueryWrapper<>();
                //拿到需要寻找的下一个前深位的库位
                wrapperLocationMap.isNull(LocationMap::getPallet_code);
                wrapperLocationMap.eq(LocationMap::getWarehouse, "D库");
                wrapperLocationMap.eq(LocationMap::getStatus, "启用");
                wrapperLocationMap.eq(LocationMap::getLocation_code_z, t + 1);
                wrapperLocationMap.like(LocationMap::getLocation_code, "DP");
                List<LocationMap> locationMaps = locationMapService.list(wrapperLocationMap);
                if (locationMaps == null || locationMaps.size() == 0) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("未找到库位！");
                } else {
                    logger.info(LocalDateTime.now() + "---");
                    //先找1排再2排
                    for (int i = 1; i < 3; i++) {
                        //再找列
//                        for (int j = 1; j < 37; j++) {
                        //再找最深库位
                        if (i == 1) {
                            for (int k = 6; k > 1; k--) {
                                for (int j = 1; j < 37; j++) {
                                    for (LocationMap locationMap : locationMaps) {
                                        if (locationMap.getLocation_area().toString().substring(2, 3).equals(String.valueOf(i)) &&
                                                locationMap.getLocation_code_y().toString().equals(String.valueOf(j)) &&
                                                locationMap.getLocation_code_d().toString().equals(String.valueOf(k))) {
                                            interReturn.setStatus(true);
                                            interReturn.setResult(locationMap);
                                            return interReturn;
                                        }
                                    }
                                }
                            }
                        } else if (i == 2) {
                            for (int k = 9; k > 1; k--) {
                                for (int j = 1; j < 37; j++) {
                                    for (LocationMap locationMap : locationMaps) {
                                        if (locationMap.getLocation_area().toString().substring(2, 3).equals(String.valueOf(i)) &&
                                                locationMap.getLocation_code_y().toString().equals(String.valueOf(j)) &&
                                                locationMap.getLocation_code_d().toString().equals(String.valueOf(k))) {
                                            interReturn.setStatus(true);
                                            interReturn.setResult(locationMap);
                                            return interReturn;
                                        }
                                    }
                                }
                            }
                        }
//                        }
                    }
                    logger.info(LocalDateTime.now() + "---");
                }
            }
            interReturn.setStatus(false);
            interReturn.setMessage("仓库已存满！");
        } catch (Exception x) {
            logger.info("获得一个合适的库位异常：" + x);
        }
        return interReturn;
    }

    //-------------上官青云---------------
}

package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.Inventory;
import com.example.wmsmp.entity.IsUsed;
import com.example.wmsmp.entity.LocationMap;
import com.example.wmsmp.entity.VMapInv;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.mapper.LocationMapMapper;
import com.example.wmsmp.service.InventoryService;
import com.example.wmsmp.service.LocationMapService;
import com.example.wmsmp.service.VMapInvService;
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

@Api(value = "123", tags = "MAP货位绑定关系")
@RestController
@CrossOrigin
//@Slf4j
public class LocationMapController {
    @Autowired
    LocationMapService locationMapService;

    @Autowired
    InventoryService inventoryService;

    @Autowired
    VMapInvService vMapInvService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    private static Logger logger = Logger.getLogger(LocationMapController.class);


    @ApiOperation(value = "根据库名获取已用未用（已排除禁用货位）三维使用", notes = "参数：LocationMap类中的Warehouse")
    @PostMapping("/LocationMap/GetIsUsedByWarehouse")
    @CrossOrigin
    public InterReturn GetIsUsedByWarehouse(@RequestBody LocationMap locationMap) {
        logger.info("GetIsUsedByWarehouse 入参：" + locationMap);
        InterReturn interReturn = new InterReturn();
        IsUsed isUsed = new IsUsed();
        try {
            //判断参数中库名是否为空
            if (locationMap.getWarehouse() == null) {
                interReturn.setStatus(false);
                interReturn.setMessage("库名为空");
                return interReturn;
            } else {
                String warehouse = locationMap.getWarehouse();//库名
                LambdaQueryWrapper<LocationMap> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.eq(LocationMap::getWarehouse, warehouse);
                queryWrapper1.isNotNull(LocationMap::getPallet_code);
                queryWrapper1.ne(LocationMap::getStatus, "禁用");
                isUsed.setUnused("" + locationMapService.count(queryWrapper1));//已用

                LambdaQueryWrapper<LocationMap> queryWrapper2 = new LambdaQueryWrapper<>();
                queryWrapper2.eq(LocationMap::getWarehouse, warehouse);
                queryWrapper2.isNull(LocationMap::getPallet_code);
                queryWrapper2.ne(LocationMap::getStatus, "禁用");
                isUsed.setUsed("" + locationMapService.count(queryWrapper2));//未用

                interReturn.setStatus(true);
                interReturn.setResult(isUsed);
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("获取已用未用出错：" + e.getCause());
            System.out.println("获取已用未用出错：" + e.getMessage());
            logger.error("获取已用未用出错：" + e.getMessage());
        }
        return interReturn;
    }


    /**
     * @Author:lcy
     * @Description:原有接口，根据原型做一些查询条件的修改
     * @DateTime 2023/7/19 10:56
     * @Params
     * @Return
     */
    @ApiOperation(value = "模糊条件查询货位关系", notes = "参数：LocationMap类、pageNo、pageSize")
    @PostMapping("/LocationMap/GetAllLocationMapByFuzzy")
    @CrossOrigin
    public InterReturn GetAllLocationMapByFuzzy(@RequestBody LocationMap locationMap, int pageNo, int pageSize) {
//        logger.info("进入 GetAllLocationMapByFuzzy");
//        logger.info("接收到参数：" + locationMap);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            //mybatisplus
            Page<LocationMap> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<LocationMap> wrapper = new LambdaQueryWrapper<>();

            // 模糊查询
            wrapper.like(locationMap.getLocation_code() != null && !locationMap.getLocation_code().isEmpty(),
                    LocationMap::getLocation_code, locationMap.getLocation_code());


            wrapper.eq(locationMap.getLocation_area() != null,
                    LocationMap::getLocation_area, locationMap.getLocation_area());
            wrapper.eq(locationMap.getWarehouse() != null && !locationMap.getWarehouse().isEmpty(),
                    LocationMap::getWarehouse, locationMap.getWarehouse());
            wrapper.eq(locationMap.getStatus() != null && !locationMap.getStatus().isEmpty(),
                    LocationMap::getStatus, locationMap.getStatus());

            // 模糊查询
            wrapper.like(locationMap.getPallet_code() != null && !locationMap.getPallet_code().isEmpty(),
                    LocationMap::getPallet_code, locationMap.getPallet_code());

            wrapper.orderByAsc(LocationMap::getId);
            List<LocationMap> locationMaps = locationMapService.page(page, wrapper).getRecords();
            if (locationMaps == null || locationMaps.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到此货位绑定关系！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查找到货位绑定关系！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(locationMaps);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错：" + e.getCause());
            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        //  logger.info("GetAllLocationMapByFuzzy 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "更新货位关系", notes = "根据location更新用户 传LocationMap类 托盘号必填 为空则清空托盘号")
    @PostMapping("/LocationMap/UpdateLocationMapByLocation")
    @CrossOrigin
    public InterReturn UpdateLocationMapByLocation(@RequestBody LocationMap locationMap) {
//        logger.info("进入 UpdateLocationMapByLocation");
//        logger.info("接收到参数：" + locationMap);
        InterReturn interReturn = new InterReturn();
        try {
            if (locationMap.getLocation_code() == null || locationMap.getLocation_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或货位号为空");
                System.out.println("参数异常或货位号为空");
                logger.info("UpdateLocationMapByLocation 返回：" + interReturn);
                return interReturn;
            }
            //mybatisplus
            LambdaUpdateWrapper<LocationMap> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(LocationMap::getLocation_code, locationMap.getLocation_code());
            wrapper.set(LocationMap::getPallet_code, locationMap.getPallet_code());
//            wrapper.set(locationMap.getHeight() != null, LocationMap::getHeight, locationMap.getHeight());//待改
//            wrapper.set(locationMap.getVolume() != null, LocationMap::getVolume, locationMap.getVolume());
            wrapper.set(locationMap.getStatus() != null && !locationMap.getStatus().isEmpty(), LocationMap::getStatus, locationMap.getStatus());
            wrapper.set(LocationMap::getUpdater, locationMap.getUpdater());
            wrapper.set(LocationMap::getUpdate_time, LocalDateTime.now());
            boolean bool = locationMapService.update(null, wrapper);

            if (bool) {
                interReturn.setStatus(true);
                interReturn.setMessage("更新货位关系成功！");
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
//        logger.info("UpdateLocationMapByLocation 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "循环生成货位数据", notes = "依据最大货位号生成 前缀prefix 传LocationMap类")
    //@PostMapping("/LocationMap/CreateData")
    @CrossOrigin
    public InterReturn CreateData(@RequestBody LocationMap locationMap, String prefix) {
        logger.info("进入 CreateData");
        logger.info("接收到参数：前缀：" + prefix + " 目标货位：" + locationMap);
        InterReturn interReturn = new InterReturn();
        interReturn.setStatus(false);

        if (prefix.isEmpty()) {
            interReturn.setMessage("前缀为空");
            return interReturn;
        }

        try {
            String locationTar = locationMap.getLocation_code();//目标货位
            int length = locationTar.length();//长度
            int row = Integer.parseInt(locationTar.substring(length - 6, length - 4));//排
            int column = Integer.parseInt(locationTar.substring(length - 4, length - 2));//列
            int floor = Integer.parseInt(locationTar.substring(length - 2));//列
            logger.info("目标 排:" + row + "，列:" + column + "，层:" + floor);
            //mybatisplus
            List<LocationMap> listAll = locationMapService.list();//获取已有货位信息
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            LocationMapMapper mapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper
            for (int k = 0; k < row; k++) {
                for (int i = 0; i < column; i++) {
                    for (int j = 0; j < floor; j++) {
                        String newRackNo = prefix +
                                String.format("%02d", k + 1) +
                                String.format("%02d", i + 1) +
                                String.format("%02d", j + 1);//生成新货位号
                        //  logger.info("新货位号：" + newRackNo);

                        //查询新货位号是否已存在
                        if (listAll.stream().anyMatch(p -> newRackNo.equals(p.getLocation_code()))) {
                            //存在 则跳过
                            continue;
                        } else {
                            //不存在 则插入list
                            LocationMap newMap = new LocationMap();
                            newMap.setLocation_code(newRackNo);
                            newMap.setCreator(locationMap.getCreator());//从前端获取创建人信息
                            mapMapper.insert(newMap);//准备执行sql
                        }
                    }
                }
            }

            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("生成成功！");
            } catch (Exception e) {
                logger.error("批量插入入货位数据异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("批量插入入货位数据异常，事务回滚:" + e.getCause());
            } finally {
                sqlSession.close();
            }
            return interReturn;
        } catch (Exception ee) {
            interReturn.setMessage("生成出错：" + ee.getCause());
            return interReturn;
        }
    }

    @ApiOperation(value = "test", notes = "依据最大货位号生成 前缀prefix 传LocationMap类")
    //@PostMapping("/LocationMap/test")
    @CrossOrigin
    public InterReturn test() {
        InterReturn interReturn = new InterReturn();
        interReturn.setStatus(false);
        //查询货位号
        LambdaQueryWrapper<LocationMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LocationMap::getWarehouse, "B库");
        queryWrapper.orderByAsc(LocationMap::getLocation_code);

        List<LocationMap> maps = locationMapService.list(queryWrapper);


        //    interReturn.setResult(mapsNews);

        return interReturn;
    }

    @ApiOperation(value = "获取空货格 P托盘库", notes = "1.排号 2.远、近 3.1（7层下）、2（8层上）")
    @PostMapping("/LocationMap/getEmptyLocationP")
    @CrossOrigin
    public InterReturn getEmptyLocationP(Integer row, String farNear, Integer lowHigh) {
        logger.info("getEmptyLocationP 入参：" + row + farNear + lowHigh);
        InterReturn interReturn = new InterReturn();
        //row介于1~12之间
        if (row < 1 || row > 12) {
            interReturn.setStatus(false);
            interReturn.setMessage("排号应当介于1~12之间：" + row);
            return interReturn;
        }
        //farNear取值 远或近
        if (!"远".equals(farNear) && !"近".equals(farNear)) {
            interReturn.setStatus(false);
            interReturn.setMessage("远近推荐 赋值错误：" + row);
            return interReturn;
        }
        //lowHigh取值 远或近 1或2
        if (lowHigh != 1 && lowHigh != 2) {
            interReturn.setStatus(false);
            interReturn.setMessage("高低货位推荐 赋值错误：" + row);
            return interReturn;
        }


        LambdaQueryWrapper<LocationMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LocationMap::getWarehouse, "P库");
        queryWrapper.eq(LocationMap::getStatus, "启用");
        queryWrapper.eq(LocationMap::getLocation_code_x, row);
        queryWrapper.isNull(LocationMap::getPallet_code);
        // 根据farNear字段决定是远的列还是近的列
        if ("远".equals(farNear)) {
            queryWrapper.orderByDesc(LocationMap::getLocation_code_y);//远的列
        } else {
            queryWrapper.orderByAsc(LocationMap::getLocation_code_y);//近的列
        }
        // 根据lowHigh字段决定是高货位还是低货位
        if (lowHigh == 1) {
            queryWrapper.le(LocationMap::getLocation_code_z, 7);//高货位 7层及以下
        } else {
            queryWrapper.ge(LocationMap::getLocation_code_z, 8);//低货位 8层及以上
        }

        // 获取最后一条记录
        queryWrapper.last("LIMIT 1");
        // 根据条件查询出一条记录
        LocationMap map = locationMapService.getOne(queryWrapper);
        if (map == null) {
            interReturn.setStatus(false);
            interReturn.setMessage("获取P托盘库空货格失败：无空格：" + row + farNear + lowHigh);
            logger.error("获取P托盘库空货格失败：无空格：" + row + farNear + lowHigh);
        } else {
            interReturn.setStatus(true);
            interReturn.setResult(map);
            interReturn.setMessage("获取P托盘库空货格成功" + map.getLocation_code());
        }
        return interReturn;
    }

    @ApiOperation(value = "获取最大空货位数量的排号 P托盘库", notes = "1（7层下）、2（8层上）")
    @PostMapping("/LocationMap/getEmptyLocationGroupRowP")
    @CrossOrigin
    public Integer getMaxEmptyLocationRowP(Integer lowHigh) {
        Map<Integer, Integer> map = new HashMap<>();//Integer row, String farNear, String reserve
        for (int i = 0; i < 12; i++) {
            LambdaQueryWrapper<LocationMap> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LocationMap::getWarehouse, "P库");
            queryWrapper.eq(LocationMap::getStatus, "启用");
            queryWrapper.eq(LocationMap::getLocation_code_x, i + 1);
            queryWrapper.isNull(LocationMap::getPallet_code);
            if (lowHigh == 1) {
                queryWrapper.le(LocationMap::getLocation_code_z, 7);//高货位 7层及以下
            } else {
                queryWrapper.ge(LocationMap::getLocation_code_z, 8);//低货位 8层及以上
            }
            Integer count = Integer.parseInt(locationMapService.count(queryWrapper) + "");
            map.put(i + 1, count);
        }

        int rowNo = 0;
        int maxValue = Integer.MIN_VALUE;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxValue) {
                maxValue = entry.getValue();
                rowNo = entry.getKey();
            }
        }

        return rowNo;
    }

    @ApiOperation(value = "获取外侧空货格 B料箱库", notes = "1.层号 2.远、近 3.预留")
    @PostMapping("/LocationMap/getEmptyOutLocationB")
    @CrossOrigin
    public InterReturn getEmptyOutLocationB(Integer floor, String farNear, String reserve) {
        logger.info("getEmptyOutLocationB 入参：" + floor + farNear);
        InterReturn interReturn = new InterReturn();
        //floor介于1~42之间
        if (floor < 1 || floor > 42) {
            interReturn.setStatus(false);
            interReturn.setMessage("排号应当介于1~12之间：" + floor);
            return interReturn;
        }

        //farNear取值 远或近
        if (!"远".equals(farNear) && !"近".equals(farNear)) {
            interReturn.setStatus(false);
            interReturn.setMessage("远近推荐 赋值错误：" + farNear);
            return interReturn;
        }

        LambdaQueryWrapper<LocationMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LocationMap::getWarehouse, "B库");
        queryWrapper.eq(LocationMap::getStatus, "启用");
        queryWrapper.eq(LocationMap::getLocation_code_z, floor);
        queryWrapper.isNull(LocationMap::getPallet_code);
        queryWrapper.in(LocationMap::getLocation_code_d, Arrays.asList("7", "8"));//货位深度

        // 根据farNear字段决定是远的列还是近的列
        if ("远".equals(farNear)) {
            queryWrapper.orderByDesc(LocationMap::getLocation_code_y);//远的列
        } else {
            queryWrapper.orderByAsc(LocationMap::getLocation_code_y);//近的列
        }
        // 获取最后一条记录
        queryWrapper.last("LIMIT 1");
        // 根据条件查询出一条记录
        List<LocationMap> maps = locationMapService.list(queryWrapper);
        if (maps.size() == 0) {
            interReturn.setStatus(false);
            interReturn.setMessage("获取B料箱库外侧空货格失败：无外侧空货格：" + floor);
            logger.error("获取B料箱库外侧空货格失败：无外侧空货格：" + floor);
        } else {
            interReturn.setStatus(true);
            interReturn.setResult(maps.get(0));
            interReturn.setMessage("获取B料箱库外侧空货格成功" + maps.get(0).getLocation_code());
        }
        return interReturn;
    }

    @ApiOperation(value = "按料箱号分配货位 B料箱库**", notes = "料箱号")
    @PostMapping("/LocationMap/getLocationByPalletcodeB")
    @CrossOrigin
    public InterReturn getLocationByPalletcodeB(String palletcode) {
        logger.info("进入getLocationByPalletcodeB");
        logger.info("接收到参数：" + palletcode);
        InterReturn interReturn = new InterReturn();
        try {
            //1.是否为空料箱
            LambdaQueryWrapper<Inventory> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(Inventory::getPallet_code, palletcode);
            List<Inventory> inventorys1 = inventoryService.list(queryWrapper1);
            if (inventorys1.size() == 0) {
                //1.1空料箱 分配
                //获取空料箱list
                interReturn = getNearLocationB(null);//空箱
            } else {
                //1.2满料箱
                //2.物料是否已在库里
                LambdaQueryWrapper<VMapInv> queryWrapper2 = new LambdaQueryWrapper<>();
                queryWrapper2.ne(VMapInv::getPallet_code, palletcode);
                queryWrapper2.eq(VMapInv::getMaterial_code, inventorys1.get(0).getMaterial_code());
                queryWrapper2.eq(VMapInv::getBatch, inventorys1.get(0).getBatch());
                List<VMapInv> inventorys2 = vMapInvService.list(queryWrapper2);
                if (inventorys2.size() == 0) {
                    //新料 分配远处空格子
                    int random = new Random().nextInt(42) + 1;//随机数 1-42间的整数
                    interReturn = getEmptyOutLocationB(random, "近", "");//新料

                } else {
                    //旧料 查询list 是否有近处可用空格
                    Inventory inventory = new Inventory();
                    inventory.setMaterial_code(inventorys1.get(0).getMaterial_code());
                    inventory.setBatch(inventorys1.get(0).getBatch());
                    interReturn = getNearLocationB(inventory);//旧料
                }
            }
            logger.info("getLocationByPalletcodeB返回：" + interReturn);
            return interReturn;
        } catch (Exception ee) {
            interReturn.setMessage("按料箱号分配货位出错：" + ee.getCause());
            logger.error("按料箱号分配货位出错：" + ee.getCause());
            return interReturn;
        }
    }


    @ApiOperation(value = "获取此库存的近处可用空货位 B料箱库*", notes = "前端不调用")
    //@PostMapping("/LocationMap/getNearLocationB")
    @CrossOrigin
    private InterReturn getNearLocationB(Inventory inventory) {
        InterReturn interReturn = new InterReturn();
        try {
            String type;
            LambdaQueryWrapper<VMapInv> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(VMapInv::getWarehouse, "B库");
            wrapper.eq(VMapInv::getStatus, "启用");
            wrapper.isNotNull(VMapInv::getPallet_code);
            if (inventory == null) {
                //空料箱
                type = "空料箱";
            } else {
                //旧物料
                type = "旧物料";
                wrapper.eq(VMapInv::getMaterial_code, inventory.getMaterial_code());
                wrapper.eq(VMapInv::getBatch, inventory.getBatch());
            }
            List<VMapInv> vMapInvList = vMapInvService.list(wrapper);//（此库存视图）所有带托盘的MAP or 所有此物料托盘的MAP
            if (vMapInvList.size() == 0) {
                //无此库存视图
                int random = new Random().nextInt(42) + 1;//随机数 1-42间的整数
                interReturn = getEmptyOutLocationB(random, "近", "");//空箱or新料
            } else {
                //有此库存视图
                boolean isGet = false;//是否获取到货位
                for (VMapInv vMapInv : vMapInvList) {
                    String location_code = vMapInv.getLocation_code();
                    Integer length = vMapInv.getLocation_code().length();
                    String prefix = location_code.substring(0, length - 1);//前
                    String suffix = location_code.substring(length - 1);//后
                    Integer suffixInt = Integer.parseInt(suffix);
                    if (suffixInt > 5) {
                        //远处此库存视图 查询其近处是否可用 无货
                        suffixInt -= 6;//料箱库远近货位号相差6
                        String nearLocationcode = prefix + suffixInt;//近处货位号
                        //查找近处是否在此库存视图中
                        List<VMapInv> vMapInvs = vMapInvList.stream().filter
                                        (item -> (item.getLocation_code().equals(nearLocationcode)))
                                .collect(Collectors.toList());
                        if (vMapInvs.size() > 0) {
                            //在 下一条
                        } else {
                            //不在
                            //判断近处是否有料箱
                            LambdaQueryWrapper<LocationMap> wrapper2 = new LambdaQueryWrapper<>();
                            wrapper2.eq(LocationMap::getLocation_code, nearLocationcode);
                            List<LocationMap> mapList2 = locationMapService.list(wrapper2);//近处的map
                            if (mapList2.get(0).getPallet_code() == null || "".equals(mapList2.get(0).getPallet_code().trim())) {
                                //近处货位无料箱 选中此处
                                interReturn.setStatus(true);
                                interReturn.setResult(mapList2.get(0));
                                interReturn.setMessage(type + " 获取近处：" + mapList2.get(0).getLocation_code());
                                isGet = true;
                                return interReturn;
                            } else {
                                //近处货位有料箱 下一条
                            }
                        }
                    } else {
                        //近处此库存视图 不做处理
                    }
                }
                if (!isGet) {
                    int random = new Random().nextInt(42) + 1;//随机数 1-42间的整数
                    interReturn = getEmptyOutLocationB(random, "近", "");//旧料找新位
                }
            }
        } catch (Exception ee) {

            interReturn.setMessage("获取近的可用空货位出错：" + ee.getCause());
            return interReturn;

        }
        return null;
    }


}

package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wmsmp.emums.HBoxStatusEnum;
import com.example.wmsmp.entity.Inventory;
import com.example.wmsmp.entity.LocationMap;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.service.InventoryService;
import com.example.wmsmp.service.LocationMapService;
import com.example.wmsmp.util.OpcPlcHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Api(value = "123", tags = "任务（回转库）")
@RestController
@CrossOrigin
public class TaskHController {

    @Autowired
    InventoryService inventoryService;

    @Autowired
    LocationMapService locationMapService;

    @ApiOperation(value = "回转库出库任务下发")
    @PostMapping("/TaskH/OutTask")  //storageNo仓库编号  1或者2    layer 层号 1-41
    @CrossOrigin
    public InterReturn OutTask(int storageNo, int layer) {
        InterReturn interReturn = new InterReturn();
        if (layer > 40) {
            interReturn.setStatus(false);
            interReturn.setMessage("层数不存在，请重新输入");
            return interReturn;
        }
        interReturn = OpcPlcHelper.setOutplc(storageNo, layer);
        return interReturn;
    }

    @ApiOperation(value = "回转库入库任务下发")
    @PostMapping("/TaskH/InTask")//storageNo仓库编号
    @CrossOrigin
    public InterReturn InTask(int storageNo) {
        InterReturn interReturn = OpcPlcHelper.setInPlc(storageNo);
        return interReturn;
    }

    @ApiOperation(value = "回转库终止任务下发")
    @PostMapping("/TaskH/SetTaskStop")//storageNo仓库编号
    @CrossOrigin
    public InterReturn SetTaskStop(int storageNo) {
        InterReturn interReturn = OpcPlcHelper.setTaskStop(storageNo);
        return interReturn;
    }

    @ApiOperation(value = "回转库根据物料信息申请空料箱")
    @PostMapping("/TaskH/GetStorageEmpty")
    @CrossOrigin
    public InterReturn GetStorageEmpty(@RequestBody Inventory inventory) {
        InterReturn interReturn;
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getMaterial_code, inventory.getMaterial_code());
        wrapper.eq(Inventory::getBatch, inventory.getBatch());
        List<Inventory> inventories = inventoryService.list(wrapper);
        //判断是否有和同物料一致的库存信息
        if (inventories != null && inventories.size() > 0) {
            //有一致的物料信息
            interReturn = GetStorageBase(inventories);
        } else {
            //没有一致的需要按规则分配合适库位
            interReturn = GetStorageBaseSuitable();
        }
        //拿到后就出库
        if(interReturn.isStatus()){
            LocationMap locationMap = (LocationMap) interReturn.getResult();
            interReturn = OpcPlcHelper.setOutplc(locationMap.getLocation_code_x(), locationMap.getLocation_code_z());
            interReturn.setMessage("智能货柜：申请料箱成功，料箱编号为："+locationMap.getLocation_code());
        }
        return interReturn;
    }

    /**
     * 根据库存信息获取一个半箱的库位或一个其他未满的库位或空库位
     * @param inventoryList   所有的一致物料信息
     * @return
     */
    private InterReturn GetStorageBase(List<Inventory> inventoryList) {
        InterReturn interReturn = new InterReturn();
        List<LocationMap> inventories = null;
        for (Inventory inventory : inventoryList) {
            //根据每条库存信息查询一个半箱库位
            LambdaQueryWrapper<LocationMap> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LocationMap::getLocation_code, inventory.getMaterial_code());
            wrapper.eq(LocationMap::getWarehouse, "H库");
            wrapper.eq(LocationMap::getVolume, HBoxStatusEnum.HALF_CHEST.getCode());
            wrapper.orderByAsc(LocationMap::getId);//正序
            inventories = locationMapService.list(wrapper);
            if(inventories != null && inventories.size()>0){
                interReturn.setStatus(true);
                interReturn.setMessage("回转库：料箱已分配为"+inventories.get(0).getLocation_code()+"！");
                interReturn.setResult(inventories.get(0));
                return interReturn;
            }
        }
        if(inventories == null && inventories.size()==0) {
            //没有一致按规则分配
            return GetStorageBaseSuitable();
        }
        return interReturn;
    }

    /**
     * 找合适的库位 先半再满
     * @return
     */
    private InterReturn GetStorageBaseSuitable() {
        InterReturn interReturn = new InterReturn();
        LambdaQueryWrapper<LocationMap> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocationMap::getWarehouse, "H库");
        wrapper.eq(LocationMap::getVolume, HBoxStatusEnum.HALF_CHEST.getCode());
        wrapper.orderByAsc(LocationMap::getId);//正序
        List<LocationMap> inventories = locationMapService.list(wrapper);
        if(inventories != null && inventories.size()>0){
            interReturn.setStatus(true);
            interReturn.setMessage("回转库：料箱已分配为"+inventories.get(0).getLocation_code()+"！");
            interReturn.setResult(inventories.get(0));
            return interReturn;
        }
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocationMap::getWarehouse, "H库");
        wrapper.eq(LocationMap::getVolume, HBoxStatusEnum.EMPTY.getCode());
        wrapper.orderByAsc(LocationMap::getId);//正序
        inventories = locationMapService.list(wrapper);
        if(inventories != null && inventories.size()>0){
            interReturn.setStatus(true);
            interReturn.setMessage("回转库：料箱已分配为"+inventories.get(0).getLocation_code()+"！");
            interReturn.setResult(inventories.get(0));
            return interReturn;
        }
        interReturn.setStatus(false);
        interReturn.setMessage("回转库：库位已全部满箱！");
        return interReturn;
    }

    @ApiOperation(value = "回转库根据物料信息出库")
    @PostMapping("/TaskH/GetStorageOut")
    @CrossOrigin
    public InterReturn GetStorageOut(String storageNo) {
        InterReturn interReturn = new InterReturn();
        try {
            int k = Integer.parseInt(storageNo.substring(2, 4));
            int c = Integer.parseInt(storageNo.substring(storageNo.length() - 2));
            interReturn = OpcPlcHelper.setOutplc(k,c);
            if(interReturn.isStatus()){
                interReturn.setMessage("智能货柜：料箱编号："+storageNo+"出库成功！");
            }
        } catch (Exception x) {
            interReturn.setStatus(false);
            interReturn.setMessage("智能货柜：料箱编号：" + storageNo + "有误：" + x);
        }
        return interReturn;
    }
}

package com.example.wmsmp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wmsmp.entity.Inventory;
import com.example.wmsmp.service.InventoryService;
import com.example.wmsmp.mapper.InventoryMapper;
import org.springframework.stereotype.Service;

/**
* @author 16
* @description 针对表【t_inventory(托盘物料绑定)】的数据库操作Service实现
* @createDate 2023-07-18 17:16:29
*/
@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory>
    implements InventoryService{

}





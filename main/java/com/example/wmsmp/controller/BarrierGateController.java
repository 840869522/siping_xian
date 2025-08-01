package com.example.wmsmp.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wmsmp.entity.InOrder;
import com.example.wmsmp.entity.OutOrder;
import com.example.wmsmp.led.cmd.LedCmdUtil;
import com.example.wmsmp.service.InOrderService;
import com.example.wmsmp.service.OutOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import onbon.bx06.area.TextCaptionBxArea;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(value = "123", tags = "道闸接口")
@RestController
@CrossOrigin
@Slf4j
public class BarrierGateController {

    @Resource
    private InOrderService inOrderService;

    @Resource
    private OutOrderService outOrderService;

    @Resource
    private LedCmdUtil ledCmdUtil;


    @ApiOperation(value = "获取道闸拍摄到的车辆信息", notes = "plateNo 车牌数据")
    @PostMapping("/barrier/gate/car/num")
    @CrossOrigin
    public JSON GetOutOrderByFuzzy(@RequestBody Map<String, Object> carMsg) throws Exception {
        JSONObject callback = JSONUtil.createObj();
        log.info("=====plateNo=====>" + carMsg.get("plateNo"));
        log.info("=====入参=====>" + JSONUtil.parse(carMsg));
        String plateNo = carMsg.get("plateNo").toString();
        //查询出入库订单
        if (StrUtil.isBlank(plateNo) || plateNo.equals("无车牌")) {
            callback.set("status", false);
            callback.set("msg", "车牌号为空");
            return callback;
        }
        LambdaQueryWrapper<InOrder> wrapperIn = new LambdaQueryWrapper<>();
        wrapperIn.eq(InOrder::getCar_no, plateNo);
        wrapperIn.orderByDesc(InOrder::getCreate_time);
        List<InOrder> inOrderList = inOrderService.list(wrapperIn);
        String warehouseName = null;
        if (inOrderList.size() > 0) {
            InOrder inOrder = inOrderList.get(0);
            warehouseName = inOrder.getWarehouse_name();
        } else {
            LambdaQueryWrapper<OutOrder> wrapperOut = new LambdaQueryWrapper<>();
            wrapperOut.eq(OutOrder::getCar_no, plateNo);
            wrapperOut.orderByDesc(OutOrder::getCreate_time);
            List<OutOrder> outOrderList = outOrderService.list(wrapperOut);
            if (outOrderList.size() > 0) {
                OutOrder outOrder = outOrderList.get(0);
                warehouseName = outOrder.getWarehouse_name();
            }
        }
        //向LED发送显示命令
        if (StrUtil.isNotBlank(warehouseName)) {
            String text = StrUtil.format("车牌号：{}\r\n目的地：{}     ", plateNo, warehouseName);
            //构建节目
            TextCaptionBxArea textCaptionBxArea = ledCmdUtil.buildCarText(text, 1, 0, 108, 320, 84);
            ledCmdUtil.sendLed(1, textCaptionBxArea);
            callback.set("status", true);
            callback.set("msg", "接受成功!");
        } else {
            callback.set("status", false);
            callback.set("msg", "不属于WMS管理车辆!");
        }
        return callback;
    }
}

package com.example.wmsmp.controller;


import cn.hutool.core.util.StrUtil;
import com.example.wmsmp.entity.LedModel;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.led.cmd.LedCmdUtil;
import com.example.wmsmp.service.LedService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import onbon.bx06.area.TextCaptionBxArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author:wlxun
 * @Description :LED显示内容
 * @DateTime 2024/9/02 16:49
 * @Params
 * @Return
 */
@Api(value = "123", tags = "LED显示内容")
@RestController
@CrossOrigin
@Slf4j
public class LedController {
    @Autowired
    LedService ledService;

    @Resource
    private LedCmdUtil ledCmdUtil;

    @ApiOperation(value = "led查询", notes = "LedModel类")
    @PostMapping("/led/find")
    @CrossOrigin
    public InterReturn getLed() {
        InterReturn interReturn = new InterReturn();
        try {
            List<LedModel> list = ledService.list();
            if (list == null || list.size() == 0) {
                interReturn.setResult(new LedModel());
            } else {
                interReturn.setResult(list.get(0));
            }
            interReturn.setStatus(true);
            interReturn.setMessage("查询LED显示内容成功！");
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询LED显示内容出错：" + e.getCause());
        }
        return interReturn;
    }


    @ApiOperation(value = "新增或修改LED显示内容")
    @PostMapping("/led/addOrUpdate")
    @CrossOrigin
    public InterReturn addOrUpdate(@RequestBody LedModel ledModel) {
        InterReturn interReturn = new InterReturn();
        try {
            if (StrUtil.isBlank(ledModel.getLedText())) {
                interReturn.setStatus(false);
                interReturn.setMessage("LED内容为空!");
                System.out.println("LED内容为空");
                return interReturn;
            }
            boolean boolSave = ledService.saveOrUpdate(ledModel);
            //创建成功后发送文本
            if (boolSave) {
                TextCaptionBxArea textCaptionBxArea = ledCmdUtil.buildText(ledModel.getLedText(), 1, 0, 58, 320, 49);
                ledCmdUtil.sendLed(ledModel.getLedId(), textCaptionBxArea);
                interReturn.setStatus(true);
                interReturn.setMessage("OK");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("变更LED内容失败");
            }

        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("变更LED内容出错：" + e.getCause());
            System.out.println("变更LED内容出错：" + e.getMessage());
        }
        return interReturn;
    }

}

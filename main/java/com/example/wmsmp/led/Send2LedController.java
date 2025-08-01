package com.example.wmsmp.led;

import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.led.cmd.LedCmdUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import onbon.bx06.area.DateTimeBxArea;
import onbon.bx06.area.TextCaptionBxArea;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ClassName Send2Ledcontroller
 * @Author wlixun
 * @Date 2024/8/2 16:22
 * @Version 1.0
 * @Description TODO
 **/
@Api(value = "123", tags = "LED设备对接")
@RestController
@RequestMapping("/led")
@Slf4j
public class Send2LedController {

    @Resource
    private LedCmdUtil ledCmdUtil;

    @ApiOperation(value = "发送信息至LED")
    @CrossOrigin
    @PostMapping("/send/text")
    public InterReturn materialChart(@RequestBody Map<String, Object> param) throws Exception {
        InterReturn interReturn = new InterReturn();
        //校验LED是否在线
        boolean bool = ledCmdUtil.checkOnline();
        if (!bool) {
            interReturn.setStatus(bool);
            return interReturn;
        }
        //构建时间
        DateTimeBxArea dateTimeBxArea = ledCmdUtil.buildDate();
        //构建文本
        TextCaptionBxArea textCaptionBxArea = ledCmdUtil.buildText(param.get("text").toString(), 4, 0, 0, 320, 27);
        //换行符为  "\r\n"
        boolean text = ledCmdUtil.sendLed(2,  textCaptionBxArea);
        boolean data = ledCmdUtil.sendLed(3,  dateTimeBxArea);
//        boolean text1 = ledCmdUtil.sendText("1231"+"\r\n"+"12312", 1);
        interReturn.setStatus(text);
        return interReturn;
    }
}

package com.example.wmsmp.controller;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(value = "123", tags = "道闸接口")
@RestController
@CrossOrigin
@Slf4j
public class WCSController {

    @ApiOperation(value = "出库")
    @PostMapping("/api/ReceWms/CreateOutTask")
    @CrossOrigin
    public JSON out(@RequestBody Map<String, Object> carMsg) {
        JSONObject callback = JSONUtil.createObj();
        callback.set("code", 1);
        callback.set("msg", "成功");
        log.info(JSONUtil.parse(carMsg).toString());
        return callback;
    }

    @ApiOperation(value = "移库")
    @PostMapping("/api/ReceWms/CreateMoveTask")
    @CrossOrigin
    public JSON move(@RequestBody Map<String, Object> carMsg) {
        JSONObject callback = JSONUtil.createObj();
        callback.set("code", 1);
        callback.set("msg", "成功");
        log.info(JSONUtil.parse(carMsg).toString());
        return callback;
    }
}

package com.example.wmsmp.statistic.controller;

import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.statistic.service.StatisticService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 库存分析
 *
 * @author wlixun
 * @date 2024-07-28
 */
@Api(value = "123", tags = "库存分析")
@RestController
@RequestMapping("/statistic")
@Slf4j
public class StatisticController {

    @Resource
    private StatisticService statisticService;


    /**
     * 出入库物料排行榜
     *
     * @param param type 0 出库 1入库
     * @return
     */
    @ApiOperation(value = "出入库物料排行榜")
    @CrossOrigin
    @PostMapping("/material/chart")
    public InterReturn materialChart(@RequestBody Map<String, Object> param) {
        return statisticService.materialChart(param);
    }

    /**
     * 日出入库物料排行榜
     *
     * @param param
     * @return
     */
    @ApiOperation(value = "日出入库物料排行榜")
    @CrossOrigin
    @PostMapping("/day/chart")
    public InterReturn dayChart(@RequestBody Map<String, Object> param) {
        return statisticService.dayChart(param);
    }

    /**
     * 出入库物料柱状图
     *
     * @param param type 0 出库 1入库
     * @return
     */
    @ApiOperation(value = "出入库物料柱状图")
    @CrossOrigin
    @PostMapping("/library/statement")
    public InterReturn libraryStatement(@RequestBody Map<String, Object> param) {
        return statisticService.libraryStatement(param);
    }

    /**
     * 库存分析饼状图
     *
     * @param param
     * @return
     */
    @ApiOperation(value = "库存分析饼状图")
    @CrossOrigin
    @PostMapping("/pie/chart")
    public InterReturn pieChart(@RequestBody Map<String, Object> param) {
        return statisticService.pieChart(param);
    }
}

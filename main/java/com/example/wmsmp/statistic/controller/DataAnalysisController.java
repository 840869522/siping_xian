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
 * @ClassName DataAnalysisController
 * @Author asus
 * @Date 2024/8/3 10:17
 * @Version 1.0
 * @Description TODO
 **/
@Api(value = "123", tags = "看板数据分析")
@RestController
@RequestMapping("/analysis")
@Slf4j
public class DataAnalysisController {

    @Resource
    private StatisticService statisticService;

    /**
     * 出入库任务成功率
     *
     * @param param material_storage_area 所属仓库
     * @return
     */
    @ApiOperation(value = "出入库任务成功率")
    @CrossOrigin
    @PostMapping("/success/pie")
    public InterReturn successPie(@RequestBody Map<String, Object> param) {
        return statisticService.successPie(param);
    }

    /**
     * 库存物料排行榜
     *
     * @param param material_storage_area 所属仓库
     * @return
     */
    @ApiOperation(value = "出入库物料排行榜")
    @CrossOrigin
    @PostMapping("/material/ranking")
    public InterReturn materialRanking(@RequestBody Map<String, Object> param) {
        return statisticService.materialRanking(param);
    }


    /**
     * 库存利用率
     *
     * @param param material_storage_area 所属仓库
     * @return
     */
    @ApiOperation(value = "库存利用率")
    @CrossOrigin
    @PostMapping("/inventory/use")
    public InterReturn inventoryUse(@RequestBody Map<String, Object> param) {
        return statisticService.inventoryUse(param);
    }

    /**
     * 周任务执行趋势
     *
     * @param param material_storage_area 所属仓库
     * @return
     */
    @ApiOperation(value = "周任务执行趋势")
    @CrossOrigin
    @PostMapping("/task/week")
    public InterReturn taskWeek(@RequestBody Map<String, Object> param) {
        return statisticService.taskWeek(param);
    }

    /**
     * 周任务类型分析
     *
     * @param param material_storage_area 所属仓库
     * @return
     */
    @ApiOperation(value = "周任务类型分析")
    @CrossOrigin
    @PostMapping("/task/type")
    public InterReturn taskType(@RequestBody Map<String, Object> param) {
        return statisticService.taskType(param);
    }
}

package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.LocationMap;
import com.example.wmsmp.entity.OutPick;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.entity.task.X.*;
import com.example.wmsmp.mapper.LocationMapMapper;
import com.example.wmsmp.mapper.TaskPMapper;
import com.example.wmsmp.mapper.TaskXMapper;
import com.example.wmsmp.service.LocationMapService;
import com.example.wmsmp.service.TaskXService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Api(value = "123", tags = "任务（箱组库）")
@RestController
@CrossOrigin
public class TaskXController {
    private static Logger logger = Logger.getLogger(TaskXController.class);

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    @Autowired
    TaskXService taskXService;

    @Autowired
    LocationMapService locationMapService;

    @Autowired
    private RestTemplate restTemplate;//用于调用外部接口

    @Autowired
    private Environment environment;//用于读取配置文件

    /**
     * @Author:xhc
     * @Description 箱组库单条任务号生成
     * @DateTime 2024/6/17 9:33
     * @Params
     * @Return
     */
    @ApiOperation(value = "箱组库单条任务号生成")
    @PostMapping("/TaskX/GenerateTaskNoX")
    @CrossOrigin
    public String GenerateTaskNoX() {
        String taskNoNew = "";
        String str = "TXP";//任务 箱组库 托盘库
        try {
            //使用泛型 待改
            LambdaQueryWrapper<TaskX> wrapper = new LambdaQueryWrapper<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");//设定时间格式
            String strTime = sdf.format(Calendar.getInstance().getTime());
            //模糊查询任务号
            wrapper.like(TaskX::getTask_no, str + strTime);
            wrapper.orderByDesc(TaskX::getTask_no);
            List<TaskX> taskList = taskXService.list(wrapper);
            if (taskList.size() == 0) {
                System.out.println("本日无任务");
                taskNoNew = str + strTime + "0001";
            } else {
                System.out.println("本日已有任务：" + taskList.get(0).getTask_no());
                String oldNo = taskList.get(0).getTask_no().substring(9);
                Integer newNo = Integer.parseInt(oldNo) + 1;
                String newNoStr = String.format("%04d", newNo);//'%04d'的定义:0代表前面要补的字符 4代表字符串长度 d表示参数为整数类型
                taskNoNew = str + strTime + newNoStr;
            }
        } catch (Exception e) {
            logger.error("任务号生成 出错：" + e.getMessage());
        }
        return taskNoNew;
    }

    /**
     * @Author:xhc
     * @Description 箱组库 完成下发给穿梭板的任务
     * @DateTime 2024/6/19 1:13
     * @Params
     * @Return
     */
    @ApiOperation(value = "箱组库任务完成（任务完成时调用，更改MAP和TASK状态）", notes = "SendX类 穿梭板号")
    @PostMapping("/TaskX/TaskXFinish")
    @CrossOrigin
    public InterReturn TaskXFinish(@RequestBody SendX sendXFromWeb) {
        logger.info("TaskXFinish 接收到参数：" + sendXFromWeb);
        InterReturn interReturn = new InterReturn();
        String taskXNoInCar;
        try {
            if (sendXFromWeb.getShuttleId() == null || sendXFromWeb.getShuttleId() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("穿梭板号为空");
                System.out.println("穿梭板号为空");
                logger.info("TaskXFinish 返回：" + interReturn);
                return interReturn;
            }

            String URL = environment.getProperty("apiX.status");
            SendX sendX = new SendX();
            sendX.setShuttleId(sendXFromWeb.getShuttleId());//穿梭板号 3001或3002
            HttpEntity<SendX> httpEntitysnew = new HttpEntity<>(sendX);
            //参数为 url http实体 返回类型
            logger.info("调用天地人穿梭板号状态接口，拼接出参数：" + sendX);
            Map responseMap = restTemplate.postForObject(URL, httpEntitysnew, Map.class);
            logger.info("调用天地人穿梭板号状态接口，返回的参数：" + responseMap);
            //GetX response = (GetX) responseMap;
            Map list = (Map) responseMap.get("data");
            List<Map> info = (List<Map>) list.get("list");
            Map carMsgX = info.get(0);
            if ((int) responseMap.get("code") == 10000) {
                interReturn.setStatus(true);
                interReturn.setMessage("读穿梭板号状态成功:" + responseMap.get("msg"));
                //interReturn.setResult(response.getData());
                //解析穿梭板号状态 当前任务是否完成
                if ((int) carMsgX.get("equipStatus") == 0) {
                    taskXNoInCar = "" + carMsgX.get("wmsTaskId");//wms任务号
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("穿梭板号执行指令未完成");
                    logger.info("TaskXFinish 返回：" + interReturn);
                    return interReturn;
                }
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("读穿梭板号状态出错：" + responseMap.get("msg"));
                interReturn.setResult(responseMap.get("data"));
                logger.info("TaskXFinish 返回：" + interReturn);
                return interReturn;
            }

            //0.查询此任务
            LambdaQueryWrapper<TaskX> wrapperQueryTaskX = new LambdaQueryWrapper<>();
            wrapperQueryTaskX.eq(TaskX::getTask_no, taskXNoInCar);
            TaskX taskXOne = taskXService.getOne(wrapperQueryTaskX);


            //1.更改任务表状态为 "已完成"
            LambdaUpdateWrapper<TaskX> wrapperUpdateTaskX = new LambdaUpdateWrapper<>();
            wrapperUpdateTaskX.eq(TaskX::getTask_no, taskXNoInCar);//任务号
            TaskX taskX1 = new TaskX();
            taskX1.setStatus("已完成");
            boolean boolUpdate = taskXService.update(taskX1, wrapperUpdateTaskX);
            if (boolUpdate) {
                //更新任务状态成功
                logger.info("箱组库库更新任务状态成功：" + taskXNoInCar);
                interReturn.setStatus(true);
                interReturn.setMessage("箱组库库更新任务状态成功：" + taskXNoInCar);
            } else {
                logger.info("箱组库库更新任务状态出错：" + taskXNoInCar);
                interReturn.setStatus(false);
                interReturn.setMessage("箱组库库更新任务状态出错：" + taskXNoInCar);
                return interReturn;
            }

            //2.更新托盘所在位置 清空MAPor绑定MAP
            LambdaQueryWrapper<LocationMap> wrapperQueryMap = new LambdaQueryWrapper<>();
            wrapperQueryMap.eq(LocationMap::getLocation_code, taskXOne.getLocation_code());
            {
                List<LocationMap> locationMapList = locationMapService.list(wrapperQueryMap);
                if (locationMapList.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("该位置不存在" + taskXOne.getLocation_code());
                    System.out.println("该位置不存在" + taskXOne.getLocation_code());
                    logger.info("TaskXFinish 返回：" + interReturn);
                    return interReturn;
                }
            }

            if (taskXOne.getTask_type().equals("出库")) {
                //2.1出库任务 出库为清空
                LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
                updateWrapperMap.eq(LocationMap::getLocation_code, taskXOne.getLocation_code());//货位号
                updateWrapperMap.eq(LocationMap::getWarehouse, "X库");//库名
                LocationMap locationMap = new LocationMap();
                locationMap.setPallet_code("");//托盘 清空
                locationMap.setUpdater(taskXOne.getUpdater());//操作员
                boolean boolUpdateMap = locationMapService.update(locationMap, updateWrapperMap);
                if (boolUpdateMap) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("出库更新货位关系成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("出库更新货位关系失败：" + taskXOne.getLocation_code());
                    System.out.println("出库更新货位关系失败" + taskXOne.getLocation_code());
                    logger.info("TaskXFinish 返回：" + interReturn);
                    return interReturn;
                }
            } else if (taskXOne.getTask_type().equals("入库")) {
                //2.1入库任务 入库为绑定
                LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
                updateWrapperMap.eq(LocationMap::getLocation_code, taskXOne.getLocation_code());//货位号
                updateWrapperMap.eq(LocationMap::getWarehouse, "X库");//库名
                LocationMap locationMap = new LocationMap();
                locationMap.setPallet_code(taskXOne.getPallet_code());//托盘号
                locationMap.setUpdater(taskXOne.getUpdater());//操作员
                boolean boolUpdateMap = locationMapService.update(locationMap, updateWrapperMap);
                if (boolUpdateMap) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("入库更新货位关系成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("入库更新货位关系失败：" + taskXOne.getLocation_code());
                    System.out.println("入库更新货位关系失败" + taskXOne.getLocation_code());
                    logger.info("TaskXFinish 返回：" + interReturn);
                    return interReturn;
                }
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("任务类型有误" + taskXOne.getTask_type());
            }

        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("箱组库任务完成出错：" + e.getCause());
            System.out.println("箱组库任务完成出错：" + e.getMessage());
            logger.error("箱组库任务完成出错：" + e.getMessage());
        }
        logger.info("TaskXFinish 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 箱组库任务下发
     * @DateTime 2023/9/28 8:37
     * @Params
     * @Return
     */
    @ApiOperation(value = "箱组库任务插入")
    @PostMapping("/TaskX/TaskXInsert")
    @CrossOrigin
    public InterReturn TaskXInsert(@RequestBody TaskX taskX) {
        logger.info("进入 TaskXInsert");
        logger.info("接收到参数：" + taskX);
        InterReturn interReturn = new InterReturn();
        try {
            if (taskX.getLocation_code() == null || taskX.getLocation_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("货位号为空");
                System.out.println("货位号为空");
                logger.info("TaskXInsert返回：" + interReturn);
                return interReturn;
            }

            if (taskX.getPallet_code() == null || taskX.getPallet_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("托盘号为空");
                System.out.println("托盘号为空");
                logger.info("TaskXInsert返回：" + interReturn);
                return interReturn;
            }

            if (("入库").equals(taskX.getTask_type()))
            {//入库前校验
                //1.查询此托盘是否有 任务状态是"待执行" "已下发"  的任务
                LambdaQueryWrapper<TaskX> wrapperTaskX1 = new LambdaQueryWrapper<>();
                wrapperTaskX1.eq(TaskX::getPallet_code, taskX.getPallet_code());
                wrapperTaskX1.in(TaskX::getStatus, Arrays.asList("待执行", "已下发"));//任务状态
                List<TaskX> taskList = taskXService.list(wrapperTaskX1);
                if (taskList.size() > 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage(taskX.getPallet_code() + "托盘已有“待执行”、 ”已下发”任务：" + taskList.get(0).getTask_no());
                    System.out.println(taskX.getPallet_code() + "托盘已有“待执行”、 ”已下发”任务：" + taskList.get(0).getTask_no());
                    logger.info("TaskXInsert 返回：" + interReturn);
                    return interReturn;
                }

                //2.判断托盘是否在库
                LambdaQueryWrapper<LocationMap> wrapperMap2 = new LambdaQueryWrapper<>();
                wrapperMap2.eq(LocationMap::getPallet_code, taskX.getPallet_code());
                List<LocationMap> locationMaps2 = locationMapService.list(wrapperMap2);
                if (locationMaps2.size() > 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("托盘：" + taskX.getPallet_code() + "已存在库中");
                    System.out.println("托盘：" + taskX.getPallet_code() + "已存在库中");
                    logger.info("TaskXInsert 返回：" + interReturn);
                    return interReturn;
                }

                //3.发入任务前判断货位是否有货
                LambdaQueryWrapper<LocationMap> wrapperMap3 = new LambdaQueryWrapper<>();
                wrapperMap3.eq(LocationMap::getLocation_code, taskX.getLocation_code());
                LocationMap locationMap3 = locationMapService.getOne(wrapperMap3);
                if (locationMap3.getPallet_code() == null || locationMap3.getPallet_code().length() == 0) {

                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("货位" + taskX.getLocation_code() + "存在托盘：" + locationMap3.getPallet_code());
                    System.out.println("货位" + taskX.getLocation_code() + "存在托盘：" + locationMap3.getPallet_code());
                    logger.info("TaskXInsert 返回：" + interReturn);
                    return interReturn;
                }

                //4.判断此货位是否启用
                if ("启用".equals(locationMap3.getStatus())) {

                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("货位" + taskX.getLocation_code() + "已禁用");
                    System.out.println("货位" + taskX.getLocation_code() + "已禁用");
                    logger.info("TaskPInsert 返回：" + interReturn);
                    return interReturn;
                }
            }

            //调生成任务号方法
            taskX.setTask_no(GenerateTaskNoX());
            taskX.setWarehouse("X库");
            taskX.setStatus("待执行");//待执行
            taskX.setId(null);//id赋空值 自增insert
            boolean boolSave = taskXService.save(taskX);
            if (boolSave) {
                interReturn.setStatus(true);
                interReturn.setMessage("添加箱组库任务成功");
                interReturn.setResult(taskX);
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("添加箱组库任务失败");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("箱组库任务下发出错：" + e.getCause());
            System.out.println("箱组库任务下发出错：" + e.getMessage());
            logger.error("箱组库任务下发出错：" + e.getMessage());
        }
        logger.info("TaskXInsert返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 叉车获取第一条待执行任务 更改状态为“已获取”&填入叉车号
     * @DateTime 2023/9/28 9:46
     * @Params
     * @Return
     */
    @ApiOperation(value = "叉车获取第一条待执行任务")
    @PostMapping("/TaskX/CarGetFirstTask")
    @CrossOrigin
    public InterReturn CarGetFirstTask(String carNo) {
        logger.info("进入 CarGetFirstTask");
        logger.info("接收到参数：" + carNo);
        InterReturn interReturn = new InterReturn();
        try {
            if (carNo.equals("undefined")) {
                interReturn.setStatus(false);
                interReturn.setMessage("叉车号不能为undefined");
                logger.info("CarGetFirstTask返回：" + interReturn);
                return interReturn;
            }
            LambdaQueryWrapper<TaskX> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskX::getStatus, "待执行");//任务状态
            wrapper.orderByAsc(TaskX::getId);//按id顺序 升序
            List<TaskX> taskXList = taskXService.list(wrapper);
            if (taskXList.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("无待执行任务");
                logger.info("CarGetFirstTask返回：" + interReturn);
                return interReturn;
            }
            TaskX taskXNew = taskXList.get(0);
            taskXNew.setUpdate_time(Calendar.getInstance().getTime());
            taskXNew.setCar_no(carNo);
            taskXNew.setStatus("已获取");
            LambdaUpdateWrapper<TaskX> wrapperUpdate = new LambdaUpdateWrapper<>();
            wrapperUpdate.eq(TaskX::getId, taskXNew.getId());
            boolean boolUpdate = taskXService.update(taskXNew, wrapperUpdate);
            if (boolUpdate) {
                interReturn.setStatus(true);
                interReturn.setMessage("箱组库任务更新成功");
                interReturn.setResult(taskXNew);
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("箱组库无任务更新");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("箱组库任务获取出错：" + e.getCause());
            System.out.println("箱组库任务获取出错：" + e.getMessage());
            logger.error("箱组库任务获取出错：" + e.getMessage());
        }
        logger.info("CarGetFirstTask返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:徐浩铖
     * @Description: 按页码条件查询TaskX任务
     * @DateTime: 2023/9/28 10:10
     * @Params:
     */
    @ApiOperation(value = "按页码条件查询TaskX任务", notes = "TaskX类 pageNo pageSize")
    @PostMapping("/TaskX/GetTaskXByPage")
    @CrossOrigin
    public InterReturn GetTaskXByPage(@RequestBody TaskX taskX, int pageNo, int pageSize) {
//        logger.info("进入 GetTaskXByPage");
//        logger.info("接收到参数：" + taskX);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<TaskX> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<TaskX> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(taskX.getTask_no() != null && !taskX.getTask_no().isEmpty(),
                    TaskX::getTask_no, taskX.getTask_no());//任务号
            wrapper.eq(taskX.getLocation_code() != null && !taskX.getLocation_code().isEmpty(),
                    TaskX::getLocation_code, taskX.getLocation_code());//货位号
            wrapper.eq(taskX.getPallet_code() != null && !taskX.getPallet_code().isEmpty(),
                    TaskX::getPallet_code, taskX.getPallet_code());//载具编号
            wrapper.eq(taskX.getStatus() != null && !taskX.getStatus().isEmpty(),
                    TaskX::getStatus, taskX.getStatus());//任务状态
//需要按时间查询时 放开此行              wrapper.between(TaskX::getCreate_time, startTime, endTime);//时间在这两个时间中间
            wrapper.orderByDesc(TaskX::getCreate_time);//倒序
            List<TaskX> taskXList = taskXService.page(page, wrapper).getRecords();

            if (taskXList == null || taskXList.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到TaskX任务！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询TaskX任务成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(taskXList);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询TaskX任务出错：" + e.getCause());
            System.out.println("查询TaskX任务出错：" + e.getMessage());
            logger.error("查询TaskX任务出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
  //      logger.info("GetTaskXByPage 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 按任务号更新任务状态
     * @DateTime 2023/9/29 12:13
     * @Params
     * @Return
     */
    @ApiOperation(value = "按任务号更新任务状态", notes = "传task类 任务号与新状态必填")
    @PostMapping("/TaskX/UpdateTaskXStatusByTaskNo")
    @CrossOrigin
    public InterReturn UpdateTaskXStatusByTaskNo(@RequestBody TaskX taskX) {
        logger.info("进入 UpdateTaskXStatusByTaskNo");
        logger.info("接收到参数：" + taskX);
        InterReturn interReturn = new InterReturn();
        try {
            if (taskX.getTask_no() == null || taskX.getTask_no().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("任务号为空");
                System.out.println("任务号为空");
                logger.info("UpdateTaskXStatusByTaskNo返回：" + interReturn);
                return interReturn;
            }
            if (taskX.getStatus() == null || taskX.getStatus().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("新任务状态为空");
                System.out.println("新任务状态为空");
                logger.info("UpdateTaskXStatusByTaskNo返回：" + interReturn);
                return interReturn;
            }

            LambdaUpdateWrapper<TaskX> wrapperUpdate = new LambdaUpdateWrapper<>();
            wrapperUpdate.eq(TaskX::getTask_no, taskX.getTask_no());//任务号
            TaskX taskXUpdate = new TaskX();
            taskXUpdate.setUpdater(taskX.getUpdater());//更新人
            taskXUpdate.setStatus(taskX.getStatus());//任务状态
            boolean boolUpdate = taskXService.update(taskXUpdate, wrapperUpdate);
            if (boolUpdate) {
                interReturn.setStatus(true);
                interReturn.setMessage("箱组库任务状态更新成功");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("箱组库任务状态更新失败");
                logger.info("UpdateTaskXStatusByTaskNo返回：" + interReturn);
                return interReturn;
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("箱组库任务状态更新出错：" + e.getCause());
            System.out.println("箱组库任务状态更新出错：" + e.getMessage());
            logger.error("箱组库任务状态更新出错：" + e.getMessage());
        }
        logger.info("UpdateTaskXStatusByTaskNo返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 天地人穿梭板号状态
     * @DateTime 2024/6/14 9:39
     * @Params
     * @Return
     */
    @ApiOperation(value = "天地人穿梭板状态", notes = "传穿梭板号")
    @PostMapping("/TaskX/XZKStatus")
    @CrossOrigin
    public InterReturn XZKStatus(@RequestBody SendX sendXFromWeb) {
        logger.info("XZKStatus接收到参数：" + sendXFromWeb);
        InterReturn interReturn = new InterReturn();
        try {
            String URL = environment.getProperty("apiX.status");
            SendX sendX = new SendX();
            sendX.setShuttleId(sendXFromWeb.getShuttleId());//穿梭板号 3001或3002
            logger.info("调用天地人穿梭板状态接口，拼接出参数：" + sendX);
            HttpEntity<SendX> httpEntitysnew = new HttpEntity<>(sendX);

            //参数为 url http实体 返回类型
            GetX response = restTemplate.postForObject(URL, httpEntitysnew, GetX.class);

            if (response.getCode() == 10000) {
                interReturn.setStatus(true);
                interReturn.setMessage("读穿梭板状态成功:" + response.getMsg());
                interReturn.setResult(response.getData());
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("读穿梭板状态出错：" + response.getMsg());
                interReturn.setResult(response.getData());
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("读穿梭板状态调用出错：" + e.getCause());
            System.out.println("读穿梭板状态调用出错：" + e.getMessage());
        }
        logger.info("XZKStatus 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 天地人入库
     * @DateTime 2024/6/13 8:44
     * @Params
     * @Return
     */
    @ApiOperation(value = "天地人入库", notes = "传穿梭板号")
    @PostMapping("/TaskX/XZKIn")
    @CrossOrigin
    public InterReturn XZKIn(@RequestBody SendX sendXFromWeb) {
        logger.info("进入XZKIn");
        logger.info("XZKIn接收到参数：" + sendXFromWeb);
        InterReturn interReturn = new InterReturn();
        try {
            if (sendXFromWeb.getWmsTaskId() == null || sendXFromWeb.getWmsTaskId().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("箱组库入指令 传入指令号错误:" + sendXFromWeb.getWmsTaskId());
                logger.info("XZKIn 返回：" + interReturn);
                return interReturn;
            }

            if (sendXFromWeb.getShuttleId() == 3001 || sendXFromWeb.getShuttleId() == 3002) {

            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("箱组库入指令 传入穿梭板号错误");
                logger.info("XZKIn 返回：" + interReturn);
                return interReturn;
            }

            String URL = environment.getProperty("apiX.inbound");
            SendX sendX = new SendX();
            sendX.setWmsTaskId(sendXFromWeb.getWmsTaskId());//指令号
            sendX.setShuttleId(sendXFromWeb.getShuttleId());//穿梭板号 3001或3002
            sendX.setDirection("1");
            sendX.setPalletId("1");
            sendX.setLocation("1");
            sendX.setPalletLength(1000);
            logger.info("调用天地人入库接口，拼接出参数：" + sendX);
            HttpEntity<SendX> httpEntitysnew = new HttpEntity<>(sendX);

            //参数为 url http实体 返回类型
            GetX response = restTemplate.postForObject(URL, httpEntitysnew, GetX.class);

            if (response.getCode() == 10000) {
                interReturn.setStatus(true);
                interReturn.setMessage("入库指令发送成功:" + response.getMsg());
                interReturn.setResult(response.getData());
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("入库指令发送出错：" + response.getMsg());
                interReturn.setResult(response.getData());
            }

        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("箱组库入指令调用出错：" + e.getCause());
            System.out.println("箱组库入指令调用出错：" + e.getMessage());
        }
        logger.info("XZKIn 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 天地人出库
     * @DateTime 2024/6/14 9:31
     * @Params
     * @Return
     */
    @ApiOperation(value = "天地人出库", notes = "传穿梭板号")
    @PostMapping("/TaskX/XZKOut")
    @CrossOrigin
    public InterReturn XZKOut(@RequestBody SendX sendXFromWeb) {
        logger.info("XZKOut接收到参数：" + sendXFromWeb);
        InterReturn interReturn = new InterReturn();
        try {
            if (sendXFromWeb.getWmsTaskId() == null || sendXFromWeb.getWmsTaskId().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("箱组库出指令 传入指令号错误:" + sendXFromWeb.getWmsTaskId());
                logger.info("XZKIn 返回：" + interReturn);
                return interReturn;
            }

            if (sendXFromWeb.getShuttleId() == 3001 || sendXFromWeb.getShuttleId() == 3002) {


            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("箱组库出指令 传入穿梭板号错误");
                logger.info("XZKOut 返回：" + interReturn);
                return interReturn;
            }

            String URL = environment.getProperty("apiX.outbound");
            SendX sendX = new SendX();
            sendX.setWmsTaskId(sendXFromWeb.getWmsTaskId());//指令号
            sendX.setShuttleId(sendXFromWeb.getShuttleId());//穿梭板号 3001或3002
            sendX.setDirection("1");
            sendX.setPalletId("1");
            sendX.setLocation("1");
            sendX.setPalletLength(1000);
            logger.info("调用天地人出库接口，拼接出参数：" + sendX);
            HttpEntity<SendX> httpEntitysnew = new HttpEntity<>(sendX);

            //参数为 url http实体 返回类型
            GetX response = restTemplate.postForObject(URL, httpEntitysnew, GetX.class);

            if (response.getCode() == 10000) {
                interReturn.setStatus(true);
                interReturn.setMessage("出库指令发送成功:" + response.getMsg());
                interReturn.setResult(response.getData());
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("出库指令发送出错：" + response.getMsg());
                interReturn.setResult(response.getData());
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("箱组库出指令 调用出错：" + e.getCause());
            System.out.println("箱组库出指令 调用出错：" + e.getMessage());
        }
        logger.info("XZKOut 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "天地人指令列表", notes = "传穿梭板号")
    @PostMapping("/TaskX/XZKTask")
    @CrossOrigin
    public InterReturn XZKTask(@RequestBody SendX sendXFromWeb) {
        logger.info("XZKTask接收到参数：" + sendXFromWeb);
        InterReturn interReturn = new InterReturn();
        try {
            String URL = environment.getProperty("apiX.task");
            Map<String, Object> car = new HashMap<>();
            car.put("shuttleId", sendXFromWeb.getShuttleId());
            logger.info("天地人指令列表查询，拼接入参：" + car);
            HttpEntity<Map> httpEntitysnew = new HttpEntity<>(car);

            //参数为 url http实体 返回类型
            GetX response = restTemplate.postForObject(URL, httpEntitysnew, GetX.class);

            if (response.getCode() == 10000) {
                interReturn.setStatus(true);
                interReturn.setMessage("天地人指令列表查询成功:" + response.getMsg());
                interReturn.setResult(response.getData());
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("天地人指令列表查询出错：" + response.getMsg());
                interReturn.setResult(response.getData());
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("天地人指令列表查询 调用出错：" + e.getCause());
            System.out.println("天地人指令列表查询 调用出错：" + e.getMessage());
        }
        logger.info("XZKTask 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "天地人强制完成", notes = "传任务号")
    @PostMapping("/TaskX/XZKCmdopt")
    @CrossOrigin
    public InterReturn XZKCmdopt(@RequestBody SendX sendXFromWeb) {
        logger.info("XZKCmdopt接收到参数：" + sendXFromWeb);
        InterReturn interReturn = new InterReturn();
        try {
            String URL = environment.getProperty("apiX.cmdopt");
            Map<String, Object> del = new HashMap<>();
            del.put("wmsTaskId", sendXFromWeb.getWmsTaskId());
            del.put("option", 1);
            logger.info("天地人强制完成，拼接入参：" + del);
            HttpEntity<Map> httpEntitysnew = new HttpEntity<>(del);

            //参数为 url http实体 返回类型
            GetX response = restTemplate.postForObject(URL, httpEntitysnew, GetX.class);

            if (response.getCode() == 10000) {
                interReturn.setStatus(true);
                interReturn.setMessage("强制完成成功:" + response.getMsg());
                interReturn.setResult(response.getData());
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("强制完成出错：" + response.getMsg());
                interReturn.setResult(response.getData());
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("强制完成调用出错：" + e.getCause());
            System.out.println("强制完成调用出错：" + e.getMessage());
        }
        logger.info("XZKCmdopt 返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:xhc
     * @Description 配盘信息→箱组库任务
     * @DateTime 2023/10/31 16:37
     * @Params
     * @Return
     */
    @ApiOperation(value = "配盘信息→箱组库任务生成")
    @PostMapping("/TaskX/Pick2TaskX")
    @CrossOrigin
    public InterReturn Pick2TaskX(@RequestBody List<OutPick> outPickList) {
        logger.info("进入 Pick2TaskX");
        logger.info("接收到参数：" + outPickList);
        InterReturn interReturn = new InterReturn();
        try {
            for (OutPick outPick : outPickList) {
                if (outPick.getPallet_code() == null || outPick.getPallet_code().length() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("载具号有空值");
                    System.out.println("载具号有空值");
                    logger.info("Pick2TaskX返回：" + interReturn);
                    return interReturn;
                }

                //判断载具是否属于本库 （是否以P7开头）
                if (!outPick.getPallet_code().startsWith("P7")) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("请检查托盘" + outPick.getPallet_code() + "是否为箱组库托盘");
                    System.out.println("请检查托盘" + outPick.getPallet_code() + "是否为箱组库托盘");
                    logger.info("Pick2TaskX返回：" + interReturn);
                    return interReturn;
                }
            }

            //1.按托盘号 去重配盘信息
            List<OutPick> newPickList = outPickList.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(OutPick::getPallet_code))),
                            ArrayList::new));

            interReturn.setMessage("去重后为" + newPickList.size() + "条");

            //2.配盘→任务
            List<TaskX> taskXList = new ArrayList<>();
            //循环生成任务
            for (OutPick outPick : newPickList) {
                TaskX taskX = new TaskX();
                String palletCode = outPick.getPallet_code();
                //taskX.setTask_no("任务号 待改");//任务号移到第三步生成

                //查MAP表 托盘所在货位
                LambdaQueryWrapper<LocationMap> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(LocationMap::getPallet_code, palletCode);
                List<LocationMap> locationMaps = locationMapService.list(wrapper);
                if (locationMaps.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("载具" + palletCode + "无货位数据");
                    logger.info("Pick2TaskX返回：" + interReturn);
                    return interReturn;
                } else {
                    taskX.setLocation_code(locationMaps.get(0).getLocation_code());//货位号
                }

                taskX.setTask_type("出库");//任务类型
                taskX.setPallet_code(palletCode);//载具号
                taskX.setProt_no("站台号 待改");//站台号
                taskX.setWarehouse("X库");//库名
                taskX.setStatus("待执行");//任务状态

                Date time = Calendar.getInstance().getTime();//创建时间赋值
//                taskX.setCreator();
//                taskX.setUpdater();
                taskX.setCreate_time(time);
                taskX.setUpdate_time(time);

                taskXList.add(taskX);
            }

            //3.任务插入数据库
            String taskNoNew = GenerateTaskNoX();
            String newNoPre = taskNoNew.substring(0, 9);//前
            String newNoBack = taskNoNew.substring(9);//后
            int newNo = Integer.parseInt(newNoBack);

            for (int i = 0; i < taskXList.size(); i++) {
                taskXList.get(i).setTask_no(newNoPre + String.format("%04d", (newNo + i)));
            }


            //已得到要插入的task列表 taskXList
            //未得到要冻结的map列表 MAPs

            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            TaskXMapper taskXMapper = sqlSession.getMapper(TaskXMapper.class);//获取对应Mapper
            LocationMapMapper locationMapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper

            for (TaskX taskX : taskXList) {
                taskXMapper.insert(taskX);//插入单条任务

                //冻结map
                LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
                updateWrapperMap.eq(LocationMap::getLocation_code, taskX.getLocation_code());//货位
                updateWrapperMap.eq(LocationMap::getWarehouse, "X库");//库名
                LocationMap LocationMap = new LocationMap();
                LocationMap.setPallet_code(taskX.getPallet_code() + "_" + taskX.getTask_type());//托盘
                LocationMap.setStatus("生成出库冻结");//货位冻结
                locationMapMapper.update(LocationMap, updateWrapperMap);//准备执行sql
            }


            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("生成成功！");
            } catch (Exception e) {
                logger.error("配盘信息→X库任务生成异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("配盘信息→X库任务生成异常，事务回滚:" + e.getCause());
            } finally {
                sqlSession.close();
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("配盘信息→箱组库任务生产出错：" + e.getCause());
            System.out.println("配盘信息→箱组库任务生产出错：" + e.getMessage());
            logger.error("配盘信息→箱组库任务生产出错：" + e.getMessage());
        }
        logger.info("Pick2TaskX返回：" + interReturn);
        return interReturn;
    }

}

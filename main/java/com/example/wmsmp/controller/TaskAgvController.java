package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.task.AGV.*;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.entity.task.CTU.CtuLocationMap;
import com.example.wmsmp.service.AgvLocationMapService;
import com.example.wmsmp.service.AgvTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Api(value = "123", tags = "AGV任务")
@RestController
@CrossOrigin
public class TaskAgvController {
    @Autowired
    AgvTaskService agvTaskService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    @Autowired
    private RestTemplate restTemplate;//用于调用外部接口

    @Autowired
    private Environment environment;//用于读取配置文件

    @Autowired
    AgvLocationMapService agvLocationMapService;

    private static Logger logger = Logger.getLogger(TaskAgvController.class);

    /**
     * @Author:lcy
     * @Description: 生成AGV任务号
     * @DateTime: 2023/9/26 16:30
     * @Params:
     */
    public String GenerateTaskNo() {
        String taskNo = "";
        try {
            LambdaQueryWrapper<AgvTask> agvTaskWrapper = new LambdaQueryWrapper<>();
            List<AgvTask> agvTask = agvTaskService.list(agvTaskWrapper);
            String agvTaskMax = "";
            //取出通过stream里的方法，取出最大的任务号
            if (agvTask.size() > 0)
                agvTaskMax = agvTask.stream().max(Comparator.comparing(AgvTask::getTask_no)).get().getTask_no();
            //获取当前订单日期
            LocalDate currentData = LocalDate.now();
            String currentNo = String.valueOf(currentData);
            currentNo = currentNo.replace("-", "");
            if (agvTaskMax.equals((""))) agvTaskMax = "T" + currentNo + "0000";
            //取出入库单整数部分，用int长度不够，所以的用BigInteger
            BigInteger agvTaskNoTemp = BigInteger.valueOf(Long.parseLong(agvTaskMax.substring(1)));
            if (agvTaskMax.substring(1, agvTaskMax.length() - 4).equals(currentNo)) {
                //BigInteger型加1
                agvTaskNoTemp = agvTaskNoTemp.add(BigInteger.valueOf(1));
                //组成字符型入库单号，返回给前端
                taskNo = "T" + agvTaskNoTemp;
            } else {
                taskNo = "T" + currentNo + "0001";
            }
        } catch (Exception e) {

            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
        }
        return taskNo;
    }

    /**
     * @Author:徐浩铖
     * @Description: 按页码条件查询agv任务
     * @DateTime: 2023/9/27 16:30
     * @Params:
     */
    @ApiOperation(value = "按页码条件查询agv任务", notes = "agvTask类 pageNo pageSize")
    @PostMapping("/TaskAgv/GetAGVByPage")
    @CrossOrigin
    public InterReturn GetAGVByPage(@RequestBody AgvTask agvTask, int pageNo, int pageSize) {
        // logger.info("进入 GetAGVByPage");
        // logger.info("接收到参数：" + agvTask);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<AgvTask> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<AgvTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(agvTask.getTask_no() != null && !agvTask.getTask_no().isEmpty(),
                    AgvTask::getTask_no, agvTask.getTask_no());//任务号
            wrapper.eq(agvTask.getPallet_code() != null && !agvTask.getPallet_code().isEmpty(),
                    AgvTask::getPallet_code, agvTask.getPallet_code());//载具编号
            wrapper.eq(agvTask.getOrg_position() != null && !agvTask.getOrg_position().isEmpty(),
                    AgvTask::getOrg_position, agvTask.getOrg_position());//起始位置
            wrapper.eq(agvTask.getTarget_position() != null && !agvTask.getTarget_position().isEmpty(),
                    AgvTask::getTarget_position, agvTask.getTarget_position());//目标位置
            wrapper.eq(agvTask.getStatus() != null && !agvTask.getStatus().isEmpty(),
                    AgvTask::getStatus, agvTask.getStatus());//任务状态
            wrapper.orderByDesc(AgvTask::getId);//倒序
            //需要按时间查询时 放开此行              wrapper.between(AgvTask::getCreate_time, startTime, endTime);//时间在这两个时间中间

            List<AgvTask> agvTasks = agvTaskService.page(page, wrapper).getRecords();

            if (agvTasks == null || agvTasks.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找agv任务！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询agv任务成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(agvTasks);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询agv任务出错：" + e.getCause());
            System.out.println("查询agv任务出错：" + e.getMessage());
            logger.error("查询agv任务出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        //  logger.info("GetAGVByPage 返回：" + interReturn);
        return interReturn;
    }

//    /**
//     * @Author:xhc
//     * @Description 按任务号更新agv任务状态
//     * @DateTime 2023/9/29 12:13
//     * @Params
//     * @Return
//     */
//    @ApiOperation(value = "按任务号更新任务状态", notes = "传task类 任务号与新状态必填")
//    @PostMapping("/TaskAgv/UpdateAgvTaskStatusByTaskNo")
//    @CrossOrigin
//    public InterReturn UpdateAgvTaskStatusByTaskNo(@RequestBody AgvTask agvTask) {
//        logger.info("进入 UpdateAgvTaskStatusByTaskNo");
//        logger.info("接收到参数：" + agvTask);
//        InterReturn interReturn = new InterReturn();
//        try {
//            if (agvTask.getTask_no() == null || agvTask.getTask_no().length() == 0) {
//                interReturn.setStatus(false);
//                interReturn.setMessage("agv任务号为空");
//                System.out.println("agv任务号为空");
//                logger.info("UpdateAgvTaskStatusByTaskNo返回：" + interReturn);
//                return interReturn;
//            }
//            if (agvTask.getStatus() == null || agvTask.getStatus().length() == 0) {
//                interReturn.setStatus(false);
//                interReturn.setMessage("新agv任务状态为空");
//                System.out.println("新agv任务状态为空");
//                logger.info("UpdateAgvTaskStatusByTaskNo返回：" + interReturn);
//                return interReturn;
//            }
//
//            LambdaUpdateWrapper<AgvTask> wrapperUpdate = new LambdaUpdateWrapper<>();
//            wrapperUpdate.eq(AgvTask::getTask_no, agvTask.getTask_no());//任务号
//            AgvTask agvTaskUpdate = new AgvTask();
//            agvTaskUpdate.setUpdater(agvTask.getUpdater());//更新人
//            agvTaskUpdate.setStatus(agvTask.getStatus());//任务状态
//            boolean boolUpdate = agvTaskService.update(agvTaskUpdate, wrapperUpdate);
//            if (boolUpdate) {
//                interReturn.setStatus(true);
//                interReturn.setMessage("agv任务状态更新成功");
//            } else {
//                interReturn.setStatus(false);
//                interReturn.setMessage("agv任务状态更新失败");
//                logger.info("UpdateAgvTaskStatusByTaskNo返回：" + interReturn);
//                return interReturn;
//            }
//        } catch (Exception e) {
//            interReturn.setStatus(false);
//            interReturn.setMessage("agv任务状态更新出错：" + e.getCause());
//            System.out.println("agv任务状态更新出错：" + e.getMessage());
//            logger.error("agv任务状态更新出错：" + e);
//        }
//        logger.info("UpdateAgvTaskStatusByTaskNo返回：" + interReturn);
//        return interReturn;
//    }


    /**
     * @Author:xhc
     * @Description 下发agv任务 前端调用
     * @DateTime 2024/7/10 22:28
     * @Params
     * @Return
     */
    @ApiOperation(value = "1任务发布", notes = "给agv的任务，托盘号")
    @PostMapping("/TaskAgv/TaskDownload")
    @CrossOrigin
    public InterReturn TaskDownload(@RequestBody AgvTaskSend agvTaskSend, String palletCode, String creater) {
        InterReturn interReturn = new InterReturn();
        try {
            //托盘号为空
            if (palletCode == null || palletCode.length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("托盘号为空");
                System.out.println("托盘号为空");
                logger.info("TaskDownload返回：" + interReturn);
                return interReturn;
            }

            //起点校验
            if (agvTaskSend.getSource() == null || agvTaskSend.getSource().trim().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("起点为空");
                System.out.println("起点为空");
                logger.info("TaskDownload返回：" + interReturn);
                return interReturn;
            } else {
                LambdaQueryWrapper<AgvLocationMap> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(AgvLocationMap::getLocation_code, agvTaskSend.getSource());//起点
                List<AgvLocationMap> agvLocationMaps = agvLocationMapService.list(wrapper);
                if (agvLocationMaps.size() == 0) {
                    //没找到 可能是点位 不处理
                } else {
                    if (!"启用".equals(agvLocationMaps.get(0).getStatus())) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("起点“" + agvLocationMaps.get(0).getLocation_code() + "“被”" + agvLocationMaps.get(0).getPallet_code() + "“占用");
                        logger.error("TaskDownload返回：" + interReturn);
                        return interReturn;
                    }
                }
            }

            //终点校验
            if (agvTaskSend.getTarget() == null || agvTaskSend.getTarget().trim().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("终点为空");
                System.out.println("终点为空");
                logger.info("TaskDownload返回：" + interReturn);
                return interReturn;
            } else {
                LambdaQueryWrapper<AgvLocationMap> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(AgvLocationMap::getLocation_code, agvTaskSend.getTarget());//终点
                List<AgvLocationMap> agvLocationMaps = agvLocationMapService.list(wrapper);
                if (agvLocationMaps.size() == 0) {
                    //没找到 可能是点位 不处理
                } else {
                    if ("启用".equals(agvLocationMaps.get(0).getStatus())) {
                        //启用
                        if (agvLocationMaps.get(0).getPallet_code() != null && agvLocationMaps.get(0).getPallet_code().trim().length() > 0) {
                            //有货
                            interReturn.setStatus(false);
                            interReturn.setMessage("终点“" + agvLocationMaps.get(0).getLocation_code() + "“被”" + agvLocationMaps.get(0).getPallet_code() + "“占用");
                            logger.error("TaskDownload返回：" + interReturn);
                            return interReturn;
                        } else {
                            //无货
                        }
                    } else {
                        //占用
                        interReturn.setStatus(false);
                        interReturn.setMessage("终点“" + agvLocationMaps.get(0).getLocation_code() + "“被”" + agvLocationMaps.get(0).getPallet_code() + "“占用");
                        logger.error("TaskDownload返回：" + interReturn);
                        return interReturn;
                    }
                }
            }

            //任务类型是否为1或2
            if (agvTaskSend.getType() != 1 && agvTaskSend.getType() != 2) {
                interReturn.setStatus(false);
                interReturn.setMessage("任务类型不为木箱搬运或托盘搬运");
                System.out.println("任务类型不为木箱搬运或托盘搬运");
                logger.info("TaskDownload返回：" + interReturn);
                return interReturn;
            }

            String URL = environment.getProperty("apiAGV.TaskDownload");
            String taskno = GenerateTaskNo();
            agvTaskSend.setId(taskno);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
            agvTaskSend.setCreateTime(sdf.format(Calendar.getInstance().getTime()));//当前时间

            logger.info("AGV1任务发布，拼接入参：" + agvTaskSend);
            HttpEntity<AgvTaskSend> httpEntitysnew = new HttpEntity<>(agvTaskSend);
            //参数为 url http实体 返回类型
            AgvRes response = restTemplate.postForObject(URL, httpEntitysnew, AgvRes.class);
            logger.info("AGV1任务发布，接收出参：" + response);
            AgvTask agvTask = new AgvTask();
            //调生成任务号方法
            agvTask.setTask_no(taskno);
            agvTask.setOrg_position(agvTaskSend.getSource());//起点
            agvTask.setTarget_position(agvTaskSend.getTarget());//终点
            agvTask.setPallet_code(palletCode);//托盘号
            String type = "";
            switch (agvTaskSend.getType()) {
                case 1:
                    type = "托盘搬运";
                    break;
                case 2:
                    type = "木箱搬运";
                    break;
                default:
                    break;
            }
            agvTask.setTask_type(type);//任务类型
            agvTask.setCreator(creater);//创建人
            agvTask.setId(null);//id赋空值
            if (response.getCode() == 1) {
                agvTask.setStatus("已下发");//状态
                boolean boolSave = agvTaskService.save(agvTask);
                if (boolSave) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("添加、下发Agv任务成功");
                    {
                        //冻结起点和终点
                        interReturn = FreezeStartEndByTask(taskno);

                    }
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("添加Agv任务失败");
                }
            } else {
                agvTask.setStatus(response.getMsg());//状态
                boolean boolSave = agvTaskService.save(agvTask);
                if (boolSave) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("添加Agv任务成功、下发出错：" + response.getMsg());
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("添加Agv任务失败");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("1任务发布 出错：" + e.getCause());
            System.out.println("1任务发布 出错：" + e.getMessage());
        }
        return interReturn;
    }


    /**
     * @Author:xhc
     * @Description 前端取消agv任务
     * @DateTime 2024/7/10 22:28
     * @Params
     * @Return
     */
    @ApiOperation(value = "2前端任务取消", notes = "任务类 任务的id")
    @PostMapping("/TaskAgv/TaskCancel")
    @CrossOrigin
    public InterReturn TaskCancel(@RequestBody AgvTaskSend agvTaskSend) {
        InterReturn interReturn = new InterReturn();
        try {
            //任务号为空
            if (agvTaskSend.getId() == null || agvTaskSend.getId().equals("")) {
                interReturn.setStatus(false);
                interReturn.setMessage("任务号为空");
                return interReturn;
            }

            String taskid = agvTaskSend.getId();
            String URL = environment.getProperty("apiAGV.TaskCancel");
            HttpEntity<AgvTaskSend> httpEntitysnew = new HttpEntity<>(agvTaskSend);
            //参数为 url http实体 返回类型
            AgvRes response = restTemplate.postForObject(URL, httpEntitysnew, AgvRes.class);
            if (response.getCode() == 1) {
                logger.error("TaskCancel任务下发成功，任务号：" + taskid);
                LambdaUpdateWrapper<AgvTask> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(AgvTask::getTask_no, taskid);
                AgvTask agvTask = new AgvTask();
                agvTask.setStatus("前端取消");
                boolean boolUpdate = agvTaskService.update(agvTask, updateWrapper);
                if (boolUpdate) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("任务号：" + taskid + "取消成功");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("任务号：" + taskid + "取消失败");
                }
            } else {
                logger.error("TaskCancel任务下发失败，任务号：" + taskid);
                interReturn.setStatus(false);
                interReturn.setMessage("任务号：" + taskid + "取消下发失败：" + response.getMsg());
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("2前端任务取消 调用出错：" + e.getCause());
            System.out.println("2前端任务取消 调用出错：" + e.getMessage());
        }
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description agv上报取消任务
     * @DateTime 2024/7/10 22:28
     * @Params
     * @Return
     */
    @ApiOperation(value = "3任务取消", notes = "任务类 任务的id")
    @PostMapping("/TaskAgv/report-task-cancel")
    @CrossOrigin
    public AgvRes ReportTaskCancel(@RequestBody AgvTaskSend agvTaskSend) {
        AgvRes agvRes = new AgvRes();
        try {
            String taskid = agvTaskSend.getId();
            LambdaUpdateWrapper<AgvTask> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AgvTask::getTask_no, taskid);
            AgvTask agvTask = new AgvTask();
            agvTask.setStatus("AGV取消");
            boolean boolUpdate = agvTaskService.update(agvTask, updateWrapper);
            if (boolUpdate) {
                agvRes.setCode(1);
                agvRes.setMsg("agv任务取消成功");
                agvRes.setMsg("3任务取消:" + taskid);
            } else {
                agvRes.setCode(2);
                agvRes.setMsg("agv任务取消失败");
            }
        } catch (Exception e) {
            agvRes.setCode(2);
            agvRes.setMsg("3agv任务取消出错 调用出错：" + e.getCause());
            System.out.println("3agv任务取消出错 调用出错：" + e.getMessage());
        }
        return agvRes;
    }

    /**
     * @Author:xhc
     * @Description agv上报车辆参数状态
     * @DateTime 2024/7/10 22:28
     * @Params
     * @Return
     */
    @ApiOperation(value = "4agv上报车辆参数状态", notes = "AGV状态类 id属性")
    @PostMapping("/TaskAgv/report-agvs-status")
    @CrossOrigin
    public AgvStatus ReportAgvsStatus(@RequestBody AgvStatus agvStatus) {
        //车号不为1或2
        if (agvStatus.getId() != 1 && agvStatus.getId() != 2) {
            logger.error("agv上报车辆参数状态 车号不为1或2");
            return null;
        }


        String URL = environment.getProperty("apiAGV.AGVParameter");
        HttpEntity<AgvStatus> httpEntitysnew = new HttpEntity<>(agvStatus);

        //参数为 url http实体 返回类型
        AgvStatus response = restTemplate.postForObject(URL, httpEntitysnew, AgvStatus.class);


        return response;
    }

    /**
     * @Author:xhc
     * @Description agv上报任务状态
     * @DateTime 2024/7/10 22:28
     * @Params
     * @Return
     */
    @ApiOperation(value = "5agv上报任务状态", notes = "任务状态类")
    @PostMapping("/TaskAgv/report-task-status")
    @CrossOrigin
    public AgvRes ReportTaskStatus(@RequestBody AgvTaskStatus agvTaskStatus) {
        logger.info("agv上报任务状态" + "参数：" + agvTaskStatus);
        AgvRes agvRes = new AgvRes();
        AgvTask agvTask = new AgvTask();
        if (agvTaskStatus.getStatus() == 1) {
            agvRes.setCode(1);
            return agvRes;
        }
        if (agvTaskStatus.getFaultCode() == 0) {//faultCode:0:无异常 ,1.没有叉到货物 ,2.起始点不存在,3.终点不存在,4.任务异常
            //无异常
            if (agvTaskStatus.getStatus() == 2) {//Status:0尚未开始执行,1正在执行,2已完成或不存在
                //已完成
                agvTask.setStatus("已完成");
            }
        } else {
            switch (agvTaskStatus.getFaultCode()) {
                case 1:
                    agvTask.setUdf02("没有叉到货物");
                    break;
                case 2:
                    agvTask.setUdf02("起始点不存在");
                    break;
                case 3:
                    agvTask.setUdf02("终点不存在");
                    break;
                case 4:
                    agvTask.setUdf02("任务异常");
                default:
                    break;
            }
        }
        LambdaUpdateWrapper<AgvTask> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AgvTask::getTask_no, agvTaskStatus.getTaskId());
        boolean boolUpdate = agvTaskService.update(agvTask, updateWrapper);
        if (boolUpdate) {
            agvRes.setCode(1);
            agvRes.setMsg("agv任务状态更新成功");
            {
                //释放货位
                InterReturn interReturn = FreeStartEndByTask(agvTaskStatus.getTaskId());
                if (interReturn.isStatus()) {
                    agvRes.setCode(1);
                    agvRes.setMsg("释放货位成功");
                } else {
                    agvRes.setCode(2);
                    agvRes.setMsg("释放货位失败");
                    logger.error("释放货位失败，任务号：" + agvTaskStatus.getTaskId());
                }
            }
        } else {
            agvRes.setCode(2);
            agvRes.setMsg("agv任务状态更新失败");
        }
        return agvRes;
    }


    @ApiOperation(value = "按区域获取空位置", notes = "Map类 warehouse_type")
    @PostMapping("/TaskAgv/GetEmptyLocationByType")
    @CrossOrigin
    public InterReturn GetEmptyLocationByType(@RequestBody AgvLocationMap agvLocationMap) {
        InterReturn interReturn = new InterReturn();
        //判断区域是否为空值
        if (agvLocationMap.getWarehouse_type() == null || agvLocationMap.getWarehouse_type().trim().equals("")) {
            interReturn.setStatus(false);
            interReturn.setMessage("区域不能为空");
            return interReturn;
        }

        LambdaQueryWrapper<AgvLocationMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgvLocationMap::getWarehouse_type, agvLocationMap.getWarehouse_type());
        queryWrapper.isNull(AgvLocationMap::getPallet_code);
        queryWrapper.eq(AgvLocationMap::getStatus, "启用");
        List<AgvLocationMap> list = agvLocationMapService.list(queryWrapper);
        if (list.size() == 0) {
            interReturn.setStatus(false);
            interReturn.setMessage("无“" + agvLocationMap.getWarehouse_type() + "”空位置");
            return interReturn;
        } else {
            interReturn.setStatus(true);
            interReturn.setMessage("获取“" + agvLocationMap.getWarehouse_type() + "”空位置成功");
            interReturn.setResult(list.get(0));//取第一个
            return interReturn;
        }
    }


    @ApiOperation(value = "根据任务号冻结起点和终点的占用", notes = "任务号")
    @PostMapping("/TaskAgv/FreezeStartEndByTask")
    @CrossOrigin
    @Transactional
    public InterReturn FreezeStartEndByTask(@RequestBody String Task_no) {
        InterReturn interReturn = new InterReturn();
        try {
            //任务号判空
            if (Task_no == null || Task_no.trim().equals("")) {
                interReturn.setStatus(false);
                interReturn.setMessage("任务号不能为空");
                return interReturn;
            }

            Date time = Calendar.getInstance().getTime();//创建时间赋值


            //1.查询任务
            LambdaQueryWrapper<AgvTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AgvTask::getTask_no, Task_no);
            List<AgvTask> list = agvTaskService.list(queryWrapper);
            if (list.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("任务不存在:" + Task_no);
                return interReturn;
            }
            //2.冻结起点终点
            //2.1起点
            LambdaUpdateWrapper<AgvLocationMap> updateWrapper1 = new LambdaUpdateWrapper<>();
            updateWrapper1.eq(AgvLocationMap::getLocation_code, list.get(0).getOrg_position());//起点
            List<AgvLocationMap> list1 = agvLocationMapService.list(updateWrapper1);
            if (list1.size() != 0) {
                updateWrapper1.set(AgvLocationMap::getPallet_code, list.get(0).getPallet_code() + "起点");//载具号
                updateWrapper1.set(AgvLocationMap::getStatus, "起点冻结");//状态
                updateWrapper1.set(AgvLocationMap::getUpdate_time, time);
                boolean boolUpdate1 = agvLocationMapService.update(updateWrapper1);
                if (boolUpdate1) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("冻结起点成功");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("冻结起点失败");
                    return interReturn;
                }
            }
            //2.2终点
            LambdaUpdateWrapper<AgvLocationMap> updateWrapper2 = new LambdaUpdateWrapper<>();
            updateWrapper2.eq(AgvLocationMap::getLocation_code, list.get(0).getTarget_position());//终点
            List<AgvLocationMap> list2 = agvLocationMapService.list(updateWrapper2);
            if (list2.size() != 0) {
                updateWrapper2.set(AgvLocationMap::getPallet_code, list.get(0).getPallet_code() + "终点");//载具号
                updateWrapper2.set(AgvLocationMap::getStatus, "终点冻结");//状态
                updateWrapper2.set(AgvLocationMap::getUpdate_time, time);
                boolean boolUpdate2 = agvLocationMapService.update(updateWrapper2);
                if (boolUpdate2) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("冻结终点成功");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("冻结终点失败");
                    return interReturn;
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("冻结出错：" + e.getMessage());
            return interReturn;
        }

        return interReturn;

    }


    @ApiOperation(value = "根据任务号解冻起点和终点，绑定载具和终点", notes = "任务号")
    @PostMapping("/TaskAgv/FreeStartEndByTask")
    @CrossOrigin
    @Transactional
    public InterReturn FreeStartEndByTask(@RequestBody String Task_no) {
        InterReturn interReturn = new InterReturn();
        try {
            //任务号判空
            if (Task_no == null || Task_no.trim().equals("")) {
                interReturn.setStatus(false);
                interReturn.setMessage("任务号不能为空");
                return interReturn;
            }
            //1.查询任务
            LambdaQueryWrapper<AgvTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AgvTask::getTask_no, Task_no);
            List<AgvTask> list = agvTaskService.list(queryWrapper);
            if (list.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("任务不存在:" + Task_no);
                return interReturn;
            }
            //2.释放起点终点
            //2.1起点
            LambdaUpdateWrapper<AgvLocationMap> updateWrapper1 = new LambdaUpdateWrapper<>();
            updateWrapper1.eq(AgvLocationMap::getLocation_code, list.get(0).getOrg_position());//起点
            List<AgvLocationMap> list1 = agvLocationMapService.list(updateWrapper1);
            if (list1.size() != 0) {
                updateWrapper1.set(AgvLocationMap::getPallet_code, null);//载具号
                updateWrapper1.set(AgvLocationMap::getStatus, "启用");//状态
                boolean boolUpdate1 = agvLocationMapService.update(updateWrapper1);
                if (boolUpdate1) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("解冻起点成功");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("解冻起点失败");
                    return interReturn;
                }
            }
            //2.2终点
            LambdaUpdateWrapper<AgvLocationMap> updateWrapper2 = new LambdaUpdateWrapper<>();
            updateWrapper2.eq(AgvLocationMap::getLocation_code, list.get(0).getTarget_position());//终点
            List<AgvLocationMap> list2 = agvLocationMapService.list(updateWrapper2);
            if (list2.size() != 0) {
                updateWrapper2.set(AgvLocationMap::getPallet_code, list.get(0).getPallet_code());//载具号
                updateWrapper2.set(AgvLocationMap::getStatus, "启用");//状态
                boolean boolUpdate2 = agvLocationMapService.update(updateWrapper2);
                if (boolUpdate2) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("解冻终点、绑定终点载具成功");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("解冻终点、绑定终点载具成功");
                    return interReturn;
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("解除占用出错：" + e.getMessage());
            return interReturn;
        }
        return interReturn;
    }
}


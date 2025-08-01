package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.entity.task.CTU.*;
import com.example.wmsmp.service.CtuLocationMapService;
import com.example.wmsmp.service.CtuTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Api(value = "123", tags = "CTU任务")
@RestController
@CrossOrigin
public class TaskCtuController {
    @Autowired
    CtuTaskService ctuTaskService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    @Autowired
    private RestTemplate restTemplate;//用于调用外部接口

    @Autowired
    private Environment environment;//用于读取配置文件

    @Autowired
    CtuLocationMapService ctuLocationMapService;

    private static Logger logger = Logger.getLogger(TaskCtuController.class);


    /**
     * @Author:lcy
     * @Description: 生成ctu任务号
     * @DateTime: 2023/9/26 16:30
     * @Params:
     */
    public String GenerateTaskNo() {
        String taskNo = "";
        try {
            LambdaQueryWrapper<CtuTask> ctuTaskWrapper = new LambdaQueryWrapper<>();
            List<CtuTask> ctuTasks = ctuTaskService.list(ctuTaskWrapper);
            String ctuTaskMax = "";
            //取出通过stream里的方法，取出最大的任务号
            if (ctuTasks.size() > 0)
                ctuTaskMax = ctuTasks.stream().max(Comparator.comparing(CtuTask::getTask_no)).get().getTask_no();
            //获取当前订单日期
            LocalDate currentData = LocalDate.now();
            String currentNo = String.valueOf(currentData);
            currentNo = currentNo.replace("-", "");
            if (ctuTaskMax.equals((""))) ctuTaskMax = "T" + currentNo + "0000";
            //取出入库单整数部分，用int长度不够，所以的用BigInteger
            BigInteger ctuTaskNoTemp = BigInteger.valueOf(Long.parseLong(ctuTaskMax.substring(1)));
            if (ctuTaskMax.substring(1, ctuTaskMax.length() - 4).equals(currentNo)) {
                //BigInteger型加1
                ctuTaskNoTemp = ctuTaskNoTemp.add(BigInteger.valueOf(1));
                //组成字符型入库单号，返回给前端
                taskNo = "T" + ctuTaskNoTemp;
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
     * @Description: 按页码条件查询ctu任务
     * @DateTime: 2023/9/27 16:30
     * @Params:
     */
    @ApiOperation(value = "按页码条件查询ctu任务", notes = "ctuTask类 pageNo pageSize")
    @PostMapping("/TaskCtu/GetCTUByPage")
    @CrossOrigin
    public InterReturn GetCTUByPage(@RequestBody CtuTask ctuTask, int pageNo, int pageSize) {
        // logger.info("进入 GetCTUByPage");
        // logger.info("接收到参数：" + ctuTask);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<CtuTask> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<CtuTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ctuTask.getTask_no() != null && !ctuTask.getTask_no().isEmpty(),
                    CtuTask::getTask_no, ctuTask.getTask_no());//任务号
            wrapper.eq(ctuTask.getPallet_code() != null && !ctuTask.getPallet_code().isEmpty(),
                    CtuTask::getPallet_code, ctuTask.getPallet_code());//载具编号
            wrapper.eq(ctuTask.getOrg_position() != null && !ctuTask.getOrg_position().isEmpty(),
                    CtuTask::getOrg_position, ctuTask.getOrg_position());//起始位置
            wrapper.eq(ctuTask.getTarget_position() != null && !ctuTask.getTarget_position().isEmpty(),
                    CtuTask::getTarget_position, ctuTask.getTarget_position());//目标位置
            wrapper.eq(ctuTask.getStatus() != null && !ctuTask.getStatus().isEmpty(),
                    CtuTask::getStatus, ctuTask.getStatus());//任务状态
            wrapper.orderByDesc(CtuTask::getId);//倒序
            //需要按时间查询时 放开此行              wrapper.between(CtuTask::getCreate_time, startTime, endTime);//时间在这两个时间中间

            List<CtuTask> ctuTasks = ctuTaskService.page(page, wrapper).getRecords();

            if (ctuTasks == null || ctuTasks.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找ctu任务！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询ctu任务成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(ctuTasks);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询ctu任务出错：" + e.getCause());
            System.out.println("查询ctu任务出错：" + e.getMessage());
            logger.error("查询ctu任务出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        //  logger.info("GetCTUByPage 返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:xhc
     * @Description 下发ctu任务
     * @DateTime 2024/7/10 22:28
     * @Params
     * @Return
     */
    @ApiOperation(value = "1任务发布", notes = "给ctu的任务，托盘号")
    @PostMapping("/TaskCtu/TaskDownload")
    @CrossOrigin
    public InterReturn TaskDownload(@RequestBody CtuTaskSend ctuTaskSend, String palletCode, String creater) {
        InterReturn interReturn = new InterReturn();
        try {
            //托盘号为空
            if (palletCode == null || palletCode.trim().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("料箱号为空");
                System.out.println("料箱号为空");
                logger.info("TaskDownload返回：" + interReturn);
                return interReturn;
            }

            //起点校验
            if (ctuTaskSend.getSource() == null || ctuTaskSend.getSource().trim().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("起点为空");
                System.out.println("起点为空");
                logger.info("TaskDownload返回：" + interReturn);
                return interReturn;
            } else {
                LambdaQueryWrapper<CtuLocationMap> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(CtuLocationMap::getLocation_code, ctuTaskSend.getSource());//起点
                List<CtuLocationMap> ctuLocationMaps = ctuLocationMapService.list(wrapper);
                if (ctuLocationMaps.size() == 0) {
                    //没找到 可能是点位 不处理
                } else {
                    if (!"启用".equals(ctuLocationMaps.get(0).getStatus())) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("起点“" + ctuLocationMaps.get(0).getLocation_code() + "“被”" + ctuLocationMaps.get(0).getPallet_code() + "“占用");
                        logger.error("TaskDownload返回：" + interReturn);
                        return interReturn;
                    }
                }
            }

            //终点校验
            if (ctuTaskSend.getTarget() == null || ctuTaskSend.getTarget().trim().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("终点为空");
                System.out.println("终点为空");
                logger.info("TaskDownload返回：" + interReturn);
                return interReturn;
            } else {
                LambdaQueryWrapper<CtuLocationMap> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(CtuLocationMap::getLocation_code, ctuTaskSend.getTarget());//终点
                List<CtuLocationMap> ctuLocationMaps = ctuLocationMapService.list(wrapper);
                if (ctuLocationMaps.size() == 0) {
                    //没找到 可能是点位 不处理
                } else {
                    if ("启用".equals(ctuLocationMaps.get(0).getStatus())) {
                        //启用
                        if (ctuLocationMaps.get(0).getPallet_code() != null && ctuLocationMaps.get(0).getPallet_code().trim().length() > 0) {
                            //有货
                            interReturn.setStatus(false);
                            interReturn.setMessage("终点“" + ctuLocationMaps.get(0).getLocation_code() + "“被”" + ctuLocationMaps.get(0).getPallet_code() + "“占用");
                            logger.error("TaskDownload返回：" + interReturn);
                            return interReturn;
                        } else {
                            //无货
                        }
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("终点“" + ctuLocationMaps.get(0).getLocation_code() + "“被”" + ctuLocationMaps.get(0).getPallet_code() + "“占用");
                        logger.error("TaskDownload返回：" + interReturn);
                        return interReturn;
                    }
                }
            }


            String URL = environment.getProperty("apiCTU.TaskDownload");
            String taskno = GenerateTaskNo();
            ctuTaskSend.setId(taskno);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
            ctuTaskSend.setCreateTime(sdf.format(Calendar.getInstance().getTime()));//当前时间
            ctuTaskSend.setType(1);//任务类型 固定1
            ctuTaskSend.setBoxType(1);//箱子类型 固定1
            logger.info("CTU1任务发布，拼接入参：" + ctuTaskSend);
            HttpEntity<CtuTaskSend> httpEntitysnew = new HttpEntity<>(ctuTaskSend);
            //参数为 url http实体 返回类型
            CtuRes response = restTemplate.postForObject(URL, httpEntitysnew, CtuRes.class);
            logger.info("CTU1任务发布，接收出参：" + response);
            CtuTask ctuTask = new CtuTask();
            //调生成任务号方法
            ctuTask.setTask_no(taskno);
            ctuTask.setOrg_position(ctuTaskSend.getSource());//起点
            ctuTask.setTarget_position(ctuTaskSend.getTarget());//终点
            ctuTask.setPallet_code(palletCode);//托盘号
            String type = "搬运";
            ctuTask.setTask_type(type);//任务类型
            ctuTask.setCreator(creater);//下发人
            ctuTask.setId(null);//id赋空值
            if (response.getCode() == 1) {
                ctuTask.setStatus("已下发");//状态
                boolean boolSave = ctuTaskService.save(ctuTask);
                if (boolSave) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("添加、下发Ctu任务成功");
                    {
                        //冻结起点和终点
                        interReturn = FreezeStartEndByTask(taskno);
                    }
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("添加Ctu任务失败");
                }
            } else {
                ctuTask.setStatus(response.getMsg());//状态
                boolean boolSave = ctuTaskService.save(ctuTask);
                if (boolSave) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("添加Ctu任务成功、下发出错：" + response.getMsg());
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("添加Ctu任务失败");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("1任务发布 调用出错：" + e.getStackTrace());
            System.out.println("1任务发布 调用出错：" + e.getStackTrace());
        }
        return interReturn;
    }


    /**
     * @Author:xhc
     * @Description 前端取消ctu任务
     * @DateTime 2024/7/10 22:28
     * @Params
     * @Return
     */
    @ApiOperation(value = "2前端任务取消", notes = "任务类 任务的id")
    @PostMapping("/TaskCtu/TaskCancel")
    @CrossOrigin
    public InterReturn TaskCancel(@RequestBody CtuTaskSend ctuTaskSend) {
        InterReturn interReturn = new InterReturn();
        try {
            //任务号为空
            if (ctuTaskSend.getId() == null || ctuTaskSend.getId().equals("")) {
                interReturn.setStatus(false);
                interReturn.setMessage("任务号为空");
                return interReturn;
            }

            String taskid = ctuTaskSend.getId();
            String URL = environment.getProperty("apiCTU.TaskCancel");
            HttpEntity<CtuTaskSend> httpEntitysnew = new HttpEntity<>(ctuTaskSend);
            //参数为 url http实体 返回类型
            CtuRes response = restTemplate.postForObject(URL, httpEntitysnew, CtuRes.class);
            if (response.getCode() == 1) {
                logger.error("TaskCancel任务下发成功，任务号：" + taskid);
                LambdaUpdateWrapper<CtuTask> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(CtuTask::getTask_no, taskid);
                CtuTask ctuTask = new CtuTask();
                ctuTask.setStatus("前端取消");
                boolean boolUpdate = ctuTaskService.update(ctuTask, updateWrapper);
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
     * @Description ctu上报取消任务
     * @DateTime 2024/7/10 22:28
     * @Params
     * @Return
     */
    @ApiOperation(value = "3任务取消", notes = "任务类 任务的id")
    @PostMapping("/TaskCtu/report-task-cancel")
    @CrossOrigin
    public CtuRes ReportTaskCancel(@RequestBody CtuTaskSend ctuTaskSend) {
        CtuRes ctuRes = new CtuRes();
        try {
            String taskid = ctuTaskSend.getId();
            LambdaUpdateWrapper<CtuTask> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CtuTask::getTask_no, taskid);
            CtuTask ctuTask = new CtuTask();
            ctuTask.setStatus("CTU取消");
            ctuTask.setUpdater("CTU取消");
            boolean boolUpdate = ctuTaskService.update(ctuTask, updateWrapper);
            if (boolUpdate) {
                ctuRes.setCode(1);
                ctuRes.setMsg("3任务取消成功:" + taskid);
            } else {
                ctuRes.setCode(2);
                ctuRes.setMsg("3任务取消失败:" + taskid);
            }
        } catch (Exception e) {
            ctuRes.setCode(3);
            ctuRes.setMsg("3ctu任务取消出错 调用出错：" + e.getCause());
            System.out.println("3ctu任务取消出错 调用出错：" + e.getMessage());
        }
        return ctuRes;
    }

    /**
     * @Author:xhc
     * @Description agv上报车辆参数状态 CTU未作此接口
     * @DateTime 2024/7/10 22:28
     * @Params
     * @Return
     */
    @ApiOperation(value = "4查询CTU车辆参数状态", notes = "CTU状态类 id属性")
    //@PostMapping("/TaskCtu/report-agvs-status")
    @CrossOrigin
    public CtuStatusRes ReportAgvsStatus(@RequestBody CtuStatus ctuStatus) {
        //车号不为30975或30976
        if (!ctuStatus.getRobotCode().equals("30975") && !ctuStatus.getRobotCode().equals("30976")) {
            logger.error("agv上报车辆参数状态 车号不为30975或30976");
            return null;
        }

        String URL = environment.getProperty("apiCTU.AGVParameter");
        HttpEntity<CtuStatus> httpEntitysnew = new HttpEntity<>(ctuStatus);

        //参数为 url http实体 返回类型
        CtuStatusRes response = restTemplate.postForObject(URL, httpEntitysnew, CtuStatusRes.class);


        return response;
    }

    /**
     * @Author:xhc
     * @Description 5ctu上报任务状态
     * @DateTime 2024/7/10 22:28
     * @Params
     * @Return
     */
    @ApiOperation(value = "5ctu上报任务状态", notes = "任务状态类")
    @PostMapping("/TaskCtu/report-task-status")
    @CrossOrigin
    public CtuRes ReportTaskStatus(@RequestBody CtuTaskStatus ctuTaskStatus) {
        logger.info("ctu上报任务状态" + "参数：" + ctuTaskStatus);
        CtuRes ctuRes = new CtuRes();
        CtuTask ctuTask = new CtuTask();
        if (ctuTaskStatus.getStatus() == 1) {
            ctuRes.setCode(1);
            return ctuRes;
        }

        switch (ctuTaskStatus.getStatus()) {//Status:1正在执行,2走出储位,3任务完成
            case 2:
                ctuTask.setStatus("走出储位");
                break;
            case 3:
                ctuTask.setStatus("已完成");
                break;
            default:
                break;

        }

        LambdaUpdateWrapper<CtuTask> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CtuTask::getTask_no, ctuTaskStatus.getTaskId());
        boolean boolUpdate = ctuTaskService.update(ctuTask, updateWrapper);
        if (boolUpdate) {
            ctuRes.setCode(1);
            ctuRes.setMsg("ctu任务状态更新成功");
            if (ctuTaskStatus.getStatus() == 3) {
                //释放货位
                InterReturn interReturn = FreeStartEndByTask(ctuTaskStatus.getTaskId());
                if (interReturn.isStatus()) {
                    ctuRes.setCode(1);
                    ctuRes.setMsg("释放货位成功");
                } else {
                    ctuRes.setCode(2);
                    ctuRes.setMsg("释放货位失败");
                    logger.error("释放货位失败，任务号：" + ctuTaskStatus.getTaskId());
                }
            }
        } else {
            ctuRes.setCode(2);
            ctuRes.setMsg("ctu任务状态更新失败");
        }
        return ctuRes;
    }


    @ApiOperation(value = "按区域获取空位置", notes = "Map类 warehouse_type")
    @PostMapping("/TaskCtu/GetEmptyLocationByType")
    @CrossOrigin
    public InterReturn GetEmptyLocationByType(@RequestBody CtuLocationMap ctuLocationMap) {
        InterReturn interReturn = new InterReturn();
        //判断区域是否为空值
        if (ctuLocationMap.getWarehouse_type() == null || ctuLocationMap.getWarehouse_type().trim().equals("")) {
            interReturn.setStatus(false);
            interReturn.setMessage("区域不能为空");
            return interReturn;
        }

        LambdaQueryWrapper<CtuLocationMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CtuLocationMap::getWarehouse_type, ctuLocationMap.getWarehouse_type());
        queryWrapper.isNull(CtuLocationMap::getPallet_code);
        queryWrapper.eq(CtuLocationMap::getStatus, "启用");
        List<CtuLocationMap> list = ctuLocationMapService.list(queryWrapper);
        if (list.size() == 0) {
            interReturn.setStatus(false);
            interReturn.setMessage("无“" + ctuLocationMap.getWarehouse_type() + "”空位置");
            return interReturn;
        } else {
            interReturn.setStatus(true);
            interReturn.setMessage("获取“" + ctuLocationMap.getWarehouse_type() + "”空位置成功");
            interReturn.setResult(list.get(0));//取第一个
            return interReturn;
        }
    }

    @ApiOperation(value = "根据任务号冻结起点和终点", notes = "任务号")
    @PostMapping("/TaskCtu/FreezeStartEndByTask")
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
            //1.查询任务
            LambdaQueryWrapper<CtuTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CtuTask::getTask_no, Task_no);
            List<CtuTask> list = ctuTaskService.list(queryWrapper);
            if (list.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("任务不存在:" + Task_no);
                return interReturn;
            }

            Date time = Calendar.getInstance().getTime();//创建时间赋值

            //2.冻结起点终点
            //2.1起点
            LambdaUpdateWrapper<CtuLocationMap> updateWrapper1 = new LambdaUpdateWrapper<>();
            updateWrapper1.eq(CtuLocationMap::getLocation_code, list.get(0).getOrg_position());//起点
            List<CtuLocationMap> list1 = ctuLocationMapService.list(updateWrapper1);
            if (list1.size() != 0) {
                updateWrapper1.set(CtuLocationMap::getPallet_code, list.get(0).getPallet_code() + "起点");//载具号
                updateWrapper1.set(CtuLocationMap::getStatus, "起点冻结");//状态
                updateWrapper1.set(CtuLocationMap::getUpdate_time, time);
                boolean boolUpdate1 = ctuLocationMapService.update(updateWrapper1);
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
            LambdaUpdateWrapper<CtuLocationMap> updateWrapper2 = new LambdaUpdateWrapper<>();
            updateWrapper2.eq(CtuLocationMap::getLocation_code, list.get(0).getTarget_position());//终点
            List<CtuLocationMap> list2 = ctuLocationMapService.list(updateWrapper2);
            if (list2.size() != 0) {
                {
                    updateWrapper2.set(CtuLocationMap::getPallet_code, list.get(0).getPallet_code() + "终点");//载具号
                    updateWrapper2.set(CtuLocationMap::getStatus, "终点冻结");//状态
                    updateWrapper2.set(CtuLocationMap::getUpdate_time, time);
                    boolean boolUpdate2 = ctuLocationMapService.update(updateWrapper2);
                    if (boolUpdate2) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("冻结终点成功");
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("冻结终点失败");
                        return interReturn;
                    }
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
    @PostMapping("/TaskCtu/FreeStartEndByTask")
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

            Date time = Calendar.getInstance().getTime();//创建时间赋值


            //1.查询任务
            LambdaQueryWrapper<CtuTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CtuTask::getTask_no, Task_no);
            List<CtuTask> list = ctuTaskService.list(queryWrapper);
            if (list.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("任务不存在:" + Task_no);
                return interReturn;
            }
            //2.解冻起点终点
            //2.1起点
            LambdaUpdateWrapper<CtuLocationMap> updateWrapper1 = new LambdaUpdateWrapper<>();
            updateWrapper1.eq(CtuLocationMap::getLocation_code, list.get(0).getOrg_position());//起点
            List<CtuLocationMap> list1 = ctuLocationMapService.list(updateWrapper1);
            if (list1.size() != 0) {
                updateWrapper1.set(CtuLocationMap::getPallet_code, null);//载具号
                updateWrapper1.set(CtuLocationMap::getStatus, "启用");//状态
                updateWrapper1.set(CtuLocationMap::getUpdate_time, time);
                boolean boolUpdate1 = ctuLocationMapService.update(updateWrapper1);
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
            LambdaUpdateWrapper<CtuLocationMap> updateWrapper2 = new LambdaUpdateWrapper<>();
            updateWrapper2.eq(CtuLocationMap::getLocation_code, list.get(0).getTarget_position());//终点
            List<CtuLocationMap> list2 = ctuLocationMapService.list(updateWrapper2);
            if (list2.size() != 0) {
                updateWrapper2.set(CtuLocationMap::getPallet_code, list.get(0).getPallet_code());//载具号
                updateWrapper2.set(CtuLocationMap::getStatus, "启用");//状态
                updateWrapper2.set(CtuLocationMap::getUpdate_time, time);
                boolean boolUpdate2 = ctuLocationMapService.update(updateWrapper2);
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
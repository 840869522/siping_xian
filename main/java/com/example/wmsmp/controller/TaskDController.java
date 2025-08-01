package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.LocationMap;
import com.example.wmsmp.entity.OutPick;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.entity.task.D.*;
import com.example.wmsmp.mapper.LocationMapMapper;
import com.example.wmsmp.mapper.TaskDMapper;
import com.example.wmsmp.service.LocationMapService;
import com.example.wmsmp.service.TaskDService;
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


@Api(value = "123", tags = "任务（大件库）")
@RestController
@CrossOrigin
public class TaskDController {
    private static Logger logger = Logger.getLogger(TaskDController.class);

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    @Autowired
    TaskDService taskDService;

    @Autowired
    LocationMapService locationMapService;

    @Autowired
    private RestTemplate restTemplate;//用于调用外部接口

    @Autowired
    private Environment environment;//用于读取配置文件

    @ApiOperation(value = "大件库单条任务号生成")
    @PostMapping("/TaskD/GenerateTaskNoD")
    @CrossOrigin
    public String GenerateTaskNoD() {
        String taskNoNew = "";
        String str = "TDP";//任务 大件库 托盘库
        try {
            //使用泛型 待改
            LambdaQueryWrapper<TaskD> wrapper = new LambdaQueryWrapper<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");//设定时间格式
            String strTime = sdf.format(Calendar.getInstance().getTime());
            //模糊查询任务号
            wrapper.like(TaskD::getTask_no, str + strTime);
            wrapper.orderByDesc(TaskD::getTask_no);
            List<TaskD> taskList = taskDService.list(wrapper);
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

    @ApiOperation(value = "大件库任务插入")
    @PostMapping("/TaskD/TaskDInsert")
    @CrossOrigin
    public InterReturn TaskDInsert(@RequestBody TaskD taskD) {
        logger.info("进入 TaskDInsert");
        logger.info("接收到参数：" + taskD);
        InterReturn interReturn = new InterReturn();
        try {
            if (taskD.getLocation_code() == null || taskD.getLocation_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("货位号为空");
                System.out.println("货位号为空");
                logger.info("TaskDInsert返回：" + interReturn);
                return interReturn;
            }

            if (taskD.getPallet_code() == null || taskD.getPallet_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("托盘号为空");
                System.out.println("托盘号为空");
                logger.info("TaskDInsert返回：" + interReturn);
                return interReturn;
            }

            //插入前校验
            {
                //1.查询此托盘是否有 任务状态是"待执行" "已下发"  的任务
                LambdaQueryWrapper<TaskD> wrapperTaskD1 = new LambdaQueryWrapper<>();
                wrapperTaskD1.eq(TaskD::getPallet_code, taskD.getPallet_code());
                wrapperTaskD1.in(TaskD::getStatus, Arrays.asList("待执行", "已下发"));//任务状态
                List<TaskD> taskList = taskDService.list(wrapperTaskD1);
                if (taskList.size() > 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage(taskD.getPallet_code() + "托盘已有“待执行”、 ”已下发”任务：" + taskList.get(0).getTask_no());
                    System.out.println(taskD.getPallet_code() + "托盘已有“待执行”、 ”已下发”任务：" + taskList.get(0).getTask_no());
                    logger.info("TaskDInsert 返回：" + interReturn);
                    return interReturn;
                }

                //2.判断此货位是否存在 是否启用
                LambdaQueryWrapper<LocationMap> wrapperMap2 = new LambdaQueryWrapper<>();
                wrapperMap2.eq(LocationMap::getLocation_code, taskD.getLocation_code());
                List<LocationMap> mapList2 = locationMapService.list(wrapperMap2);
                if (mapList2.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("无此货位：" + taskD.getLocation_code());
                    System.out.println("无此货位：" + taskD.getLocation_code());
                    logger.info("TaskDInsert 返回：" + interReturn);
                    return interReturn;
                }
                LocationMap locationMap2 = mapList2.get(0);
                if ("启用".equals(locationMap2.getStatus())) {

                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("货位" + taskD.getLocation_code() + "已禁用");
                    System.out.println("货位" + taskD.getLocation_code() + "已禁用");
                    logger.info("TaskDInsert 返回：" + interReturn);
                    return interReturn;
                }

                //3.判断托盘是否在库 入：在库报警 出：不在库报警
                LambdaQueryWrapper<LocationMap> wrapperMap3 = new LambdaQueryWrapper<>();
                wrapperMap3.eq(LocationMap::getPallet_code, taskD.getPallet_code());
                List<LocationMap> locationMaps3 = locationMapService.list(wrapperMap3);

                //插入前校验 入
                if (("入库").equals(taskD.getTask_type()) ||
                        ("回库").equals(taskD.getTask_type())) {

                    //3.判断托盘是否在库 入：在库报警 出：不在库报警
                    if (locationMaps3.size() > 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("托盘：" + taskD.getPallet_code() + "已在" + locationMaps3.get(0).getLocation_code());
                        System.out.println("托盘：" + taskD.getPallet_code() + "已在" + locationMaps3.get(0).getLocation_code());
                        logger.info("TaskDInsert 返回：" + interReturn);
                        return interReturn;
                    } else {
//                    interReturn.setStatus(false);
//                    interReturn.setMessage("托盘：" + taskD.getPallet_code() + "不在库中" + taskD.getLocation_code());
//                    System.out.println("托盘：" + taskD.getPallet_code() + "不在库中" + taskD.getLocation_code());
//                    logger.info("TaskDInsert 返回：" + interReturn);
//                    return interReturn;
                    }

                    //4.发入任务前判断货位是否有货  无货：发出任务 有货：报警
                    if (locationMap2.getPallet_code() == null || locationMap2.getPallet_code().length() == 0) {
//                    interReturn.setStatus(false);
//                    interReturn.setMessage("货位" + taskD.getLocation_code() + "不存在托盘");
//                    System.out.println("货位" + taskD.getLocation_code() + "不存在托盘");
//                    logger.info("TaskDInsert 返回：" + interReturn);
//                    return interReturn;
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("货位" + taskD.getLocation_code() + "存在托盘：" + locationMap2.getPallet_code());
                        System.out.println("货位" + taskD.getLocation_code() + "存在托盘：" + locationMap2.getPallet_code());
                        logger.info("TaskDInsert 返回：" + interReturn);
                        return interReturn;
                    }
                }

                //插入前校验 出
                else if (("出库").equals(taskD.getTask_type())) {
                    //3.判断托盘是否在库 入：在库报警 出：不在库报警
                    if (locationMaps3.size() > 0) {
//                    interReturn.setStatus(false);
//                    interReturn.setMessage("托盘：" + taskD.getPallet_code() + "已在" + locationMaps3.get(0).getLocation_code());
//                    System.out.println("托盘：" + taskD.getPallet_code() + "已在" + locationMaps3.get(0).getLocation_code());
//                    logger.info("TaskDInsert 返回：" + interReturn);
//                    return interReturn;
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("托盘：" + taskD.getPallet_code() + "不在库中" + taskD.getLocation_code());
                        System.out.println("托盘：" + taskD.getPallet_code() + "不在库中" + taskD.getLocation_code());
                        logger.info("TaskDInsert 返回：" + interReturn);
                        return interReturn;
                    }

                    //4.发出任务前判断货位是否有货 无货：报警 有货：发出任务
                    if (locationMap2.getPallet_code() == null || locationMap2.getPallet_code().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("货位" + taskD.getLocation_code() + "不存在托盘：");
                        System.out.println("货位" + taskD.getLocation_code() + "不存在托盘：");
                        logger.info("TaskDInsert 返回：" + interReturn);
                        return interReturn;
                    } else {
//                    interReturn.setStatus(false);
//                    interReturn.setMessage("货位" + taskD.getLocation_code() + "存在托盘：" + locationMap2.getPallet_code());
//                    System.out.println("货位" + taskD.getLocation_code() + "存在托盘：" + locationMap2.getPallet_code());
//                    logger.info("TaskDInsert 返回：" + interReturn);
//                    return interReturn;
                    }
                }
            }

            //任务类型为出库和入库时候 操作两个表 其余只插入task
            if ("出库".equals(taskD.getTask_type())
                    || "入库".equals(taskD.getTask_type())
                    || "满入入库".equals(taskD.getTask_type())
                    || "回库".equals(taskD.getTask_type())
            ) {
                //mybatisplus
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
                TaskDMapper taskDMapper = sqlSession.getMapper(TaskDMapper.class);//获取对应Mapper
                LocationMapMapper locationMapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper

                Date time = Calendar.getInstance().getTime();//创建时间赋值

                //1.taskD
                taskD.setTask_no(GenerateTaskNoD());//任务号
                taskD.setWarehouse("D库");//库名
                taskD.setStatus("待执行");//待执行
                taskD.setId(null);//id赋空值 自增insert
                taskD.setCreate_time(time);
                taskD.setUpdate_time(time);
                taskDMapper.insert(taskD);//准备执行sql

                //2.MAP
                LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
                updateWrapperMap.eq(LocationMap::getLocation_code, taskD.getLocation_code());//货位
                updateWrapperMap.eq(LocationMap::getWarehouse, "D库");//库名
                LocationMap LocationMap = new LocationMap();
                if ("出库".equals(taskD.getTask_type())) {
                    LocationMap.setPallet_code(taskD.getPallet_code() + "_" + taskD.getTask_type());//托盘
                    LocationMap.setStatus("出库冻结");//货位冻结
                } else if ("入库".equals(taskD.getTask_type())
                        || "满入入库".equals(taskD.getTask_type())
                        || "回库".equals(taskD.getTask_type())) {
                    LocationMap.setPallet_code(taskD.getPallet_code() + "_" + taskD.getTask_type());//托盘
                    LocationMap.setStatus("入库冻结");//货位冻结
                }
                locationMapMapper.update(LocationMap, updateWrapperMap);//准备执行sql

                try {
                    sqlSession.commit();//执行sql
                    interReturn.setStatus(true);
                    interReturn.setMessage("生成成功！");
                } catch (Exception e) {
                    logger.error("插入taskD&更新map异常，事务回滚", e);
                    sqlSession.rollback();
                    interReturn.setMessage("插入taskD&更新map异常，事务回滚:" + e.getCause());
                } finally {
                    sqlSession.close();
                }
            } else {

            //调生成任务号方法
            taskD.setTask_no(GenerateTaskNoD());//任务号
            taskD.setWarehouse("D库");//库名
            taskD.setStatus("待执行");//待执行
            // taskD.setProt_no(taskD.getProt_no());//出库口 前端传 A01 A02
            taskD.setId(null);//id赋空值 自增insert
            boolean boolSave = taskDService.save(taskD);
            if (boolSave) {
                interReturn.setStatus(true);
                interReturn.setMessage("添加大件库任务成功");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("添加大件库任务失败");
            }}
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("大件库任务下发出错：" + e.getCause());
            System.out.println("大件库任务下发出错：" + e.getMessage());
            logger.error("大件库任务下发出错：" + e.getMessage());
        }
        logger.info("TaskDInsert返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "按页码条件查询TaskD任务", notes = "TaskD类 pageNo pageSize")
    @PostMapping("/TaskD/GetTaskDByPage")
    @CrossOrigin
    public InterReturn GetTaskDByPage(@RequestBody TaskD taskD, int pageNo, int pageSize) {
//        logger.info("进入 GetTaskDByPage");
//        logger.info("接收到参数：" + taskD);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<TaskD> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<TaskD> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(taskD.getTask_no() != null && !taskD.getTask_no().isEmpty(),
                    TaskD::getTask_no, taskD.getTask_no());//任务号
            wrapper.eq(taskD.getLocation_code() != null && !taskD.getLocation_code().isEmpty(),
                    TaskD::getLocation_code, taskD.getLocation_code());//货位号
            wrapper.eq(taskD.getPallet_code() != null && !taskD.getPallet_code().isEmpty(),
                    TaskD::getPallet_code, taskD.getPallet_code());//载具编号
            wrapper.eq(taskD.getStatus() != null && !taskD.getStatus().isEmpty(),
                    TaskD::getStatus, taskD.getStatus());//任务状态
//需要按时间查询时 放开此行              wrapper.between(TaskD::getCreate_time, startTime, endTime);//时间在这两个时间中间
            wrapper.orderByDesc(TaskD::getCreate_time);//倒序
            List<TaskD> taskDList = taskDService.page(page, wrapper).getRecords();

            if (taskDList == null || taskDList.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找TaskD任务！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询TaskD任务成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(taskDList);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询TaskD任务出错：" + e.getCause());
            System.out.println("查询TaskD任务出错：" + e.getMessage());
            logger.error("查询TaskD任务出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
    //    logger.info("GetTaskDByPage 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "按任务号更新任务状态", notes = "传task类 任务号与新状态必填")
    @PostMapping("/TaskD/UpdateTaskDStatusByTaskNo")
    @CrossOrigin
    public InterReturn UpdateTaskDStatusByTaskNo(@RequestBody TaskD taskD) {
        logger.info("进入 UpdateTaskDStatusByTaskNo");
        logger.info("接收到参数：" + taskD);
        InterReturn interReturn = new InterReturn();
        try {
            if (taskD.getTask_no() == null || taskD.getTask_no().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("任务号为空");
                System.out.println("任务号为空");
                logger.info("UpdateTaskDStatusByTaskNo返回：" + interReturn);
                return interReturn;
            }
            if (taskD.getStatus() == null || taskD.getStatus().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("新任务状态为空");
                System.out.println("新任务状态为空");
                logger.info("UpdateTaskDStatusByTaskNo返回：" + interReturn);
                return interReturn;
            }

            LambdaUpdateWrapper<TaskD> wrapperUpdate = new LambdaUpdateWrapper<>();
            wrapperUpdate.eq(TaskD::getTask_no, taskD.getTask_no());//任务号
            TaskD taskDUpdate = new TaskD();
            taskDUpdate.setUpdater(taskD.getUpdater());//更新人
            taskDUpdate.setStatus(taskD.getStatus());//任务状态
            boolean boolUpdate = taskDService.update(taskDUpdate, wrapperUpdate);
            if (boolUpdate) {
                interReturn.setStatus(true);
                interReturn.setMessage("大件库任务状态更新成功");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("大件库任务状态更新失败");
                logger.info("UpdateTaskDStatusByTaskNo返回：" + interReturn);
                return interReturn;
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("大件库任务状态更新出错：" + e.getCause());
            System.out.println("大件库任务状态更新出错：" + e.getMessage());
            logger.error("大件库任务状态更新出错：" + e.getMessage());
        }
        logger.info("UpdateTaskDStatusByTaskNo返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 1入库申请 用于wcs调用
     * @DateTime 2024/6/13 15:30
     * @Params
     * @Return
     */
    @ApiOperation(value = "1入库申请", notes = "包含二维码的类")
    @PostMapping("/TaskD/fromwcs/inwaretask")
    @CrossOrigin
    public DReturn inwaretask(@RequestBody Dinware dinware) {
        logger.info("inwaretask 接收到参数：" + dinware);
        DReturn dReturn = new DReturn();
        dReturn.setReturnStatus(-1);
        try {
            String palletCode = dinware.getBarCode();//获取托盘编号
            //1.查任务表 if(没此托盘号任务)
            LambdaQueryWrapper<TaskD> wrapper = new LambdaQueryWrapper<>();

            wrapper.eq(TaskD::getWarehouse, "D库");//库名
            wrapper.eq(TaskD::getPallet_code, palletCode);//载具编号
            wrapper.eq(TaskD::getTask_type, "入库");//任务类型
            wrapper.eq(TaskD::getStatus, "待执行");//任务状态
            wrapper.orderByDesc(TaskD::getTask_no);//倒序2
            List<TaskD> taskDList = taskDService.list(wrapper);
            if (taskDList.size() <= 0) {
                dReturn.setReturnStatus(1);
                dReturn.setReturnInfo("错误：wms无" + palletCode + "托盘入库任务");
            } else {
                //2.分配货位 (根据托盘内的物料 根据MAP的组号)
                //暂时手动下发

                //3.调用接口"3任务接收（发送下游任务）"
                Tasks taskone = new Tasks();
                taskone.setBarCode(palletCode);//托盘码
                taskone.setEndNode(taskDList.get(0).getLocation_code().substring(2, 8));//任务终点
                taskone.setStartNode(dinware.getFromPort());//任务起点
                taskone.setTaskId(taskDList.get(0).getTask_no());//任务号
                taskone.setTaskType(0);//任务类型 0入库 1出库 2移库
                taskone.setOrder(0);//排序 0最先

                InterReturn inreturn = DJKtaskReceive(taskone);//下发入库任务
                if (inreturn.isStatus()) {
                    dReturn.setReturnStatus(0);
                    dReturn.setReturnInfo("大件库入库任务下发成功");
                } else {//
                    dReturn.setReturnStatus(1);
                    dReturn.setReturnInfo(inreturn.getMessage());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
                    dReturn.setMsgTime(sdf.format(Calendar.getInstance().getTime()));
                    logger.info("inwaretask 返回：" + dReturn);

                    //错误信息 复制到任务状态
                    LambdaQueryWrapper<TaskD> wrapperUpdate = new LambdaQueryWrapper<>();
                    wrapperUpdate.eq(TaskD::getTask_no, taskDList.get(0).getTask_no());//任务号
                    TaskD taskDUpdate = new TaskD();
                    taskDUpdate.setStatus(inreturn.getMessage());//任务状态
                    boolean boolUpdate = taskDService.update(taskDUpdate, wrapperUpdate);


                    return dReturn;
                }
                //4.更新任务状态为 "已下发"
                LambdaQueryWrapper<TaskD> wrapperUpdate = new LambdaQueryWrapper<>();
                wrapperUpdate.eq(TaskD::getTask_no, taskDList.get(0).getTask_no());//任务号
                TaskD taskDUpdate = new TaskD();
                taskDUpdate.setStatus("已下发");//任务状态
                boolean boolUpdate = taskDService.update(taskDUpdate, wrapperUpdate);
                if (boolUpdate) {
                    //任务下发成功
                    logger.info("D大件库线程更新任务状态成功：" + taskDList.get(0).getTask_no());
                    dReturn.setReturnStatus(0);
                    dReturn.setReturnInfo("大件库入库任务下发成功，wms更新任务状态成功");
                } else {
                    logger.info("D大件库线程更新任务状态出错：" + taskDList.get(0).getTask_no());
                    dReturn.setReturnStatus(1);
                    dReturn.setReturnInfo("错误：wms更新任务状态出错" + taskDList.get(0).getTask_no());
                }
            }
        } catch (Exception e) {
            dReturn.setReturnStatus(1);
            dReturn.setReturnInfo("大件库入库申请出错：" + e.getCause());
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
        dReturn.setMsgTime(sdf.format(Calendar.getInstance().getTime()));
        logger.info("inwaretask 返回：" + dReturn);
        return dReturn;
    }

    /**
     * @Author:xhc
     * @Description 5任务状态上报 用于wcs调用
     * @DateTime 2024/6/13 16:46
     * @Params
     * @Return
     */
    @ApiOperation(value = "5任务状态上报", notes = "任务状态与任务号等信息")
    @PostMapping("/TaskD/fromwcs/task")
    @CrossOrigin
    public DReturn task(@RequestBody Dtask dtask) {
        //logger.info("task 接收到参数：" + dtask);
        DReturn dReturn = new DReturn();
        dReturn.setReturnStatus(-1);
        try {
            String taskId = dtask.getTaskId();//任务号
            Integer taskStatus = dtask.getTaskStatus();//任务状态  0已接收 1已开始 3任务中断 4放货完成 8任务结束
            if (taskStatus != 8) {
                //dReturn.setReturnInfo("大件库任务状态不为8");
                dReturn.setReturnStatus(0);
                dReturn.setReturnInfo("大件库任务" + taskId + "，状态" + taskStatus + "，上报接收成功");
                System.out.println("大件库任务" + taskId + "，状态" + taskStatus + "，上报接收成功");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
                dReturn.setMsgTime(sdf.format(Calendar.getInstance().getTime()));
                return dReturn;
            } else {
                //根据任务号查询到任务
                LambdaQueryWrapper<TaskD> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(TaskD::getTask_no, taskId);
                List<TaskD> taskDList = taskDService.list(queryWrapper);

                //1.更改任务表状态为 "已完成"
                LambdaUpdateWrapper<TaskD> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(TaskD::getTask_no, taskId);//任务号
                TaskD taskD = new TaskD();
                taskD.setStatus("已完成");
                boolean boolUpdateTask = taskDService.update(taskD, updateWrapper);
                if (boolUpdateTask) {
                    //更新任务状态成功
                    logger.info("大件库更新任务状态成功：" + taskId);
                } else {
                    logger.info("大件库更新任务状态出错：" + taskId);
                    dReturn.setReturnStatus(1);
                    dReturn.setReturnInfo("大件库更新任务状态出错：" + taskId);
                    return dReturn;
                }


                //2.更改map表状态 出库为清空 入库绑定
                if ("出库".equals(taskDList.get(0).getTask_type())) {
                    // 出库清空
                    LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
                    updateWrapperMap.eq(LocationMap::getLocation_code, taskDList.get(0).getLocation_code());//货位号
                    updateWrapperMap.eq(LocationMap::getWarehouse, "D库");//库名
                    LocationMap locationMap = new LocationMap();
                    locationMap.setPallet_code("");//托盘清空
                    locationMap.setStatus("启用");//货位解除冻结
                    boolean boolUpdateMap = locationMapService.update(locationMap, updateWrapperMap);
                    if (boolUpdateMap) {
                        logger.info("大件库更新MAP出成功：" + taskId);
                    } else {
                        logger.info("大件库更新MAP出错误：" + taskId);
                        dReturn.setReturnStatus(1);
                        dReturn.setReturnInfo("大件库更新MAP出错误：" + taskId);
                        return dReturn;
                    }
                } else if ("入库".equals(taskDList.get(0).getTask_type())) {
                    // 入库绑定
                    LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
                    updateWrapperMap.eq(LocationMap::getLocation_code, "DP" + dtask.getGridId());//货位号
                    updateWrapperMap.eq(LocationMap::getWarehouse, "D库");//库名
                    LocationMap locationMap = new LocationMap();
                    locationMap.setPallet_code(taskDList.get(0).getPallet_code());//托盘号
                    locationMap.setStatus("启用");//货位解除冻结
                    boolean boolUpdateMap = locationMapService.update(locationMap, updateWrapperMap);
                    if (boolUpdateMap) {
                        logger.info("大件库更新MAP入成功：" + taskId);
                    } else {
                        logger.info("大件库更新MAP入出错：" + taskId);
                        dReturn.setReturnStatus(1);
                        dReturn.setReturnInfo("大件库更新MAP入出错：" + taskId);
                        return dReturn;
                    }
                } else if ("移库".equals(taskDList.get(0).getTask_type())) {
                    //移库更新两个MAP
                    //mybatisplus
                    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
                    LocationMapMapper mapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper
                    Date time = Calendar.getInstance().getTime();//更新时间赋值

                    //1.原货位 清空
                    LambdaUpdateWrapper<LocationMap> updateWrapperMapOld = new LambdaUpdateWrapper<>();
                    updateWrapperMapOld.eq(LocationMap::getLocation_code, taskDList.get(0).getUdf03());//原货位
                    updateWrapperMapOld.eq(LocationMap::getWarehouse, "D库");//库名
                    LocationMap LocationMapOld = new LocationMap();
                    LocationMapOld.setUpdate_time(time);
                    LocationMapOld.setPallet_code("");//托盘清空
                    LocationMapOld.setStatus("启用");//货位解除冻结
                    mapMapper.update(LocationMapOld, updateWrapperMapOld);//准备执行sql
                    //2.目标货位 绑定
                    LambdaUpdateWrapper<LocationMap> updateWrapperMapNew = new LambdaUpdateWrapper<>();
                    updateWrapperMapNew.eq(LocationMap::getLocation_code,"DP" + dtask.getGridId());//目标货位
                    updateWrapperMapNew.eq(LocationMap::getWarehouse, "D库");//库名
                    LocationMap LocationMapNew = new LocationMap();
                    LocationMapNew.setUpdate_time(time);
                    LocationMapNew.setPallet_code(taskDList.get(0).getPallet_code());//料箱绑定
                    LocationMapNew.setStatus("启用");//货位解除冻结
                    mapMapper.update(LocationMapNew, updateWrapperMapNew);//准备执行sql

                    try {
                        sqlSession.commit();//执行sql
                        dReturn.setReturnInfo("大件库移库更新MAP成功：" + taskId);
                    } catch (Exception e) {
                        logger.error("大件库移库更新MAP异常，事务回滚", e);
                        sqlSession.rollback();
                        dReturn.setReturnStatus(0);
                        dReturn.setReturnInfo("大件库移库更新MAP异常：" + taskId + ":" + e);
                    } finally {
                        sqlSession.close();
                    }

                } else {
                    // 未知任务类型的处理逻辑
                    logger.info("未知任务类型：" + taskId + ":" + taskDList.get(0).getTask_type());
                    dReturn.setReturnStatus(0);
                    dReturn.setReturnInfo("未知任务类型：" + taskId + ":" + taskDList.get(0).getTask_type());
                    System.out.println("未知任务类型：" + taskId + ":" + taskDList.get(0).getTask_type());
                }
                dReturn.setReturnStatus(0);
                dReturn.setReturnInfo("大件库任务" + taskId + "已完成，状态上报成功");
                System.out.println("大件库任务" + taskId + "已完成，状态上报成功");
            }
        } catch (Exception e) {
            dReturn.setReturnStatus(1);
            dReturn.setReturnInfo("大件库任务状态上报出错：" + e.getCause());
            System.out.println("大件库任务状态上报出错：" + e.getCause());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
        dReturn.setMsgTime(sdf.format(Calendar.getInstance().getTime()));
        //.toString("yyyy-MM-dd HH:mm:ss")
        logger.info("task 返回：" + dReturn);
        return dReturn;
    }

    /**
     * @Author:xhc
     * @Description 3任务接收（发送下游任务）
     * @DateTime 2024/6/13 17:10
     * @Params
     * @Return
     */
    @ApiOperation(value = "3任务接收（发送下游任务）", notes = "任务类 货位号为截取后的位数字")
    @PostMapping("/TaskD/DJKtaskReceive")
    @CrossOrigin
    public InterReturn DJKtaskReceive(@RequestBody Tasks task) {
        logger.info("进入 DJKtaskReceive");
        logger.info("接收到参数：" + task);
        InterReturn interReturn = new InterReturn();
        try {
            //String URL = apiDConfig.getApitaskReceiveURL();
            String URL = environment.getProperty("apiD.taskReceive");

            DtaskReceive dtaskReceive = new DtaskReceive();
            dtaskReceive.setGroupId("组号");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
            dtaskReceive.setMsgTime(sdf.format(Calendar.getInstance().getTime()));//下发时间
            dtaskReceive.setPriorityCode(0);//优先级
            dtaskReceive.setWarehouse("SPJG");//仓库名称 固定值SPJG

            List<Tasks> tasksList = new ArrayList<>();
            Tasks tasks1 = new Tasks();
            tasks1.setBarCode(task.getBarCode());//托盘码
            tasks1.setEndNode(task.getEndNode());//任务终点
            tasks1.setStartNode(task.getStartNode());//任务起点
            tasks1.setTaskId(task.getTaskId());//任务号
            tasks1.setTaskType(task.getTaskType());//任务类型 0入库 1出库 2移库
            tasks1.setOrder(0);//排序 0最先
            tasksList.add(tasks1);
            dtaskReceive.setTasks(tasksList);
            logger.info("大件库3任务下发，拼接入参：" + dtaskReceive);
            HttpEntity<DtaskReceive> httpEntitysnew = new HttpEntity<>(dtaskReceive);


            //参数为 url http实体 返回类型
            DReturn response = restTemplate.postForObject(URL, httpEntitysnew, DReturn.class);

            if (response.getReturnStatus() == 0) {
                interReturn.setStatus(true);
                interReturn.setMessage("发送下游任务成功");
                interReturn.setResult(response);
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("发送下游任务出错：" + response.getReturnInfo());
                interReturn.setResult(response);
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("发送下游任务 调用出错：" + e.getCause());
            System.out.println("发送下游任务 调用出错：" + e.getMessage());
        }
        logger.info("DJKtaskReceive 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "4任务目的位置更改 ", notes = "任务类  taskId属性 endNode属性")
    @PostMapping("/TaskD/DJKtaskChange")
    @CrossOrigin
    public InterReturn DJKtaskChange(@RequestBody Map map) {
        logger.info("DJKtaskChange 接收到参数：" + map);
        InterReturn interReturn = new InterReturn();
        try {
            //String URL = apiDConfig.getApitaskChangeURL();
            String URL = environment.getProperty("apiD.taskChange");

            Map<String, Object> change = new HashMap<>();
            change.put("taskId", map.get("taskId"));//任务号
            change.put("endNode", map.get("endNode"));//新的终点
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
            change.put("msgTime", sdf.format(Calendar.getInstance().getTime()));//当前时间

            logger.info("大件库4任务目的位置更改，拼接入参：" + change);
            HttpEntity<Map> httpEntitysnew = new HttpEntity<>(change);

            //参数为 url http实体 返回类型
            DReturn response = restTemplate.postForObject(URL, httpEntitysnew, DReturn.class);

            if (response.getReturnStatus() == 0) {
                interReturn.setStatus(true);
                interReturn.setMessage("4任务目的位置更改成功");
                interReturn.setResult(response);
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("4任务目的位置更改出错：" + response.getReturnInfo());
                interReturn.setResult(response);
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("4任务目的位置更改 调用出错：" + e.getCause());
            System.out.println("4任务目的位置更改 调用出错：" + e.getMessage());
        }
        logger.info("DJKtaskChange 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "6任务取消", notes = "Map类型 taskId")
    @PostMapping("/TaskD/DJKtaskCancel")
    @CrossOrigin
    public InterReturn DJKtaskCancel(@RequestBody Map map) {
        logger.info("进入 DJKtaskCancel");
        logger.info("接收到参数：" + map);
        InterReturn interReturn = new InterReturn();
        try {
            //String URL = apiDConfig.getApitaskCancelURL();
            String URL = environment.getProperty("apiD.taskCancel");
            Map<String, Object> del = new HashMap<>();
            del.put("taskId", map.get("taskId"));//任务号
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
            del.put("msgTime", sdf.format(Calendar.getInstance().getTime()));//当前时间

            logger.info("大件库6任务取消，拼接入参：" + del);
            HttpEntity<Map> httpEntitysnew = new HttpEntity<>(del);

            //参数为 url http实体 返回类型
            DReturn response = restTemplate.postForObject(URL, httpEntitysnew, DReturn.class);

            if (response.getReturnStatus() == 0) {
                interReturn.setStatus(true);
                interReturn.setMessage("任务取消成功");
                interReturn.setResult(response);
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("任务取消出错：" + response.getReturnInfo());
                interReturn.setResult(response);
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("任务取消 调用出错：" + e.getCause());
            System.out.println("任务取消 调用出错：" + e.getMessage());
        }
        logger.info("DJKtaskCancel 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "9货位信息同步", notes = "Map类型 cargoLocationId和cargoLocationStatus")
    @PostMapping("/TaskD/DJKcargoLocationSyn")
    @CrossOrigin
    public InterReturn DJKcargoLocationSyn(@RequestBody Map map) {
        logger.info("进入 DJKcargoLocationSyn");
        logger.info("接收到参数：" + map);
        InterReturn interReturn = new InterReturn();
        try {
            //String URL = apiDConfig.getApicargoLocationSynURL();
            String URL = environment.getProperty("apiD.cargoLocationSyn");
            Map<String, Object> Syn = new HashMap<>();
            Syn.put("warehouse", "SPJG");//仓库编码 固定值SPJG
            Syn.put("cargoLocationId", map.get("cargoLocationId"));//货位号
            Syn.put("cargoLocationStatus", map.get("cargoLocationStatus"));//Y有货 N无货
            logger.info("大件库9货位信息同步，拼接入参：" + Syn);
            HttpEntity<Map> httpEntitysnew = new HttpEntity<>(Syn);

            //参数为 url http实体 返回类型
            DReturn response = restTemplate.postForObject(URL, httpEntitysnew, DReturn.class);
            if (response.getReturnStatus() == 0) {
                interReturn.setStatus(true);
                interReturn.setMessage("货位信息同步成功");
                interReturn.setResult(response);
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("货位信息同步出错：" + response.getReturnInfo());
                interReturn.setResult(response);
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("货位信息同步 调用出错：" + e.getCause());
            System.out.println("货位信息同步 调用出错：" + e.getMessage());
        }
        logger.info("DJKcargoLocationSyn 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "12设备状态", notes = "穿梭车编号")
    @PostMapping("/TaskD/DJKdeviceStatus")
    @CrossOrigin
    public InterReturn DJKdeviceStatus(@RequestBody String carID) {
        logger.info("进入 DJKdeviceStatus");
        logger.info("接收到参数：" + carID);
        InterReturn interReturn = new InterReturn();
        try {
            //String URL = apiDConfig.getApideviceStatusURL();
            String URL = environment.getProperty("apiD.deviceStatus");
            Map<String, Object> del = new HashMap<>();
            del.put("deviceNo", carID);//设备编号
            del.put("deviceType ", 1);//设备类型
            logger.info("大件库12设备状态，拼接入参：" + del);
            HttpEntity<Map> httpEntitysnew = new HttpEntity<>(del);

            //参数为 url http实体 返回类型
            DReturn response = restTemplate.postForObject(URL, httpEntitysnew, DReturn.class);
            if (response.getReturnStatus() == 0) {
                interReturn.setStatus(true);
                interReturn.setMessage("设备状态成功");
                interReturn.setResult(response);
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("设备状态出错：" + response.getReturnInfo());
                interReturn.setResult(response);
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("设备状态 调用出错：" + e.getCause());
            System.out.println("设备状态 调用出错：" + e.getMessage());
        }
        logger.info("DJKdeviceStatus 返回：" + interReturn);
        return interReturn;
    }



    @ApiOperation(value = "配盘信息→大件库任务生成")
    @PostMapping("/TaskD/Pick2TaskD")
    @CrossOrigin
    public InterReturn Pick2TaskD(@RequestBody List<OutPick> outPickList) {
        logger.info("进入 Pick2TaskD");
        logger.info("接收到参数：" + outPickList);
        InterReturn interReturn = new InterReturn();
        try {
            for (OutPick outPick : outPickList) {
                if (outPick.getPallet_code() == null || outPick.getPallet_code().length() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("载具号有空值");
                    System.out.println("载具号有空值");
                    logger.info("Pick2TaskD返回：" + interReturn);
                    return interReturn;
                }

                //判断载具是否属于本库 （是否以P6开头）
                if (!outPick.getPallet_code().startsWith("P6")) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("请检查托盘" + outPick.getPallet_code() + "是否为大件库托盘");
                    System.out.println("请检查托盘" + outPick.getPallet_code() + "是否为大件库托盘");
                    logger.info("Pick2TaskD返回：" + interReturn);
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
            List<TaskD> taskDList = new ArrayList<>();
            //循环生成任务
            for (OutPick outPick : newPickList) {
                TaskD taskD = new TaskD();
                String palletCode = outPick.getPallet_code();

                //查MAP表 托盘所在货位
                LambdaQueryWrapper<LocationMap> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(LocationMap::getPallet_code, palletCode);
                List<LocationMap> locationMaps = locationMapService.list(wrapper);
                if (locationMaps.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("载具" + palletCode + "无货位数据");
                    logger.info("Pick2TaskD返回：" + interReturn);
                    return interReturn;
                } else {
                    taskD.setLocation_code(locationMaps.get(0).getLocation_code());//货位号

                    //************排序的重点
                    taskD.setUdf01(locationMaps.get(0).getLocation_code_d());//货位的组号
                }

                taskD.setTask_type("出库");//任务类型
                taskD.setPallet_code(palletCode);//载具号
                taskD.setProt_no("站台号 待改");//站台号
                taskD.setWarehouse("D库");//库名
                taskD.setStatus("待执行");//任务状态

                Date time = Calendar.getInstance().getTime();//创建时间赋值
//                taskD.setCreator();
//                taskD.setUpdater();
                taskD.setCreate_time(time);
                taskD.setUpdate_time(time);

                taskDList.add(taskD);
            }

            //3.任务插入数据库
            String taskNoNew = GenerateTaskNoD();
            String newNoPre = taskNoNew.substring(0, 9);//前
            String newNoBack = taskNoNew.substring(9);//后
            int newNo = Integer.parseInt(newNoBack);

            //按组号升序排序
            taskDList.sort(Comparator.comparing(TaskD::getUdf01));//TaskD::getUdf01 货位的组号

            for (int i = 0; i < taskDList.size(); i++) {
                taskDList.get(i).setTask_no(newNoPre + String.format("%04d", (newNo + i)));
                //出库口赋值 都从二号口出
                taskDList.get(i).setProt_no("A02");
            }


            //已得到要插入的task列表 taskDList
            //未得到要冻结的map列表 MAPs

            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            TaskDMapper taskDMapper = sqlSession.getMapper(TaskDMapper.class);//获取对应Mapper
            LocationMapMapper locationMapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper

            for (TaskD taskD : taskDList) {
                taskDMapper.insert(taskD);//插入单条任务

                //冻结map
                LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
                updateWrapperMap.eq(LocationMap::getLocation_code, taskD.getLocation_code());//货位
                updateWrapperMap.eq(LocationMap::getWarehouse, "D库");//库名
                LocationMap LocationMap = new LocationMap();
                LocationMap.setPallet_code(taskD.getPallet_code() + "_" + taskD.getTask_type());//托盘
                LocationMap.setStatus("生成出库冻结");//货位冻结
                locationMapMapper.update(LocationMap, updateWrapperMap);//准备执行sql
            }


            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("生成成功！");
            } catch (Exception e) {
                logger.error("配盘信息→D库任务生成异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("配盘信息→D库任务生成异常，事务回滚:" + e.getCause());
            } finally {
                sqlSession.close();
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("配盘信息→大件库任务生产出错：" + e.getCause());
            System.out.println("配盘信息→大件库任务生产出错：" + e.getMessage());
            logger.error("配盘信息→大件库任务生产出错：" + e.getMessage());
        }
        logger.info("Pick2TaskD返回：" + interReturn);
        return interReturn;
    }

}

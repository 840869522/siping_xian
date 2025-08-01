package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.LocationMap;
import com.example.wmsmp.entity.OutPick;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.entity.task.Z.*;
import com.example.wmsmp.mapper.LocationMapMapper;
import com.example.wmsmp.mapper.TaskPMapper;
import com.example.wmsmp.service.LocationMapService;
import com.example.wmsmp.service.TaskPService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Api(value = "123", tags = "任务（P托盘库）")
@RestController
@CrossOrigin
public class TaskPController {
    private static Logger logger = Logger.getLogger(TaskPController.class);

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    @Autowired
    TaskPService taskPService;

    @Autowired
    LocationMapService locationMapService;

    public String GenerateTaskNoP() {
        String taskNoNew = "";
        String str = "TZP";//任务 自动化库 托盘库
        try {
            //使用泛型 待改
            LambdaQueryWrapper<TaskP> wrapper = new LambdaQueryWrapper<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");//设定时间格式
            String strTime = sdf.format(Calendar.getInstance().getTime());
            //模糊查询任务号
            wrapper.like(TaskP::getTask_no, str + strTime);
            wrapper.orderByDesc(TaskP::getTask_no);
            List<TaskP> taskList = taskPService.list(wrapper);
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

    @ApiOperation(value = "托盘库任务插入")
    @PostMapping("/TaskP/TaskPInsert")
    @CrossOrigin
    public InterReturn TaskPInsert(@RequestBody TaskP taskP) {
        logger.info("进入 TaskPInsert");
        logger.info("接收到参数：" + taskP);
        InterReturn interReturn = new InterReturn();
        try {
            if (taskP.getLocation_code() == null || taskP.getLocation_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("货位号为空");
                System.out.println("货位号为空");
                logger.info("TaskPInsert 返回：" + interReturn);
                return interReturn;
            }

            if (taskP.getPallet_code() == null || taskP.getPallet_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("托盘号为空");
                System.out.println("托盘号为空");
                logger.info("TaskPInsert 返回：" + interReturn);
                return interReturn;
            }


            //插入前校验
            if ("空托码垛".equals(taskP.getTask_type())) {


            } else {
                //1.查询此托盘是否有 任务状态是"待执行" "已下发"  的任务
                if ("空托盘垛".equals(taskP.getTask_type())) {

                } else {
                    LambdaQueryWrapper<TaskP> wrapperTaskP1 = new LambdaQueryWrapper<>();
                    wrapperTaskP1.eq(TaskP::getPallet_code, taskP.getPallet_code());
                    wrapperTaskP1.in(TaskP::getStatus, Arrays.asList("待执行", "已下发"));//任务状态
                    List<TaskP> taskList = taskPService.list(wrapperTaskP1);
                    if (taskList.size() > 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage(taskP.getPallet_code() + "托盘已有“待执行”、 ”已下发”任务：" + taskList.get(0).getTask_no());
                        System.out.println(taskP.getPallet_code() + "托盘已有“待执行”、 ”已下发”任务：" + taskList.get(0).getTask_no());
                        logger.info("TaskPInsert 返回：" + interReturn);
                        return interReturn;
                    }
                }
                //2.判断此货位是否存在 是否启用
                LambdaQueryWrapper<LocationMap> wrapperMap2 = new LambdaQueryWrapper<>();
                wrapperMap2.eq(LocationMap::getLocation_code, taskP.getLocation_code());
                List<LocationMap> mapList2 = locationMapService.list(wrapperMap2);
                if (mapList2.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("无此货位：" + taskP.getLocation_code());
                    System.out.println("无此货位：" + taskP.getLocation_code());
                    logger.info("TaskPInsert 返回：" + interReturn);
                    return interReturn;
                }
                LocationMap locationMap2 = mapList2.get(0);
                if ("启用".equals(locationMap2.getStatus())) {

                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("货位" + taskP.getLocation_code() + "已禁用");
                    System.out.println("货位" + taskP.getLocation_code() + "已禁用");
                    logger.info("TaskPInsert 返回：" + interReturn);
                    return interReturn;
                }

                //3.判断托盘是否在库 入：在库报警 出：不在库报警
                if ("空托盘垛".equals(taskP.getTask_type())) {

                } else {
                    LambdaQueryWrapper<LocationMap> wrapperMap3 = new LambdaQueryWrapper<>();
                    wrapperMap3.eq(LocationMap::getPallet_code, taskP.getPallet_code());
                    List<LocationMap> locationMaps3 = locationMapService.list(wrapperMap3);


                    if (("入库").equals(taskP.getTask_type()) ||
                            ("回库").equals(taskP.getTask_type())) {
                        //插入前校验 入
                        //3.判断托盘是否在库 入：在库报警 出：不在库报警
                        if (locationMaps3.size() > 0) {
                            interReturn.setStatus(false);
                            interReturn.setMessage("托盘：" + taskP.getPallet_code() + "已在" + locationMaps3.get(0).getLocation_code());
                            System.out.println("托盘：" + taskP.getPallet_code() + "已在" + locationMaps3.get(0).getLocation_code());
                            logger.info("TaskPInsert 返回：" + interReturn);
                            return interReturn;
                        } else {
//                    interReturn.setStatus(false);
//                    interReturn.setMessage("托盘：" + taskP.getPallet_code() + "不在库中" + taskP.getLocation_code());
//                    System.out.println("托盘：" + taskP.getPallet_code() + "不在库中" + taskP.getLocation_code());
//                    logger.info("TaskPInsert 返回：" + interReturn);
//                    return interReturn;
                        }

                        //4.发入任务前判断货位是否有货  无货：发出任务 有货：报警
                        if (locationMap2.getPallet_code() == null || locationMap2.getPallet_code().length() == 0) {
//                    interReturn.setStatus(false);
//                    interReturn.setMessage("货位" + taskP.getLocation_code() + "不存在托盘");
//                    System.out.println("货位" + taskP.getLocation_code() + "不存在托盘");
//                    logger.info("TaskPInsert 返回：" + interReturn);
//                    return interReturn;
                        } else {
                            interReturn.setStatus(false);
                            interReturn.setMessage("货位" + taskP.getLocation_code() + "存在托盘：" + locationMap2.getPallet_code());
                            System.out.println("货位" + taskP.getLocation_code() + "存在托盘：" + locationMap2.getPallet_code());
                            logger.info("TaskPInsert 返回：" + interReturn);
                            return interReturn;
                        }
                    } else if (("出库").equals(taskP.getTask_type())) {
                        //插入前校验 出
                        //3.判断托盘是否在库 入：在库报警 出：不在库报警
                        if (locationMaps3.size() > 0) {
//                            interReturn.setStatus(false);
//                            interReturn.setMessage("托盘：" + taskP.getPallet_code() + "已在" + locationMaps3.get(0).getLocation_code());
//                            System.out.println("托盘：" + taskP.getPallet_code() + "已在" + locationMaps3.get(0).getLocation_code());
//                            logger.info("TaskPInsert 返回：" + interReturn);
//                            return interReturn;
                        } else {
                            interReturn.setStatus(false);
                            interReturn.setMessage("托盘：" + taskP.getPallet_code() + "不在库中" + taskP.getLocation_code());
                            System.out.println("托盘：" + taskP.getPallet_code() + "不在库中" + taskP.getLocation_code());
                            logger.info("TaskPInsert 返回：" + interReturn);
                            return interReturn;
                        }

                        //4.发出任务前判断货位是否有货 无货：报警 有货：发出任务
                        if (locationMap2.getPallet_code() == null || locationMap2.getPallet_code().length() == 0) {
                            interReturn.setStatus(false);
                            interReturn.setMessage("货位" + taskP.getLocation_code() + "不存在托盘：");
                            System.out.println("货位" + taskP.getLocation_code() + "不存在托盘：");
                            logger.info("TaskPInsert 返回：" + interReturn);
                            return interReturn;
                        } else {
//                            interReturn.setStatus(false);
//                            interReturn.setMessage("货位" + taskP.getLocation_code() + "存在托盘：" + locationMap2.getPallet_code());
//                            System.out.println("货位" + taskP.getLocation_code() + "存在托盘：" + locationMap2.getPallet_code());
//                            logger.info("TaskPInsert 返回：" + interReturn);
//                            return interReturn;
                        }
                        //5.判断Prot_no是否为空
                        if (taskP.getProt_no() == null || taskP.getProt_no().length() == 0) {
                            interReturn.setStatus(false);
                            interReturn.setMessage("出库任务" + taskP.getTask_no() + "终点为空");
                            System.out.println("出库任务" + taskP.getTask_no() + "终点为空");
                            logger.info("TaskPInsert 返回：" + interReturn);
                            return interReturn;
                        }
                    }
                }
            }

            //任务类型为出库和入库时候 操作两个表 其余只插入task
            if ("出库".equals(taskP.getTask_type())
                    || "入库".equals(taskP.getTask_type())
                    || "满入入库".equals(taskP.getTask_type())
                    || "回库".equals(taskP.getTask_type())
            ) {
                //mybatisplus
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
                TaskPMapper taskPMapper = sqlSession.getMapper(TaskPMapper.class);//获取对应Mapper
                LocationMapMapper locationMapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper

                Date time = Calendar.getInstance().getTime();//创建时间赋值

                //1.taskP
                taskP.setTask_no(GenerateTaskNoP());//任务号
                taskP.setWarehouse("P库");//库名
                taskP.setStatus("待执行");//待执行
                if ("空托码垛".equals(taskP.getTask_type())) {
                    taskP.setUdf02("无");//堆垛机号
                } else {
                    //堆垛机号赋值
                    String row = taskP.getLocation_code().substring(2, 4);
                    int rowInt = Integer.parseInt(row);
                    int ddjNo = (rowInt + 1) / 2;
                    taskP.setUdf02("堆垛机" + ddjNo);//堆垛机号
                }
                taskP.setId(null);//id赋空值 自增insert
                taskP.setCreate_time(time);
                taskP.setUpdate_time(time);
                taskPMapper.insert(taskP);//准备执行sql

                //2.MAP
                LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
                updateWrapperMap.eq(LocationMap::getLocation_code, taskP.getLocation_code());//货位
                updateWrapperMap.eq(LocationMap::getWarehouse, "P库");//库名
                LocationMap LocationMap = new LocationMap();
                if ("出库".equals(taskP.getTask_type())) {
                    LocationMap.setPallet_code(taskP.getPallet_code() + "_" + taskP.getTask_type());//托盘
                    LocationMap.setStatus("出库冻结");//货位冻结
                } else if ("入库".equals(taskP.getTask_type())
                        || "满入入库".equals(taskP.getTask_type())
                        || "回库".equals(taskP.getTask_type())) {
                    LocationMap.setPallet_code(taskP.getPallet_code() + "_" + taskP.getTask_type());//托盘
                    LocationMap.setStatus("入库冻结");//货位冻结
                }
                locationMapMapper.update(LocationMap, updateWrapperMap);//准备执行sql

                try {
                    sqlSession.commit();//执行sql
                    interReturn.setStatus(true);
                    interReturn.setMessage("生成成功！");
                } catch (Exception e) {
                    logger.error("插入taskP&更新map异常，事务回滚", e);
                    sqlSession.rollback();
                    interReturn.setMessage("插入taskP&更新map异常，事务回滚:" + e.getCause());
                } finally {
                    sqlSession.close();
                }
            } else {
                //调生成任务号方法
                taskP.setTask_no(GenerateTaskNoP());//任务号
                taskP.setWarehouse("P库");//库名
                taskP.setStatus("待执行");//待执行
                taskP.setId(null);//id赋空值 自增insert
                boolean boolSave = taskPService.save(taskP);
                if (boolSave) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("添加托盘库任务成功");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("添加托盘库任务失败");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("托盘库任务下发出错：" + e.getCause());
            System.out.println("托盘库任务下发出错：" + e.getMessage());
            logger.error("托盘库任务下发出错：" + e.getMessage());
        }
        logger.info("TaskPInsert 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "按页码条件查询TaskP任务", notes = "TaskP类 pageNo pageSize")
    @PostMapping("/TaskP/GetTaskPByPage")
    @CrossOrigin
    public InterReturn GetTaskPByPage(@RequestBody TaskP taskP, int pageNo, int pageSize) {
//        logger.info("进入 GetTaskPByPage");
//        logger.info("接收到参数：" + taskP);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<TaskP> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<TaskP> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(taskP.getTask_no() != null && !taskP.getTask_no().isEmpty(),
                    TaskP::getTask_no, taskP.getTask_no());//任务号
            wrapper.eq(taskP.getLocation_code() != null && !taskP.getLocation_code().isEmpty(),
                    TaskP::getLocation_code, taskP.getLocation_code());//货位号
            wrapper.eq(taskP.getPallet_code() != null && !taskP.getPallet_code().isEmpty(),
                    TaskP::getPallet_code, taskP.getPallet_code());//载具编号
            wrapper.eq(taskP.getUdf02() != null && !taskP.getUdf02().isEmpty(),
                    TaskP::getUdf02, taskP.getUdf02());//堆垛机号
            wrapper.eq(taskP.getStatus() != null && !taskP.getStatus().isEmpty(),
                    TaskP::getStatus, taskP.getStatus());//任务状态
//需要按时间查询时 放开此行              wrapper.between(TaskP::getCreate_time, startTime, endTime);//时间在这两个时间中间
            wrapper.orderByDesc(TaskP::getCreate_time);//倒序
            List<TaskP> taskPList = taskPService.page(page, wrapper).getRecords();

            if (taskPList == null || taskPList.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找TaskP任务！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询TaskP任务成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(taskPList);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询TaskP任务出错：" + e.getCause());
            System.out.println("查询TaskP任务出错：" + e.getMessage());
            logger.error("查询TaskP任务出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        // logger.info("GetTaskPByPage 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "按任务号更新任务状态", notes = "传task类 任务号与新状态必填")
    @PostMapping("/TaskP/UpdateTaskPStatusByTaskNo")
    @CrossOrigin
    public InterReturn UpdateTaskPStatusByTaskNo(@RequestBody TaskP taskP) {
//        logger.info("进入 UpdateTaskPStatusByTaskNo");
//        logger.info("接收到参数：" + taskP);
        InterReturn interReturn = new InterReturn();
        try {
            if (taskP.getTask_no() == null || taskP.getTask_no().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("任务号为空");
                System.out.println("任务号为空");
                logger.info("UpdateTaskPStatusByTaskNo返回：" + interReturn);
                return interReturn;
            }
            if (taskP.getStatus() == null || taskP.getStatus().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("新任务状态为空");
                System.out.println("新任务状态为空");
                logger.info("UpdateTaskPStatusByTaskNo返回：" + interReturn);
                return interReturn;
            }

            LambdaUpdateWrapper<TaskP> wrapperUpdate = new LambdaUpdateWrapper<>();
            wrapperUpdate.eq(TaskP::getTask_no, taskP.getTask_no());//任务号
            TaskP taskPUpdate = new TaskP();
            taskPUpdate.setUpdater(taskP.getUpdater());//更新人
            taskPUpdate.setStatus(taskP.getStatus());//任务状态
            boolean boolUpdate = taskPService.update(taskPUpdate, wrapperUpdate);
            if (boolUpdate) {
                interReturn.setStatus(true);
                interReturn.setMessage("托盘库任务状态更新成功");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("托盘库任务状态更新失败");
                logger.info("UpdateTaskPStatusByTaskNo返回：" + interReturn);
                return interReturn;
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("托盘库任务状态更新出错：" + e.getCause());
            System.out.println("托盘库任务状态更新出错：" + e.getMessage());
            logger.error("托盘库任务状态更新出错：" + e.getMessage());
        }
//        logger.info("UpdateTaskPStatusByTaskNo返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "配盘信息→P库任务生成")
    @PostMapping("/TaskP/Pick2TaskP")
    @CrossOrigin
    public InterReturn Pick2TaskP(@RequestBody List<OutPick> outPickList) {
        logger.info("进入 Pick2TaskP");
        logger.info("接收到参数：" + outPickList);
        InterReturn interReturn = new InterReturn();
        try {
            for (OutPick outPick : outPickList) {
                if (outPick.getPallet_code() == null || outPick.getPallet_code().length() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("托盘号有空值");
                    System.out.println("托盘号有空值");
                    logger.info("Pick2TaskP 返回：" + interReturn);
                    return interReturn;
                }

                //判断载具是否属于本库 （是否以P5开头）
                if (!outPick.getPallet_code().startsWith("P5")) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("请检查托盘" + outPick.getPallet_code() + "是否为托盘区托盘");
                    System.out.println("请检查托盘" + outPick.getPallet_code() + "是否为托盘区托盘");
                    logger.info("Pick2TaskP 返回：" + interReturn);
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
            List<TaskP> taskPList = new ArrayList<>();
            //循环生成任务
            for (OutPick outPick : newPickList) {
                TaskP taskP = new TaskP();
                String palletCode = outPick.getPallet_code();

                //查MAP表 托盘所在货位
                LambdaQueryWrapper<LocationMap> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(LocationMap::getPallet_code, palletCode);
                List<LocationMap> locationMaps = locationMapService.list(wrapper);
                if (locationMaps.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("载具" + palletCode + "无货位数据");
                    logger.info("Pick2TaskP返回：" + interReturn);
                    return interReturn;
                } else {
                    taskP.setLocation_code(locationMaps.get(0).getLocation_code());//货位号

                    //************排序的重点   P库无需排序
                    //taskP.setUdf01(locationMaps.get(0).getLocation_code_d());//货位的组号
                }

                taskP.setTask_type("出库");//任务类型
                taskP.setPallet_code(palletCode);//载具号
                taskP.setProt_no(outPick.getPickstation_no());//站台号
                taskP.setWarehouse("P库");//库名
                taskP.setStatus("待执行");//任务状态

                {
                    //堆垛机号赋值
                    String row = taskP.getLocation_code().substring(2, 4);
                    int rowInt = Integer.parseInt(row);
                    int ddjNo = (rowInt + 1) / 2;
                    taskP.setUdf02("堆垛机" + ddjNo);//堆垛机号
                }


                Date time = Calendar.getInstance().getTime();//创建时间赋值
//                taskP.setCreator();
//                taskP.setUpdater();
                taskP.setCreate_time(time);
                taskP.setUpdate_time(time);

                taskPList.add(taskP);
            }

            //3.任务插入数据库&冻结map
            String taskNoNew = GenerateTaskNoP();
            String newNoPre = taskNoNew.substring(0, 9);//前
            String newNoBack = taskNoNew.substring(9);//后
            int newNo = Integer.parseInt(newNoBack);

            //按组号升序排序 P库无需排序
            //  taskPList.sort(Comparator.comparing(TaskP::getUdf01));//TaskP::getUdf01 货位的组号  P库无需排序


            for (int i = 0; i < taskPList.size(); i++) {
                taskPList.get(i).setTask_no(newNoPre + String.format("%04d", (newNo + i)));
            }

            //已得到要插入的task列表 taskPList
            //未得到要冻结的map列表 MAPs

            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            TaskPMapper taskPMapper = sqlSession.getMapper(TaskPMapper.class);//获取对应Mapper
            LocationMapMapper locationMapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper

            for (TaskP taskP : taskPList) {
                taskPMapper.insert(taskP);//插入单条任务

                //冻结map
                LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
                updateWrapperMap.eq(LocationMap::getLocation_code, taskP.getLocation_code());//货位
                updateWrapperMap.eq(LocationMap::getWarehouse, "P库");//库名
                LocationMap LocationMap = new LocationMap();
                LocationMap.setPallet_code(taskP.getPallet_code() + "_" + taskP.getTask_type());//托盘
                LocationMap.setStatus("生成出库冻结");//货位冻结
                locationMapMapper.update(LocationMap, updateWrapperMap);//准备执行sql
            }


            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("生成成功！");
            } catch (Exception e) {
                logger.error("配盘信息→P库任务生成异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("配盘信息→P库任务生成异常，事务回滚:" + e.getCause());
            } finally {
                sqlSession.close();
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("配盘信息→P库任务生成出错：" + e.getCause());
            System.out.println("配盘信息→P库任务生成出错：" + e.getMessage());
            logger.error("配盘信息→P库任务生成出错：" + e.getMessage());
        }
        logger.info("Pick2TaskP 返回：" + interReturn);
        return interReturn;
    }
}
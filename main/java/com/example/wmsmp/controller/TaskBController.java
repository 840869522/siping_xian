package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.config.SpringUtil;
import com.example.wmsmp.entity.*;
import com.example.wmsmp.entity.base.BaseETag;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.entity.task.TaskSort;
import com.example.wmsmp.entity.task.Z.*;
import com.example.wmsmp.mapper.LocationMapMapper;
import com.example.wmsmp.mapper.TaskBMapper;
import com.example.wmsmp.service.*;
import com.example.wmsmp.util.OutHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Api(value = "123", tags = "任务（B料箱库）")
@RestController
@CrossOrigin
public class TaskBController {
    private static Logger logger = Logger.getLogger(TaskBController.class);

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    @Autowired
    TaskBService taskBService;

    @Autowired
    TaskSortService taskSortService;

    @Autowired
    LocationMapController locationMapController;

    @Autowired
    OutOrderDetailService outOrderDetailService;

    @Autowired
    LocationMapService locationMapService;

    @Autowired
    BaseETagService baseETagService;

    @Autowired
    OutPickService outPickService;

    public String GenerateTaskNoB() {
        String taskNoNew = "";
        String str = "TZB";//任务 自动化库 料箱库
        try {
            //使用泛型 待改
            LambdaQueryWrapper<TaskB> wrapper = new LambdaQueryWrapper<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");//设定时间格式
            String strTime = sdf.format(Calendar.getInstance().getTime());
            //模糊查询任务号
            wrapper.like(TaskB::getTask_no, str + strTime);
            wrapper.orderByDesc(TaskB::getTask_no);
            List<TaskB> taskList = taskBService.list(wrapper);
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
     * @Author:上官青云
     * @Description 分拣机任务插入 前端界面调用
     * @DateTime 2024/7/19 17:33
     * @Params
     * @Return
     */
    @ApiOperation(value = "分拣机任务插入", notes = "传处理后的部分符合分拣机分拣同一的出库单明细")
    @PostMapping("/TaskB/TaskSortInsert")
    @CrossOrigin
    public InterReturn TaskSortInsert(@RequestBody OutOrder outOrder) {
        logger.info("进入 TaskSortInsert");
        logger.info("接收到参数：" + outOrder);
        InterReturn interReturn = new InterReturn();
        try {
            //0.判断是否已下发
            LambdaQueryWrapper<TaskSort> wrapperTaskSort = new LambdaQueryWrapper<>();
            wrapperTaskSort.eq(TaskSort::getOrder_no, outOrder.getOut_order_id());
            List<TaskSort> taskSorts = taskSortService.list(wrapperTaskSort);
            if (taskSorts.size() > 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("已下发此单据分拣机任务：" + outOrder.getOut_order_id());
                logger.info("TaskSortInsert 返回：" + interReturn);
                return interReturn;
            } else {

            }

            //查询明细
            LambdaQueryWrapper<OutOrderDetail> wrapperOutOrderDetail = new LambdaQueryWrapper<>();
            wrapperOutOrderDetail.eq(OutOrderDetail::getOut_order_id, outOrder.getOut_order_id());
            List<OutOrderDetail> list = outOrderDetailService.list(wrapperOutOrderDetail);
            if (list.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("无此单据明细");
                logger.info("TaskSortInsert 返回：" + interReturn);
                return interReturn;
            }

            //1.生成任务列表
            List<TaskSort> taskSortList = new ArrayList<>();
            //料箱存放的数量参数
            int boxSetSum = 2;
            //料箱数量
            int boxSum = 0;
            //料箱编号
            int boxNo = 1;
            //单条物料总数量
            Integer materialSum;
            for (OutOrderDetail outOrderDetailItem : list) {
                materialSum = outOrderDetailItem.getOrder_count();
                while (materialSum > 0) {
                    TaskSort taskSort = new TaskSort();
                    //若大于料箱存放的数量参数需要用下一个料箱，并将料箱数量初始化
                    if (boxSum >= boxSetSum) {
                        boxNo++;
                        boxSum = 0;
                    }
                    if (boxSum != 0) {
                        Integer inty = boxSetSum - boxSum;
                        taskSort.setNum(inty);//装箱数
                        materialSum = materialSum - inty;
                        boxSum = boxSum + inty;
                    } else {
                        Integer integer2 = materialSum > boxSetSum ? boxSetSum : materialSum;
                        taskSort.setNum(integer2);//装箱数
                        materialSum = materialSum - integer2;
                        boxSum = boxSum + integer2;
                    }
                    taskSort.setOrder_no(outOrderDetailItem.getOut_order_id());//出库单号
                    taskSort.setTask_type("wave_sort_task");//任务类型
                    taskSort.setTask_type_desc("分拣机任务");//任务描述
                    //   String.format("%02d", newNo);//'%02d'的定义:0代表前面要补的字符 2代表字符串长度 d表示参数为整数类型
                    taskSort.setBox_id(String.format("%02d", boxNo));//箱子序号
                    taskSort.setMaterial_code(outOrderDetailItem.getMaterial_code());//物料编号
                    taskSort.setBatch(outOrderDetailItem.getBatch());//批次
                    taskSort.setOut_order_detail_id(outOrderDetailItem.getOut_order_detail_id());//出库明细id
                    taskSort.setStatus("待执行");//任务状态
                    taskSortList.add(taskSort);
                }
            }
            interReturn.setResult(taskSortList);

            //2.插入任务列表
            boolean bl = taskSortService.saveBatch(taskSortList);
            if (bl) {
                interReturn.setStatus(true);
                interReturn.setMessage("插入任务列表成功");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("插入任务列表失败");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("TaskSortInsert 出错catch：" + e.getMessage());
            logger.info("TaskSortInsert 返回：" + interReturn);
            return interReturn;
        }
        logger.info("TaskSortInsert 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "料箱库任务插入")
    @PostMapping("/TaskB/TaskBInsert")
    @CrossOrigin
    public InterReturn TaskBInsert(@RequestBody TaskB taskB) {
        logger.info("进入 TaskBInsert");
        logger.info("接收到参数：" + taskB);
        InterReturn interReturn = new InterReturn();
        try {
            if (taskB.getLocation_code() == null || taskB.getLocation_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("货位号为空");
                System.out.println("货位号为空");
                logger.info("TaskBInsert 返回：" + interReturn);
                return interReturn;
            }

            if (taskB.getPallet_code() == null || taskB.getPallet_code().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("料箱号为空");
                System.out.println("料箱号为空");
                logger.info("TaskBInsert 返回：" + interReturn);
                return interReturn;
            }

            //插入前校验
            {
                //1.查询此托盘是否有 任务状态是"待执行" "已下发"  的任务
                LambdaQueryWrapper<TaskB> wrapperTaskB1 = new LambdaQueryWrapper<>();
                wrapperTaskB1.eq(TaskB::getPallet_code, taskB.getPallet_code());
                wrapperTaskB1.in(TaskB::getStatus, Arrays.asList("待执行", "已下发"));//任务状态
                List<TaskB> taskList = taskBService.list(wrapperTaskB1);
                if (taskList.size() > 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage(taskB.getPallet_code() + "料箱已有“待执行”、 ”已下发”任务：" + taskList.get(0).getTask_no());
                    System.out.println(taskB.getPallet_code() + "料箱已有“待执行”、 ”已下发”任务：" + taskList.get(0).getTask_no());
                    logger.info("TaskBInsert 返回：" + interReturn);
                    return interReturn;
                }

                //2.判断此货位是否存在 是否启用
                LambdaQueryWrapper<LocationMap> wrapperMap2 = new LambdaQueryWrapper<>();
                wrapperMap2.eq(LocationMap::getLocation_code, taskB.getLocation_code());
                List<LocationMap> mapList2 = locationMapService.list(wrapperMap2);
                if (mapList2.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("无此货位：" + taskB.getLocation_code());
                    System.out.println("无此货位：" + taskB.getLocation_code());
                    logger.info("TaskBInsert 返回：" + interReturn);
                    return interReturn;
                }
                LocationMap locationMap2 = mapList2.get(0);
                if ("启用".equals(locationMap2.getStatus())) {

                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("货位" + taskB.getLocation_code() + "已禁用");
                    System.out.println("货位" + taskB.getLocation_code() + "已禁用");
                    logger.info("TaskBInsert 返回：" + interReturn);
                    return interReturn;
                }

                //3.判断托盘是否在库 入：在库报警 出：不在库报警
                LambdaQueryWrapper<LocationMap> wrapperMap3 = new LambdaQueryWrapper<>();
                wrapperMap3.eq(LocationMap::getPallet_code, taskB.getPallet_code());
                List<LocationMap> locationMaps3 = locationMapService.list(wrapperMap3);

                //插入前校验 入
                if (("入库").equals(taskB.getTask_type()) ||
                        ("回库").equals(taskB.getTask_type())) {

                    //3.判断托盘是否在库 入：在库报警 出：不在库报警
                    if (locationMaps3.size() > 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("料箱：" + taskB.getPallet_code() + "已在" + locationMaps3.get(0).getLocation_code());
                        System.out.println("料箱：" + taskB.getPallet_code() + "已在" + locationMaps3.get(0).getLocation_code());
                        logger.info("TaskBInsert 返回：" + interReturn);
                        return interReturn;
                    } else {
//                    interReturn.setStatus(false);
//                    interReturn.setMessage("料箱：" + taskP.getPallet_code() + "不在库中" + taskP.getLocation_code());
//                    System.out.println("料箱：" + taskP.getPallet_code() + "不在库中" + taskP.getLocation_code());
//                    logger.info("TaskPInsert 返回：" + interReturn);
//                    return interReturn;
                    }

                    //4.发入任务前判断货位是否有货  无货：发出任务 有货：报警
                    if (locationMap2.getPallet_code() == null || locationMap2.getPallet_code().length() == 0) {
//                    interReturn.setStatus(false);
//                    interReturn.setMessage("货位" + taskP.getLocation_code() + "不存在料箱");
//                    System.out.println("货位" + taskP.getLocation_code() + "不存在料箱");
//                    logger.info("TaskPInsert 返回：" + interReturn);
//                    return interReturn;
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("货位" + taskB.getLocation_code() + "存在料箱：" + locationMap2.getPallet_code());
                        System.out.println("货位" + taskB.getLocation_code() + "存在料箱：" + locationMap2.getPallet_code());
                        logger.info("TaskBInsert 返回：" + interReturn);
                        return interReturn;
                    }


                }

                //插入前校验 出
                else if (("出库").equals(taskB.getTask_type())) {
                    //3.判断托盘是否在库 入：在库报警 出：不在库报警
                    if (locationMaps3.size() > 0) {
//                    interReturn.setStatus(false);
//                    interReturn.setMessage("料箱：" + taskP.getPallet_code() + "已在" + locationMaps3.get(0).getLocation_code());
//                    System.out.println("料箱：" + taskP.getPallet_code() + "已在" + locationMaps3.get(0).getLocation_code());
//                    logger.info("TaskPInsert 返回：" + interReturn);
//                    return interReturn;
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("料箱：" + taskB.getPallet_code() + "不在库中" + taskB.getLocation_code());
                        System.out.println("料箱：" + taskB.getPallet_code() + "不在库中" + taskB.getLocation_code());
                        logger.info("TaskBInsert 返回：" + interReturn);
                        return interReturn;
                    }

                    //4.发出任务前判断货位是否有货 无货：报警 有货：发出任务
                    if (locationMap2.getPallet_code() == null || locationMap2.getPallet_code().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("货位" + taskB.getLocation_code() + "不存在料箱：");
                        System.out.println("货位" + taskB.getLocation_code() + "不存在料箱：");
                        logger.info("TaskBInsert 返回：" + interReturn);
                        return interReturn;
                    } else {
//                    interReturn.setStatus(false);
//                    interReturn.setMessage("货位" + taskP.getLocation_code() + "存在料箱：" + locationMap2.getPallet_code());
//                    System.out.println("货位" + taskP.getLocation_code() + "存在料箱：" + locationMap2.getPallet_code());
//                    logger.info("TaskPInsert 返回：" + interReturn);
//                    return interReturn;
                    }

                    //5.判断Prot_no是否为空
                    if (taskB.getProt_no() == null || taskB.getProt_no().length() == 0) {
                        interReturn.setStatus(false);
                        interReturn.setMessage("出库任务" + taskB.getTask_no() + "终点为空");
                        System.out.println("出库任务" + taskB.getTask_no() + "终点为空");
                        logger.info("TaskBInsert 返回：" + interReturn);
                        return interReturn;
                    }
                }
            }


            //任务类型为出库和入库时候 操作两个表 其余只插入task
            if ("出库".equals(taskB.getTask_type())
                    || "入库".equals(taskB.getTask_type())
                    || "满入入库".equals(taskB.getTask_type())
                    || "移库".equals(taskB.getTask_type())
                    || "回库".equals(taskB.getTask_type())
            ) {
                //mybatisplus
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
                TaskBMapper taskBMapper = sqlSession.getMapper(TaskBMapper.class);//获取对应Mapper
                LocationMapMapper locationMapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper

                Date time = Calendar.getInstance().getTime();//创建时间赋值

                //1.taskB
                taskB.setTask_no(GenerateTaskNoB());//任务号
                taskB.setWarehouse("B库");//库名
                taskB.setStatus("待执行");//待执行
                taskB.setId(null);//id赋空值 自增insert
                taskB.setCreate_time(time);
                taskB.setUpdate_time(time);
                taskBMapper.insert(taskB);//准备执行sql

                //2.MAP
                LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
                updateWrapperMap.eq(LocationMap::getLocation_code, taskB.getLocation_code());//货位
                updateWrapperMap.eq(LocationMap::getWarehouse, "B库");//库名
                LocationMap LocationMap = new LocationMap();
                if ("出库".equals(taskB.getTask_type())) {
                    LocationMap.setPallet_code(taskB.getPallet_code() + "_" + taskB.getTask_type());//料箱
                    LocationMap.setStatus("出库冻结");//货位冻结
                } else if ("入库".equals(taskB.getTask_type())
                        || "满入入库".equals(taskB.getTask_type())
                        || "回库".equals(taskB.getTask_type())) {
                    LocationMap.setPallet_code(taskB.getPallet_code() + "_" + taskB.getTask_type());//料箱
                    LocationMap.setStatus("入库冻结");//货位冻结
                }
                locationMapMapper.update(LocationMap, updateWrapperMap);//准备执行sql

                try {
                    sqlSession.commit();//执行sql
                    interReturn.setStatus(true);
                    interReturn.setMessage("生成成功！");

                    //二楼人工工位回库 解绑电子标签
                    OutHelper outHelper = new OutHelper();
                    Set<String> protNosMan = outHelper.getSetMan();//二楼人工
                    if (protNosMan.contains(taskB.getProt_no())) {
                        //此位置符合
                        interReturn = UnbindETag(taskB.getProt_no());//二楼人工工位回库 解绑电子标签
                    } else {
                        //此位置不符
                        //正常位置 不操作电子标签
                    }

                } catch (Exception e) {
                    logger.error("插入taskB&更新map异常，事务回滚", e);
                    sqlSession.rollback();
                    interReturn.setMessage("插入taskB&更新map异常，事务回滚:" + e.getCause());
                } finally {
                    sqlSession.close();
                }
            } else {
                //调生成任务号方法
                taskB.setTask_no(GenerateTaskNoB());//任务号
                taskB.setWarehouse("B库");//库名
                taskB.setStatus("待执行");//待执行
                taskB.setProt_no(taskB.getProt_no());//出库口
                taskB.setId(null);//id赋空值 自增insert
                boolean boolSave = taskBService.save(taskB);
                if (boolSave) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("添加料箱库任务成功");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("添加料箱库任务失败");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("料箱库任务下发出错：" + e.getCause());
            System.out.println("料箱库任务下发出错：" + e.getMessage());
            logger.error("料箱库任务下发出错：" + e.getMessage());
        }
        logger.info("TaskBInsert 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "按页码条件查询TaskB任务", notes = "TaskB类 pageNo pageSize")
    @PostMapping("/TaskB/GetTaskBByPage")
    @CrossOrigin
    public InterReturn GetTaskBByPage(@RequestBody TaskB taskB, int pageNo, int pageSize) {
//        logger.info("进入 GetTaskBByPage");
//        logger.info("接收到参数：" + taskB);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<TaskB> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<TaskB> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(taskB.getTask_no() != null && !taskB.getTask_no().isEmpty(),
                    TaskB::getTask_no, taskB.getTask_no());//任务号
            wrapper.eq(taskB.getLocation_code() != null && !taskB.getLocation_code().isEmpty(),
                    TaskB::getLocation_code, taskB.getLocation_code());//货位号
            wrapper.eq(taskB.getPallet_code() != null && !taskB.getPallet_code().isEmpty(),
                    TaskB::getPallet_code, taskB.getPallet_code());//载具编号
            wrapper.eq(taskB.getStatus() != null && !taskB.getStatus().isEmpty(),
                    TaskB::getStatus, taskB.getStatus());//任务状态
//需要按时间查询时 放开此行              wrapper.between(TaskB::getCreate_time, startTime, endTime);//时间在这两个时间中间
            wrapper.orderByDesc(TaskB::getCreate_time);//倒序
            List<TaskB> taskBList = taskBService.page(page, wrapper).getRecords();

            if (taskBList == null || taskBList.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找TaskB任务！");
                logger.info("GetTaskBByPage 返回：" + interReturn);
                return interReturn;
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询TaskB任务成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(taskBList);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询TaskB任务出错：" + e.getCause());
            System.out.println("查询TaskB任务出错：" + e.getMessage());
            logger.error("查询TaskB任务出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        // logger.info("GetTaskBByPage 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "按任务号更新任务状态", notes = "传task类 任务号与新状态必填")
    @PostMapping("/TaskB/UpdateTaskBStatusByTaskNo")
    @CrossOrigin
    public InterReturn UpdateTaskBStatusByTaskNo(@RequestBody TaskB taskB) {
//        logger.info("进入 UpdateTaskBStatusByTaskNo");
//        logger.info("接收到参数：" + taskB);
        InterReturn interReturn = new InterReturn();
        try {
            if (taskB.getTask_no() == null || taskB.getTask_no().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("任务号为空");
                System.out.println("任务号为空");
                logger.info("UpdateTaskBStatusByTaskNo返回：" + interReturn);
                return interReturn;
            }
            if (taskB.getStatus() == null || taskB.getStatus().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("新任务状态为空");
                System.out.println("新任务状态为空");
                logger.info("UpdateTaskBStatusByTaskNo返回：" + interReturn);
                return interReturn;
            }

            LambdaUpdateWrapper<TaskB> wrapperUpdate = new LambdaUpdateWrapper<>();
            wrapperUpdate.eq(TaskB::getTask_no, taskB.getTask_no());//任务号
            TaskB taskBUpdate = new TaskB();
            taskBUpdate.setUpdater(taskB.getUpdater());//更新人
            taskBUpdate.setStatus(taskB.getStatus());//任务状态
            boolean boolUpdate = taskBService.update(taskBUpdate, wrapperUpdate);
            if (boolUpdate) {
                interReturn.setStatus(true);
                interReturn.setMessage("料箱库任务状态更新成功");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("料箱库任务状态更新失败");
                logger.info("UpdateTaskBStatusByTaskNo返回：" + interReturn);
                return interReturn;
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("料箱库任务状态更新出错：" + e.getCause());
            System.out.println("料箱库任务状态更新出错：" + e.getMessage());
            logger.error("料箱库任务状态更新出错：" + e.getMessage());
        }
//        logger.info("UpdateTaskBStatusByTaskNo返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "电子标签按下", notes = "位置号")
    @PostMapping("/TaskZ/ETagOff")
    @CrossOrigin
    public ZReturn ETagOff(@RequestBody ZTag zTag) {
        logger.info("ETagUp 接收到参数：" + zTag);
        ZReturn zReturn = new ZReturn();
        String protNo = zTag.getTag_code();
        if (Integer.parseInt(protNo) < 4000) {
            //上层
            zReturn = ETagUp(protNo);
        } else {
            //下层
            zReturn = ETagDown(protNo);
        }
        logger.info("ETagUp 返回：" + zTag);
        return zReturn;
    }

    @ApiOperation(value = "上方电子标签按下 wcs调用", notes = "位置号")
    @PostMapping("/TaskZ/ETagUp")
    @CrossOrigin
    @Transactional
    public ZReturn ETagUp(@RequestBody String protNo) {
        ZReturn zReturn = new ZReturn();
        logger.info("ETagUp 接收到参数：" + protNo);

        //判断参数是否合法
        OutHelper outHelper = new OutHelper();
        Set<String> protNos = outHelper.getSetB();
        if (!protNos.contains(protNo)) {
            zReturn.setCode("100");
            zReturn.setMessage("参数不正确：" + protNo);
            logger.info("ETagUp 返回：" + zReturn);
            return zReturn;
        }

        //根据位置号获取料箱号
        LambdaQueryWrapper<BaseETag> wrapperQuery1 = new LambdaQueryWrapper<>();
        wrapperQuery1.eq(BaseETag::getTag_location, protNo);//分拣口
        BaseETag baseETag = baseETagService.getOne(wrapperQuery1);
        String palletCode = baseETag.getPallet_code(); //料箱号

        //判断此料箱是否还有要待执行的拣选任务
        LambdaQueryWrapper<OutPick> wrapperQuery2 = new LambdaQueryWrapper<>();
        wrapperQuery2.eq(OutPick::getPallet_code, palletCode);
        wrapperQuery2.eq(OutPick::getStatus, "待执行");
        wrapperQuery2.orderByAsc(OutPick::getId);
        List<OutPick> outPickList = outPickService.list(wrapperQuery2);
        if (outPickList.size() > 0) {
            //1.完成此拣选配盘任务
            LambdaUpdateWrapper<OutPick> outPickLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            outPickLambdaUpdateWrapper.eq(OutPick::getId, outPickList.get(0).getId());
            OutPick outPick = new OutPick();
            outPick.setStatus("已完成");
            boolean bool = outPickService.update(outPick, outPickLambdaUpdateWrapper);
            if (bool) {
                zReturn.setCode("200");
                zReturn.setMessage("更新配盘任务完成");
            } else {
                zReturn.setCode("211");
                zReturn.setMessage(outPickList.get(0).getId() + "更新配盘任务出错：" + protNo + palletCode);
                logger.info("ETagUp 返回：" + zReturn);
                return zReturn;
            }
            //2.↑↑↑↑↑ 分拣动作 上库存减
            OutPickController outPickController = SpringUtil.getBean(OutPickController.class);
            InterReturn upMinus = outPickController.PickPallet23(outPickList.get(0));//上方电子标签按下
            if (upMinus.isStatus()) {
                zReturn.setCode("200");
                zReturn.setMessage("UP分拣完成" + protNo + palletCode);
            } else {
                zReturn.setCode("222");
                zReturn.setMessage("UP拣出错：" + protNo + palletCode);
                logger.info("ETagUp 返回：" + zReturn);
                return zReturn;
            }

            //3.↓↓↓↓↓ 分拣动作 下库存增 （区分楼上楼下 楼上不进行本步骤 落下时进行）
            if ("一楼".equals(baseETag.getRemark().substring(0, 2))) {

                LambdaQueryWrapper<BaseETag> wrapperQuery3 = new LambdaQueryWrapper<>();
                wrapperQuery3.eq(BaseETag::getTag_location, outPickList.get(0).getTarget_no());
                List<BaseETag> baseETagList = baseETagService.list(wrapperQuery3);
                if (baseETagList.size() > 0) {
                    String palletCodeDown = baseETagList.get(0).getPallet_code();//下方托盘号
                    //palletCodeDown判空
                    if (palletCodeDown == null || "".equals(palletCodeDown.trim())) {
                        zReturn.setCode("333");
                        zReturn.setMessage("DOWN分拣出错，下方料箱号为空" + outPickList.get(0).getTarget_no());
                        logger.error("ETagUp 返回：" + zReturn);
                        return zReturn;
                    }
                    outPickList.get(0).setPalletCode(palletCodeDown);//为流水服务 不影响实际业务
                    InterReturn downAdd = outPickController.PickPallet33(outPickList.get(0), palletCodeDown);//上方电子标签按下
                    if (downAdd.isStatus()) {
                        zReturn.setCode("200");
                        zReturn.setMessage("DOWN分拣完成" + protNo + palletCodeDown);
                    } else {
                        zReturn.setCode("233");
                        zReturn.setMessage("DOWN分拣出错：" + protNo + palletCodeDown);
                        logger.error("ETagUp 返回：" + zReturn);
                        return zReturn;
                    }
                } else {
                    zReturn.setCode("444");
                    zReturn.setMessage("DOWN分拣出错，无此拣货位置信息：" + outPickList.get(0).getTarget_no());
                    logger.error("ETagUp 返回：" + zReturn);
                    return zReturn;
                }

            } else {
                //二楼 无逻辑 （物料滑落时或全完成时调用PickPallet33）

            }


            //4.是否是最后一条
            if (outPickList.size() == 1) {
                //①最后一条 拣选完成
                //4.1电子标签解绑
                InterReturn interReturn = UnbindETag(protNo);//上层
                if (interReturn.isStatus()) {
                    zReturn.setCode("200");
                    zReturn.setMessage("电子标签解绑完成：" + protNo);
                } else {
                    zReturn.setCode("231");
                    zReturn.setMessage("电子标签解绑出错：" + protNo);
                    logger.info("ETagDown 返回：" + zReturn);
                    return zReturn;
                }
                //4.2回库任务下发 回库
                TaskB taskB = new TaskB();

                //自动分配货位号
                InterReturn interReturnNew = locationMapController.getLocationByPalletcodeB(palletCode);//上拍灯 自动分配货位号
                if (!interReturnNew.isStatus()) {
                    zReturn.setCode("234");
                    zReturn.setMessage("货到人回库申请错误：" + interReturnNew.getMessage());
                    return zReturn;
                } else {
                    taskB.setLocation_code(((LocationMap) interReturnNew.getResult()).getLocation_code());//货位号
                }

                taskB.setPallet_code(palletCode);
                taskB.setProt_no(protNo);
                taskB.setTask_type("回库");
                taskB.setCreator("一楼货到人拣选完成原料箱入库");
                InterReturn interReturnTask = TaskBInsert(taskB);
                if (interReturnTask.isStatus()) {
                    zReturn.setCode("200");
                    zReturn.setMessage("回库任务插入完成：" + protNo);
                } else {
                    zReturn.setCode("235");
                    zReturn.setMessage("回库任务插入出错：" + taskB);
                    logger.info("ETagUp 返回：" + zReturn);
                    return zReturn;
                }
                //4.3 隔壁是否有箱子 有则发隔壁箱子亮灯 无则等着下个箱子到来
                BaseETagController baseETagController = SpringUtil.getBean(BaseETagController.class);
                String nextPallet = baseETagController.GetNextPallet(protNo);
                if (nextPallet == null || nextPallet.trim().isEmpty()) {
                    logger.info("隔壁无料箱：" + nextPallet + "则等待");
                } else {
                    logger.info("隔壁无料箱：" + nextPallet + "则亮隔壁灯");
                    TaskZController taskZController = SpringUtil.getBean(TaskZController.class);
                    ZReturn zzz = taskZController.ZDKETagLight(nextPallet);//拍上触发隔壁 待改 是否需要处理返回值
                }
            } else {
                //②还有下一条 发下一条给电子标签亮灯
                TaskZController taskZController = SpringUtil.getBean(TaskZController.class);
                ZReturn zzz = taskZController.ZDKETagLight(palletCode);//拍上触发
                if ("200".equals(zzz.getCode())) {
                    logger.info("电子标签点亮成功：触发任务的" + outPickList.get(1).getId() + "：" + outPickList.get(1).getPallet_code());
                    zReturn.setCode("200");
                    zReturn.setMessage("电子标签点亮成功：触发任务的" + outPickList.get(1).getId() + "：" + outPickList.get(1).getPallet_code());
                    return zReturn;
                } else {
                    logger.info("电子标签点亮失败：触发任务的" + outPickList.get(1).getId() + "：" + outPickList.get(1).getPallet_code());
                    zReturn.setCode("666");
                    zReturn.setMessage("电子标签点亮失败：触发任务的" + outPickList.get(1).getId() + "：" + outPickList.get(1).getPallet_code() + zzz.getMessage());
                    return zReturn;
                }
            }
        } else {
            zReturn.setCode("399");
            zReturn.setMessage("此料箱已无拣选任务：" + palletCode);
            logger.info("ETagUp 返回：" + zReturn);
            return zReturn;
        }

        logger.info("ETagUp 返回：" + zReturn);
        return zReturn;
    }

    @ApiOperation(value = "下方电子标签按下", notes = "位置号")
    @PostMapping("/TaskZ/ETagDown")
    @CrossOrigin
    public ZReturn ETagDown(@RequestBody String protNo) {
        logger.info("ETagDown 接收到参数：" + protNo);
        ZReturn zReturn = new ZReturn();
        try {
            //判断参数是否合法
            OutHelper outHelper = new OutHelper();
            Set<String> protNos = outHelper.getSetB();
            if (!protNos.contains(protNo)) {
                zReturn.setCode("100");
                zReturn.setMessage("参数不正确：" + protNo);
                logger.info("ETagDown 返回：" + zReturn);
                return zReturn;
            }

//            LambdaQueryWrapper<BaseETag> wrapperQuery3 = new LambdaQueryWrapper<>();
//            wrapperQuery3.eq(BaseETag::getTag_location, protNo);
//            List<BaseETag> baseETagList = baseETagService.list(wrapperQuery3);

            //1呼叫空料箱

            //2.出库 电子标签解绑

            //出库或者回库 不做回库
            if (true) {
                //1.出库 电子标签解绑   时机错误暂时注释
//                InterReturn interReturn = UnbindETag(protNo);//下层
//                if (interReturn.isStatus()) {
//                    zReturn.setCode("200");
//                    zReturn.setMessage("电子标签解绑完成：" + protNo);
//                } else {
//                    zReturn.setCode("231");
//                    zReturn.setMessage("电子标签解绑出错：" + protNo);
//                    logger.info("ETagDown 返回：" + zReturn);
//                    return zReturn;
//                }

                //2.PLC占位清除 见线程 mfc_choose_over  改为WCS触发
//                TaskB taskB = new TaskB();
//                taskB.setLocation_code(protNo);
//                taskB.setPallet_code(baseETagList.get(0).getPallet_code());
//                taskB.setProt_no(protNo);
//                taskB.setTask_type("满箱给agv");
//                taskB.setCreator("货到人下层按下");
//
//                InterReturn interReturnTask = TaskBInsert(taskB);
//                if (interReturnTask.isStatus()) {
//                    zReturn.setCode("200");
//                    zReturn.setMessage("PLC占位清除任务插入完成：" + protNo+" "+baseETagList.get(0).getPallet_code());
//                }else {
//                    zReturn.setCode("231");
//                    zReturn.setMessage("PLC占位清除任务插入出错：" + protNo+" "+baseETagList.get(0).getPallet_code());
//                    logger.info("ETagDown 返回：" + zReturn);
//                    return zReturn;
//                }
            } else {
                //回库    不做
            }
        } catch (Exception e) {
            zReturn.setCode("800");
            zReturn.setMessage("下方电子标签按下 catch：" + e.getMessage());
            logger.info("ETagDown 返回：" + zReturn);
            return zReturn;

        }
        logger.info("ETagDown 返回：" + zReturn);
        return zReturn;
    }

    /**
     * @Author:xhc
     * @Description 解绑电子标签的料箱
     * @DateTime 2024/6/30 9:03
     * @Params
     * @Return
     */
    @ApiOperation(value = "解绑电子标签的料箱", notes = "位置号")
    @PostMapping("/TaskZ/UnbindETag")
    @CrossOrigin
    public InterReturn UnbindETag(String tag_location) {
        InterReturn zReturn = new InterReturn();
        logger.info("UnbindETag 接收到参数：" + tag_location);
        try {
            LambdaUpdateWrapper<BaseETag> wrapperUpdate = new LambdaUpdateWrapper<>();
            wrapperUpdate.eq(BaseETag::getTag_location, tag_location);
            BaseETag baseETag = new BaseETag();
            baseETag.setPallet_code("");
            boolean boolUpdateLabel = baseETagService.update(baseETag, wrapperUpdate);
            if (boolUpdateLabel) {
                zReturn.setStatus(true);
                zReturn.setMessage("电子标签解绑更新成功：" + tag_location);
                logger.info("电子标签解绑更新成功：" + tag_location);
            } else {
                zReturn.setStatus(false);
                zReturn.setMessage("电子标签解绑更新失败：" + tag_location);
                logger.info("电子标签解绑更新失败：" + tag_location);
                return zReturn;
            }
        } catch (Exception e) {
            zReturn.setStatus(false);
            zReturn.setMessage("UnbindETag解绑电子标签的料箱 catch：" + e.getStackTrace());
            logger.info("UnbindETag解绑电子标签的料箱 catch：" + e.getStackTrace());
            return zReturn;
        }
        return zReturn;
    }

    @ApiOperation(value = "配盘信息→B库任务生成")
    @PostMapping("/TaskB/Pick2TaskB")
    @CrossOrigin
    public InterReturn Pick2TaskB(@RequestBody List<OutPick> outPickList) {
        logger.info("进入 Pick2TaskB");
        logger.info("接收到参数：" + outPickList);
        InterReturn interReturn = new InterReturn();
        try {
            for (OutPick outPick : outPickList) {
                if (outPick.getPallet_code() == null || outPick.getPallet_code().length() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("托盘号有空值");
                    System.out.println("托盘号有空值");
                    logger.info("Pick2TaskB返回：" + interReturn);
                    return interReturn;
                }

                //判断载具是否属于本库 （是否以B开头）
                if (!outPick.getPallet_code().startsWith("B")) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("请检查料箱" + outPick.getPallet_code() + "是否为料箱区托盘");
                    System.out.println("请检查料箱" + outPick.getPallet_code() + "是否为料箱区托盘");
                    logger.info("Pick2TaskB返回：" + interReturn);
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
            List<TaskB> taskBList = new ArrayList<>();
            //循环生成任务
            for (OutPick outPick : newPickList) {
                TaskB taskB = new TaskB();
                String palletCode = outPick.getPallet_code();

                //查MAP表 托盘所在货位
                LambdaQueryWrapper<LocationMap> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(LocationMap::getPallet_code, palletCode);
                List<LocationMap> locationMaps = locationMapService.list(wrapper);
                if (locationMaps.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("载具" + palletCode + "无货位数据");
                    logger.info("Pick2TaskB返回：" + interReturn);
                    return interReturn;
                } else {
                    taskB.setLocation_code(locationMaps.get(0).getLocation_code());//货位号

                    //************排序的重点  依据
                    taskB.setUdf01(locationMaps.get(0).getLocation_area());//货位的组号
                }

                taskB.setTask_type("出库");//任务类型
                taskB.setPallet_code(palletCode);//载具号
                taskB.setProt_no(outPick.getPickstation_no());//站台号
                taskB.setWarehouse("B库");//库名
                taskB.setStatus("待执行");//任务状态

                Date time = Calendar.getInstance().getTime();//创建时间赋值
//                taskB.setCreator();
//                taskB.setUpdater();
                taskB.setCreate_time(time);
                taskB.setUpdate_time(time);

                taskBList.add(taskB);
            }

            //3.任务插入数据库
            String taskNoNew = GenerateTaskNoB();
            String newNoPre = taskNoNew.substring(0, 9);//前
            String newNoBack = taskNoNew.substring(9);//后
            int newNo = Integer.parseInt(newNoBack);

            //按组号升序排序
            taskBList.sort(Comparator.comparing(TaskB::getUdf01));//TaskB::getUdf01 货位的组号

            for (int i = 0; i < taskBList.size(); i++) {
                taskBList.get(i).setTask_no(newNoPre + String.format("%04d", (newNo + i)));//任务号赋值
            }

            //已得到要插入的task列表 taskPList
            //未得到要冻结的map列表 MAPs

            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            TaskBMapper taskBMapper = sqlSession.getMapper(TaskBMapper.class);//获取对应Mapper
            LocationMapMapper locationMapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper

            for (TaskB taskB : taskBList) {
                taskBMapper.insert(taskB);//插入单条任务

                //冻结map
                LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
                updateWrapperMap.eq(LocationMap::getLocation_code, taskB.getLocation_code());//货位
                updateWrapperMap.eq(LocationMap::getWarehouse, "B库");//库名
                LocationMap LocationMap = new LocationMap();
                LocationMap.setPallet_code(taskB.getPallet_code() + "_" + taskB.getTask_type());//托盘
                LocationMap.setStatus("生成出库冻结");//货位冻结
                locationMapMapper.update(LocationMap, updateWrapperMap);//准备执行sql
            }


            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("生成成功！");
            } catch (Exception e) {
                logger.error("配盘信息→B库任务生成异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("配盘信息→B库任务生成异常，事务回滚:" + e.getCause());
            } finally {
                sqlSession.close();
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("配盘信息→B库任务生成出错：" + e.getCause());
            System.out.println("配盘信息→B库任务生成出错：" + e.getMessage());
            logger.error("配盘信息→B库任务生成出错：" + e.getMessage());
        }
        logger.info("Pick2TaskB返回：" + interReturn);
        return interReturn;
    }
}

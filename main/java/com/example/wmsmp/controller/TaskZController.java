package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.wmsmp.config.SpringUtil;
import com.example.wmsmp.entity.LocationMap;
import com.example.wmsmp.entity.OutPick;
import com.example.wmsmp.entity.base.BaseETag;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.task.AGV.AgvLocationMap;
import com.example.wmsmp.entity.task.AGV.AgvTaskSend;
import com.example.wmsmp.entity.task.CTU.CtuLocationMap;
import com.example.wmsmp.entity.task.CTU.CtuTaskSend;
import com.example.wmsmp.entity.task.TaskSort;
import com.example.wmsmp.entity.task.Z.*;
import com.example.wmsmp.mapper.LocationMapMapper;
import com.example.wmsmp.mapper.TaskBMapper;
import com.example.wmsmp.mapper.TaskPMapper;
import com.example.wmsmp.service.*;
import com.example.wmsmp.util.OutHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;


@Api(value = "123", tags = "任务（自动化库）")
@RestController
@CrossOrigin
public class TaskZController {
    private static Logger logger = Logger.getLogger(TaskZController.class);

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    @Autowired
    TaskPService taskPService;

    @Autowired
    BaseETagService baseETagService;

    @Autowired
    TaskSortService taskSortService;

    @Autowired
    TaskBService taskBService;

    @Autowired
    OutPickService outPickService;

    @Autowired
    LocationMapService locationMapService;

    @Autowired
    private RestTemplate restTemplate;//用于调用外部接口

    @Autowired
    private Environment environment;//用于读取配置文件

    @ApiOperation(value = "1下发自动库任务", notes = "任务类 货位号为截取后的位数字")
    @PostMapping("/TaskZ/ZDKtaskReceive")
    @CrossOrigin
    public InterReturn ZDKtaskReceive(@RequestBody ZTask task) {
        logger.info("进入 ZDKtaskReceive");
        logger.info("接收到参数：" + task);
        InterReturn interReturn = new InterReturn();
        try {
            String URL = environment.getProperty("apiZ.wmsWhTask");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
            task.setCreate_time(sdf.format(Calendar.getInstance().getTime()));//下发时间
            logger.info("自动库1任务下发，拼接入参：" + task);
            HttpEntity<ZTask> httpEntitysnew = new HttpEntity<>(task);
            //参数为 url http实体 返回类型
            ZReturn response = restTemplate.postForObject(URL, httpEntitysnew, ZReturn.class);
            if (("200").equals(response.getCode())) {
                interReturn.setStatus(true);
                interReturn.setMessage("发送下游任务成功");
                interReturn.setResult(response);
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("发送下游任务出错：" + response.getMessage());
                interReturn.setResult(response);
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("发送下游任务 调用出错：" + e.getCause());
            System.out.println("发送下游任务 调用出错：" + e.getMessage());
        }
        logger.info("ZDKtaskReceive 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 4下发分拣机任务 线程调用
     * @Params
     * @Return
     */
    @ApiOperation(value = "4下发分拣机任务（线程调用）", notes = "传入波次号（出库单号）")
    @PostMapping("/TaskZ/ZDKtaskSorter")
    @CrossOrigin
    public InterReturn ZDKtaskSorter(@RequestBody String order_no) {
        logger.info("进入 ZDKtaskSorter");
        logger.info("接收到参数：" + order_no);
        InterReturn interReturn = new InterReturn();
        try {
            //1.查任务表 找order_no波次的任务
            LambdaQueryWrapper<TaskSort> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TaskSort::getOrder_no, order_no);
            List<TaskSort> waveSortList = taskSortService.list(queryWrapper);
            if (waveSortList.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("无此波次分拣机任务:" + order_no);
                logger.info("ZDKtaskSorter 返回：" + interReturn);
                return interReturn;
            }

            //2.下发给分拣机wcs
            ZTaskSorter1 zTaskSorter1 = new ZTaskSorter1();
            zTaskSorter1.setOrder_no(waveSortList.get(0).getOrder_no());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
            zTaskSorter1.setCreate_time(sdf.format(Calendar.getInstance().getTime()));//下发时间
            zTaskSorter1.setTask_type("wave_sort_task");//wave_sort_task
            zTaskSorter1.setTask_type_desc("分拣机任务");//分拣机任务
            List<ZTaskSorter2> list2 = new ArrayList<>();
            for (TaskSort sort : waveSortList) {
                ZTaskSorter2 zTaskSorter2 = new ZTaskSorter2();

                //2024年8月31日 此处无需补零 移动到出库单生成分拣机任务的时候
                //示例 String.format("%04d", newNo);//'%04d'的定义:0代表前面要补的字符 4代表字符串长度 d表示参数为整数类型
                //zTaskSorter2.setBox_id(waveSortList.get(0).getOrder_no() + "_" + String.format("%02d", Integer.parseInt(sort.getBox_id())));//波次号_箱子序号
                zTaskSorter2.setBox_id(waveSortList.get(0).getOrder_no() + "_" + sort.getBox_id());//波次号_箱子序号
                zTaskSorter2.setBarcode(sort.getMaterial_code());
                zTaskSorter2.setBatch(sort.getBatch());
                zTaskSorter2.setNum(sort.getNum());
                zTaskSorter2.setDetail_id(sort.getOut_order_detail_id());
                list2.add(zTaskSorter2);
            }
            zTaskSorter1.setList(list2);


            String URL = environment.getProperty("apiZ.wmsSortTask");
            logger.info("自动库4下发分拣机任务，拼接入参：" + zTaskSorter1);
            HttpEntity<ZTaskSorter1> httpEntitysnew = new HttpEntity<>(zTaskSorter1);
            //参数为 url http实体 返回类型
            ZReturn response = restTemplate.postForObject(URL, httpEntitysnew, ZReturn.class);
            String status;
            if (("200").equals(response.getCode())) {
                //任务下发成功
                System.out.println("分拣机任务下发成功");
                status = "已下发";
            } else {
                logger.info("分拣机下发出库任务出错：" + response.getMessage());
                status = "下发出错：" + response.getMessage();
            }


            //3.改任务状态为 下发结果
            LambdaQueryWrapper<TaskSort> wrapperUpdate = new LambdaQueryWrapper<>();
            wrapperUpdate.eq(TaskSort::getOrder_no, waveSortList.get(0).getOrder_no());//任务号
            TaskSort update = new TaskSort();
            update.setStatus(status);//任务状态
            boolean boolUpdate = taskSortService.update(update, wrapperUpdate);
            if (boolUpdate) {
                //任务下发成功
                logger.info("wms下发分拣机任务完成，更新任务状态为已下发");
                interReturn.setStatus(true);
                interReturn.setMessage("wms下发分拣机任务完成，更新任务状态为已下发");
                interReturn.setResult(response);
            } else {
                logger.info("wms下发分拣机任务完成后，更新任务状态失败：" + waveSortList.get(0).getOrder_no());
                interReturn.setStatus(true);
                interReturn.setMessage("wms下发分拣机任务完成后，更新任务状态失败：" + waveSortList.get(0).getOrder_no());
                logger.info("ZDKtaskSorter 返回：" + interReturn);
                return interReturn;
            }

        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("发送分拣机任务 调用出错：" + e.getCause());
            System.out.println("发送分拣机任务 调用出错：" + e.getMessage());
        }
        logger.info("ZDKtaskSorter 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "5下发电子标签亮灯", notes = "料箱号")
    @PostMapping("/TaskZ/ZDKETagLight")
    @CrossOrigin
    public ZReturn ZDKETagLight(@RequestBody String palletcode) {

        ZReturn zReturn = new ZReturn();

        LambdaQueryWrapper<OutPick> queryOutPickWrapper = new LambdaQueryWrapper<>();
        queryOutPickWrapper.eq(OutPick::getPallet_code, palletcode);
        queryOutPickWrapper.eq(OutPick::getStatus, "待执行");
        queryOutPickWrapper.orderByAsc(OutPick::getId);//按id递增
        List<OutPick> outPickList = outPickService.list(queryOutPickWrapper);
        if (outPickList.size() > 0) {
            int colorNo;
            if (outPickList.size() == 1) {
                colorNo = 3;
            } else {
                colorNo = 1;
            }
            //生成 电子标签亮灯 任务类 第一条配盘任务
            ZTaskLight1 zTaskLight1 = new ZTaskLight1();
            zTaskLight1.setUuid(outPickList.get(0).getId());

            String wallNo;

            if ("3016".equals(outPickList.get(0).getPickstation_no()) || "3019".equals(outPickList.get(0).getPickstation_no())) {
                wallNo = "1";
            } else if ("3020".equals(outPickList.get(0).getPickstation_no()) || "3042".equals(outPickList.get(0).getPickstation_no())) {
                wallNo = "2";
            } else if ("3043".equals(outPickList.get(0).getPickstation_no()) || "3059".equals(outPickList.get(0).getPickstation_no())) {
                wallNo = "3";
            } else {
                logger.info("原料箱地址错误，无法找到墙号 ：" + outPickList.get(0).getId() + " " + palletcode);
                zReturn.setCode("660");
                zReturn.setMessage("原料箱地址错误，无法找到墙号 ：" + outPickList.get(0).getId() + " " + palletcode);
                return zReturn;
            }
            zTaskLight1.setWall_no(wallNo);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
            zTaskLight1.setCreate_time(sdf.format(Calendar.getInstance().getTime()));//下发时间
            zTaskLight1.setTask_type("tag_light");//tag_light
            zTaskLight1.setTask_type_desc("电子标签亮灯");//电子标签亮灯
            List<ZTaskLight2> zTaskLight2List = new ArrayList<>();
            //上方亮灯
            ZTaskLight2 zTaskLight2Up = new ZTaskLight2();
            zTaskLight2Up.setTag_code(outPickList.get(0).getPickstation_no());//位置
            zTaskLight2Up.setNum(outPickList.get(0).getPick_qty());//拣选数量
            zTaskLight2Up.setColor(colorNo);//颜色

            //下方亮灯
            ZTaskLight2 zTaskLight2Down = new ZTaskLight2();
            zTaskLight2Down.setTag_code(outPickList.get(0).getTarget_no());//位置
            zTaskLight2Down.setNum(outPickList.get(0).getPick_qty());//拣选数量
            zTaskLight2Down.setColor(colorNo);//颜色 待改 出库单的最后一条 显示绿色

            zTaskLight2List.add(zTaskLight2Up);
            zTaskLight2List.add(zTaskLight2Down);
            zTaskLight1.setData_list(zTaskLight2List);

            String URL = environment.getProperty("apiZ.wmsDpsTask");
            logger.info("自动库5点亮电子标签 任务，拼接入参：" + zTaskLight1);
            HttpEntity<ZTaskLight1> httpEntitysnew = new HttpEntity<>(zTaskLight1);
            //参数为 url http实体 返回类型
            ZReturn response = restTemplate.postForObject(URL, httpEntitysnew, ZReturn.class);
            if (("200").equals(response.getCode())) {
                //电子标签亮灯 下发成功
                logger.info("wms下发电子标签亮灯完成");
                zReturn.setCode("200");
                zReturn.setMessage("wms下发电子标签亮灯完成");
            } else {
                zReturn.setCode("236");
                zReturn.setMessage("wms下发电子标签亮灯出错：" + response.getMessage());
                zReturn.setMessage(response + "");
                return zReturn;
            }
        } else {
            logger.info("电子标签无此托盘待执行配盘信息 ：" + palletcode);
            zReturn.setCode("660");
            zReturn.setMessage("电子标签无此托盘待执行配盘信息 ：" + palletcode);
            return zReturn;
        }
        return zReturn;
    }

    @ApiOperation(value = "2入库申请", notes = "包含二维码的类")
    @PostMapping("/TaskZ/taskApply")
    @CrossOrigin
    public ZReturn taskApply(@RequestBody ZApply zApply) {
        logger.info("taskApply 接收到参数：" + zApply);
        ZReturn zReturn = new ZReturn();
        if (("robo_in_task").equals(zApply.getTask_type())) {
            //P堆垛机入库
            zReturn = taskApplyP(zApply);
        } else if (("mfc_in_task").equals(zApply.getTask_type())) {
            //B料箱入库
            zReturn = taskApplyB(zApply);
        } else if (("empty_tray_1f_in").equals(zApply.getTask_type())) {
            //P一楼 空托盘垛 码垛完成 申请入库
            zReturn = taskApplyF1TrayInP(zApply);
        } else if (("empty_tray_2f_in").equals(zApply.getTask_type())) {
            //P二楼 拆盘后的空托盘 未扫到码 申请入库
            zReturn = taskApplyF2TrayInP(zApply);
        } else if (("empty_tray_2f_out").equals(zApply.getTask_type())) {
            //P二楼 空托盘垛 缺少空盘 申请出库
            zReturn = taskApplyF2TrayOutP(zApply);
        } else if (("robo_in_machine_2f").equals(zApply.getTask_type())) {
            //P二楼 码盘完成 回库请求
            zReturn = taskApplyF2BackP(zApply);
        } else if (("mfc_in_machine_2f").equals(zApply.getTask_type())) {
            //B二楼 拣选完成 回库请求
            zReturn = taskApplyF2BackB(zApply);
        } else {
            zReturn.setCode("999");
            zReturn.setMessage("处理wcs请求出错：未知任务类型 " + zApply.getTask_type());
        }
        logger.info("taskApply 返回：" + zReturn);
        return zReturn;
    }


    /**
     * @Author:xhc
     * @Description 托盘库下游入库任务申请逻辑
     * @DateTime 2024/6/25 0:34
     * @Params
     * @Return
     */
    public ZReturn taskApplyP(@RequestBody ZApply zApply) {
        ZReturn zReturn = new ZReturn();
        zReturn.setCode("123");
        zReturn.setTask_id(zApply.getUuid());//uuid
        try {
            String palletCode = zApply.getBarcode();//载具号

            //1.查任务表 if(没此托盘号任务)
            LambdaQueryWrapper<TaskP> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskP::getWarehouse, "P库");//库名
            wrapper.eq(TaskP::getPallet_code, palletCode);//载具编号
            wrapper.eq(TaskP::getTask_type, "入库");//任务类型
            wrapper.eq(TaskP::getStatus, "待执行");//任务状态
            wrapper.orderByDesc(TaskP::getTask_no);//倒序2
            List<TaskP> taskPList = taskPService.list(wrapper);
            if (taskPList.size() == 0) {
                zReturn.setCode("125");
                zReturn.setMessage("错误：wms无" + palletCode + "托盘入库任务");
            } else {
                //2.分配货位 (根据托盘内的物料 根据MAP的组号)
                //暂时手动下发

                //3.调用接口"3任务接收（发送下游任务）"
                ZTask taskone = new ZTask();
                String taskno = taskPList.get(0).getTask_no();
                taskone.setTask_id(taskno);//任务号
                taskone.setBarcode(zApply.getBarcode());//条码
                taskone.setStart_dest(zApply.getStart_dest());//起点


                //将ZP010203 转为 1_2_3
                String endStr = taskPList.get(0).getLocation_code().substring(2, 8);
                String endStrNew = Integer.valueOf(endStr.substring(0, 2)) + "_" +
                        Integer.parseInt(endStr.substring(2, 4)) + "_" +
                        Integer.parseInt(endStr.substring(4, 6));
                taskone.setEnd_dest(endStrNew);//终点
                taskone.setStart_dest(zApply.getStart_dest());//起点
                taskone.setTask_type(zApply.getTask_type());//任务类型
                taskone.setTask_type_desc("堆垛机扫描器入库");//
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
                taskone.setCreate_time(sdf.format(Calendar.getInstance().getTime()));//下发时间
                taskone.setMfc_out_priority("5");//穿梭车出库优先级（出库任务有）
                taskone.setMfc_level("5");//穿梭车层（有穿梭车地址必带）
                InterReturn inreturn = ZDKtaskReceive(taskone);//下发入库任务

                if (inreturn.isStatus()) {
                    zReturn.setCode("200");
                    zReturn.setMessage("托盘库入库任务下发成功");
                } else {
                    zReturn.setCode("201");
                    zReturn.setMessage(inreturn.getMessage());
                    return zReturn;
                }
                //4.更新任务状态为 "已下发"
                LambdaQueryWrapper<TaskP> wrapperUpdate = new LambdaQueryWrapper<>();
                wrapperUpdate.eq(TaskP::getTask_no, taskPList.get(0).getTask_no());//任务号
                TaskP taskPUpdate = new TaskP();
                taskPUpdate.setStatus("已下发");//任务状态
                taskPUpdate.setProt_no(zApply.getStart_dest());//操作口
                boolean boolUpdate = taskPService.update(taskPUpdate, wrapperUpdate);
                if (boolUpdate) {
                    //任务下发成功
                    logger.info("托盘库更新任务下发状态成功：" + taskPList.get(0).getTask_no());
                    zReturn.setCode("200");
                    zReturn.setMessage("托盘库更新任务下发状态成功，wms更新任务状态成功");
                } else {
                    logger.info("托盘库更新任务下发状态出错：" + taskPList.get(0).getTask_no());
                    zReturn.setCode("222");
                    zReturn.setMessage("错误：wms更新任务状态出错" + taskPList.get(0).getTask_no());
                    return zReturn;
                }
            }
        } catch (Exception e) {
            zReturn.setCode("999");
            zReturn.setMessage("托盘库任务申请出错：" + e.getCause());
        }
        return zReturn;
    }

    /**
     * @Author:xhc
     * @Description 料箱库下游入库任务申请逻辑
     * @DateTime 2024/6/25 0:36
     * @Params
     * @Return
     */
    public ZReturn taskApplyB(@RequestBody ZApply zApply) {
        ZReturn zReturn = new ZReturn();
        zReturn.setCode("123");
        zReturn.setTask_id(zApply.getUuid());//uuid
        try {
            String palletCode = zApply.getBarcode();//载具号

            //单机
//            zReturn.setCode("200");
//            zReturn.setMessage("料箱库任务申请成功");
            //单机结束

            //1.查任务表 if(没此料箱号任务)
            LambdaQueryWrapper<TaskB> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskB::getWarehouse, "B库");//库名
            wrapper.eq(TaskB::getPallet_code, palletCode);//载具编号
            wrapper.eq(TaskB::getTask_type, "入库");//任务类型
            wrapper.eq(TaskB::getStatus, "待执行");//任务状态
            wrapper.orderByDesc(TaskB::getTask_no);//倒序2
            List<TaskB> taskBList = taskBService.list(wrapper);
            if (taskBList.size() == 0) {
                zReturn.setCode("125");
                zReturn.setMessage("错误：wms无" + palletCode + "料箱入库任务");
            } else {
                //2.分配货位 (根据料箱内的物料 根据MAP的组号)
                //暂时手动下发

                //3.调用接口"3任务接收（发送下游任务）"
                ZTask taskone = new ZTask();
                String taskno = taskBList.get(0).getTask_no();
                taskone.setTask_id(taskno);//任务号
                taskone.setBarcode(zApply.getBarcode());//条码
                taskone.setStart_dest(zApply.getStart_dest());//起点


                //将ZB01410118 转为 01层410118
                String endStr = taskBList.get(0).getLocation_code().substring(4, 10);
                taskone.setEnd_dest(endStr);//终点
                taskone.setTask_type(zApply.getTask_type());//任务类型
                taskone.setTask_type_desc("mfc扫描器入库");//
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
                taskone.setCreate_time(sdf.format(Calendar.getInstance().getTime()));//下发时间
                taskone.setMfc_out_priority("5");//穿梭车出库优先级（出库任务有）
                taskone.setMfc_level(taskBList.get(0).getLocation_code().substring(2, 4));//层号
                InterReturn inreturn = ZDKtaskReceive(taskone);//下发入库任务
                if (inreturn.isStatus()) {
                    zReturn.setCode("200");
                    zReturn.setMessage("料箱库入库任务下发成功");
                } else {
                    zReturn.setCode("201");
                    zReturn.setMessage(inreturn.getMessage());
                }
                //4.更新任务状态为 "已下发"
                LambdaQueryWrapper<TaskB> wrapperUpdate = new LambdaQueryWrapper<>();
                wrapperUpdate.eq(TaskB::getTask_no, taskBList.get(0).getTask_no());//任务号
                TaskB taskBUpdate = new TaskB();
                taskBUpdate.setStatus("已下发");//任务状态
                taskBUpdate.setProt_no(zApply.getStart_dest());//操作口
                boolean boolUpdate = taskBService.update(taskBUpdate, wrapperUpdate);
                if (boolUpdate) {
                    //任务下发成功
                    logger.info("料箱库更新任务下发状态成功：" + taskBList.get(0).getTask_no());
                    zReturn.setCode("200");
                    zReturn.setMessage("料箱库更新任务下发状态成功，wms更新任务状态成功");
                } else {
                    logger.info("料箱库更新任务下发状态出错：" + taskBList.get(0).getTask_no());
                    zReturn.setCode("222");
                    zReturn.setMessage("错误：wms更新任务状态出错" + taskBList.get(0).getTask_no());
                }
            }
        } catch (Exception e) {
            zReturn.setCode("999");
            zReturn.setMessage("料箱库任务申请出错：" + e.getCause());
        }
        return zReturn;
    }

    /**
     * @Author:xhc
     * @Description 1楼 空托盘垛 码垛完成 申请入库
     * @DateTime 2024/7/6 16:46
     * @Params
     * @Return
     */
    private ZReturn taskApplyF1TrayInP(ZApply zApply) {
        ZReturn zReturn = new ZReturn();
        zReturn.setCode("123");
        zReturn.setTask_id(zApply.getUuid());//uuid
        try {
            String palletCode = "空托盘垛";//载具号

            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            TaskPMapper taskPMapper = sqlSession.getMapper(TaskPMapper.class);//获取对应Mapper
            LocationMapMapper locationMapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper

            Date time = Calendar.getInstance().getTime();//创建时间赋值

            //1.taskP
            TaskP taskP = new TaskP();
            TaskPController taskPController = SpringUtil.getBean(TaskPController.class);
            taskP.setId(null);//id赋空值 自增insert
            taskP.setWarehouse("P库");//库名
            LocationMapController locationMapController = SpringUtil.getBean(LocationMapController.class);

            //取map中最大的排号
            int maxKey = locationMapController.getMaxEmptyLocationRowP(1);//暂定取7层以下

            //自动分配货位号
            InterReturn interReturn = locationMapController.getEmptyLocationP(maxKey, "近", 1);//自动分配货位号
            if (!interReturn.isStatus()) {
                //待改
                zReturn.setCode("234");
                zReturn.setMessage("空托盘垛回库申请错误：" + interReturn.getMessage());
                return zReturn;
            } else {
                taskP.setLocation_code(((LocationMap) interReturn.getResult()).getLocation_code());//货位号
                {
                    //堆垛机号赋值
                    String row = taskP.getLocation_code().substring(2, 4);
                    int rowInt = Integer.parseInt(row);
                    int ddjNo = (rowInt + 1) / 2;
                    taskP.setUdf02("堆垛机" + ddjNo);//堆垛机号
                }
            }

            taskP.setTask_type("回库");//空托盘垛
            taskP.setPallet_code(palletCode);//空托盘垛
            taskP.setProt_no("1074");//码盘机点位
            taskP.setStatus("待执行");//待执行
            taskP.setCreator("一楼空托盘垛入库");
            taskP.setCreate_time(time);
            taskP.setUpdate_time(time);
            String taskNoTray = taskPController.GenerateTaskNoP();//获取任务号
            taskP.setTask_no(taskNoTray);//任务号
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
                zReturn.setCode("200");
            } catch (Exception e) {
                logger.error("插入taskP&更新map异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("插入taskP&更新map异常，事务回滚:" + e.getCause());
                zReturn.setCode("888");
            } finally {
                sqlSession.close();
            }
        } catch (Exception e) {
            zReturn.setCode("999");
            zReturn.setMessage("托盘库入空托盘垛任务申请出错：" + e.getCause());
        }
        return zReturn;
    }

    /**
     * @Author:xhc
     * @Description 二楼 拆盘后的空托盘 未扫到码 申请入库
     * @DateTime 2024/8/9 15:19
     * @Params
     * @Return
     */
    private ZReturn taskApplyF2TrayInP(ZApply zApply) {
        ZReturn zReturn = new ZReturn();
        zReturn.setCode("123");
        zReturn.setTask_id(zApply.getUuid());//uuid
        try {
            String palletCode = "空托盘";//载具号

            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            TaskPMapper taskPMapper = sqlSession.getMapper(TaskPMapper.class);//获取对应Mapper
            LocationMapMapper locationMapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper

            Date time = Calendar.getInstance().getTime();//创建时间赋值

            //1.taskP
            TaskP taskP = new TaskP();
            TaskPController taskPController = SpringUtil.getBean(TaskPController.class);
            taskP.setId(null);//id赋空值 自增insert
            taskP.setWarehouse("P库");//库名
            LocationMapController locationMapController = SpringUtil.getBean(LocationMapController.class);

            //取map中最大的排号
            int maxKey = locationMapController.getMaxEmptyLocationRowP(1);//暂定取7层以下

            //自动分配货位号
            InterReturn interReturn = locationMapController.getEmptyLocationP(maxKey, "近", 1);//自动分配货位号
            if (!interReturn.isStatus()) {
                //待改
                zReturn.setCode("234");
                zReturn.setMessage("空托盘回库申请错误：" + interReturn.getMessage());
                return zReturn;
            } else {
                taskP.setLocation_code(((LocationMap) interReturn.getResult()).getLocation_code());//货位号
                {
                    //堆垛机号赋值
                    String row = taskP.getLocation_code().substring(2, 4);
                    int rowInt = Integer.parseInt(row);
                    int ddjNo = (rowInt + 1) / 2;
                    taskP.setUdf02("堆垛机" + ddjNo);//堆垛机号
                }
            }

            taskP.setTask_type("回库");//空托盘垛
            taskP.setPallet_code(palletCode);//空托盘
            taskP.setProt_no(zApply.getStart_dest());//起始点位
            taskP.setStatus("待执行");//待执行
            taskP.setCreator("二楼空托盘入库");
            taskP.setCreate_time(time);
            taskP.setUpdate_time(time);
            String taskNoTray = taskPController.GenerateTaskNoP();//获取任务号
            taskP.setTask_no(taskNoTray);//任务号
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
                zReturn.setCode("200");
            } catch (Exception e) {
                logger.error("插入taskP&更新map异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("插入taskP&更新map异常，事务回滚:" + e.getCause());
                zReturn.setCode("888");
            } finally {
                sqlSession.close();
            }
        } catch (Exception e) {
            zReturn.setCode("999");
            zReturn.setMessage("托盘库入空托盘入任务申请出错：" + e.getCause());
        }
        return zReturn;
    }


    /**
     * @Author:xhc
     * @Description 2楼 空托盘垛 缺少空盘 申请出库
     * @DateTime 2024/7/7 12:35
     * @Params
     * @Return
     */
    private ZReturn taskApplyF2TrayOutP(ZApply zApply) {
        ZReturn zReturn = new ZReturn();
        zReturn.setCode("123");
        zReturn.setTask_id(zApply.getUuid());//uuid
        try {
            String palletCode = "空托盘垛";//载具号

            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            TaskPMapper taskPMapper = sqlSession.getMapper(TaskPMapper.class);//获取对应Mapper
            LocationMapMapper locationMapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper

            Date time = Calendar.getInstance().getTime();//创建时间赋值

            //1.taskP
            TaskP taskP = new TaskP();
            TaskPController taskPController = SpringUtil.getBean(TaskPController.class);
            taskP.setId(null);//id赋空值 自增insert
            taskP.setWarehouse("P库");//库名

            //获取库内”空托盘垛“的map信息
            LambdaQueryWrapper<LocationMap> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LocationMap::getWarehouse, "P库");
            queryWrapper.eq(LocationMap::getPallet_code, "空托盘垛");
            queryWrapper.eq(LocationMap::getStatus, "启用");
            List<LocationMap> locationMapList = locationMapService.list(queryWrapper);
            if (locationMapList.size() == 0) {
                zReturn.setCode("456");
                zReturn.setMessage("库中已无空托盘垛");
                return zReturn;
            }
            taskP.setLocation_code(locationMapList.get(0).getLocation_code());//货位号
            {
                //堆垛机号赋值
                String row = taskP.getLocation_code().substring(2, 4);
                int rowInt = Integer.parseInt(row);
                int ddjNo = (rowInt + 1) / 2;
                taskP.setUdf02("堆垛机" + ddjNo);//堆垛机号
            }

            taskP.setTask_type("出库");//空托盘垛
            taskP.setPallet_code(palletCode);//空托盘垛
            taskP.setProt_no("2011");//二楼拆盘机点位
            taskP.setStatus("待执行");//待执行
            taskP.setCreate_time(time);
            taskP.setCreator("二楼申请空托盘垛出库");
            taskP.setUpdate_time(time);
            String taskNoTray = taskPController.GenerateTaskNoP();//获取任务号
            taskP.setTask_no(taskNoTray);//任务号
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
                zReturn.setCode("200");
                zReturn.setMessage("2楼空托盘垛任务生成成功！");
            } catch (Exception e) {
                logger.error("插入taskP&更新map异常，事务回滚", e);
                sqlSession.rollback();
                zReturn.setMessage("插入taskP&更新map异常，事务回滚:" + e.getCause());
                zReturn.setCode("888");
            } finally {
                sqlSession.close();
            }
        } catch (Exception e) {
            zReturn.setCode("999");
            zReturn.setMessage("托盘库出空托盘垛任务申请出错：" + e.getCause());
        }
        return zReturn;
    }

    /**
     * @Author:xhc
     * @Description 二楼 码盘完成 回库请求
     * @DateTime 2024/7/8 15:58
     * @Params
     * @Return
     */
    private ZReturn taskApplyF2BackP(ZApply zApply) {
        ZReturn zReturn = new ZReturn();
        zReturn.setCode("123");
        zReturn.setTask_id(zApply.getUuid());//uuid
        try {
            String palletCode = zApply.getBarcode();//载具号
            if (palletCode == null || palletCode.trim().length() == 0) {
                zReturn.setCode("192");
                zReturn.setMessage("托盘号为空");
                System.out.println("托盘号为空");
                return zReturn;
            }


            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            TaskPMapper taskPMapper = sqlSession.getMapper(TaskPMapper.class);//获取对应Mapper
            LocationMapMapper locationMapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper

            Date time = Calendar.getInstance().getTime();//创建时间赋值

            //1.taskP
            TaskP taskP = new TaskP();
            TaskPController taskPController = SpringUtil.getBean(TaskPController.class);
            taskP.setId(null);//id赋空值 自增insert
            taskP.setWarehouse("P库");//库名
            LocationMapController locationMapController = SpringUtil.getBean(LocationMapController.class);

            //取map中最大的排号
            int maxKey = locationMapController.getMaxEmptyLocationRowP(zApply.getLowHigh());//按高矮取


            //自动分配货位号
            InterReturn interReturn = locationMapController.getEmptyLocationP(maxKey, "近", zApply.getLowHigh());//自动分配货位号
            if (!interReturn.isStatus()) {
                //待改
                zReturn.setCode("234");
                zReturn.setMessage("二楼码垛完成回库申请错误：" + interReturn.getMessage());
                return zReturn;
            } else {
                taskP.setLocation_code(((LocationMap) interReturn.getResult()).getLocation_code());//货位号
                {
                    //堆垛机号赋值
                    String row = taskP.getLocation_code().substring(2, 4);
                    int rowInt = Integer.parseInt(row);
                    int ddjNo = (rowInt + 1) / 2;
                    taskP.setUdf02("堆垛机" + ddjNo);//堆垛机号
                }
            }

            taskP.setTask_type("回库");//
            taskP.setPallet_code(palletCode);//
            taskP.setProt_no(zApply.getStart_dest());//申请的点位
            taskP.setStatus("待执行");//待执行
            taskP.setCreate_time(time);
            taskP.setCreator("二楼码盘完成入库");
            taskP.setUpdate_time(time);
            String taskNoTray = taskPController.GenerateTaskNoP();//获取任务号
            taskP.setTask_no(taskNoTray);//任务号
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
                zReturn.setCode("200");
                zReturn.setMessage("二楼回库任务生成成功！");
            } catch (Exception e) {
                logger.error("批量插入异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("批量插入异常，事务回滚:" + e.getCause());
                zReturn.setCode("888");
            } finally {
                sqlSession.close();
            }
        } catch (Exception e) {
            zReturn.setCode("999");
            zReturn.setMessage("托盘库入空托盘垛任务申请出错：" + e.getCause());
        }
        return zReturn;
    }


    /**
     * @Author:xhc
     * @Description 二楼 拣选完成 回库请求
     * @DateTime 2024/7/8 15:58
     * @Params
     * @Return
     */
    private ZReturn taskApplyF2BackB(ZApply zApply) {
        ZReturn zReturn = new ZReturn();
        zReturn.setCode("123");
        zReturn.setTask_id(zApply.getUuid());//uuid
        try {
            String palletCode = zApply.getBarcode();//载具号
            if (palletCode == null || palletCode.trim().length() == 0) {
                zReturn.setCode("192");
                zReturn.setMessage("料箱号为空");
                System.out.println("料箱号为空");
                return zReturn;
            }

            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            TaskBMapper taskBMapper = sqlSession.getMapper(TaskBMapper.class);//获取对应Mapper
            LocationMapMapper locationMapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper

            Date time = Calendar.getInstance().getTime();//创建时间赋值

            //1.taskB
            TaskB taskB = new TaskB();
            TaskBController taskBController = SpringUtil.getBean(TaskBController.class);
            taskB.setId(null);//id赋空值 自增insert
            taskB.setWarehouse("B库");//库名
            LocationMapController locationMapController = SpringUtil.getBean(LocationMapController.class);

            //自动分配货位号
            InterReturn interReturn = locationMapController.getLocationByPalletcodeB(palletCode);//机械手回库 自动分配货位号
            if (!interReturn.isStatus()) {
                //待改
                zReturn.setCode("234");
                zReturn.setMessage("二楼机械手拣选完成入库申请错误：" + interReturn.getMessage());
                return zReturn;
            } else {
                taskB.setLocation_code(((LocationMap) interReturn.getResult()).getLocation_code());//货位号
            }

            taskB.setTask_type("回库");//
            taskB.setPallet_code(palletCode);//
            taskB.setProt_no(zApply.getStart_dest());//申请的点位
            taskB.setStatus("待执行");//待执行
            taskB.setCreate_time(time);
            taskB.setCreator("二楼机械手拣选完成入库");
            taskB.setUpdate_time(time);
            String taskNoTray = taskBController.GenerateTaskNoB();//获取任务号
            taskB.setTask_no(taskNoTray);//任务号
            taskBMapper.insert(taskB);//准备执行sql

            //2.MAP
            LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
            updateWrapperMap.eq(LocationMap::getLocation_code, taskB.getLocation_code());//货位
            updateWrapperMap.eq(LocationMap::getWarehouse, "B库");//库名
            LocationMap LocationMap = new LocationMap();
            if ("出库".equals(taskB.getTask_type())) {
                LocationMap.setPallet_code(taskB.getPallet_code() + "_" + taskB.getTask_type());//托盘
                LocationMap.setStatus("出库冻结");//货位冻结
            } else if ("入库".equals(taskB.getTask_type())
                    || "满入入库".equals(taskB.getTask_type())
                    || "回库".equals(taskB.getTask_type())) {
                LocationMap.setPallet_code(taskB.getPallet_code() + "_" + taskB.getTask_type());//托盘
                LocationMap.setStatus(taskB.getTask_type() + "冻结");//货位冻结
            }
            locationMapMapper.update(LocationMap, updateWrapperMap);//准备执行sql

            try {
                sqlSession.commit();//执行sql
                interReturn.setStatus(true);
                interReturn.setMessage("生成成功！");
                zReturn.setCode("200");
                zReturn.setMessage("二楼机械手拣选完成入库任务生成成功！");


                //二楼机械手回库申请 解绑电子标签
                OutHelper outHelper = new OutHelper();
                Set<String> protNosRobot = outHelper.getSetRobot();//二楼机械手
                if (protNosRobot.contains(taskB.getProt_no())) {
                    //此位置符合
                    interReturn = taskBController.UnbindETag(taskB.getProt_no());//二楼机械手回库申请 解绑电子标签
                } else {
                    //此位置不符
                    logger.error("此位置不属于机械手拣选位：" + taskB.getProt_no());//起点发错
                }


            } catch (Exception e) {
                logger.error("批量插入异常，事务回滚", e);
                sqlSession.rollback();
                interReturn.setMessage("批量插入异常，事务回滚:" + e.getCause());
                zReturn.setCode("888");
            } finally {
                sqlSession.close();
            }
        } catch (Exception e) {
            zReturn.setCode("999");
            zReturn.setMessage("二楼机械手拣选完成入库申请出错：" + e.getCause());
        }
        return zReturn;
    }


    @ApiOperation(value = "3任务状态上报", notes = "参数 待改")
    @PostMapping("/TaskZ/taskReport")
    @CrossOrigin
    public ZReturn taskReport(@RequestBody ZTaskReport zTaskReport) {
        //logger.info("taskReport 接收到参数：" + zTaskReport);
        System.out.println("taskReport 接收到参数：" + zTaskReport);
        ZReturn zReturn = new ZReturn();
        zReturn.setCode("123");

        try {
            String taskId = zTaskReport.getTask_id();//任务号
            String task_type = zTaskReport.getTask_type();//任务号
            String taskStatus = zTaskReport.getTask_state();//任务状态 任务状态：1-完成；2-放货有货；3-空取；4-穿梭车取消   其他-异常待定；

            //分拣机任务完成
            if ("wave_sort_task".equals(task_type)) {
                if (("1").equals(taskStatus)) {//完成
                    LambdaUpdateWrapper<TaskSort> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(TaskSort::getOrder_no, taskId.substring(0, taskId.length() - 3));
                    updateWrapper.eq(TaskSort::getBox_id, taskId.substring(taskId.length() - 2));
                    TaskSort taskSort = new TaskSort();
                    taskSort.setStatus("已完成");
                    taskSort.setUpdater("分拣机落包完成");
                    boolean bl = taskSortService.update(taskSort, updateWrapper);
                    if (bl) {
                        zReturn.setCode("200");
                        zReturn.setMessage("wcs上报分拣机任务完成：" + taskId);
                        System.out.println("wcs上报分拣机任务完成：" + taskId);
                        logger.info("wcs上报分拣机任务完成：" + taskId);
                        return zReturn;
                    } else {
                        zReturn.setCode("203");
                        zReturn.setMessage("分拣机任务" + taskId + "更新波次任务状态出错");
                        System.out.println("分拣机任务" + taskId + "更新波次任务状态出错");
                        logger.info("分拣机任务" + taskId + "更新波次任务状态出错");
                        return zReturn;
                    }
                } else {
                    //异常 其他
                    zReturn.setCode("888");
                    zReturn.setMessage("分拣机任务" + taskId + "，状态" + taskStatus + "，上报接收成功");
                    System.out.println("分拣机任务" + taskId + "，状态" + taskStatus + "，上报接收成功");
                    return zReturn;
                }
            }

            //料箱机械手 拣选完成 库存变更
            else if ("mfc_PickPallet23".equals(task_type)) {
                if (("1").equals(taskStatus)) {//完成
                    LambdaQueryWrapper<OutPick> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(OutPick::getId, taskId);//配盘表id
                    wrapper.eq(OutPick::getPallet_code, zTaskReport.getBarcode());//wcs传来的料箱号
                    List<OutPick> outPicks = outPickService.list(wrapper);
                    if (outPicks.size() == 0) {
                        zReturn.setCode("692");
                        zReturn.setMessage("无此id" + taskId + "，箱号" + zTaskReport.getBarcode() + "，机械手任务（配盘信息）");
                        System.out.println("无此id" + taskId + "，箱号" + zTaskReport.getBarcode() + "，机械手任务（配盘信息）");
                        return zReturn;
                    } else {
                        OutPickController outPickController = SpringUtil.getBean(OutPickController.class);
                        InterReturn interReturn = outPickController.PickPallet23(outPicks.get(0));//料箱机械手 拣选完成
                        if (interReturn.isStatus()) {
                            zReturn.setCode("200");
                            zReturn.setMessage(interReturn.getMessage() + ":" + taskId);
                            System.out.println(interReturn.getMessage() + ":" + taskId);
                            return zReturn;
                        } else {
                            zReturn.setCode("250");
                            zReturn.setMessage(interReturn.getMessage() + ":" + taskId);
                            System.out.println(interReturn.getMessage() + ":" + taskId);
                            logger.error("机械手拣选完成数据处理出错：" + interReturn);
                            return zReturn;
                        }
                    }
                }
            }

            //料箱下一楼到位 给CTU发任务
            else if ("sort_box_to_ctu".equals(task_type)) {
                zReturn.setTask_id(taskId);
                if (("1").equals(taskStatus)) {//完成
                    logger.info("料箱下一楼到位 准备给CTU发任务：" + zTaskReport.getBarcode());
                    //1.获取空位
                    CtuTaskSend ctuTaskSend = new CtuTaskSend();
                    ctuTaskSend.setPrio(1);
                    ctuTaskSend.setSource("7083");//取货地址
                    TaskCtuController taskCtuController = SpringUtil.getBean(TaskCtuController.class);
                    CtuLocationMap ctuLocationMap = new CtuLocationMap();
                    ctuLocationMap.setWarehouse_type("入库暂存区");
                    InterReturn interReturn1 = taskCtuController.GetEmptyLocationByType(ctuLocationMap);
                    if (interReturn1.isStatus()) {
                        ctuTaskSend.setTarget(((CtuLocationMap) interReturn1.getResult()).getLocation_code());//卸货地址
                    } else {
                        zReturn.setCode("259");
                        zReturn.setMessage(interReturn1.getMessage());
                        System.out.println(interReturn1.getMessage());
                        logger.error("料箱下一楼到位下发CTU任务获取空位出错：" + interReturn1.getMessage());
                        return zReturn;
                    }

                    //2.插入CTU任务
                    InterReturn interReturn2 = taskCtuController.TaskDownload(ctuTaskSend, zTaskReport.getBarcode(), "提升机下发");//料箱下一楼到位 给CTU发任务
                    if (interReturn2.isStatus()) {
                        zReturn.setCode("200");
                        zReturn.setMessage("已插入CTU任务");
                        logger.info("料箱下一楼到位下发CTU任务成功：" + interReturn2.getMessage());
                        return zReturn;
                    } else {
                        zReturn.setCode("200");
                        zReturn.setMessage(interReturn2.getMessage());
                        System.out.println(interReturn2.getMessage());
                        logger.error("料箱下一楼到位下发CTU任务出错：" + interReturn2.getMessage());
                        return zReturn;

                    }
                }
            }

            //正常任务的完成上报
            else {
                //已完成的任务不再多次完成(按任务号查询，状态为"已完成"则跳出)
                LambdaQueryWrapper<TaskB> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(TaskB::getTask_no, taskId);//任务号
                wrapper.eq(TaskB::getStatus, "已完成");//任务状态
                long count = taskBService.count(wrapper);
                if (count > 0) {
                    zReturn.setCode("124");
                    zReturn.setMessage("重复上报！该任务已完成：" + taskId);
                    return zReturn;
                }


                //分库   截取前3个字符
                String tasknoPre = taskId.substring(0, 3);
                zReturn.setTask_id(taskId);

                if (("1").equals(taskStatus)) {//完成
                    if (("TZP").equals(tasknoPre)) {
                        zReturn = taskReportFinishP(zTaskReport);//托盘
                    } else if (("TZB").equals(tasknoPre)) {
                        zReturn = taskReportFinishB(zTaskReport);//料箱
                    } else {
                        zReturn.setCode("900");
                        zReturn.setMessage("任务前缀出错误：任务反馈 " + zTaskReport.getTask_id());
                    }
                } else if (("2").equals(taskStatus)) {//满入
                    if (("TZP").equals(tasknoPre)) {
                        //托盘满入异常  人工处理1，取消任务 2.堆垛机复位 3.调用下发任务接口(类型robo_change_task) id 终点 任务类型
                        zReturn = taskReportfullIn_EmptyOutP(zReturn, taskId, "满入");
                    } else if (("TZB").equals(tasknoPre)) {
                        //料箱满入异常
                        zReturn = taskReportfullIn_EmptyOutB(zReturn, taskId, "满入");


                        //下发新任务 mfc_change_task 任务调整


                        //更新原task 满入 待改
//                    TaskB taskB = new TaskB();
//                    taskB.setTask_no(zTaskReport.getTask_id());//任务号
//                    taskB.setTask_type("mfc_change_task");//任务类型
//                    taskB.setLocation_code(     自动获取同层       );//终点 满入 同层终点
//                    taskB.setStatus("待执行");//任务状态
//                    boolean boolInsert = taskBService.update(taskB);
//                    if (boolInsert) {
//                        //任务插入成功
//                        logger.info("料箱库插入任务成功：" + taskB.getTask_no());
//                        zReturn.setCode("200");
//                        zReturn.setMessage("料箱库插入任务成功");
//                    } else {
//                        logger.info("料箱库插入任务出错：" + taskB.getTask_no());
//                        zReturn.setCode("222");
//                        zReturn.setMessage("料箱库插入任务错误:" + taskB);
//                    }


                    } else {
                        zReturn.setCode("900");
                        zReturn.setMessage("任务前缀出错误：任务反馈 " + zTaskReport.getTask_id());
                    }
                } else if (("3").equals(taskStatus)) {//空取
                    if (("TZP").equals(tasknoPre)) {
                        //托盘空取异常  人工处理1，取消任务 2.堆垛机复位
                        zReturn = taskReportfullIn_EmptyOutP(zReturn, taskId, "空取");
                    } else if (("TZB").equals(tasknoPre)) {
                        //料箱空取异常
                        zReturn = taskReportfullIn_EmptyOutB(zReturn, taskId, "空取");
                    } else {
                        zReturn.setCode("999");
                        zReturn.setMessage("任务状态出错误：任务反馈 " + zTaskReport.getTask_id());
                    }
                } else {
                    //异常 其他
                    zReturn.setCode("200");
                    zReturn.setMessage("自动库任务" + taskId + "，状态" + taskStatus + "，上报接收成功");
                    System.out.println("自动库任务" + taskId + "，状态" + taskStatus + "，上报接收成功");
                    return zReturn;
                }
            }
        } catch (Exception e) {
            zReturn.setCode("999");
            zReturn.setMessage("自动库任务状态上报出错：" + e);
            System.out.println("自动库任务状态上报出错：" + e);
        }
        logger.info("taskReport 返回：" + zReturn);
        return zReturn;
    }

    /**
     * @Author:xhc
     * @Description P托盘库满入&空出的处理逻辑
     * @DateTime 2024/6/26 22:36
     * @Params
     * @Return
     */
    public ZReturn taskReportfullIn_EmptyOutP(ZReturn zReturn, String taskId, String fullIn_EmptyOut) {
        LambdaUpdateWrapper<TaskP> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TaskP::getTask_no, taskId);//任务号
        TaskP taskP = new TaskP();
        taskP.setStatus(fullIn_EmptyOut);
        boolean boolUpdateTask = taskPService.update(taskP, updateWrapper);
        if (boolUpdateTask) {
            zReturn.setCode("200");
            zReturn.setMessage("托盘库任务" + taskId + "，上报接收成功，任务状态更新为" + fullIn_EmptyOut);
            System.out.println("托盘库任务" + taskId + "，上报接收成功，任务状态更新为" + fullIn_EmptyOut);
            return zReturn;
        } else {
            zReturn.setCode("205");
            zReturn.setMessage("托盘库任务" + taskId + "，上报接收成功，任务状态 " + fullIn_EmptyOut + " 更新出错");
            System.out.println("托盘库任务" + taskId + "，上报接收成功，任务状态 " + fullIn_EmptyOut + " 更新出错");
            return zReturn;
        }
    }

    /**
     * @Author:xhc
     * @Description B料箱盘库满入&空出的处理逻辑
     * @DateTime 2024/6/27 13:20
     * @Params
     * @Return
     */
    public ZReturn taskReportfullIn_EmptyOutB(ZReturn zReturn, String taskId, String fullIn_EmptyOut) {
        LambdaUpdateWrapper<TaskB> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TaskB::getTask_no, taskId);//任务号
        TaskB taskB = new TaskB();
        taskB.setStatus(fullIn_EmptyOut);
        boolean boolUpdateTask = taskBService.update(taskB, updateWrapper);
        if (boolUpdateTask) {
            zReturn.setCode("200");
            zReturn.setMessage("料箱库任务" + taskId + "，上报接收成功，任务状态更新为" + fullIn_EmptyOut);
            System.out.println("料箱库任务" + taskId + "，上报接收成功，任务状态更新为" + fullIn_EmptyOut);
            return zReturn;
        } else {
            zReturn.setCode("205");
            zReturn.setMessage("料箱库任务" + taskId + "，上报接收成功，任务状态 " + fullIn_EmptyOut + " 更新出错");
            System.out.println("料箱库任务" + taskId + "，上报接收成功，任务状态 " + fullIn_EmptyOut + " 更新出错");
            return zReturn;
        }
    }

    /**
     * @Author:xhc
     * @Description P托盘库任务完成上报逻辑
     * @DateTime 2024/6/25 1:18
     * @Params
     * @Return
     */
    public ZReturn taskReportFinishP(ZTaskReport zTaskReport) {
        ZReturn zReturn = new ZReturn();
        String taskId = zTaskReport.getTask_id();//任务号
        zReturn.setTask_id(taskId);
        zReturn.setCode("123");
        //根据任务号查询到任务
        LambdaQueryWrapper<TaskP> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskP::getTask_no, taskId);
        List<TaskP> taskPList = taskPService.list(queryWrapper);

        //1.更改任务表状态为 "已完成"
        LambdaUpdateWrapper<TaskP> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TaskP::getTask_no, taskId);//任务号
        TaskP taskP = new TaskP();
        taskP.setStatus("已完成");
        boolean boolUpdateTask = taskPService.update(taskP, updateWrapper);
        if (boolUpdateTask) {
            //更新任务状态成功
            logger.info("托盘库更新任务状态成功：" + taskId);
        } else {
            logger.info("托盘库更新任务状态出错：" + taskId);
            zReturn.setCode("111");
            zReturn.setMessage("托盘库更新任务状态出错：" + taskId);
            return zReturn;
        }


        //2.更改map表状态 出库为清空 入库绑定
        if ("出库".equals(taskPList.get(0).getTask_type())) {
            // 出库清空
            LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
            updateWrapperMap.eq(LocationMap::getLocation_code, taskPList.get(0).getLocation_code());//货位号
            updateWrapperMap.eq(LocationMap::getWarehouse, "P库");//库名
            LocationMap locationMap = new LocationMap();
            locationMap.setPallet_code("");//托盘清空
            locationMap.setStatus("启用");//货位解除冻结

            boolean boolUpdateMap = locationMapService.update(locationMap, updateWrapperMap);
            if (boolUpdateMap) {
                logger.info("托盘库更新MAP出成功：" + taskId);
            } else {
                logger.info("托盘库更新MAP出错误：" + taskId);
                zReturn.setCode("222");
                zReturn.setMessage("托盘库更新MAP出错误：" + taskId);
                return zReturn;
            }

            //3.仅出库口 下发AGV自动搬运任务
            {
                OutHelper outHelper = new OutHelper();
                Set<String> protNosOut = outHelper.getSetOutP();//出库口
                if (protNosOut.contains(taskPList.get(0).getProt_no())) {
                    logger.info("托盘出库口出库到位 准备给AGV发任务：" + zTaskReport.getBarcode());
                    //1.获取空位
                    AgvTaskSend agvTaskSend = new AgvTaskSend();
                    agvTaskSend.setPrio(1);
                    agvTaskSend.setType(1);
                    switch (taskPList.get(0).getProt_no()) {
                        case "1041":
                            agvTaskSend.setSource("出口1");
                            break;
                        case "1026":
                            agvTaskSend.setSource("出口2");
                            break;
                        case "1009":
                            agvTaskSend.setSource("出口3");
                            break;
                        default:
                            agvTaskSend.setSource("无此对应出口：" + taskPList.get(0).getProt_no());
                            break;
                    }
                    TaskAgvController taskAgvController = SpringUtil.getBean(TaskAgvController.class);
                    AgvLocationMap agvLocationMap = new AgvLocationMap();
                    agvLocationMap.setWarehouse_type("出库暂存区");
                    InterReturn interReturn1 = taskAgvController.GetEmptyLocationByType(agvLocationMap);
                    if (interReturn1.isStatus()) {
                        agvTaskSend.setTarget(((AgvLocationMap) interReturn1.getResult()).getLocation_code());//卸货地址
                    } else {
                        zReturn.setCode("259");
                        zReturn.setMessage(interReturn1.getMessage());
                        System.out.println(interReturn1.getMessage());
                        logger.error("托盘出库口出库到位下发AGV任务获取空位出错：" + interReturn1.getMessage());
                        return zReturn;
                    }

                    //2.插入AGV任务
                    InterReturn interReturn2 = taskAgvController.TaskDownload(agvTaskSend, zTaskReport.getBarcode(), "出库到位下发");//托盘出库到位 给AGV发任务
                    if (interReturn2.isStatus()) {
                        zReturn.setCode("200");
                        zReturn.setMessage("已插入AGV任务");
                        logger.info("托盘出库口出库到位下发AGV任务成功：" + interReturn2.getMessage());
                        return zReturn;
                    } else {
                        zReturn.setCode("200");
                        zReturn.setMessage(interReturn2.getMessage());
                        System.out.println(interReturn2.getMessage());
                        logger.error("料箱下一楼到位下发AGV任务出错：" + interReturn2.getMessage());
                        return zReturn;
                    }
                }
            }
        } else if ("入库".equals(taskPList.get(0).getTask_type())
                || "回库".equals(taskPList.get(0).getTask_type())
                || "满入入库".equals(taskPList.get(0).getTask_type())) {
            // 入库绑定
            LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
            updateWrapperMap.eq(LocationMap::getLocation_code, taskPList.get(0).getLocation_code());//货位号
            updateWrapperMap.eq(LocationMap::getWarehouse, "P库");//库名
            LocationMap locationMap = new LocationMap();
            locationMap.setPallet_code(taskPList.get(0).getPallet_code());//托盘号
            locationMap.setStatus("启用");//货位解除冻结
            boolean boolUpdateMap = locationMapService.update(locationMap, updateWrapperMap);
            if (boolUpdateMap) {
                logger.info("托盘库更新MAP入成功：" + taskId);
            } else {
                logger.info("托盘库更新MAP入出错：" + taskId);
                zReturn.setCode("222");
                zReturn.setMessage("托盘库更新MAP入出错：" + taskId);
                return zReturn;
            }
        } else if ("空托码垛".equals(taskPList.get(0).getTask_type())) {
            //无需更新MAP
            //task在if外已经更新


        } else {
            // 未知任务类型的处理逻辑 托盘库没有移库任务
            logger.info("未知任务类型：" + taskId + ":" + taskPList.get(0).getTask_type());
        }
        zReturn.setCode("200");
        zReturn.setMessage("托盘库任务" + taskId + "已完成，状态上报成功");
        System.out.println("托盘库任务" + taskId + "已完成，状态上报成功");
        return zReturn;
    }

    /**
     * @Author:xhc
     * @Description B料箱库任务完成上报逻辑
     * @DateTime 2024/6/25 1:19
     * @Params
     * @Return
     */
    public ZReturn taskReportFinishB(ZTaskReport zTaskReport) {
        ZReturn zReturn = new ZReturn();
        String taskId = zTaskReport.getTask_id();//任务号
        zReturn.setTask_id(taskId);
        zReturn.setCode("123");
        //根据任务号查询到任务
        LambdaQueryWrapper<TaskB> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskB::getTask_no, taskId);
        List<TaskB> taskBList = taskBService.list(queryWrapper);
        String palletcodeThis = taskBList.get(0).getPallet_code();
        String portThis = taskBList.get(0).getProt_no();

        //1.更改任务表状态为 "已完成"
        LambdaUpdateWrapper<TaskB> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TaskB::getTask_no, taskId);//任务号
        TaskB taskB = new TaskB();
        taskB.setStatus("已完成");
        boolean boolUpdateTask = taskBService.update(taskB, updateWrapper);
        if (boolUpdateTask) {
            //更新任务状态成功
            logger.info("料箱库更新任务状态成功：" + taskId);
        } else {
            logger.info("料箱库更新任务状态出错：" + taskId);
            zReturn.setCode("111");
            zReturn.setMessage("料箱库更新任务状态出错：" + taskId);
            return zReturn;
        }

        //2.更改map表状态 出库为清空 入库绑定
        if ("出库".equals(taskBList.get(0).getTask_type())) {
            // 出库清空
            LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
            updateWrapperMap.eq(LocationMap::getLocation_code, taskBList.get(0).getLocation_code());//货位号
            updateWrapperMap.eq(LocationMap::getWarehouse, "B库");//库名
            LocationMap locationMap = new LocationMap();
            locationMap.setPallet_code("");//料箱清空
            locationMap.setStatus("启用");//货位解除冻结
            boolean boolUpdateMap = locationMapService.update(locationMap, updateWrapperMap);
            if (boolUpdateMap) {
                logger.info("料箱库更新MAP出成功：" + taskId);
            } else {
                logger.info("料箱库更新MAP出错误：" + taskId);
                zReturn.setCode("222");
                zReturn.setMessage("料箱库更新MAP出错误：" + taskId);
                return zReturn;
            }

            //3.出库口是货到人时 更新电子标签绑定的料箱号
            OutHelper outHelper = new OutHelper();
            Set<String> protNosAll = outHelper.getSetB();
            Set<String> protNosFirstFloor = outHelper.getSetFirstFloor();//一楼货到人
            Set<String> protNosRobot = outHelper.getSetRobot();//二楼机械手
            Set<String> protNosMan = outHelper.getSetMan();//二楼人工
            if (protNosAll.contains(portThis)) {
                //绑定电子标签的料箱号
                LambdaUpdateWrapper<BaseETag> updateWrapperLabel = new LambdaUpdateWrapper<>();
                updateWrapperLabel.eq(BaseETag::getTag_location, portThis);//电子标签地址 出库口
                BaseETag baseETag = new BaseETag();

                baseETag.setPallet_code(palletcodeThis);//料箱绑定
                boolean boolUpdateLabel = baseETagService.update(baseETag, updateWrapperLabel);
                if (boolUpdateLabel) {
                    logger.info("电子标签绑定更新成功：" + taskId + "：" + portThis);

                    if (protNosFirstFloor.contains(portThis))//一楼货到人
                    {
                        logger.info("一楼货到人：" + taskId + "：" + portThis);
                        //4.判断隔壁是否有料箱 有则等待其拣选结束 无则本箱亮灯
                        BaseETagController baseETagController = SpringUtil.getBean(BaseETagController.class);
                        String nextPallet = baseETagController.GetNextPallet(portThis);
                        if (nextPallet == null || nextPallet.trim().isEmpty()) {
                            logger.info("隔壁无料箱：" + nextPallet + "则亮灯");
                            //点亮电子标签 获取配盘任务信息
                            zReturn = ZDKETagLight(palletcodeThis);//完成信号触发
                        } else {
                            logger.info("隔壁有料箱：" + nextPallet + "则等待");
                        }

                    } else if (protNosRobot.contains(portThis))//二楼机械手
                    {
                        logger.info("二楼机械手：" + taskId + "：" + portThis);

                        //4.调用wcs9号接口 出库到位（机械手）
                        zReturn = robotHandTask(palletcodeThis, portThis);
                        if (!"200".equals(zReturn.getCode())) {
                            logger.error("出库到位（机械手）下发配盘信息出错：" + zReturn.getMessage());
                        }
                    } else if (protNosMan.contains(portThis))//二楼人工
                    {
                        logger.info("二楼人工：" + taskId + "：" + portThis);

                        //没写 待改

                    } else {
                        logger.error("出库到位位置错误：" + taskId + "：" + taskBList.get(0).getLocation_code());

                    }


                } else {
                    logger.info("电子标签绑定更新失败：" + taskId + "：" + taskBList.get(0).getLocation_code());
                    zReturn.setCode("555");
                    zReturn.setMessage("电子标签绑定更新失败：" + taskId + "：" + taskBList.get(0).getLocation_code());
                    return zReturn;
                }
            }


        } else if ("入库".equals(taskBList.get(0).getTask_type())
                || "回库".equals(taskBList.get(0).getTask_type())
                || "满入入库".equals(taskBList.get(0).getTask_type())) {
            // 入库绑定
            LambdaUpdateWrapper<LocationMap> updateWrapperMap = new LambdaUpdateWrapper<>();
            updateWrapperMap.eq(LocationMap::getLocation_code, taskBList.get(0).getLocation_code());//货位号
            updateWrapperMap.eq(LocationMap::getWarehouse, "B库");//库名
            LocationMap locationMap = new LocationMap();
            locationMap.setPallet_code(palletcodeThis);//料箱号
            locationMap.setStatus("启用");//货位解除冻结
            boolean boolUpdateMap = locationMapService.update(locationMap, updateWrapperMap);
            if (boolUpdateMap) {
                logger.info("料箱库更新MAP入成功：" + taskId);
            } else {
                logger.info("料箱库更新MAP入出错：" + taskId);
                zReturn.setCode("222");
                zReturn.setMessage("料箱库更新MAP入出错：" + taskId);
                return zReturn;
            }
        } else if ("移库".equals(taskBList.get(0).getTask_type())) {
            //移库更新两个MAP
            //mybatisplus
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
            LocationMapMapper mapMapper = sqlSession.getMapper(LocationMapMapper.class);//获取对应Mapper
            Date time = Calendar.getInstance().getTime();//更新时间赋值

            //1.原货位 清空
            LambdaUpdateWrapper<LocationMap> updateWrapperMapOld = new LambdaUpdateWrapper<>();
            updateWrapperMapOld.eq(LocationMap::getLocation_code, taskBList.get(0).getUdf03());//原货位
            updateWrapperMapOld.eq(LocationMap::getWarehouse, "B库");//库名
            LocationMap LocationMapOld = new LocationMap();
            LocationMapOld.setUpdate_time(time);
            LocationMapOld.setPallet_code("");//料箱清空
            LocationMapOld.setStatus("启用");//货位解除冻结
            mapMapper.update(LocationMapOld, updateWrapperMapOld);//准备执行sql
            //2.目标货位 绑定
            LambdaUpdateWrapper<LocationMap> updateWrapperMapNew = new LambdaUpdateWrapper<>();
            updateWrapperMapNew.eq(LocationMap::getLocation_code, taskBList.get(0).getLocation_code());//目标货位
            updateWrapperMapNew.eq(LocationMap::getWarehouse, "B库");//库名
            LocationMap LocationMapNew = new LocationMap();
            LocationMapNew.setUpdate_time(time);
            LocationMapNew.setPallet_code(palletcodeThis);//料箱绑定
            LocationMapNew.setStatus("启用");//货位解除冻结
            mapMapper.update(LocationMapNew, updateWrapperMapNew);//准备执行sql

            try {
                sqlSession.commit();//执行sql
                zReturn.setMessage("料箱库移库更新MAP成功：" + taskId);
            } catch (Exception e) {
                logger.error("料箱库移库更新MAP异常，事务回滚", e);
                sqlSession.rollback();
                zReturn.setCode("222");
                zReturn.setMessage("料箱库移库更新MAP异常：" + taskId + ":" + e);
            } finally {
                sqlSession.close();
            }
        } else {
            // 未知任务类型的处理逻辑 移库  待改
            logger.info("未知任务类型：" + taskId + ":" + taskBList.get(0).getTask_type());
        }
        zReturn.setCode("200");
        zReturn.setMessage("料箱库任务" + taskId + "已完成，状态上报成功");
        System.out.println("料箱库任务" + taskId + "已完成，状态上报成功");
        return zReturn;
    }

    @ApiOperation(value = "8桁架箱任务下发", notes = "参数 待改")
    @PostMapping("/TaskZ/trussBoxTask")
    @CrossOrigin
    public InterReturn trussBoxTask(@RequestBody ZTrussBoxTask zTrussBoxTask) {
        logger.info("进入 trussBoxTask");
        logger.info("接收到参数：" + zTrussBoxTask);
        InterReturn interReturn = new InterReturn();

        //判断数量是否合法
        if (zTrussBoxTask.getBox_count() == null) {
            interReturn.setStatus(false);
            interReturn.setMessage("数量为空");
            logger.info("trussBoxTask 返回：" + zTrussBoxTask);
            return interReturn;
        }

        try {
            String URL = environment.getProperty("apiZ.trussBoxTask");
            logger.info("自动库8桁架箱任务下发，拼接入参：" + zTrussBoxTask);
            HttpEntity<ZTrussBoxTask> httpEntitysnew = new HttpEntity<>(zTrussBoxTask);
            //参数为 url http实体 返回类型
            ZReturn response = restTemplate.postForObject(URL, httpEntitysnew, ZReturn.class);
            if (("200").equals(response.getCode())) {
                interReturn.setStatus(true);
                interReturn.setMessage("8桁架箱任务下发成功");
                interReturn.setResult(response);
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("8桁架箱任务下发出错：" + response.getMessage());
                interReturn.setResult(response);
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("8桁架箱任务下发 调用出错：" + e.getCause());
            System.out.println("8桁架箱任务下发 调用出错：" + e.getMessage());
        }
        logger.info("trussBoxTask 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "9机械手拣选任务下发", notes = "料箱号 格口位置号")
    @PostMapping("/TaskZ/robotHandTask")
    @CrossOrigin
    public ZReturn robotHandTask(@RequestBody String palletcode, String dest) {
        logger.info("robotHandTask 接到参数：" + palletcode + "和" + dest);
        ZReturn zReturn = new ZReturn();
        try {
            //查询此料箱的配盘表
            LambdaQueryWrapper<OutPick> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OutPick::getPallet_code, palletcode);
            wrapper.eq(OutPick::getStatus, "待执行");
            List<OutPick> outPicks = outPickService.list(wrapper);
            String URL = environment.getProperty("apiZ.machineSort");

            //此处使用实际的格口号 配盘表中的仅第一条可用
            for (OutPick outPick : outPicks) {
                ZHandBoxTask zTrussBoxTask = new ZHandBoxTask();//单次抓取任务
                zTrussBoxTask.setTask_id(outPick.getId());//任务号
                zTrussBoxTask.setBarcode(outPick.getPallet_code());//料箱编号
                zTrussBoxTask.setDest(dest);//格口位置号
                zTrussBoxTask.setAll_mun(outPick.getPick_qty().toString());//抓取数量

                if ("1".equals(outPick.getCell_id())) {
                    zTrussBoxTask.setCell_position("1");//宫格位置
                    zTrussBoxTask.setCell_mun("1");//宫格数
                } else {
                    String[] result = outPick.getCell_id().split("/");
                    zTrussBoxTask.setCell_position(result[0]);//宫格位置
                    zTrussBoxTask.setCell_mun(result[1]);//宫格数
                }

                HttpEntity<ZHandBoxTask> httpEntitysnew = new HttpEntity<>(zTrussBoxTask);
                //参数为 url http实体 返回类型
                zReturn = restTemplate.postForObject(URL, httpEntitysnew, ZReturn.class);
            }
        } catch (Exception e) {
            zReturn.setCode("222");
            zReturn.setMessage("9机械手拣选任务下发 出错：" + e.getCause());
            System.out.println("9机械手拣选任务下发 出错：" + e.getMessage());
            logger.error("9机械手拣选任务下发 出错：" + e.getMessage());
        }
        logger.info("9机械手拣选任务下发 返回：" + zReturn);
        return zReturn;
    }
}
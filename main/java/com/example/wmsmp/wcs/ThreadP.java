package com.example.wmsmp.wcs;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wmsmp.config.SpringUtil;
import com.example.wmsmp.entity.task.Z.*;
import com.example.wmsmp.service.TaskPService;
import org.apache.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ThreadP extends Thread {
    private static Logger logger = Logger.getLogger(ThreadP.class);


    public void run() {
        TaskPService taskPService = SpringUtil.getBean(TaskPService.class);
        RestTemplate restTemplate = new RestTemplate();//用于调用外部接口
        Environment environment = SpringUtil.getBean(Environment.class);//用于读取配置文件
        int i = 0;
        while (true) {
            try {
                Thread.sleep(3000);
                if (i >= 1000)
                    i = 0;
                i++;
                System.out.println("P托盘库WCS线程正在运行..." + i);

                //1.1 查任务表 找待下发的任务
                LambdaQueryWrapper<TaskP> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(TaskP::getWarehouse, "P库");//库名
                wrapper.in(TaskP::getTask_type, Arrays.asList("出库", "回库", "满入入库", "空托码垛"));//任务类型
                wrapper.eq(TaskP::getStatus, "待执行");//任务状态
                wrapper.orderByAsc(TaskP::getTask_no);//顺序
                List<TaskP> taskPList = taskPService.list(wrapper);
                if (taskPList.size() == 0) {
                    //没有出库任务
                    continue;
                }

                //1.2 查任务表 找已下发
//                LambdaQueryWrapper<TaskP> wrapperSent = new LambdaQueryWrapper<>();
//                wrapperSent.eq(TaskP::getWarehouse, "P库");//库名
//                wrapperSent.eq(TaskP::getTask_type, "出库");//任务类型
//                wrapperSent.eq(TaskP::getStatus, "已下发");//任务状态
//                List<TaskP> taskDListSent = taskPService.list(wrapperSent);
//                if (taskDListSent.size() > 20) {//已下发数量
//                    //已经有多条已下发
//                    continue;
//                }

                ZTask taskone = new ZTask();
                taskone.setTask_id(taskPList.get(0).getTask_no());//任务号
                taskone.setBarcode(taskPList.get(0).getPallet_code());//条码

                if ("出库".equals(taskPList.get(0).getTask_type())) {
                    taskone.setTask_type("robo_out_task");//任务类型
                    taskone.setTask_type_desc("堆垛机出库");//
                    String endStr = taskPList.get(0).getLocation_code().substring(2, 8);
                    String endStrNew = Integer.valueOf(endStr.substring(0, 2)) + "_" +
                            Integer.parseInt(endStr.substring(2, 4)) + "_" +
                            Integer.parseInt(endStr.substring(4, 6));
                    taskone.setStart_dest(endStrNew);//起点
                    taskone.setEnd_dest(taskPList.get(0).getProt_no());//终点
                } else if ("回库".equals(taskPList.get(0).getTask_type())) {
                    taskone.setTask_type("robo_in_task");//任务类型
                    taskone.setTask_type_desc("堆垛机回库");//
                    String endStr = taskPList.get(0).getLocation_code().substring(2, 8);
                    String endStrNew = Integer.valueOf(endStr.substring(0, 2)) + "_" +
                            Integer.parseInt(endStr.substring(2, 4)) + "_" +
                            Integer.parseInt(endStr.substring(4, 6));
                    taskone.setEnd_dest(endStrNew);//终点
                    taskone.setStart_dest(taskPList.get(0).getProt_no());//起点
                } else if ("满入入库".equals(taskPList.get(0).getTask_type())) {
                    taskone.setTask_type("robo_change_task");//任务类型
                    taskone.setTask_type_desc("堆垛机满入新任务入库");//
                    String endStr = taskPList.get(0).getLocation_code().substring(2, 8);
                    String endStrNew = Integer.valueOf(endStr.substring(0, 2)) + "_" +
                            Integer.parseInt(endStr.substring(2, 4)) + "_" +
                            Integer.parseInt(endStr.substring(4, 6));
                    taskone.setEnd_dest(endStrNew);//终点
                    taskone.setStart_dest("堆垛机载货台");//起点
                } else if ("空托码垛".equals(taskPList.get(0).getTask_type())) {
                    taskone.setTask_type("robo_empty_tray_in_1f");//任务类型
                    taskone.setTask_type_desc("一楼空托盘入码垛机");//
                    taskone.setEnd_dest("1074");//终点
                    taskone.setStart_dest(taskPList.get(0).getProt_no());//起点
                } else {
                    //未识别的任务类型
                    System.out.println("TaskP任务类型异常：" + taskPList.get(0).getTask_type());
                    logger.error("TaskP任务类型异常：" + taskPList.get(0).getTask_type());
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
                taskone.setCreate_time(sdf.format(Calendar.getInstance().getTime()));//下发时间
                taskone.setMfc_out_priority("5");//穿梭车出库优先级（出库任务有）
                taskone.setMfc_level("5");//穿梭车层（有穿梭车地址必带


                //记录调用接口的入参
                String URL = environment.getProperty("apiZ.wmsWhTask");
                logger.info("P托盘库线程 调用下发任务接口入参：" + taskone);
                HttpEntity<ZTask> httpEntitysnew = new HttpEntity<>(taskone);
                //参数为 url http实体 返回类型
                ZReturn response = restTemplate.postForObject(URL, httpEntitysnew, ZReturn.class);

                String status = "";
                if (("200").equals(response.getCode())) {
                    //任务下发成功
                    System.out.println("P托盘库线程任务下发成功");
                    status = "已下发";
                } else {
                    logger.info("P托盘库线程下发出库任务出错：" + response.getMessage());
                    status = "下发出错：" + response.getMessage();
                }


                //3.改任务状态为已下发
                LambdaQueryWrapper<TaskP> wrapperUpdate = new LambdaQueryWrapper<>();
                wrapperUpdate.eq(TaskP::getTask_no, taskPList.get(0).getTask_no());//任务号
                TaskP taskPUpdate = new TaskP();
                taskPUpdate.setStatus(status);//任务状态
                boolean boolUpdate = taskPService.update(taskPUpdate, wrapperUpdate);
                if (boolUpdate) {
                    //任务下发成功
                    logger.info("P托盘库线程更新任务状态成功：" + taskPList.get(0).getTask_no());
                } else {
                    logger.info("P托盘库线程更新任务状态出错：" + taskPList.get(0).getTask_no());
                }
            } catch (Exception e) {
                logger.error("TaskP线程发生错误", e);
            }
        }

    }
}

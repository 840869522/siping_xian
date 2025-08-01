package com.example.wmsmp.wcs;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wmsmp.config.SpringUtil;
import com.example.wmsmp.entity.task.Z.TaskB;
import com.example.wmsmp.entity.task.Z.ZReturn;
import com.example.wmsmp.entity.task.Z.ZTask;
import com.example.wmsmp.service.TaskBService;
import org.apache.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ThreadB extends Thread {
    private static Logger logger = Logger.getLogger(ThreadB.class);


    public void run() {
        TaskBService taskBService = SpringUtil.getBean(TaskBService.class);
        RestTemplate restTemplate = new RestTemplate();//用于调用外部接口
        Environment environment = SpringUtil.getBean(Environment.class);//用于读取配置文件
        int i = 0;
        while (true) {
            try {
                Thread.sleep(3000);
                if (i >= 1000)
                    i = 0;
                i++;
                System.out.println("B托盘库WCS线程正在运行..." + i);

                //1.查任务表 找出库任务
                LambdaQueryWrapper<TaskB> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(TaskB::getWarehouse, "B库");//库名
                wrapper.in(TaskB::getTask_type, Arrays.asList("出库", "回库", "移库", "满箱给agv", "满入入库"));//任务类型
                wrapper.eq(TaskB::getStatus, "待执行");//任务状态
                wrapper.orderByAsc(TaskB::getTask_no);//顺序
                List<TaskB> taskBList = taskBService.list(wrapper);
                if (taskBList.size() == 0) {
                    //没有出库任务
                    continue;
                }

                //1.2 查任务表 找已下发
                LambdaQueryWrapper<TaskB> wrapperSent = new LambdaQueryWrapper<>();
                wrapperSent.eq(TaskB::getWarehouse, "B库");//库名
                wrapperSent.eq(TaskB::getTask_type, "出库");//任务类型
                wrapperSent.eq(TaskB::getStatus, "已下发");//任务状态
                List<TaskB> taskDListSent = taskBService.list(wrapperSent);
                if (taskDListSent.size() > 30) {//已下发数量
                    //已经有多条已下发
                    continue;
                }

                //2.下发给料箱库德玛的wcs
                ZTask taskone = new ZTask();
                taskone.setTask_id(taskBList.get(0).getTask_no());//任务号
                taskone.setBarcode(taskBList.get(0).getPallet_code());//条码

                if ("出库".equals(taskBList.get(0).getTask_type())) {
                    taskone.setTask_type("mfc_out_task");//任务类型
                    taskone.setTask_type_desc("穿梭车出库");//
                    taskone.setStart_dest(taskBList.get(0).getLocation_code().substring(4, 10));//起点
                    taskone.setEnd_dest(taskBList.get(0).getProt_no());//终点
                    taskone.setMfc_level(taskBList.get(0).getLocation_code().substring(2, 4));//层号
                } else if ("回库".equals(taskBList.get(0).getTask_type())) {
                    taskone.setTask_type("mfc_in_task");//任务类型
                    taskone.setTask_type_desc("回库");//
                    taskone.setStart_dest(taskBList.get(0).getProt_no());//起点
                    taskone.setEnd_dest(taskBList.get(0).getLocation_code().substring(4, 10));//终点
                    taskone.setMfc_level(taskBList.get(0).getLocation_code().substring(2, 4));//层号
                } else if ("移库".equals(taskBList.get(0).getTask_type())) {
                    taskone.setTask_type("mfc_move_task");//任务类型
                    taskone.setTask_type_desc("穿梭车移库");//
                    taskone.setStart_dest(taskBList.get(0).getUdf03().substring(4, 10));//起点
                    taskone.setEnd_dest(taskBList.get(0).getLocation_code().substring(4, 10));//终点
                    taskone.setMfc_level(taskBList.get(0).getLocation_code().substring(2, 4));//层号
                } else if ("满箱给agv".equals(taskBList.get(0).getTask_type())) {
                    taskone.setTask_type("mfc_choose_over");//任务类型
                    taskone.setTask_type_desc("满箱给agv");//解除输送线PLC出库口绑定
                    taskone.setStart_dest(taskBList.get(0).getProt_no());//起点
                } else if ("满入入库".equals(taskBList.get(0).getTask_type())) {
                    taskone.setTask_type("mfc_change_task");//任务类型
                    taskone.setTask_type_desc("满入新货位入库");//满入异常处理后 原任务更新新目标地址
                    taskone.setEnd_dest(taskBList.get(0).getLocation_code().substring(4, 10));//终点
                    taskone.setMfc_level(taskBList.get(0).getLocation_code().substring(2, 4));//层号
                } else {
                    System.out.println("TaskB任务类型异常：" + taskBList.get(0).getTask_type());
                    logger.error("TaskB任务类型异常：" + taskBList.get(0).getTask_type());
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
                taskone.setCreate_time(sdf.format(Calendar.getInstance().getTime()));//下发时间
                taskone.setMfc_out_priority("5");//穿梭车出库优先级（出库任务有）


                //记录调用接口的入参
                String URL = environment.getProperty("apiZ.wmsWhTask");
                logger.info("B料箱库线程 调用下发任务接口入参：" + taskone);
                HttpEntity<ZTask> httpEntitysnew = new HttpEntity<>(taskone);
                //参数为 url http实体 返回类型
                ZReturn response = restTemplate.postForObject(URL, httpEntitysnew, ZReturn.class);

                String status = "";
                if (("200").equals(response.getCode())) {
                    //任务下发成功
                    System.out.println("B料箱库线程任务下发成功");
                    status = "已下发";
                } else {
                    logger.info("B料箱库线程下发出库任务出错：" + response.getMessage());
                    status = "下发出错：" + response.getMessage();
                }

                //3.改任务状态为已下发
                LambdaQueryWrapper<TaskB> wrapperUpdate = new LambdaQueryWrapper<>();
                wrapperUpdate.eq(TaskB::getTask_no, taskBList.get(0).getTask_no());//任务号
                TaskB taskBUpdate = new TaskB();

                //满箱给agv 直接完成
                if ("满箱给agv".equals(taskBList.get(0).getTask_type())) {
                    taskBUpdate.setStatus("已完成");//任务状态
                } else {
                    taskBUpdate.setStatus(status);//任务状态
                }
                boolean boolUpdate = taskBService.update(taskBUpdate, wrapperUpdate);
                if (boolUpdate) {
                    //任务下发成功
                    logger.info("B料箱库线程更新任务状态成功：" + taskBList.get(0).getTask_no());
                } else {
                    logger.info("B料箱库线程更新任务状态出错：" + taskBList.get(0).getTask_no());
                }
            } catch (Exception e) {
                logger.error("TaskB线程发生错误", e);
            }
        }
    }
}

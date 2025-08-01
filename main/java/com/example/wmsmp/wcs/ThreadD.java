package com.example.wmsmp.wcs;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wmsmp.config.SpringUtil;
import com.example.wmsmp.entity.task.D.*;
import com.example.wmsmp.entity.task.Z.TaskB;
import com.example.wmsmp.service.TaskDService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@Component
public class ThreadD extends Thread {
    private static Logger logger = Logger.getLogger(ThreadD.class);

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    //@Autowired
    //private Environment environment;//用于读取配置文件

    // @Autowired
    private RestTemplate restTemplate;//用于调用外部接口

    public void run() {
        TaskDService taskDService = SpringUtil.getBean(TaskDService.class);
        RestTemplate restTemplate = new RestTemplate();//用于调用外部接口
        Environment environment = SpringUtil.getBean(Environment.class);//用于读取配置文件
        int i = 0;
        while (true) {
            try {
                Thread.sleep(3000);
                if (i >= 1000)
                    i = 0;
                i++;
                System.out.println("D大件库WCS线程正在运行..." + i);

                //1.1 查任务表 找出库任务
                LambdaQueryWrapper<TaskD> wrapperWait = new LambdaQueryWrapper<>();
                wrapperWait.eq(TaskD::getWarehouse, "D库");//库名
                wrapperWait.in(TaskD::getTask_type, Arrays.asList("出库", "移库", "满入入库"));//任务类型
                wrapperWait.eq(TaskD::getStatus, "待执行");//任务状态
                wrapperWait.orderByAsc(TaskD::getTask_no);//顺序
                List<TaskD> taskDList = taskDService.list(wrapperWait);
                if (taskDList.size() == 0) {
                    //没有出库任务
                    continue;
                }

                //1.2 查任务表 找已下发
                LambdaQueryWrapper<TaskD> wrapperSent = new LambdaQueryWrapper<>();
                wrapperSent.eq(TaskD::getWarehouse, "D库");//库名
                wrapperSent.eq(TaskD::getTask_type, "出库");//任务类型
                wrapperSent.eq(TaskD::getStatus, "已下发");//任务状态
                wrapperSent.orderByAsc(TaskD::getTask_no);//顺序
                List<TaskD> taskDListSent = taskDService.list(wrapperSent);
                if (taskDListSent.size() > 5) {//已下发数量
                    //已经有多条已下发
                    continue;
                }

                //2.下发给大件库速锐的wcs
                DtaskReceive dtaskReceive = new DtaskReceive();
                dtaskReceive.setGroupId("组号");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定时间格式
                dtaskReceive.setMsgTime(sdf.format(Calendar.getInstance().getTime()));//下发时间
                dtaskReceive.setPriorityCode(0);//优先级
                dtaskReceive.setWarehouse("SPJG");//仓库名称 固定值SPJG

                List<Tasks> tasksList = new ArrayList<>();
                Tasks tasks1 = new Tasks();
                tasks1.setTaskId(taskDList.get(0).getTask_no());//任务号
                tasks1.setBarCode(taskDList.get(0).getPallet_code());//托盘码
                if ("出库".equals(taskDList.get(0).getTask_type())) {
                    tasks1.setEndNode(taskDList.get(0).getProt_no());//任务终点
                    tasks1.setStartNode(taskDList.get(0).getLocation_code().substring(2, 8));//任务起点
                    tasks1.setTaskType(1);//任务类型 0入库 1出库 2移库
                    tasks1.setOrder(3);//排序 0最先
                } else if ("移库".equals(taskDList.get(0).getTask_type())) {
                    tasks1.setEndNode(taskDList.get(0).getLocation_code().substring(2, 8));//任务终点
                    tasks1.setStartNode(taskDList.get(0).getUdf03().substring(2, 8));//任务起点
                    tasks1.setTaskType(2);//任务类型 0入库 1出库 2移库
                    tasks1.setOrder(3);//排序 0最先
                } else {
                    System.out.println("TaskD任务类型异常：" + taskDList.get(0).getTask_type());
                    logger.error("TaskD任务类型异常：" + taskDList.get(0).getTask_type());
                }


                tasksList.add(tasks1);
                dtaskReceive.setTasks(tasksList);

                //记录调用接口的入参
                logger.info("D大件库线程 调用下发任务接口入参：" + dtaskReceive);
                HttpEntity<DtaskReceive> httpEntitysnew = new HttpEntity<>(dtaskReceive);
                String URL = environment.getProperty("apiD.taskReceive");
                //参数为 url http实体 返回类型
                DReturn response = restTemplate.postForObject(URL, httpEntitysnew, DReturn.class);

                String status = "";
                if (response.getReturnStatus() == 0) {
                    //任务下发成功
                    System.out.println("D大件库线程任务下发成功");
                    status = "已下发";
                } else {
                    logger.info("D大件库线程下发出库任务出错：" + response.getReturnInfo());
                    status = "下发出错：" + response.getReturnInfo();
                }


                //3.改任务状态为 下发结果
                LambdaQueryWrapper<TaskD> wrapperUpdate = new LambdaQueryWrapper<>();
                wrapperUpdate.eq(TaskD::getTask_no, taskDList.get(0).getTask_no());//任务号
                TaskD taskDUpdate = new TaskD();
                taskDUpdate.setStatus(status);//任务状态
                boolean boolUpdate = taskDService.update(taskDUpdate, wrapperUpdate);
                if (boolUpdate) {
                    //任务下发成功
                    logger.info("D大件库线程更新任务状态成功：" + taskDList.get(0).getTask_no());
                } else {
                    logger.info("D大件库线程更新任务状态出错：" + taskDList.get(0).getTask_no());
                }
            } catch (Exception e) {
                logger.error("TaskD线程发生错误", e);
            }
        }
    }
}

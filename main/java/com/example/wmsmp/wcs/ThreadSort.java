package com.example.wmsmp.wcs;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wmsmp.config.SpringUtil;
import com.example.wmsmp.controller.TaskZController;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.task.TaskSort;
import com.example.wmsmp.service.TaskSortService;
import org.apache.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class ThreadSort extends Thread {

    private static Logger logger = Logger.getLogger(ThreadX.class);

    public void run() {
        TaskSortService taskSortService = SpringUtil.getBean(TaskSortService.class);
        RestTemplate restTemplate = new RestTemplate();//用于调用外部接口
        Environment environment = SpringUtil.getBean(Environment.class);//用于读取配置文件
        int i = 0;
        while (true) {
            try {
                Thread.sleep(5000);
                //1.读取待执行的任务号
                LambdaQueryWrapper<TaskSort> wrapperWait1 = new LambdaQueryWrapper<>();
                wrapperWait1.eq(TaskSort::getTask_type, "wave_sort_task");//任务类型
                wrapperWait1.eq(TaskSort::getStatus, "待执行");//任务状态
                wrapperWait1.orderByAsc(TaskSort::getId);//顺序
                List<TaskSort> taskSortList1 = taskSortService.list(wrapperWait1);
                if (taskSortList1.size() == 0) {
                    //没有分拣任务
                    continue;
                }

                //2.根据任务详情，调用接口，执行任务                 //3.成功后改为已下发(接口已实现)
                TaskZController taskZController = SpringUtil.getBean(TaskZController.class);
                InterReturn interReturn = taskZController.ZDKtaskSorter(taskSortList1.get(0).getOrder_no());
                if (interReturn.isStatus()) {
                    //任务下发成功
                    System.out.println("分拣机任务下发成功");
                } else {
                    logger.info("分拣机下发出库任务出错：" + interReturn.getMessage());
                    System.out.println("分拣机下发出库任务出错：" + interReturn.getMessage());
                }
                System.out.println("分拣机WCS线程正在运行...");
            } catch (Exception e) {
                logger.error("循环读取分拣机任务表发生错误", e);
            }
        }
    }
}

package com.example.wmsmp.wcs;

import com.example.wmsmp.controller.InOrderDetailController;
import org.apache.log4j.Logger;

public class ThreadX extends Thread {
    private static Logger logger = Logger.getLogger(ThreadX.class);


    public void run() {
        int i = 0;

        while (true) {
            try {
                Thread.sleep(500);
                //读取数据库表，并作其他操作

                //1.读取待执行的任务号

                //2.根据任务号，读取任务详情

                //3.根据任务详情，调用分拣机接口，执行任务

                //4.成功后改为已下发


                if (i >= 1000)
                    i = 0;
                i++;
                System.out.println("P托盘库WCS线程正在运行..." + i);

            } catch (Exception e) {
                logger.error("TaskX线程发生错误", e);
            }
        }
    }
}

package com.example.wmsmp;

import com.example.wmsmp.config.SpringUtil;
import com.example.wmsmp.led.server.LedSDKServer;
import com.example.wmsmp.wcs.ThreadB;
import com.example.wmsmp.wcs.ThreadD;
import com.example.wmsmp.wcs.ThreadP;
import com.example.wmsmp.wcs.ThreadSort;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;

@Import(SpringUtil.class)
@SpringBootApplication
@EnableTransactionManagement
//@MapperScan("com.example.wmsmp.mapper")
@MapperScan("com.example.wmsmp.**.mapper")
public class WmsmpApplication implements CommandLineRunner {

    @Resource
    private LedSDKServer ledSDKServer;

    public static void main(String[] args) {
        SpringApplication.run(WmsmpApplication.class, args);
        //wcs
        ThreadP threadP = new ThreadP();//托盘库
        threadP.start();

        ThreadB threadB = new ThreadB();//料箱库
        threadB.start();

        ThreadD threadD = new ThreadD();//大件库
        threadD.start();

        ThreadSort threadSort = new ThreadSort();//分拣机任务下发
        threadSort.start();
    }

    @Override
    public void run(String... args) throws Exception {
        ledSDKServer.start();
    }
}

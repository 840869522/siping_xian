package com.example.wmsmp.led.server;

import lombok.extern.slf4j.Slf4j;
import onbon.bx06.Bx6GEnv;
import onbon.bx06.Bx6GScreenClient;
import onbon.bx06.series.Bx6E;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ClassName LedSDKServer
 * @Author wlixun
 * @Date 2024/8/2 14:50
 * @Version 1.0
 * @Description TODO
 **/
@Component
@Slf4j
public class LedSDKServer {

    @Value("${led.ip}")
    private String ip;

    @Value("${led.port}")
    private Integer port;

    @Value("${led.timeout}")
    private Integer timeout;

    public Bx6GScreenClient screen;

    /**
     * 开启LED显示器连接
     *
     * @throws Exception
     */
    public boolean start() throws Exception {
        //初始化SDK
        Bx6GEnv.initial(timeout);
        screen = new Bx6GScreenClient("MyScreen", new Bx6E());
        //连接LED显示器
        boolean connect = screen.connect(ip, port);
        if (connect) log.info("室外led=={}:{}==显示器连接成功!", ip, port);
        else {
            log.info("室外led=={}:{}==显示器连接失败!", ip, port);
            return connect;
        }
        // 启动后手动校准时间
        screen.syncTime();
        return connect;
    }

    /**
     * 关闭LED显示器连接
     */
    public void stop() {
        // 继开与控制器之间的链接
        screen.disconnect();
    }
}

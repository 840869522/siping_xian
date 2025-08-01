package com.example.wmsmp.led.cmd;

import com.example.wmsmp.led.server.LedSDKServer;
import lombok.extern.slf4j.Slf4j;
import onbon.bx06.Bx6GScreen;
import onbon.bx06.Bx6GScreenClient;
import onbon.bx06.Bx6GScreenProfile;
import onbon.bx06.area.*;
import onbon.bx06.area.page.TextBxPage;
import onbon.bx06.file.ProgramBxFile;
import onbon.bx06.message.led.ReturnControllerStatus;
import onbon.bx06.utils.DisplayStyleFactory;
import onbon.bx06.utils.TextBinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.awt.*;
import java.io.IOException;

/**
 * @ClassName LedCmdUtil
 * @Author asus
 * @Date 2024/8/2 15:33
 * @Version 1.0
 * @Description TODO
 **/
@Component
@Slf4j
public class LedCmdUtil {

    @Resource
    private LedSDKServer ledSDKServer;

    @Value("${led.x}")
    private Integer x;

    @Value("${led.y}")
    private Integer y;

    @Value("${led.width}")
    private Integer width;

    @Value("${led.height}")
    private Integer height;

    // 获取显示特技方式
    private DisplayStyleFactory.DisplayStyle[] styles = DisplayStyleFactory.getStyles().toArray(new DisplayStyleFactory.DisplayStyle[0]);

    public boolean checkOnline() throws Exception {
        Bx6GScreenClient screen = ledSDKServer.screen;
        Bx6GScreen.Result<ReturnControllerStatus> result = screen.checkControllerStatus();
        log.info("LED显示器状态为", result.isOK());
        System.out.println(result.isOK());
        if (result.isOK()) {
            ReturnControllerStatus reply = result.reply;
            return true;
        } else {
            //链接断开则进行重连
            boolean start = ledSDKServer.start();
            return start;
        }
    }

    /**
     * 向LED下发节目
     *
     * @param programId 节目唯一标识
     * @param area      内容
     * @return
     * @throws Exception
     */
    public boolean sendLed(Integer programId, BxArea... area) throws Exception {
        Bx6GScreenClient screen = ledSDKServer.screen;
        //下发命令如果网络不通则重连下发 如果重连失败则退出发送
        if (screen == null) {
            boolean start = ledSDKServer.start();
            if (start) screen = ledSDKServer.screen;
            else return false;
        }
        //获取控制器参数
        Bx6GScreenProfile profile = screen.getProfile();
        // 创建节目文件
        ProgramBxFile programBxFile = new ProgramBxFile(programId, profile);
        // 是否显示节目边框
        programBxFile.setFrameShow(false);
        //将文本区添加至节目中
        for (BxArea bxArea : area) {
            programBxFile.addArea(bxArea);
        }
        //校验是否超出屏幕
        if (programBxFile.validate() != null) {
            log.error("LED文字超出屏幕!");
            return false;
        }
        //删除之前发送的节目及动画
//        screen.deletePrograms();
        screen.deleteProgram(programId);
        screen.deleteAllDynamic();
        //下发节目到LED
        screen.writeProgramQuickly(programBxFile);
        return true;
    }

    /**
     * 构建文本内容
     *
     * @param text 文本信息
     * @return
     */
    public TextCaptionBxArea buildText(String text, Integer displayStyle, Integer x, Integer y, Integer width, Integer height) throws IOException {
        Bx6GScreenClient screen = ledSDKServer.screen;
        if (screen == null) return null;
        // 创建一个字幕页
        TextCaptionBxArea area = new TextCaptionBxArea(x, y, width, height, screen.getProfile());
        // 使能区域边框
        area.setFrameShow(false);
        // 使用内置边框3
        area.loadFrameImage(0);
        // 将创建的 page 添加到 area 中
        // 构建文本
        TextBxPage textBxPage = new TextBxPage(text);
        // 设置文本水平对齐方式
        textBxPage.setHorizontalAlignment(TextBinary.Alignment.CENTER);
        // 设置文本垂直居中方式
        textBxPage.setVerticalAlignment(TextBinary.Alignment.CENTER);
        // 设置文本字体
        textBxPage.setFont(new Font("宋体", Font.PLAIN, 14));
        // 设置文本颜色
        textBxPage.setForeground(Color.red);
        // 设置区域背景色，默认为黑色
        textBxPage.setBackground(Color.darkGray);
        // 调整特技方式 静止不动
        textBxPage.setDisplayStyle(styles[displayStyle]);
        // 调整特技速度
        textBxPage.setSpeed(10);
        // 调整停留时间, 单位 10ms
        textBxPage.setStayTime(0);
        textBxPage.setHeadTailInterval(-2);
        area.addPage(textBxPage);
        return area;
    }

    public TextCaptionBxArea buildCarText(String text, Integer displayStyle, Integer x, Integer y, Integer width, Integer height) throws IOException {
        Bx6GScreenClient screen = ledSDKServer.screen;
        if (screen == null) return null;
        // 创建一个字幕页
        TextCaptionBxArea area = new TextCaptionBxArea(x, y, width, height, screen.getProfile());
        // 使能区域边框
        area.setFrameShow(false);
        // 使用内置边框3
        area.loadFrameImage(0);
        // 将创建的 page 添加到 area 中
        // 构建文本
        TextBxPage textBxPage = new TextBxPage(text);
        // 设置文本水平对齐方式
        textBxPage.setHorizontalAlignment(TextBinary.Alignment.CENTER);
        // 设置文本垂直居中方式
        textBxPage.setVerticalAlignment(TextBinary.Alignment.NEAR);
        // 设置文本字体
        textBxPage.setFont(new Font("宋体", Font.PLAIN, 14));
        // 设置文本颜色
        textBxPage.setForeground(Color.red);
        // 设置区域背景色，默认为黑色
        textBxPage.setBackground(Color.darkGray);
        // 调整特技方式 静止不动
        textBxPage.setDisplayStyle(styles[displayStyle]);
        // 调整特技速度
        textBxPage.setSpeed(10);
        // 调整停留时间, 单位 10ms
        textBxPage.setStayTime(0);
        textBxPage.setHeadTailInterval(-2);
        area.addPage(textBxPage);
        return area;
    }

    public DateTimeBxArea buildDate() throws Exception {
        Bx6GScreenClient screen = ledSDKServer.screen;
        if (screen == null) return null;
        // 构建文本
        DateTimeBxArea dtArea = new DateTimeBxArea(0, 28, screen.getProfile());
        // 设置字体
        dtArea.setFont(new Font("宋体", Font.PLAIN, 14));
        // 设置显示颜色
        dtArea.setForeground(Color.yellow);
        // 多行显示还是单行显示
        dtArea.setMultiline(false);
        // 年月日显示格式
        // 如果不需要显示，设置为null
        dtArea.setDateStyle(DateStyle.YYYY_MM_DD_1);
        dtArea.setTimeStyle(TimeStyle.HH_MM_SS_1);
        dtArea.setWeekStyle(null);
        return dtArea;
    }

}

package com.example.wmsmp.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 上官青云
 * @date 2023/4/27 16:45
 * @desc 处理日期的工具类
 */
@Slf4j
public class DateUtil {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 使用全局变量： sdf，将字符串转换为java.util.Date类型并返回
     * @param stringDate 需要被转换的日期字符串
     * @return 转换之后的日期
     */
    public static Date convertFromStringToDate(String stringDate) {
        Date date = null;
        try {
            date = sdf.parse(stringDate);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            return date;
        }
    }

    /**
     * 使用全局变量： sdf，将日期转换成字符串
     * @param date 需要被转换的日期
     * @return 转换之后的字符串形式的日期
     */
    public static String convertFromDateToString(Date date) {
        return sdf.format(date);
    }

    /**
     * 获取系统当前时间年月日时分秒
     * @return
     */
    public static Date convertStringSFM(){
        // 通过Date对象获取
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return convertFromStringToDate(format.format(date));
    }

    /**
     * 获取系统当前时间年月日时分秒
     * @return
     */
    public static Date convertStringNYR() {
        String str = String.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        // 通过Date对象获取
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(str);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        return new Date();
    }
}

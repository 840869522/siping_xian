package com.htsn.wcs.common;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtil {
	public static Properties readPropertiesFile(String filePath, Logger logger) {
		Properties prop = null;
		try {
			prop = new Properties();
			FileInputStream fis = new FileInputStream(filePath);
			prop.load(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			String errorMsg = "application.properties文件不存在";
			if (logger != null)
				logger.error(errorMsg, e);
			System.out.println(errorMsg + "：" + e);
		} catch (IOException e) {
			String errorMsg = "读取application.properties文件发生错误";
			if (logger != null)
				logger.error(errorMsg, e);
			System.out.println(errorMsg + "：" + e);
		}
		return prop;
	}
}

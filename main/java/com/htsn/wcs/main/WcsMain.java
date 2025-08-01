package com.htsn.wcs.main;

import com.htsn.wcs.common.ConfigUtil;
import com.htsn.wcs.common.StringUtil;
import org.apache.log4j.Logger;

import java.util.Properties;

public class WcsMain {

	private static Logger logger = Logger.getLogger(WcsMain.class);

	public static void main(String[] args) {
		Properties prop = initConfig();// 读取配置文件信息
		if (prop == null)
			return;
		initDB(prop);// 初始化数据库连接

		new Thread(() -> {
			// 循环读取数据库表
			loopReadDB();
		}).start();
	}

	private static void loopReadDB() {
		try {
			while (true) {
				Thread.sleep(500);
				//读取数据库表，并作其他操作
			}
		} catch (Exception e) {
			logger.error("循环读取Task表发生错误", e);
		}
	}

	/**
	 * 读取配置文件信息
	 *
	 * @return
	 */
	private static Properties initConfig() {
		Properties prop = ConfigUtil.readPropertiesFile("src/main/resources/application.properties", logger);
		if (prop == null) {
			logger.info("application.properties文件缺失");
		}
		return prop;
	}

	/**
	 * 初始数据K连接
	 *
	 * @param prop 配置文件信息
	 */
	private static void initDB(Properties prop) {
		// 初始化数据K连接配置
		String driver = prop.getProperty("spring.datasource.driver-class-name");
		String url = prop.getProperty("spring.datasource.url");
		String user = prop.getProperty("spring.datasource.username");
		String password = prop.getProperty("spring.datasource.password");
		if (StringUtil.isEmpty(driver) || StringUtil.isEmpty(url) || StringUtil.isEmpty(user) || StringUtil.isEmpty(password)) {
			logger.info("application.properties文件中缺失数据库的相应配置信息");
			return;
		}

//		//读取程序版本号
//		String version = prop.getProperty("VERSION");
//		logger.info("***************************************************************************");
//		if (StringUtil.isNotEmpty(version)) {
//			logger.info("WCS程序的版本号：" + version);
//		}

//		//连接数据库
//		DBHelper.init(driver, url, user, password);
	}
}

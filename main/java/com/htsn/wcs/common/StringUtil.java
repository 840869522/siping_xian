package com.htsn.wcs.common;

import java.util.Date;

/**
 * String对象的一些常用方法
 *
 * @author Terry
 */
public class StringUtil {
	/**
	 * 将字符串的首字母转换为小写（其他字符不变）
	 *
	 * @param str
	 * @return
	 */
	public static String firstToLowerCase(String str) {
		char[] ch = str.toCharArray();
		if (ch[0] >= 'A' && ch[0] <= 'Z') {
			ch[0] = (char) (ch[0] + 32);
		}
		return new String(ch);
	}

	/**
	 * 字符串非空判断
	 *
	 * @param str
	 * @return
	 */
	public static boolean isNotEmpty(String str) {
		return str != null && str.length() > 0;
	}

	/**
	 * 字符串为空判断
	 *
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * 将字节转为十六进制字符串
	 *
	 * @param b       待转换的字节
	 * @param isUpper 是否需要转为大写
	 * @return
	 */
	public static String byteToHex(byte b, boolean isUpper) {
		String hex = Integer.toHexString(b & 0xFF);
		if (hex.length() < 2) {
			hex = "0" + hex;
		}
		return isUpper ? hex.toUpperCase() : hex;
	}

	/**
	 * 将字节数组转为十六进制字符串
	 *
	 * @param bytes        待转换的字节数组
	 * @param isUpper      是否需要转为大写
	 * @param isEmptySplit 是否需要空格将字节分开
	 * @return
	 */
	public static String bytesToHex(byte[] bytes, boolean isUpper, boolean isEmptySplit) {
		if (null == bytes)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() < 2) {
				sb.append(0);
			}
			sb.append(hex);
			if (isEmptySplit) {
				sb.append(" ");
			}
		}
		return isUpper ? sb.toString().toUpperCase() : sb.toString();
	}

	/**
	 * 将十六进制字符串转为字节
	 *
	 * @param inHex 待转换的Hex字符串
	 * @return
	 */
	public static byte hexToByte(String inHex) {
		return (byte) Integer.parseInt(inHex, 16);
	}

	/**
	 * 将16进制字符串转为十进制字符串
	 *
	 * @param inHex 待转换的Hex字符串
	 * @return
	 */
	public static String hexsToDecs(String inHex, boolean isUpper, boolean isEmptySplit) {
		StringBuffer sbInt = new StringBuffer();
		String[] inHexs = inHex.split(" ");
		for (int i = 0; i < inHexs.length; i++) {
			int sInt = Integer.parseInt(inHexs[i], 16);
			sbInt.append(sInt);
			if (isEmptySplit) {
				sbInt.append(" ");
			}
		}
		return isUpper ? sbInt.toString().toUpperCase() : sbInt.toString();
	}

	/**
	 * 将时间转换为8个字节长度（16字符）的十六进制字符串
	 *
	 * @param time    时间
	 * @param isUpper 是否需要转为大写
	 */
	public static synchronized String timeToHex8BytesStr(Date time, boolean isUpper) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long currTime = time.getTime();
		String timeHex = Long.toHexString(currTime);
		if (isUpper)
			timeHex = timeHex.toUpperCase();
		int lengthSpan = 16 - timeHex.length();
		for (int i = 0; i < lengthSpan; i++) {
			timeHex = "0" + timeHex;
		}
		return timeHex;
	}

	/**
	 * 将时间转换为8个字节长度的十六进制字节数组
	 *
	 * @param time    时间
	 * @param isUpper 是否需要转为大写
	 */
	public static byte[] timeToHex8Bytes(Date time, boolean isUpper, boolean isLowBitFront) {
		String timeHex = timeToHex8BytesStr(time, isUpper);
		byte[] byteArr = new byte[8];
		if (isLowBitFront) {
			byteArr[0] = hexToByte(timeHex.substring(14, 16));
			byteArr[1] = hexToByte(timeHex.substring(12, 14));
			byteArr[2] = hexToByte(timeHex.substring(10, 12));
			byteArr[3] = hexToByte(timeHex.substring(8, 10));
			byteArr[4] = hexToByte(timeHex.substring(6, 8));
			byteArr[5] = hexToByte(timeHex.substring(4, 6));
			byteArr[6] = hexToByte(timeHex.substring(2, 4));
			byteArr[7] = hexToByte(timeHex.substring(0, 2));
		} else {
			byteArr[0] = hexToByte(timeHex.substring(0, 2));
			byteArr[1] = hexToByte(timeHex.substring(2, 4));
			byteArr[2] = hexToByte(timeHex.substring(4, 6));
			byteArr[3] = hexToByte(timeHex.substring(6, 8));
			byteArr[4] = hexToByte(timeHex.substring(8, 10));
			byteArr[5] = hexToByte(timeHex.substring(10, 12));
			byteArr[6] = hexToByte(timeHex.substring(12, 14));
			byteArr[7] = hexToByte(timeHex.substring(14, 16));
		}
		return byteArr;
	}

	/**
	 * ASCII转字符串
	 *
	 * @param value
	 * @return
	 */
	public static String asciiToString(String value) {
		StringBuffer sbu = new StringBuffer();
		String[] chars = value.split(" ");
		for (int i = 0; i < chars.length; i++) {
			sbu.append((char) Integer.parseInt(chars[i]));
		}
		return sbu.toString();
	}
}

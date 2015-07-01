/**
 * Tools.java
 */
package com.samsung.scrc.wsg.k.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

/**
 * @author yuxie
 * 
 * @date Nov 28, 2014
 * 
 */
public class Tools {
//	public static Logger log = LogManager.getLogger(Tools.class.getName());
	private static String hexString = "0123456789ABCDEF";

	public static String encodeString2Hex(String str) {
		byte[] bytes = str.getBytes();
		// log.trace("Bytes length: " + bytes.length);
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			// log.trace(Integer.toBinaryString(bytes[i]));
			sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
			sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
		}
		return sb.toString();
	}

	public static String decodeHex2String(String hex) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(hex.length() / 2);
		for (int i = 0; i < hex.length(); i += 2) {
			baos.write(hexString.indexOf(hex.charAt(i)) << 4
					| hexString.indexOf(hex.charAt(i + 1)));
		}
		return new String(baos.toByteArray());
	}

	public static String md52String(String message) {
		String digest = null;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(message.getBytes("UTF-8"));
			// converting byte array to Hexadecimal String
			StringBuilder sb = new StringBuilder(2 * hash.length);
			for (byte b : hash) {
				sb.append(String.format("%02x", b & 0xff));
			}
			digest = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return digest;
	}

	public static String unicode2String(String str) {
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(str);
		char ch;
		while (matcher.find()) {
			ch = (char) Integer.parseInt(matcher.group(2), 16);
			str = str.replace(matcher.group(1), ch + "");
		}
		return str;
	}

	public static String f2wurl(String ori) {
		String rst = unicode2String(ori.replace("$", "\\u"));
		try {
			String urlRst = URLEncoder.encode(rst, "utf-8");
			return urlRst;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
}

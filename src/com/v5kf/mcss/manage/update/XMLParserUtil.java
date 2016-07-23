package com.v5kf.mcss.manage.update;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.text.TextUtils;

import com.v5kf.mcss.CustomApplication;

/**
 * XML文档解析工具类
 * 
 * @author Chyrain
 * 
 */
public class XMLParserUtil {

	/**
	 * 获取版本更新信息
	 * 
	 * @param is
	 *            读取连接服务version.xml文档的输入流
	 * @return
	 */
	public static VersionInfo getUpdateInfo(InputStream is) {
		VersionInfo info = new VersionInfo();
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_TAG:
					if ("version".equals(parser.getName())) {
						info.setVersion(parser.nextText());
					} else if ("updateTime".equals(parser.getName())) {
						info.setUpdateTime(parser.nextText());
					} else if ("downloadURL".equals(parser.getName())) {
						info.setDownloadURL(parser.nextText());
					} else if ("displayMessage".equals(parser.getName())) {
						info.setDisplayMessage(parseTxtFormat(parser.nextText(), "##"));
					} else if ("displayTitle".equals(parser.getName())) {
						info.setDisplayTitle(parser.nextText());
					} else if ("appName".equals(parser.getName())) {
						info.setAppName(parser.nextText());
					} else if ("versionCode".equals(parser.getName())) {
						info.setVersionCode(Integer.parseInt(parser.nextText()));
					} else if ("level".equals(parser.getName())) {
						info.setLevel(Integer.parseInt(parser.nextText()));
					} else {
						String channelName = CustomApplication.getInstance().getChannelName();
						if (!TextUtils.isEmpty(channelName) && channelName.equals(parser.getName())) {
							info.setChannelURL(parser.nextText());
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return info;
	}
	
	
	/**
	 * 获取版本更新信息
	 * 
	 * @param is
	 *            读取连接服务version.xml文档的输入流
	 * @return
	 */
	public static VersionInfo getUpdateInfo(String str) {
		VersionInfo info = new VersionInfo();
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			ByteArrayInputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"));
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_TAG:
					if ("version".equals(parser.getName())) {
						info.setVersion(parser.nextText());
					} else if ("updateTime".equals(parser.getName())) {
						info.setUpdateTime(parser.nextText());
					} else if ("downloadURL".equals(parser.getName())) {
						info.setDownloadURL(parser.nextText());
					} else if ("displayMessage".equals(parser.getName())) {
						info.setDisplayMessage(parseTxtFormat(parser.nextText(), "##"));
					} else if ("displayTitle".equals(parser.getName())) {
						info.setDisplayTitle(parseTxtFormat(parser.nextText(), "##"));
					} else if ("appName".equals(parser.getName())) {
						info.setAppName(parser.nextText());
					} else if ("versionCode".equals(parser.getName())) {
						info.setVersionCode(Integer.parseInt(parser.nextText()));
					} else if ("level".equals(parser.getName())) {
						info.setLevel(Integer.parseInt(parser.nextText()));
					} else {
						String channelName = CustomApplication.getInstance().getChannelName();
						if (!TextUtils.isEmpty(channelName) && channelName.equals(parser.getName())) {
							info.setChannelURL(parser.nextText());
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return info;
	}

	/**
	 * 根据指定字符格式化字符串（换行）
	 * 
	 * @param data
	 *            需要格式化的字符串
	 * @param formatChar
	 *            指定格式化字符
	 * @return
	 */
	public static String parseTxtFormat(String data, String formatChar) {
		StringBuffer backData = new StringBuffer();
		String[] txts = data.split(formatChar);
		for (int i = 0; i < txts.length; i++) {
			backData.append(txts[i]);
			backData.append("\n");
		}
		return backData.toString();
	}

}

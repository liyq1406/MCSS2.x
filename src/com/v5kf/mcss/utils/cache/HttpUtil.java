package com.v5kf.mcss.utils.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.V5ClientConfig;
import com.v5kf.client.lib.V5HttpUtil;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.utils.FileUtil;

/**
 * Http 请求工具类
 * 
 * @author Scorpio.Liu
 * 
 */
public class HttpUtil {

	protected static final String TAG = "HttpUtil";

	/**
	 * 获取响应字符串
	 * 
	 * @param path
	 *            路径
	 * @param parameters
	 *            参数
	 * @return 响应字符串
	 */
	public static String getResponseStr(String path, Map<String, String> parameters) {
		StringBuffer buffer = new StringBuffer();
		URL url;
		try {
			if (parameters != null && !parameters.isEmpty()) {
				for (Map.Entry<String, String> entry : parameters.entrySet()) {
					// 完成转码操作
					buffer.append(entry.getKey()).append("=")
							.append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
				}
				buffer.deleteCharAt(buffer.length() - 1);
			}
			String urlStr = V5HttpUtil.urlStringCheck(path);
			url = new URL(urlStr);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(3000);
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);// 表示从服务器获取数据
			urlConnection.setDoOutput(true);// 表示向服务器写数据
			// 获得上传信息的字节大小以及长度
			byte[] mydata = buffer.toString().getBytes();
			// 表示设置请求体的类型是文本类型
			urlConnection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			urlConnection.setRequestProperty("Content-Length", String.valueOf(mydata.length));
			// 获得输出流,向服务器输出数据
			OutputStream outputStream = urlConnection.getOutputStream();
			outputStream.write(mydata, 0, mydata.length);
			outputStream.close();
			int responseCode = urlConnection.getResponseCode();
			if (responseCode == 200) {
				return changeInputStream(urlConnection.getInputStream());
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String changeInputStream(InputStream inputStream) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] data = new byte[1024];
		int len = 0;
		String result = "";
		if (inputStream != null) {
			try {
				while ((len = inputStream.read(data)) != -1) {
					outputStream.write(data, 0, len);
				}
				result = new String(outputStream.toByteArray(), "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static InputStream getInputStream(String path) {
		String urlStr = V5HttpUtil.urlStringCheck(path);
		Logger.i(TAG, "urlStr:" + urlStr);
		URL url;
		HttpURLConnection urlConnection;
		try {
			url = new URL(urlStr);
			
            if (url.getProtocol().toLowerCase().equals("https")) {
            	// [修改]trust all hosts，解决https访问失败问题
                trustAllHosts();
                
                HttpsURLConnection https = (HttpsURLConnection)url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                urlConnection = https;
            } else {
            	urlConnection = (HttpURLConnection)url.openConnection();
            }
            if (Config.AUTH != null && !Config.AUTH.isEmpty()) {
            	urlConnection.addRequestProperty("Authorization", Config.AUTH);
            }
			urlConnection.setConnectTimeout(V5ClientConfig.SOCKET_TIMEOUT);
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoInput(true);// 表示从服务器获取数据
			urlConnection.connect();
			if (urlConnection.getResponseCode() == 200)
				return urlConnection.getInputStream();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Logger.w(TAG, "[getInputStream] return null");
		return null;
	}

	public static byte[] readStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);

		}
		outSteam.close();
		inStream.close();
		return outSteam.toByteArray();
	}

	/**
	 * 网络图片保存到File
	 * @param url
	 * @param f
	 */
	public static void CopyStream(String url, File f) {
		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;
		try {
			inputStream = getInputStream(url);
			byte[] data = new byte[1024];
			int len = 0;
			fileOutputStream = new FileOutputStream(f);
			while ((len = inputStream.read(data)) != -1) {
				fileOutputStream.write(data, 0, len);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			FileUtil.deleteFile(f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			FileUtil.deleteFile(f.getAbsolutePath());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	/**
	 * Trust every server - dont check for any certificate
	 */
	private static void trustAllHosts() {
	    // Create a trust manager that does not validate certificate chains
	    TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	 
	        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	            return new java.security.cert.X509Certificate[] {};
	        }
	 
	        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            Logger.d(TAG, "ssl trust checkClientTrusted");
	        }
	 
	        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	        	Logger.d(TAG, "ssl trust checkServerTrusted");
	        }
	    } };
	 
	    // Install the all-trusting trust manager
	    try {
	        SSLContext sc = SSLContext.getInstance("TLS");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		 
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}


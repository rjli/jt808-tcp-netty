package cn.hylexus.jt808.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import cn.hylexus.jt808.common.GpsServerConsts;

public class ReadConfUtil {

	/**
	 *  系统服务端的配置
	 * @return
	 */
	public static String getServerConf() {
		String ipaddress =null;
		try {
			Properties pro = new Properties();
			InputStream in = GpsServerConsts.class.getResourceAsStream("/baseconf.properties");
			pro.load(in);
			String ip = pro.getProperty("gps.server.ip");
			String port = pro.getProperty("gps.server.port");
			String name = pro.getProperty("gps.server.name");
			ipaddress = ip+"/"+port+"/"+name;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ipaddress;
	}
	
	/**
	 * 读取gps通讯服务的端口的配置
	 * @return
	 */
	public static int getGpsPort() {
		int port = 0;
		try {
			Properties pro = new Properties();
			InputStream in = GpsServerConsts.class.getResourceAsStream("/baseconf.properties");
			pro.load(in);
			port = Integer.parseInt(pro.getProperty("gpsport"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return port;
	}
	public static void main(String[] args) {
		System.out.println(getServerConf());
		System.out.println(getGpsPort());
	}
}

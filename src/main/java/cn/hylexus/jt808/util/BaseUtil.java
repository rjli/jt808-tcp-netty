package cn.hylexus.jt808.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rksp.util.GsonUtil;


/**
 * 系统共用的util类
 * @author cheryl
 *
 */
public class BaseUtil {
	private final static Logger log = LoggerFactory.getLogger(BaseUtil.class);
	/**
	 * 生产终端设备的鉴权码
	 * [终端手机号，当前日期的毫秒值]
	 * @param terminalPhone
	 * @return
	 */
	public static String  generateAuthCode(String terminalPhone) {
		return  terminalPhone+ new Date().getTime();
		
	}
	//纠偏经纬度
	
	/**
	 *  经度，纬度坐标转换
	 * @param coords 坐标拼接的字符串
	 * @param from  现在对应的坐标系
	 * @param to  要转换的对应的坐标系
	 * api:http://lbsyun.baidu.com/index.php?title=webapi/guide/changeposition
	 * @return
	 */
	public static String getRealJWD(String coords,String from, String to){
		String message ="";
		String encoding="UTF-8";
		String path = "http://api.map.baidu.com/geoconv/v1/?from="+from+"&to="+to+"&ak=qePPr6rygb0n4BTYzL8C9hMC";
        try{
        	String content = "coords="+URLEncoder.encode(coords,encoding);////在参数传递时 ， 涉及需要转义的字符和汉字时需要进行编码
        	String pmessage = urlConnReq(path ,encoding, content,"POST");
        	log.info("post方式:"+pmessage);
        	JsonObject obj = GsonUtil.getJsonObject(pmessage);
        	String status = obj.get("status").getAsString();
        	if(status.equals("0")) {
        		JsonArray result = obj.get("result").getAsJsonArray();
            	StringBuilder str = new StringBuilder();
            	for(int i=0; i<result.size();i++) {
            		
            		JsonObject ponit = result.get(i).getAsJsonObject();
            		str.append(String.valueOf(ponit.get("x").getAsBigDecimal().setScale(6,BigDecimal.ROUND_DOWN))+",");
            		str.append(String.valueOf(ponit.get("y").getAsBigDecimal().setScale(6,BigDecimal.ROUND_DOWN))+";");
            	}
            	if(str !=null) {
            		message = str.toString().substring(0,str.length()-1);
            		
            	}
        	}else {
        		message = "error";
        	}
        	log.info("返回的message信息:"+message);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return message;
	}

	
//	@Test
//	public void test() {
//		String coords = "112.558088,37.782940";
//		String from ="1";
//		String to ="5";
//		String result = getRealJWD(coords,from,to);
//	}
	

    /**
     * URLConnection 方式访问webservice接口
     * @param path
     * @param encoding
     * @param content
     * @param message
     * @param method
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ProtocolException
     * @throws UnsupportedEncodingException
     */
	private static String urlConnReq(String path , String encoding, String content ,String method)
			throws MalformedURLException, IOException, ProtocolException,
			UnsupportedEncodingException {
		String message;
		if(method.equalsIgnoreCase("GET"))
		{
			path = path+"?"+content;
		}
		URL url = new URL(path);
		HttpURLConnection conn= (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestProperty("Content-type","application/x-www-form-urlencoded");
		if(method.equalsIgnoreCase("POST"))
		{
			conn.setRequestMethod("POST");
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.writeBytes(content.toString());
			out.flush();
			out.close();
		}
		BufferedReader in = null;
		in = new BufferedReader(new InputStreamReader(conn.getInputStream(),encoding));
		StringBuffer sb = new StringBuffer();
		String inputLine;
		while((inputLine = in.readLine()) != null){
		    sb.append(inputLine);
		}
		message = sb.toString();
		return message;
	}   
	
}

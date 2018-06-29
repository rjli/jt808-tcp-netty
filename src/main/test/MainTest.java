import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.axis2.AxisFault;
import org.junit.Test;

import com.rksp.util.DateUtil;
import com.sharetime.gps.pojo.Gps指令集;

import cn.hylexus.jt808.common.GpsServerConsts;
import cn.hylexus.jt808.common.TPMSConsts;
import cn.hylexus.jt808.util.HexStringUtils;
import rk.stub.gps.GpsBaseSvc;
import rk.stub.gps.GpsBaseSvcStub;

public class MainTest {
	private String ENCODING = "utf-8";

	// @Test
	// public void test() throws Exception {
	// GpsBaseSvc gpsBaseSvc = new GpsBaseSvcStub(GpsServerConsts.gps_server_base);
	// Gps指令集 directive = new Gps指令集();
	// directive.setSim卡号("19855241");
	// directive.set发送时间(DateUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss"));
	// directive.set消息id(TPMSConsts.cmd_terminal_param_settings);
	// directive.set消息流水号(1);
	// directive.set状态("成功");
	// gpsBaseSvc.createTheDirective(directive);
	// }

	@Test
	public void test1() {
		String message = "7E000200000145307999700102E57E";
		byte[] bytes = HexStringUtils.hexString2Bytes(message).getBytes();
		System.out.println(HexStringUtils.toHexString(bytes));
	}
	
	@Test
	public void test() {
		String message = "7E000200000145307999700102E57E";
//		String message = "7E0200003A014530799970010B000000000000000302405FB006B5CF08000001F400AC1711231654390104000000EDEB16000C00B2898602B716177019129900060089FFFFFFFF767E";
		byte[] bytes = HexStringUtils.hexStringToByte(message);
		System.out.println(HexStringUtils.toHexString(bytes));
		try {
//			Socket socket = new Socket("218.26.8.116", 76);
			Socket socket = new Socket("127.0.0.1", 8080);
			// 创建IO
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());
			// 向服务器端发送一条消息
			for (int i = 0; i < 1; i++) {
				out.write(bytes, 0, bytes.length); // 向输出流写入 bytes
				out.flush();
			}
			System.out.println(socket.getLocalSocketAddress() + "发送" + Arrays.toString(bytes) + "完毕！");
			// 获取服务端的返回信息
			byte[] b =new byte[1024];//定义字节为2
			byte[] c;
			int i=0;
			while(true) {
		        b[i] = in.readByte(); //两个字节，遍历两次，写入b
				if(b[i] == 126 && i>0) {
					c = new byte[i+1];
					System.arraycopy(b, 0, c, 0, i+1);
				    break;    
				}
			    i++;
			}
			System.out.println(socket.getLocalSocketAddress()+"接受到的数据："+HexStringUtils.toHexString(c));
			out.close();
			in.close();
			socket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void testTimer() throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 9);
		calendar.set(Calendar.MINUTE, 05);
		calendar.set(Calendar.SECOND, 00);
		Date time = calendar.getTime();

		// 定时器实例
		Timer t = null;
		for (int i = 0; i < 30; i++) {
			t = new Timer();
			t.schedule(new MyTask(new Socket("218.26.12.164", 8189),i), time, 120 * 1000); // timeInterval 是一天的毫秒数，也是执行间隔
//			t.schedule(new MyTask(new Socket("127.0.0.1", 189),i), time, 120 * 1000); // timeInterval 是一天的毫秒数，也是执行间隔

		}

	};

	static class MyTask extends java.util.TimerTask {
		private Socket socket;
		private DataOutputStream out;
		private DataInputStream in;
		private String message = "";
		private int count;

		public MyTask(Socket socket,int i) throws Exception, IOException {
			this.socket = socket;
			// 创建IO
			this.out = new DataOutputStream(socket.getOutputStream());
			this.in = new DataInputStream(socket.getInputStream());
			this.count = i;
		}

		public void run() {
//			String message = "7E000200000145307999700102E57E";
			String message = "7E0200003A014530799970010B000000000000000302405FB006B5CF08000001F400AC1711231654390104000000EDEB16000C00B2898602B716177019129900060089FFFFFFFF767E";
//			String message = "7E0200003A01453079997000FA000000000000000102407B9C06B56D98000000000000170929141121010400000000EB16000C00B2898602B716177019129900060089FFFFFFFF477E";
			byte[] bytes = HexStringUtils.hexStringToByte(message);
			System.out.println(socket.getLocalSocketAddress());
			// 创建IO
			try {
				// 向服务器端发送一条消息
				for (int i = 0; i < 3; i++) {
					out.write(bytes, 0, bytes.length); // 向输出流写入 bytes
					out.flush();
				}
				System.out.println("定时器主要执行的代码:"+"count:"+this.count+"soclet:"+socket.getLocalSocketAddress() + "发送" + Arrays.toString(bytes) + "完毕！"+new Date().toLocaleString());
				// 获取服务端的返回信息
				byte[] b =new byte[1024];//定义字节为2
				byte[] c;
				int i=0;
				while(true) {
			        b[i] = in.readByte(); //两个字节，遍历两次，写入b
					if(b[i] == 126 && i>0) {
						c = new byte[i+1];
						System.arraycopy(b, 0, c, 0, i+1);
					    break;    
					}
				    i++;
				}
				System.out.println(socket.getLocalSocketAddress()+"count:"+this.count+"接受到的数据："+HexStringUtils.toHexString(c));
//				out.close();
//				in.close();
//				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args) {
		MainTest mainTest = new MainTest();
		try {
			mainTest.testTimer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}

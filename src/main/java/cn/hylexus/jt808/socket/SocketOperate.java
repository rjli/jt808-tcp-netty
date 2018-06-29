package cn.hylexus.jt808.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rksp.util.GsonUtil;
import com.rksp.util.StringUtil;
import com.sharetime.gps.pojo.Gps区域;
import com.sharetime.gps.pojo.Gps指令集;
import com.sharetime.gps.vo.TerminalParamSettingVo;

import cn.hylexus.jt808.common.TPMSConsts;
import cn.hylexus.jt808.service.TerminalMsgSendService;
import cn.hylexus.jt808.vo.req.PolygonSettingArea;
import cn.hylexus.jt808.vo.req.TerminalParametersSettingInfo;

/**
 * 处理客户端发送报文的类
 * 用来保存终端传送过来的报文
 * @author cheryl
 *
 */
public class SocketOperate extends Thread {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Socket socket;
    private final String encoding = "utf-8";
    private TerminalMsgSendService msgSendService;
    private PojoToMessage pojoToMessage;
	public SocketOperate(Socket socket) {
		this.socket = socket;
		this.msgSendService = new TerminalMsgSendService();
		this.pojoToMessage = new PojoToMessage();
	}

	@SuppressWarnings("unused")
	public void run() {
		logger.info("监听客户端发送的消息");
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),encoding));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),encoding));
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			// 读取客户端发送来的消息
			String strReq = in.readLine();
			logger.info("Client：" + strReq);
			String strResp ="";
			MessageVo messageVo  = (MessageVo) GsonUtil.getObject(strReq, MessageVo.class);
			boolean flag = false;
			System.out.println(messageVo.getMessageId());
			if( TPMSConsts.cmd_terminal_param_settings == messageVo.getMessageId()) {
				logger.info("发送参数："+messageVo.getParamStr());
				TerminalParamSettingVo pojo =(TerminalParamSettingVo) GsonUtil.getObject(messageVo.getParamStr(), TerminalParamSettingVo.class); 
				TerminalParametersSettingInfo msg = pojoToMessage.changeToerminalParametersSettigInfo(pojo);
				Gps指令集  directive = new Gps指令集();
				directive.setSim卡号(messageVo.getPhone());
				directive.set关联id(pojo.getPojoId());
				directive.set消息内容(messageVo.getParamStr());
				directive.set消息id(TPMSConsts.cmd_terminal_param_settings);
				flag = this.msgSendService.sendTerminalParamSettingMsg(messageVo.getPhone(),msg,directive);
				if(flag) {
					strResp = "消息发送成功";
				}else {
					strResp = "消息发送失败";
				}
			}
//			else if( TPMSConsts.cmd_polygon_area_settings == messageVo.getMessageId())  {
//				System.out.println("发送参数："+messageVo.getParamStr());
//				Gps区域 pojo =(Gps区域) GsonUtil.getObject(messageVo.getParamStr(), Gps区域.class); 
//				PolygonSettingArea msg = pojoToMessage.changeToPolygonAreaSettigInfo(pojo);
//			}
			else if(TPMSConsts.cmd_terminal_param_query == messageVo.getMessageId()){
				System.out.println("发送参数："+messageVo.getParamStr());
//				Gps指令集  directive = new Gps指令集();
//				directive.setSim卡号(messageVo.getPhone());
//				directive.set消息内容(messageVo.getParamStr());
//				directive.set消息id(TPMSConsts.cmd_terminal_param_query);
//				flag = this.msgSendService.sendTerminalParamQueryMsg(messageVo.getPhone(),directive);
				flag = this.msgSendService.sendTerminalParamQueryMsg(messageVo.getPhone());
				if(flag) {
					strResp = "消息发送成功";
				}else {
					strResp = "消息发送失败";
				}
			}
			else if(TPMSConsts.cmd_position_query == messageVo.getMessageId()){
				System.out.println("发送参数："+messageVo.getParamStr());
//				Gps指令集  directive = new Gps指令集();
//				directive.setSim卡号(messageVo.getPhone());
//				directive.set消息内容(messageVo.getParamStr());
//				directive.set消息id(TPMSConsts.cmd_position_query);
				flag = this.msgSendService.sendLocationQuery(messageVo.getPhone());
				if(flag) {
					strResp = "消息发送成功";
				}else {
					strResp = "消息发送失败";
				}
			}
			logger.info("Server：" + strResp);
			bw.write(strResp);
			bw.flush();
			bw.close();
			socket.close();
			logger.info("socket stop.....");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {

		}
	}
}

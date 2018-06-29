package cn.hylexus.jt808.service;


import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.hylexus.jt808.server.SessionManager;
import cn.hylexus.jt808.service.codec.MsgEncoder;
import cn.hylexus.jt808.util.BaseUtil;
import cn.hylexus.jt808.util.HexStringUtils;
import cn.hylexus.jt808.vo.PackageData;
import cn.hylexus.jt808.vo.PackageData.MsgHeader;
import cn.hylexus.jt808.vo.Session;
import cn.hylexus.jt808.vo.req.LocationInformationQueryResp;
import cn.hylexus.jt808.vo.req.TerminalAuthenticationMsg;
import cn.hylexus.jt808.vo.req.TerminalLoctionInfoReportMsg;
import cn.hylexus.jt808.vo.req.TerminalParamQueryMsgResp;
import cn.hylexus.jt808.vo.req.TerminalRegisterMsg;
import cn.hylexus.jt808.vo.resp.ServerCommonRespMsgBody;
import cn.hylexus.jt808.vo.resp.TerminalCommonRespMsg;
import cn.hylexus.jt808.vo.resp.TerminalRegisterMsgRespBody;
import cn.hylexus.jt808.vo.resp.TerminalCommonRespMsg.TerminalCommonRespInfo;
/**
 * 终端消息接受的处理服务
 * @author cheryl
 *
 */
public class TerminalMsgProcessService extends BaseMsgProcessService {

	private final Logger log = LoggerFactory.getLogger(getClass());
//	private final Logger weblog = LoggerFactory.getLogger("weblog");
	private MsgEncoder msgEncoder;
	private SessionManager sessionManager;
	private MsgToDPlatformGpsPojoService gpsPojoService = new MsgToDPlatformGpsPojoService();

	public TerminalMsgProcessService() { 
		this.msgEncoder = new MsgEncoder();
		this.sessionManager = SessionManager.getInstance();
	}
    /**
     * 处理终端注册信息
     * @param msg
     * @throws Exception
     */
	public void processRegisterMsg(TerminalRegisterMsg msg) throws Exception {
		log.info("终端注册:{}", JSON.toJSONString(msg, true));
		final String sessionId = Session.buildId(msg.getChannel());
		Session session = sessionManager.findBySessionId(sessionId);
		String terminalPhone = msg.getMsgHeader().getTerminalPhone();
		if (session == null) {
			session = Session.buildSession(msg.getChannel(), terminalPhone);
		}
		session.setAuthenticated(true);
		session.setTerminalPhone(terminalPhone);
		
		sessionManager.put(session.getId(), session);
		/**
		 * 把终端同车辆进行绑定操作
		 * 车辆注册成功或者之前绑定过则返回鉴权码，否则返回replyCode转成String之后的
		 */
		log.info("终端注册消息：",msg.toString());
		String replyCodeStr = gpsPojoService.createTerminalDevice(msg);
		/**
		 * 终端注册应答消息的拼接
		 */
		TerminalRegisterMsgRespBody respMsgBody = new TerminalRegisterMsgRespBody();
		byte[] replyCodeBytes = replyCodeStr.getBytes();
//		System.out.println("replyCodeStr:"+replyCodeStr+"replyCodeBytes:"+Arrays.toString(replyCodeBytes));
		if(replyCodeBytes.length >1) {//只有注册成功之后才会有鉴权码
			respMsgBody.setReplyCode(TerminalRegisterMsgRespBody.success);
			respMsgBody.setReplyToken(replyCodeStr); 
		}else {
			respMsgBody.setReplyCode(replyCodeBytes[0]);
		}
		respMsgBody.setReplyFlowId(msg.getMsgHeader().getFlowId());
		int flowId = super.getFlowId(msg.getChannel());
		log.info("终端注册应答-消息:{}", respMsgBody.toString());
		byte[] bs = this.msgEncoder.encode4TerminalRegisterResp(msg, respMsgBody, flowId);
		//add  by cheryl
		log.info("终端注册应答-消息字节数组:{}",HexStringUtils.toHexString(bs));
		super.send2Client(msg.getChannel(), bs);
	}
	
	/**
	 * 处理终端位置汇报信息
	 * @param msg
	 * @throws Exception
	 */
	public void processTerminalLocationInfoReportMsg(TerminalLoctionInfoReportMsg msg)  throws Exception  {
		log.info("终端位置信息:{}", JSON.toJSONString(msg, true));
		final String sessionId = Session.buildId(msg.getChannel());
		Session session = sessionManager.findBySessionId(sessionId);
		String terminalPhone = msg.getMsgHeader().getTerminalPhone();
		if (session == null) {
			session = Session.buildSession(msg.getChannel(), terminalPhone);
		}
		session.setAuthenticated(true);
		session.setTerminalPhone(terminalPhone);
		
		sessionManager.put(session.getId(), session);
		/**
		 * 把终端位置信息添加到数据库
		 */
		byte replyCode = gpsPojoService.creatLocus(msg);
		
		final MsgHeader reqHeader = msg.getMsgHeader();
		ServerCommonRespMsgBody respMsgBody = new ServerCommonRespMsgBody(reqHeader.getFlowId(), reqHeader.getMsgId(),replyCode);
		int flowId = super.getFlowId(msg.getChannel());
		byte[] bs = this.msgEncoder.encode4ServerCommonRespMsg(msg, respMsgBody, flowId);
		super.send2Client(msg.getChannel(), bs);
	}
	
    /**
     * 处理终端鉴权信息
     * @param msg
     * @throws Exception
     */
	public void processAuthMsg(TerminalAuthenticationMsg msg) throws Exception {

		log.info("终端鉴权:{}", JSON.toJSONString(msg, true));

		final String sessionId = Session.buildId(msg.getChannel());
		Session session = sessionManager.findBySessionId(sessionId);
		String terminalPhone = msg.getMsgHeader().getTerminalPhone();
		if (session == null) {
			session = Session.buildSession(msg.getChannel(), terminalPhone);
		}
		session.setAuthenticated(true);
		session.setTerminalPhone(terminalPhone);
		sessionManager.put(session.getId(), session);
		/**
		 * 判断终端的鉴权码是否一致
		 */
		boolean flag = gpsPojoService.getTheReplyCodeByTerminalPhone(terminalPhone,msg.getAuthCode());
		
		ServerCommonRespMsgBody respMsgBody = new ServerCommonRespMsgBody();
		if(flag) {
			respMsgBody.setReplyCode(ServerCommonRespMsgBody.success);
		}else {
			respMsgBody.setReplyCode(ServerCommonRespMsgBody.failure);
		}
		respMsgBody.setReplyFlowId(msg.getMsgHeader().getFlowId());
		respMsgBody.setReplyId(msg.getMsgHeader().getMsgId());
		int flowId = super.getFlowId(msg.getChannel());
		byte[] bs = this.msgEncoder.encode4ServerCommonRespMsg(msg, respMsgBody, flowId);
		log.info("平台通用应答回复的消息={}",HexStringUtils.toHexString(bs));
		super.send2Client(msg.getChannel(), bs);
	}
    /**
     * 处理终端心跳信息
     * @param req
     * @throws Exception
     */
	public void processTerminalHeartBeatMsg(PackageData req) throws Exception {
		log.info("心跳信息:{}", JSON.toJSONString(req, true));
		final MsgHeader reqHeader = req.getMsgHeader();
		String terminalPhone = reqHeader.getTerminalPhone();
		/**
		 * 更新终端设备的最后心跳时间
		 */
		boolean flag = gpsPojoService.changeTerminalLastHeatBeart(terminalPhone);
		byte replyCode = ServerCommonRespMsgBody.failure;
		if(flag) {
			 replyCode = ServerCommonRespMsgBody.success;
		}
		ServerCommonRespMsgBody respMsgBody = new ServerCommonRespMsgBody(reqHeader.getFlowId(), reqHeader.getMsgId(),replyCode);
		int flowId = super.getFlowId(req.getChannel());
		byte[] bs = this.msgEncoder.encode4ServerCommonRespMsg(req, respMsgBody, flowId);
		super.send2Client(req.getChannel(), bs);
	}
    
	/**
	 * 处理终端注销信息
	 * @param req
	 * @throws Exception
	 */
	public void processTerminalLogoutMsg(PackageData req) throws Exception {
		log.info("终端注销:{}", JSON.toJSONString(req, true));
		final MsgHeader reqHeader = req.getMsgHeader();
		/**
		 * 把终端同车辆进行解绑操作
		 * 清空车辆中终端id以及序列号，清空终端的鉴权码信息
		 */
		boolean flag = gpsPojoService.clearReplationBetweenVehicleAndTerminal(reqHeader.getTerminalPhone());
		byte replyCode = ServerCommonRespMsgBody.failure;
		if(flag) {
			replyCode = ServerCommonRespMsgBody.success;
		}
		ServerCommonRespMsgBody respMsgBody = new ServerCommonRespMsgBody(reqHeader.getFlowId(), reqHeader.getMsgId(),replyCode);
		int flowId = super.getFlowId(req.getChannel());
		byte[] bs = this.msgEncoder.encode4ServerCommonRespMsg(req, respMsgBody, flowId);
		super.send2Client(req.getChannel(), bs);
	}
	
	/**
     * 处理位置信息查询应答
     * @param msg
     * @throws Exception
     */
	public void processLocationQuery(LocationInformationQueryResp msg) throws Exception {
		log.info("位置信息查询应答:{}", JSON.toJSONString(msg, true));
		final String sessionId = Session.buildId(msg.getChannel());
		Session session = sessionManager.findBySessionId(sessionId);
		String terminalPhone = msg.getMsgHeader().getTerminalPhone();
		if (session == null) {
			session = Session.buildSession(msg.getChannel(), terminalPhone);
		}
		session.setAuthenticated(true);
		session.setTerminalPhone(terminalPhone);
		
		sessionManager.put(session.getId(), session);
		/**
		 * 创建位置汇报消息
		 */
		byte replyCode = gpsPojoService.creatLocus(msg);
		
		final MsgHeader reqHeader = msg.getMsgHeader();
		ServerCommonRespMsgBody respMsgBody = new ServerCommonRespMsgBody(reqHeader.getFlowId(), reqHeader.getMsgId(),replyCode);
		int flowId = super.getFlowId(msg.getChannel());
		byte[] bs = this.msgEncoder.encode4ServerCommonRespMsg(msg, respMsgBody, flowId);
		super.send2Client(msg.getChannel(), bs);
	}
	
	
    /**
     * 处理查询终端参数应答消息
     * @param msg
     * @throws Exception
     */
	public void processParamQueryMsgResp(TerminalParamQueryMsgResp msg)  throws Exception  {
		log.info("查询终端参数应答:{}", JSON.toJSONString(msg, true));
		final MsgHeader reqHeader = msg.getMsgHeader();
		/**
		 * 更新终端中相关的参数信息
		 */
		boolean flag = gpsPojoService.updateTerminalInfo(msg);
		ServerCommonRespMsgBody respMsgBody = new ServerCommonRespMsgBody(reqHeader.getFlowId(), reqHeader.getMsgId(),
				ServerCommonRespMsgBody.success);
		int flowId = super.getFlowId(msg.getChannel());
		byte[] bs = this.msgEncoder.encode4ServerCommonRespMsg(msg, respMsgBody, flowId);
		super.send2Client(msg.getChannel(), bs);
	}
	
	/**
     * 处理终端通用应答
     * @param msg
     * @throws Exception
     */
	public void processTerminalCommonMsg(TerminalCommonRespMsg req) throws Exception {
		log.info("终端通用应答:{}", JSON.toJSONString(req, true));
		final MsgHeader reqHeader = req.getMsgHeader();
	    TerminalCommonRespInfo info = req.getTerminalCommonRespInfo();
		//更新对应的实体类的内容
		boolean replyCode = gpsPojoService.updateGpsDirective(reqHeader.getTerminalPhone(),info);
		byte result = ServerCommonRespMsgBody.failure ;
		if(replyCode) {
			log.info("指令更新成功！");
			result = ServerCommonRespMsgBody.success;
		}else {
			log.info("指令更新失败！");
		}
//		ServerCommonRespMsgBody respMsgBody = new ServerCommonRespMsgBody(reqHeader.getFlowId(), reqHeader.getMsgId(),result);
//		int flowId = super.getFlowId(req.getChannel());
//		byte[] bs = this.msgEncoder.encode4TerminalCommonRespMsg(req, respMsgBody, flowId);
//		super.send2Client(req.getChannel(), bs);
	}

}

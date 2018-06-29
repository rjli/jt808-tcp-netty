package cn.hylexus.jt808.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.rksp.util.DateUtil;
import com.sharetime.gps.pojo.Gps指令集;
import com.sharetime.gps.vo.GpsBaseCdt;

import cn.hylexus.jt808.common.GpsServerConsts;
import cn.hylexus.jt808.common.TPMSConsts;
import cn.hylexus.jt808.server.SessionManager;
import cn.hylexus.jt808.service.codec.MsgEncoder;
import cn.hylexus.jt808.util.HexStringUtils;
import cn.hylexus.jt808.vo.Session;
import cn.hylexus.jt808.vo.req.PolygonDelArea;
import cn.hylexus.jt808.vo.req.PolygonSettingArea;
import cn.hylexus.jt808.vo.req.TerminalAuthenticationMsg;
import cn.hylexus.jt808.vo.req.TerminalParametersSettingInfo;
import cn.hylexus.jt808.vo.resp.ServerCommonRespMsgBody;
import cn.hylexus.jt808.vo.resp.TerminalRegisterMsgRespBody;
import io.netty.channel.Channel;
import rk.stub.gps.GpsBaseSvc;
import rk.stub.gps.GpsBaseSvcStub;

/**
 * 终端消息发送处理服务
 * @author cheryl
 *
 */
public class TerminalMsgSendService extends BaseMsgProcessService{

	private final Logger log = LoggerFactory.getLogger(getClass());
	private MsgEncoder msgEncoder;
	private SessionManager sessionManager;
	
	public TerminalMsgSendService() {
		this.msgEncoder = new MsgEncoder();
		this.sessionManager = SessionManager.getInstance();
	}
	
	//发送终端参数查询消息
	public boolean sendTerminalParamQueryMsg(String phone) throws Exception {
		log.info("终端参数查询:");
		Session session = sessionManager.findByTerminalPhone(phone);
		boolean sendFlag = false;
		if (session != null) {
			Channel channel = session.getChannel();
			if(channel != null) {
//				GpsBaseSvc gpsBaseSvc = new GpsBaseSvcStub(GpsServerConsts.gps_server_base);
				int flowId = super.getFlowId(channel);
//				directive.set发送时间(DateUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss"));
//				directive.set消息流水号(flowId);
				byte[] bs = this.msgEncoder.encode4ParamQuery(phone, flowId);
				log.info("终端参数查询-消息字节数组:{}",HexStringUtils.toHexString(bs));
				boolean flag = super.send2Client(channel, bs);
				if(flag) {
					log.info("消息发送成功！");
//					directive.set状态("成功");
					sendFlag = true;
					if(flag) {
						log.info("消息发送成功！");
						sendFlag = true;
					}else {
						log.info("消息发送失败！");
					}
				}else {
					log.info("消息发送失败！");
//					directive.set状态("失败");
				}
//				gpsBaseSvc.createTheDirective(directive);
			}else {
				log.info("终端的通道失去链接");
			}
			
		}else {
			log.info("终端链接已经断开");
		}
		return sendFlag;
	}
	
    /**
     * 终端参数设置报文
     * @param params
     * @throws Exception 
     */
	public boolean sendTerminalParamSettingMsg(String phone,TerminalParametersSettingInfo msg,Gps指令集 directive) throws Exception {
		log.info("开始发送终端参数设置报文:"+msg.toString());
		Session session = sessionManager.findByTerminalPhone(phone);
		boolean sendFlag = false;
		if (session != null) {
			log.info("获取到："+phone+"的session");
			Channel channel = session.getChannel();
			if(channel != null) {
				log.info("获取到："+phone+"的channel");
				int flowId = super.getFlowId(channel);
				log.info("终端参数设置消息："+msg.toString());
				byte[] bs = this.msgEncoder.encode4ParamSetting(phone,msg, flowId);
				GpsBaseSvc gpsBaseSvc = new GpsBaseSvcStub(GpsServerConsts.gps_server_base);
				directive.set发送时间(DateUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss"));
				directive.set消息流水号(flowId);
				log.info("终端参数设置-消息字节数组:{}",HexStringUtils.toHexString(bs));
				boolean flag = super.send2Client(channel, bs);
				if(flag) {
					log.info("消息发送成功！");
					directive.set状态("成功");
					sendFlag = true;
					if(flag) {
						log.info("消息发送成功！");
						sendFlag = true;
					}else {
						log.info("消息发送失败！");
					}
				}else {
					log.info("消息发送失败！");
					directive.set状态("失败");
				}
				gpsBaseSvc.createTheDirective(directive);
			}else {
				log.info("终端的通道失去链接");
			}
		}else {
			log.info("终端链接已经断开");
		}
		return sendFlag;
	}
	
	//发送位置信息查询
	public boolean sendLocationQuery(String phone) throws Exception {
		log.info("位置信息查询:");
		boolean sendFlag = false;
		Session session = sessionManager.findByTerminalPhone(phone);
		if (session != null) {
			Channel channel = session.getChannel();
			if(channel != null) {
				int flowId = super.getFlowId(channel);
				byte[] bs = this.msgEncoder.encode4LocationQuery(phone, flowId);
//				GpsBaseSvc gpsBaseSvc = new GpsBaseSvcStub(GpsServerConsts.gps_server_base);
//				directive.set发送时间(DateUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss"));
//				directive.set消息流水号(flowId);
				log.info("位置信息查询-消息字节数组:{}",HexStringUtils.toHexString(bs));
				boolean flag = super.send2Client(channel, bs);
				if(flag) {
					log.info("消息发送成功！");
//					directive.set状态("成功");
					sendFlag = true;
					if(flag) {
						log.info("消息发送成功！");
						sendFlag = true;
					}else {
						log.info("消息发送失败！");
					}
				}else {
					log.info("消息发送失败！");
//					directive.set状态("失败");
				}
//				gpsBaseSvc.createTheDirective(directive);
			}else {
				log.info("终端的通道失去链接");
			}
			
		}else {
			log.info("终端链接已经断开");
		}
		return sendFlag;
	}
	
	 /**
     * 设置多边形区域
     * @param params
     * @throws Exception 
     */
	public void sendPolygonParamSettingArea(String phone,String params) throws Exception {
//		log.info("设置多边形区域:");
//		Session session = sessionManager.findByTerminalPhone(phone);
//		if (session != null) {
//			Channel channel = session.getChannel();
//			if(channel != null) {
//				int flowId = super.getFlowId(channel);
//				PolygonSettingArea msg = new PolygonSettingArea();
//				byte[] bs = this.msgEncoder.encode4PolygonSettingArea(phone,msg, flowId);
//				log.info("设置多边形区域-消息字节数组:{}",HexStringUtils.toHexString(bs));
//				super.send2Client(channel, bs);
//			}else {
//				log.info("终端的通道失去链接");
//			}
//		}else {
//			log.info("终端链接已经断开");
//		}
	}
	
	/**
     * 删除多边形区域
     * @param params
     * @throws Exception 
     */
	public void sendPolygonParamDelArea(String phone,String params) throws Exception {
//		log.info("删除多边形区域:");
//		Session session = sessionManager.findByTerminalPhone(phone);
//		if (session != null) {
//			Channel channel = session.getChannel();
//			if(channel != null) {
//				int flowId = super.getFlowId(channel);
//				PolygonDelArea msg = new PolygonDelArea();
//				byte[] bs = this.msgEncoder.encode4PolygonDelArea(phone,msg, flowId);
//				log.info("删除多边形区域-消息字节数组:{}",HexStringUtils.toHexString(bs));
//				super.send2Client(channel, bs);
//			}else {
//				log.info("终端的通道失去链接");
//			}
//		}else {
//			log.info("终端链接已经断开");
//		}
	}
}

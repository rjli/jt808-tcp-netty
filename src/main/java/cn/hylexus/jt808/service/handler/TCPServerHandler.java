package cn.hylexus.jt808.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rksp.util.StringUtil;

import cn.hylexus.jt808.common.TPMSConsts;
import cn.hylexus.jt808.server.SessionManager;
import cn.hylexus.jt808.service.MsgToDPlatformGpsPojoService;
import cn.hylexus.jt808.service.TerminalMsgProcessService;
import cn.hylexus.jt808.service.TerminalMsgSendService;
import cn.hylexus.jt808.service.codec.MsgDecoder;
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
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

public class TCPServerHandler extends ChannelInboundHandlerAdapter { // (1)

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final SessionManager sessionManager;
	private final MsgDecoder decoder;
	private TerminalMsgProcessService msgProcessService;
	private MsgToDPlatformGpsPojoService msPojoService;

	public TCPServerHandler() {
		this.sessionManager = SessionManager.getInstance();
		this.decoder = new MsgDecoder();
		this.msgProcessService = new TerminalMsgProcessService();
		this.msPojoService = new MsgToDPlatformGpsPojoService();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException { // (2)
		try {
			ByteBuf buf = (ByteBuf) msg;
			logger.info("接收到的消息长度：{}",buf.readableBytes());
			if (buf.readableBytes() <= 0) {
				// ReferenceCountUtil.safeRelease(msg);
				return;
			}

			byte[] bs = new byte[buf.readableBytes()];
			
			buf.readBytes(bs);
			logger.info("读完后的数据长度：{}" , buf.readableBytes());
			String receiveMsg = "7E"+HexStringUtils.toHexString(bs)+"7E";
			logger.info("收到的消息包:{}",receiveMsg);
			// 字节数据转换为针对于808消息结构的实体类
			PackageData pkg = this.decoder.bytes2PackageData(bs);
			// 引用channel,以便回送数据给硬件
			pkg.setChannel(ctx.channel());
			logger.info("消息体中的Channel：{}" ,pkg.getChannel());
			this.processPackageData(pkg,receiveMsg);
		} finally {
			release(msg);
		}
	}

	/**
	 * 
	 * 处理业务逻辑
	 * 
	 * @param packageData
	 * 
	 */
	private void processPackageData(PackageData packageData,String receiveMsg) {
		final MsgHeader header = packageData.getMsgHeader();
		/**
		 * 1、终端注册 （0100）2、注册应答（8100）3、注册成功 4、鉴权信息（0102）5、鉴权信息(0102) 6、鉴权应答（8001）7、定位数据（0200）8、平台通用应答（8001）
		 * 9、查询终端参数应答(0x0104) 10、位置信息查询应答(0x0201)
		 */
	    String type = "成功";
	    String messageType = "未知";
		// 1. 终端心跳-消息体为空 ==> 平台通用应答
		if (TPMSConsts.msg_id_terminal_heart_beat == header.getMsgId()) {
			logger.info(">>>>>[终端心跳],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
			messageType = "终端心跳";
			try {
				this.msgProcessService.processTerminalHeartBeatMsg(packageData);
				logger.info("<<<<<[终端心跳],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
			} catch (Exception e) {
				type="失败";
				logger.error("<<<<<[终端心跳]处理错误,phone={},flowid={},err={}", header.getTerminalPhone(), header.getFlowId(),
						e.getMessage());
				e.printStackTrace();
			}
			
		}

		// 5. 终端鉴权 ==> 平台通用应答
		else if (TPMSConsts.msg_id_terminal_authentication == header.getMsgId()) {
			logger.info(">>>>>[终端鉴权],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
			messageType = "终端鉴权";
			try {
				TerminalAuthenticationMsg authenticationMsg = new TerminalAuthenticationMsg(packageData);
				logger.info(">>>>>[接收到的消息],authenticationMsg={}",authenticationMsg);
				this.msgProcessService.processAuthMsg(authenticationMsg);
				logger.info("<<<<<[终端鉴权],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
			} catch (Exception e) {
				logger.error("<<<<<[终端鉴权]处理错误,phone={},flowid={},err={}", header.getTerminalPhone(), header.getFlowId(),
						e.getMessage());
				type="失败";
				e.printStackTrace();
			}
		}
		// 6. 终端注册 ==> 终端注册应答
		else if (TPMSConsts.msg_id_terminal_register == header.getMsgId()) {
			logger.info(">>>>>[终端注册],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
			messageType = "终端注册";
			try {
				
				TerminalRegisterMsg msg = this.decoder.toTerminalRegisterMsg(packageData);
				this.msgProcessService.processRegisterMsg(msg);
				
				logger.info("<<<<<[终端注册],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
				
			} catch (Exception e) {
				logger.error("<<<<<[终端注册]处理错误,phone={},flowid={},err={}", header.getTerminalPhone(), header.getFlowId(),
						e.getMessage());
				type="失败";
				e.printStackTrace();
			}
		}
		// 7. 终端注销(终端注销数据消息体为空) ==> 平台通用应答
		else if (TPMSConsts.msg_id_terminal_log_out == header.getMsgId()) {
			logger.info(">>>>>[终端注销],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
			messageType = "终端注销";
			try {
				this.msgProcessService.processTerminalLogoutMsg(packageData);
				logger.info("<<<<<[终端注销],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
			} catch (Exception e) {
				logger.error("<<<<<[终端注销]处理错误,phone={},flowid={},err={}", header.getTerminalPhone(), header.getFlowId(),
						e.getMessage());
				type="失败";
				e.printStackTrace();
			}
		}
		// 8.定位数据(位置信息汇报)  ==> 平台通用应答
		else if(TPMSConsts.msg_id_terminal_location_info_upload == header.getMsgId()) {
			logger.info(">>>>>[终端位置信息汇报],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
			messageType = "终端位置信息汇报";
			try {
				TerminalLoctionInfoReportMsg msg = this.decoder.toTerminalLoctionInfoReportMsg(packageData);
				this.msgProcessService.processTerminalLocationInfoReportMsg(msg);
				logger.info("<<<<<[终端位置信息汇报处理完成]");
			} catch (Exception e) {
				logger.error("<<<<<[终端位置信息汇报]处理错误,phone={},flowid={},err={}", header.getTerminalPhone(), header.getFlowId(),
						e.getMessage());
				type="失败";
				e.printStackTrace();
			}
		}
		
		// 9.查询终端参数应答   ==> 平台通用应答
		else if(TPMSConsts.msg_id_terminal_param_query_resp == header.getMsgId()) {
			logger.info(">>>>>[查询终端参数应答],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
			messageType = "查询终端参数应答";
			try {
				TerminalParamQueryMsgResp msg = this.decoder.toTerminalParamQueryMsgResp(packageData);
				this.msgProcessService.processParamQueryMsgResp(msg);
				logger.info("<<<<<[查询终端参数应答处理完成]");
			} catch (Exception e) {
				logger.error("<<<<<[查询终端参数应答处理错误],phone={},flowid={},err={}", header.getTerminalPhone(), header.getFlowId(),
						e.getMessage());
				type="失败";
				e.printStackTrace();
			}
		}	
		
		// 10. 位置信息查询应答 ==> 平台通用应答
		else if (TPMSConsts.cmd_position_query_resp == header.getMsgId()) {
			logger.info(">>>>>[位置信息查询应答],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
			messageType = "位置信息查询应答";
			try {
				LocationInformationQueryResp msg = this.decoder.toLocationInformationQueryResp(packageData);
				this.msgProcessService.processLocationQuery(msg);
				logger.info("<<<<<[位置信息查询应答处理完成]");
			} catch (Exception e) {
				logger.error("<<<<<[位置信息查询应答]处理错误,phone={},flowid={},err={}", header.getTerminalPhone(), header.getFlowId(),
						e.getMessage());
				type="失败";
				e.printStackTrace();
			}
			
		}
		
		// 11.终端通用应答 ==> 平台通用应答
		else if (TPMSConsts.msg_id_terminal_common_resp == header.getMsgId()) {
			logger.info(">>>>>[终端通用应答],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
			messageType = "终端通用应答";
			try {
				TerminalCommonRespMsg msg = this.decoder.toTerminalCommonRespMsg(packageData);
				this.msgProcessService.processTerminalCommonMsg(msg);
				logger.info("<<<<<[终端通用应答处理完成],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
			} catch (Exception e) {
				logger.error("<<<<<[终端通用应答]处理错误,phone={},flowid={},err={}", header.getTerminalPhone(), header.getFlowId(),
						e.getMessage());
				type="失败";
				e.printStackTrace();
			}
		}
		// 其他情况
		else {
			logger.error(">>>>>>[未知消息类型],phone={},msgId={},package={}", header.getTerminalPhone(), header.getMsgId(),
					packageData);
		}
		//把接受到的报文保存到数据库中
		if(!"未知".equals(messageType)) {
			//把接收到的保存存储到数据库中
			String oid = msPojoService.createTheMessage(packageData,type,messageType,receiveMsg);
			String info = StringUtil.isEmpty(oid)?"失败":"成功";
			logger.info("<<<<<[接受到的数据包消息报文"+info+"保存到数据库中],消息包={}", packageData.toString());
		
		}else {
			logger.info("接收的消息类型未知,不进行保存操作。");
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
		logger.error("发生异常:{}", cause.getMessage());
		cause.printStackTrace();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Session session = Session.buildSession(ctx.channel());
		sessionManager.put(session.getId(), session);
		logger.info("终端连接:{}", session);
		logger.info("当前客户端数量：{}",sessionManager.toList().size());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		final String sessionId = ctx.channel().id().asLongText();
		Session session = sessionManager.findBySessionId(sessionId);
		
		MsgToDPlatformGpsPojoService gpsPojoService = new MsgToDPlatformGpsPojoService();
		String phone = session.getTerminalPhone();
		if(!StringUtil.isEmpty(phone)) {
			gpsPojoService.creatTerminalFacilityDropsRecord(phone);//创建终端设备掉线记录
		}
		
		this.sessionManager.removeBySessionId(sessionId);
		logger.info("终端断开连接:{}", session);
		logger.info("当前客户端数量：{}",sessionManager.toList().size());
		ctx.channel().close();
		// ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.READER_IDLE) {
				Session session = this.sessionManager.removeBySessionId(Session.buildId(ctx.channel()));
				logger.error("服务器主动断开连接:{}", session);
				ctx.close();
			}
		}
	}

	private void release(Object msg) {
		try {
			ReferenceCountUtil.release(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
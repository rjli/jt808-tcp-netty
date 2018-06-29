package cn.hylexus.jt808.service.codec;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hylexus.jt808.common.TPMSConsts;
import cn.hylexus.jt808.util.BitOperator;
import cn.hylexus.jt808.util.JT808ProtocolUtils;
import cn.hylexus.jt808.vo.PackageData;
import cn.hylexus.jt808.vo.Session;
import cn.hylexus.jt808.vo.req.LocationInformationQueryResp;
import cn.hylexus.jt808.vo.req.PolygonDelArea;
import cn.hylexus.jt808.vo.req.PolygonParamItem;
import cn.hylexus.jt808.vo.req.PolygonSettingArea;
import cn.hylexus.jt808.vo.req.TerminalParamItem;
import cn.hylexus.jt808.vo.req.TerminalParametersSettingInfo;
import cn.hylexus.jt808.vo.req.TerminalRegisterMsg;
import cn.hylexus.jt808.vo.resp.ServerCommonRespMsgBody;
import cn.hylexus.jt808.vo.resp.TerminalRegisterMsgRespBody;

public class MsgEncoder {
	private BitOperator bitOperator;
	private JT808ProtocolUtils jt808ProtocolUtils;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	public MsgEncoder() {
		this.bitOperator = new BitOperator();
		this.jt808ProtocolUtils = new JT808ProtocolUtils();
	}

	public byte[] encode4TerminalRegisterResp(TerminalRegisterMsg req, TerminalRegisterMsgRespBody respMsgBody,
			int flowId) throws Exception {
		// 消息体字节数组
//		logger.info("=======对终端注册应答消息进行编码======");
//		logger.info("消息流水号={},响应结果={},鉴权码 ={}" ,respMsgBody.getReplyFlowId(),respMsgBody.getReplyCode(),respMsgBody.getReplyToken());
		byte[] msgBody = null;
		// 鉴权码(STRING) 只有在成功后才有该字段
		if (respMsgBody.getReplyCode() == TerminalRegisterMsgRespBody.success) {
			msgBody = this.bitOperator.concatAll(Arrays.asList(//
					bitOperator.integerTo2Bytes(respMsgBody.getReplyFlowId()), // 流水号(2)
					new byte[] { respMsgBody.getReplyCode() }, // 结果
					respMsgBody.getReplyToken().getBytes(TPMSConsts.string_charset)// 鉴权码(STRING)
			));
		} else {
			msgBody = this.bitOperator.concatAll(Arrays.asList(//
					bitOperator.integerTo2Bytes(respMsgBody.getReplyFlowId()), // 流水号(2)
					new byte[] { respMsgBody.getReplyCode() }// 错误代码
			));
		}

		// 消息头
		int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBody.length, 0b000, false, 0);
		byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(req.getMsgHeader().getTerminalPhone(),
				TPMSConsts.cmd_terminal_register_resp, msgBody, msgBodyProps, flowId);
		byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBody);
	
		// 校验码
		int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length);
//		logger.info("消息体={},消息体的长度={},校验码={}", Arrays.toString(headerAndBody) , headerAndBody.length,checkSum);
		// 连接并且转义
		return this.doEncode(headerAndBody, checkSum);
	}

	// public byte[] encode4ServerCommonRespMsg(TerminalAuthenticationMsg req,
	// ServerCommonRespMsgBody respMsgBody, int flowId) throws Exception {
	public byte[] encode4ServerCommonRespMsg(PackageData req, ServerCommonRespMsgBody respMsgBody, int flowId)
			throws Exception {
//		logger.info("=======对平台通用应答消息进行编码======");
//		logger.info("消息流水号={},应答id={},响应结果 ={}" ,respMsgBody.getReplyFlowId(),respMsgBody.getReplyId(),respMsgBody.getReplyCode());

		byte[] msgBody = this.bitOperator.concatAll(Arrays.asList(//
				bitOperator.integerTo2Bytes(respMsgBody.getReplyFlowId()), // 应答流水号
				bitOperator.integerTo2Bytes(respMsgBody.getReplyId()), // 应答ID,对应的终端消息的ID
				new byte[] { respMsgBody.getReplyCode() }// 结果
		));

		// 消息头
		int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBody.length, 0b000, false, 0);
		byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(req.getMsgHeader().getTerminalPhone(),
				TPMSConsts.cmd_common_resp, msgBody, msgBodyProps, flowId);
		byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBody);
		
		// 校验码
		int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length);
//		logger.info("消息体={},消息体的长度={},校验码={}", Arrays.toString(headerAndBody) , headerAndBody.length,checkSum);
		// 连接并且转义
		return this.doEncode(headerAndBody, checkSum);
	}
	
	/**
	 * 终端通用应答
	 * @param req
	 * @param respMsgBody
	 * @param flowId
	 * @return
	 * @throws Exception
	 */
	public byte[] encode4TerminalCommonRespMsg(PackageData req, ServerCommonRespMsgBody respMsgBody, int flowId)
			throws Exception {

		byte[] msgBody = this.bitOperator.concatAll(Arrays.asList(//
				bitOperator.integerTo2Bytes(respMsgBody.getReplyFlowId()), // 应答流水号
				bitOperator.integerTo2Bytes(respMsgBody.getReplyId()), // 应答ID,对应的终端消息的ID
				new byte[] { respMsgBody.getReplyCode() }// 结果
		));

		// 消息头
		int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBody.length, 0b000, false, 0);
		byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(req.getMsgHeader().getTerminalPhone(),
				TPMSConsts.msg_id_terminal_common_resp, msgBody, msgBodyProps, flowId);
		byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBody);
		
		// 校验码
		int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length);
		// 连接并且转义
		return this.doEncode(headerAndBody, checkSum);
	}
	
    /**
     * 终端参数设置
     * @param msgBodyBytes
     * @param session
     * @return
     * @throws Exception
     */
	public byte[] encode4ParamSetting(String phone,TerminalParametersSettingInfo respMsgBody, int flowId) throws Exception {
		// 消息头
		byte[] msgBodyBytes = bitOperator.integerTo1Bytes(respMsgBody.getCount());
		for(TerminalParamItem item : respMsgBody.getTerminalParamItemList()) {
			byte[] msgValue = item.getParamValue();
			msgBodyBytes = this.bitOperator.concatAll(msgBodyBytes,//
					bitOperator.integerTo4Bytes(item.getMsgId()),
					bitOperator.integerTo1Bytes(item.getParamLength()),msgValue);
		}
		int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBodyBytes.length, 0b000, false, 0);
		byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(phone,
				TPMSConsts.cmd_terminal_param_settings, msgBodyBytes, msgBodyProps, flowId);
		// 连接消息头和消息体
		byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBodyBytes);
		// 校验码
		int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length);
		// 连接并且转义
		return this.doEncode(headerAndBody, checkSum);
	}
	
	public byte[] encode4ParamSetting1(byte[] msgBodyBytes, Session session) throws Exception {
		// 消息头
		int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBodyBytes.length, 0b000, false, 0);
		byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(session.getTerminalPhone(),
				TPMSConsts.cmd_terminal_param_settings, msgBodyBytes, msgBodyProps, session.currentFlowId());
		// 连接消息头和消息体
		byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBodyBytes);
		// 校验码
		int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length-1);
		// 连接并且转义
		return this.doEncode(headerAndBody, checkSum);
	}
    /**
     * 终端参数查询
     * @return
     */
	public byte[] encode4ParamQuery(String phone, int flowId)  throws Exception {
		logger.info("=======终端消息查询======");
		byte[] msgBody = {};
		// 消息头
		int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBody.length, 0b000, false, 0);
		byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(phone,
				TPMSConsts.cmd_terminal_param_query, msgBody, msgBodyProps, flowId);
		byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBody);
		
		// 校验码
		int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length);
//		logger.info("消息体={},消息体的长度={},校验码={}", Arrays.toString(headerAndBody) , headerAndBody.length,checkSum);
		// 连接并且转义
		return this.doEncode(headerAndBody, checkSum);
	}
	
	
	/**
     * 位置信息查询
     * @return
     */
	public byte[] encode4LocationQuery( String phone, int flowId) throws Exception {
		logger.info("=======位置信息查询======");
		byte[] msgBody = {};
		// 消息头
		int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBody.length, 0b000, false, 0);
		byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(phone,
				TPMSConsts.cmd_position_query, msgBody, msgBodyProps, flowId);
		byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBody);
		// 校验码
		int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length);
		// 连接并且转义
		return this.doEncode(headerAndBody, checkSum);
	}
	
	 /**
     * 设置多边形区域
     * @param msgBodyBytes
     * @param session
     * @return
     * @throws Exception
     */
	public byte[] encode4PolygonSettingArea(String phone,PolygonSettingArea respMsgBody, int flowId) throws Exception {
		// 消息体
		byte[] msgBodyBytes = {};
//		byte[] msgBodyBytes = this.bitOperator.concatAll(Arrays.asList(//
//				bitOperator.integerTo2Bytes(respMsgBody.getAreaID()) // 区域ID
//			
//		));
		
		int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBodyBytes.length, 0b000, false, 0);
		// 消息头
		byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(phone,
				TPMSConsts.cmd_polygon_area_settings, msgBodyBytes, msgBodyProps, flowId);
		// 连接消息头和消息体
		byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBodyBytes);
		// 校验码
		int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length-1);
		// 连接并且转义
		return this.doEncode(headerAndBody, checkSum);
	}
	
	 /**
     * 删除多边形区域
     * @param msgBodyBytes
     * @param session
     * @return
     * @throws Exception
     */
	public byte[] encode4PolygonDelArea(String phone,PolygonDelArea respMsgBody, int flowId) throws Exception {
		// 消息体
		byte[] msgBodyBytes = {};
//		byte[] msgBodyBytes = this.bitOperator.concatAll(Arrays.asList(//
//				bitOperator.integerTo2Bytes(respMsgBody.getAreaCount()), // 区域数
//				bitOperator.bytes2bytes(respMsgBody.getAreaIds())//区域ID
//		));
		
		int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBodyBytes.length, 0b000, false, 0);
		// 消息头
		byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(phone,
				TPMSConsts.cmd_polygon_area_del, msgBodyBytes, msgBodyProps, flowId);
		// 连接消息头和消息体
		byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBodyBytes);
		// 校验码
		int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length-1);
		// 连接并且转义
		return this.doEncode(headerAndBody, checkSum);
	}
	
	private byte[] doEncode(byte[] headerAndBody, int checkSum) throws Exception {
		byte[] noEscapedBytes = this.bitOperator.concatAll(Arrays.asList(//
				new byte[] { TPMSConsts.pkg_delimiter }, // 0x7e
				headerAndBody, // 消息头+ 消息体
				bitOperator.integerTo1Bytes(checkSum), // 校验码
				new byte[] { TPMSConsts.pkg_delimiter }// 0x7e
		));
		// 转义
		return jt808ProtocolUtils.doEscape4Send(noEscapedBytes, 1, noEscapedBytes.length - 2);
	}
	
}

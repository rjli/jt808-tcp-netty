package cn.hylexus.jt808.vo.resp;

import cn.hylexus.jt808.vo.PackageData;

/**
 * 终端通用应答消息
 * @author cheryl
 *
 */
public class TerminalCommonRespMsg extends PackageData  {

	private TerminalCommonRespInfo terminalCommonRespInfo;
	
	
	public TerminalCommonRespMsg() {
	}

	public TerminalCommonRespMsg(PackageData packageData) {
		this();
		this.channel = packageData.getChannel();
		this.checkSum = packageData.getCheckSum();
		this.msgBodyBytes = packageData.getMsgBodyBytes();
		this.msgHeader = packageData.getMsgHeader();
	}

	public TerminalCommonRespInfo getTerminalCommonRespInfo() {
		return terminalCommonRespInfo;
	}

	public void setTerminalCommonRespInfo(TerminalCommonRespInfo terminalCommonRespInfo) {
		this.terminalCommonRespInfo = terminalCommonRespInfo;
	}

	@Override
	public String toString() {
		return "TerminalCommonRespMsg [terminalCommonRespInfo=" + terminalCommonRespInfo + "]";
	}


	public static class TerminalCommonRespInfo {
		// byte[0-1] 应答流水号 对应的终端消息的流水号
		private int replyFlowId;
		// byte[2-3] 应答ID 对应的终端消息的ID
		private int replyId;
		/**
		 * 0：成功∕确认<br>
		 * 1：失败<br>
		 * 2：消息有误<br>
		 * 3：不支持<br>
		 * 4：报警处理确认<br>
		 */
		private byte replyCode;
		
		public int getReplyFlowId() {
			return replyFlowId;
		}

		public void setReplyFlowId(int flowId) {
			this.replyFlowId = flowId;
		}

		public int getReplyId() {
			return replyId;
		}

		public void setReplyId(int msgId) {
			this.replyId = msgId;
		}

		public byte getReplyCode() {
			return replyCode;
		}

		public void setReplyCode(byte code) {
			this.replyCode = code;
		}

		@Override
		public String toString() {
			return "ServerCommonRespMsg [replyFlowId=" + replyFlowId + ", replyId=" + replyId + ", replyCode=" + replyCode
					+ "]";
		}
	}

}

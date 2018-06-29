package cn.hylexus.jt808.vo.req;

import java.util.List;

import cn.hylexus.jt808.vo.PackageData;

/**
 * 查询终端参数应答
 * @author cheryl
 */
public class TerminalParamQueryMsgResp extends PackageData {
	
	private TerminalParametersqQueryInfo terminalParametersqQueryInfo;

	public TerminalParamQueryMsgResp() {
	}

	public TerminalParamQueryMsgResp(PackageData packageData) {
		this();
		this.channel = packageData.getChannel();
		this.checkSum = packageData.getCheckSum();
		this.msgBodyBytes = packageData.getMsgBodyBytes();
		this.msgHeader = packageData.getMsgHeader();
	}

    
	public TerminalParametersqQueryInfo getTerminalParametersqQueryInfo() {
		return terminalParametersqQueryInfo;
	}

	public void setTerminalParametersqQueryInfo(TerminalParametersqQueryInfo terminalParametersqQueryInfo) {
		this.terminalParametersqQueryInfo = terminalParametersqQueryInfo;
	}


	public static class TerminalParametersqQueryInfo extends TerminalParametersSettingInfo{
		// WORD 应答流水号 对应的终端消息的流水号
		private int replyFlowId;

		public int getReplyFlowId() {
			return replyFlowId;
		}

		public void setReplyFlowId(int replyFlowId) {
			this.replyFlowId = replyFlowId;
		}

		@Override
		public String toString() {
			return "TerminalParametersqQueryInfo [replyFlowId=" + replyFlowId + ", count=" + count
					+ ", terminalParamItemList=" + terminalParamItemList + ", heartbeatInterval=" + heartbeatInterval
					+ ", locationReportingStrategy=" + locationReportingStrategy + ", serverAddress=" + serverAddress
					+ ", serverPort=" + serverPort + ", sleepTimeReportInteval=" + sleepTimeReportInteval
					+ ", defaultTimeReportInteval=" + defaultTimeReportInteval + ", maxSpeed=" + maxSpeed
					+ ", overspeedDuration=" + overspeedDuration + ", mileage=" + mileage + ", provinceId=" + provinceId
					+ ", cityId=" + cityId + ", licensePlate=" + licensePlate + ", licensePlateColor="
					+ licensePlateColor + "]";
		}

	}

}

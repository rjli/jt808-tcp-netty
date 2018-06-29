package cn.hylexus.jt808.vo.req;

import cn.hylexus.jt808.vo.PackageData;
import cn.hylexus.jt808.vo.req.TerminalLoctionInfoReportMsg.TerminalLoctionInfo;

/**
 * 位置信息查询应答
 * @author Administrator
 *
 */
public class LocationInformationQueryResp  extends PackageData {

	private LocationInformationQueryInfo locationInformationQueryInfo;
	
	public LocationInformationQueryResp() {
	}
	
	public LocationInformationQueryResp(PackageData packageData) {
		this();
		this.channel = packageData.getChannel();
		this.checkSum = packageData.getCheckSum();
		this.msgBodyBytes = packageData.getMsgBodyBytes();
		this.msgHeader = packageData.getMsgHeader();
	}
	
	public LocationInformationQueryInfo getLocationInformationQueryInfo() {
		return locationInformationQueryInfo;
	}

	public void setLocationInformationQueryInfo(LocationInformationQueryInfo locationInformationQueryInfo) {
		this.locationInformationQueryInfo = locationInformationQueryInfo;
	}



	public static class LocationInformationQueryInfo extends TerminalLoctionInfo{
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
			return "LocationInformationQueryInfo [replyFlowId=" + replyFlowId + ", warningFlag=" + warningFlag
					+ ", status=" + status + ", latitude=" + latitude + ", longitude=" + longitude + ", altitude="
					+ altitude + ", speed=" + speed + ", course=" + course + ", time=" + time + ", mileage=" + mileage
					+ "]";
		}

        
		
		
	}
	
}

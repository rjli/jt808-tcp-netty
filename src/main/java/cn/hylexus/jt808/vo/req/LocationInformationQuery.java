package cn.hylexus.jt808.vo.req;

import java.util.Arrays;

import cn.hylexus.jt808.vo.PackageData;

/**
 * 位置信息查询
 * 
 * @author hylexus
 *
 */
public class LocationInformationQuery extends PackageData {

	public LocationInformationQuery() {
	}

	public LocationInformationQuery(PackageData packageData) {
		this();
		this.channel = packageData.getChannel();
		this.checkSum = packageData.getCheckSum();
		this.msgBodyBytes = packageData.getMsgBodyBytes();
		this.msgHeader = packageData.getMsgHeader();
	}

	@Override
	public String toString() {
		return "LocationInformationQuery [msgHeader=" + msgHeader
				+ ", msgBodyBytes=" + Arrays.toString(msgBodyBytes) + ", checkSum=" + checkSum + ", channel=" + channel
				+ "]";
	}
}

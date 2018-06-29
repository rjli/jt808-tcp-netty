package cn.hylexus.jt808.socket;

import java.util.ArrayList;
import java.util.List;

import com.rksp.util.StringUtil;
import com.sharetime.gps.pojo.Gps区域;
import com.sharetime.gps.pojo.Gps终端设备;
import com.sharetime.gps.vo.TerminalParamSettingVo;

import cn.hylexus.jt808.common.TPMSParamterConsts;
import cn.hylexus.jt808.util.BitOperator;
import cn.hylexus.jt808.vo.req.PolygonParamItem;
import cn.hylexus.jt808.vo.req.PolygonSettingArea;
import cn.hylexus.jt808.vo.req.TerminalParamItem;
import cn.hylexus.jt808.vo.req.TerminalParametersSettingInfo;

public class PojoToMessage {

	private BitOperator bitOperator;
	public PojoToMessage() {
		bitOperator = new BitOperator();
	}

	
	/**
	 * TerminalParamSettingVo --->转换为对应的TerminalParametersSettingInfo
	 */
	public TerminalParametersSettingInfo changeToerminalParametersSettigInfo(TerminalParamSettingVo pojo) {
		TerminalParametersSettingInfo msg = new TerminalParametersSettingInfo();
		List<TerminalParamItem> paramItems = new ArrayList<TerminalParamItem>();
		if(!StringUtil.isEmpty(pojo.getHeartbeatInterval())) {
			changeintToByte4(pojo.getHeartbeatInterval(), TPMSParamterConsts.heartbeatInterval, paramItems);
		}
		if(!StringUtil.isEmpty(pojo.getServerAddress())) {
			changeStringToBytes(pojo.getServerAddress(),TPMSParamterConsts.serverAddress, paramItems);
		}
		if(!StringUtil.isEmpty(pojo.getServerPort())) {
			changeintToByte4(pojo.getServerPort(), TPMSParamterConsts.serverPort,paramItems);
		}
		if(!StringUtil.isEmpty(pojo.getSleepTimeReportInteval())) {
			changeintToByte4(pojo.getSleepTimeReportInteval(),TPMSParamterConsts.sleepTimeReportInteval, paramItems);
		}
		if(!StringUtil.isEmpty(pojo.getDefaultTimeReportInteval())) {
			changeintToByte4(pojo.getDefaultTimeReportInteval(), TPMSParamterConsts.defaultTimeReportInteval,paramItems);
		}
		if(!StringUtil.isEmpty(pojo.getMaxSpeed())) {
			changeintToByte4(pojo.getMaxSpeed(), TPMSParamterConsts.maxspeed,paramItems);
		}
		if(!StringUtil.isEmpty(pojo.getOverspeedDuration())) {
			changeintToByte4(pojo.getOverspeedDuration(), TPMSParamterConsts.overspeedDuration,paramItems);
		}
		if(pojo.getProvinceId() != null) {
			changeintToByte4(pojo.getProvinceId(), TPMSParamterConsts.provinceId,paramItems);
		}
		if(pojo.getCityId() != null) {
			changeintToByte4(pojo.getCityId(), TPMSParamterConsts.provinceId,paramItems);
		}
		if(pojo.getLicensePlateColor() != null) {
			changeintToByte1(pojo.getLicensePlateColor(), TPMSParamterConsts.licensePlateColor,paramItems);
		}
		if(!StringUtil.isEmpty(pojo.getLicensePlate())) {
			changeStringToBytes(pojo.getLicensePlate(), TPMSParamterConsts.licensePlate,paramItems);
		}
		msg.setTerminalParamItemList(paramItems);
		msg.setCount(paramItems.size());
		return msg;
	}


	/**
	 * Gps区域 --->转换为对应的PolygonSettingArea
	 */
	public PolygonSettingArea changeToPolygonAreaSettigInfo(Gps区域 pojo) {
		PolygonSettingArea msg = new PolygonSettingArea();
		List<PolygonParamItem> paramItems = new ArrayList<PolygonParamItem>();
		byte[]  bytes = null;
		if(!StringUtil.isEmpty(pojo.get区域名称())) {
		}
		if(!StringUtil.isEmpty(pojo.get区域编码())) {
		}
		if(!StringUtil.isEmpty(pojo.get单位名称())) {
		}
		if(!StringUtil.isEmpty(pojo.get颜色标识())) {
		}
		if(!StringUtil.isEmpty(pojo.get录入人())) {
		}
		
		msg.setPolygonParamItemList(paramItems);
		msg.setCount(paramItems.size());
		return msg;
	}
	
	private void changeintToByte4(int  param, int msgId, List<TerminalParamItem> paramItems) {
		TerminalParamItem paramItem = new TerminalParamItem();
		paramItem.setMsgId(msgId);
		byte[] bytes = this.bitOperator.integerTo4Bytes(param);
		paramItem.setParamValue(bytes);
		paramItem.setParamLength(bytes.length);
		paramItems.add(paramItem);
	}
	
	private void changeintToByte1(int param, int msgId, List<TerminalParamItem> paramItems) {
		TerminalParamItem paramItem = new TerminalParamItem();
		paramItem.setMsgId(msgId);
		byte[] bytes = this.bitOperator.integerTo1Bytes(param);
		paramItem.setParamValue(bytes);
		paramItem.setParamLength(bytes.length);
		paramItems.add(paramItem);
	}
	
	private void changeintToByte4(String param, int msgId, List<TerminalParamItem> paramItems) {
		TerminalParamItem paramItem = new TerminalParamItem();
		paramItem.setMsgId(msgId);
		byte[] bytes = this.bitOperator.integerTo4Bytes(Integer.parseInt(param));
		paramItem.setParamValue(bytes);
		paramItem.setParamLength(bytes.length);
		paramItems.add(paramItem);
	}
	
	private void changeStringToBytes(String param, int msgId,List<TerminalParamItem> paramItems) {
		TerminalParamItem paramItem = new TerminalParamItem();
		paramItem.setMsgId(msgId);
		byte[] bytes = param.getBytes();
		paramItem.setParamValue(bytes);
		paramItem.setParamLength(bytes.length);
		paramItems.add(paramItem);
	}
	
	
}

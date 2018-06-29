package cn.hylexus.jt808.vo.req;

import java.util.List;
/**
 * 设置终端参数
 * @author cheryl
 */
public class TerminalParametersSettingInfo {
	// 参数项总数
	protected int count;
    //附加消息列表
	protected List<TerminalParamItem> terminalParamItemList;
    //附加消息中具体内容的值
	//终端⼼跳发送间隔，单位为（s）
	protected int heartbeatInterval;
//	位置汇报策略，0：定时汇报；1：定距汇报；2：定时和定距汇报
	protected int locationReportingStrategy;
	//主服务器地址,IP或域名
	protected String serverAddress;
	//服务器TCP端口
	protected int serverPort;
	//休眠时汇报时间间隔，单位为秒（s），>0
	protected int sleepTimeReportInteval;
	//缺省时间汇报间隔，单位为秒（s），>0
	protected int defaultTimeReportInteval;
	//最高速度，单位为千⽶每⼩时（km/h）
	protected int maxSpeed;
	//超速持续时间，单位为秒（s）
	protected int overspeedDuration;
	//车辆⾥程表读数，1/10km
	protected int mileage;
	//车辆所在的省域ID，1～255
	protected int provinceId;
	//车辆所在的市域ID，1～255
	protected int cityId;
	//公安交通同管理部门颁发的机动车号牌
	protected String licensePlate;
	//车牌颜色，按照JT/T415—2006中5.4.12的规定
	protected int licensePlateColor;
	
	public List<TerminalParamItem> getTerminalParamItemList() {
		return terminalParamItemList;
	}

	public void setTerminalParamItemList(List<TerminalParamItem> terminalParamItemList) {
		this.terminalParamItemList = terminalParamItemList;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public void setHeartbeatInterval(int heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getSleepTimeReportInteval() {
		return sleepTimeReportInteval;
	}

	public void setSleepTimeReportInteval(int sleepTimeReportInteval) {
		this.sleepTimeReportInteval = sleepTimeReportInteval;
	}

	public int getDefaultTimeReportInteval() {
		return defaultTimeReportInteval;
	}

	public void setDefaultTimeReportInteval(int defaultTimeReportInteval) {
		this.defaultTimeReportInteval = defaultTimeReportInteval;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public int getOverspeedDuration() {
		return overspeedDuration;
	}

	public void setOverspeedDuration(int overspeedDuration) {
		this.overspeedDuration = overspeedDuration;
	}

	public int getMileage() {
		return mileage;
	}

	public void setMileage(int mileage) {
		this.mileage = mileage;
	}

	public int getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public String getLicensePlate() {
		return licensePlate;
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
	}

	public int getLicensePlateColor() {
		return licensePlateColor;
	}

	public void setLicensePlateColor(int licensePlateColor) {
		this.licensePlateColor = licensePlateColor;
	}


	public int getLocationReportingStrategy() {
		return locationReportingStrategy;
	}

	public void setLocationReportingStrategy(int locationReportingStrategy) {
		this.locationReportingStrategy = locationReportingStrategy;
	}

	@Override
	public String toString() {
		return "TerminalParametersSettingInfo [count=" + count + ", terminalParamItemList=" + terminalParamItemList
				+ ", heartbeatInterval=" + heartbeatInterval + ", locationReportingStrategy="
				+ locationReportingStrategy + ", serverAddress=" + serverAddress + ", serverPort=" + serverPort
				+ ", sleepTimeReportInteval=" + sleepTimeReportInteval + ", defaultTimeReportInteval="
				+ defaultTimeReportInteval + ", maxSpeed=" + maxSpeed + ", overspeedDuration=" + overspeedDuration
				+ ", mileage=" + mileage + ", provinceId=" + provinceId + ", cityId=" + cityId + ", licensePlate="
				+ licensePlate + ", licensePlateColor=" + licensePlateColor + "]";
	}

	

}

package cn.hylexus.jt808.common;
/**
 * 消息体中的
 * @author cheryl
 *
 */
public class TPMSParamterConsts {

	public static int heartbeatInterval = 0x0001; //终端心跳发送间隔，单位为秒（s）
	public static int serverAddress = 0x0013;  //主服务器地址,IP 或域名
	public static int serverPort = 0x0018; // 服务器 TCP 端口
	public static int sleepTimeReportInteval = 0x0027; //休眠时汇报时间间隔，单位为秒（s），>0
	public static int defaultTimeReportInteval = 0x0029; //缺省时间汇报间隔，单位为秒（s），>0 
	
	public static int maxspeed =0x0055; //最高速度，单位为公里每小时（km/h）
	public static int overspeedDuration = 0x0056; //超速持续时间，单位为秒（s）
	public static int odometerReading = 0x0080; //车辆里程表读数，1/10km
	public static int provinceId = 0x0081; //车辆所在的省域 ID
	public static int cityId= 0x0082; //车辆所在的市域 ID
	public static int licensePlate= 0x0083; //公安交通管理部门颁发的机动车号牌
	public static int licensePlateColor = 0x0084; //车牌颜色，按照 JT/T415-2006 的 5.4.12 
}

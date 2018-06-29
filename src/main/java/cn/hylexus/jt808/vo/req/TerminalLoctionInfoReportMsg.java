package cn.hylexus.jt808.vo.req;

import java.util.Arrays;
import java.util.List;

import cn.hylexus.jt808.vo.PackageData;

/**
 * 终端位置信息汇报
 * 
 * @author cheryl
 *
 */
public class TerminalLoctionInfoReportMsg extends PackageData {

	private TerminalLoctionInfo terminalLoctionInfo;

	
	public TerminalLoctionInfoReportMsg() {
	}
	
	public TerminalLoctionInfoReportMsg(PackageData packageData) {
		this();
		this.channel = packageData.getChannel();
		this.checkSum = packageData.getCheckSum();
		this.msgBodyBytes = packageData.getMsgBodyBytes();
		this.msgHeader = packageData.getMsgHeader();
	}

	public TerminalLoctionInfo getTerminalLoctionInfo() {
		return terminalLoctionInfo;
	}

	public void setTerminalLoctionInfo(TerminalLoctionInfo terminalLoctionInfo) {
		this.terminalLoctionInfo = terminalLoctionInfo;
	}


	@Override
	public String toString() {
		return "TerminalLoctionInfoReportMsg [terminalLoctionInfo=" + terminalLoctionInfo + ", msgHeader=" + msgHeader
				+ ", msgBodyBytes=" + Arrays.toString(msgBodyBytes) + ", checkSum=" + checkSum + ", channel=" + channel
				+ "]";
	}

	
	public static class TerminalLoctionInfo {
		// ------位置基本信息------
		/**
		 * 报警标志（DWORD）
		 */
		protected int warningFlag;
		/**
		 * 状态（DWORD）
		 */
		protected int status;
		/**
		 * 纬度（DWORD）,以度为单位的经度值乘以10的6次方，精确到百万 分之一度
		 */
		protected int latitude;
		/**
		 * 经度（DWORD）,以度为单位的经度值乘以10的6次方，精确到百万 分之一度
		 */
		protected int longitude;
		// 高程 （WORD）,海拔高度，单位为米（m）
		protected int altitude;
        //速度 （WORD）1/10km/h 
		protected int speed;
		//方向  【 0-359，正北为 0，顺时针】 
		protected int course;
		//时间   BCD[6]  【YY-MM-DD-hh-mm-ss（GMT+8 时间】
		protected String time;
		//附加消息
		//里程 DWORD，1/10km，对应车上⾥程表读数 ，附加消息id为0x01
		protected int mileage; 
        
		public int getWarningFlag() {
			return warningFlag;
		}
         
		public void setWarningFlag(int warningFlag) {
			this.warningFlag = warningFlag;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public int getLatitude() {
			return latitude;
		}

		public void setLatitude(int latitude) {
			this.latitude = latitude;
		}

		public int getLongitude() {
			return longitude;
		}

		public void setLongitude(int longitude) {
			this.longitude = longitude;
		}

		public int getAltitude() {
			return altitude;
		}
		
		public void setAltitude(int altitude) {
			this.altitude = altitude;
		}
		
		public int getSpeed() {
			return speed;
		}
		
		public void setSpeed(int speed) {
			this.speed = speed;
		}
		
		public int getCourse() {
			return course;
		}

		public void setCourse(int course) {
			this.course = course;
		}

		public String getTime() {
			return time;
		}
		
		public void setTime(String time) {
			this.time = time;
		}
		
		public int getMileage() {
			return mileage;
		}

		public void setMileage(int mileage) {
			this.mileage = mileage;
		}

		@Override
		public String toString() {
			return "TerminalLoctionInfo [warningFlag=" + warningFlag + ", status=" + status + ", latitude=" + latitude
					+ ", longitude=" + longitude + ", altitude=" + altitude + ", speed=" + speed + ", course="
					+ course + ", time=" + time + ", mileage=" + mileage + "]";
		}
		
	}

}

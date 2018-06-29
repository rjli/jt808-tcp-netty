package cn.hylexus.jt808.service.codec;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hylexus.jt808.common.TPMSConsts;
import cn.hylexus.jt808.util.BCD8421Operater;
import cn.hylexus.jt808.util.BitOperator;
import cn.hylexus.jt808.vo.PackageData;
import cn.hylexus.jt808.vo.PackageData.MsgHeader;
import cn.hylexus.jt808.vo.req.LocationInformationQueryResp;
import cn.hylexus.jt808.vo.req.LocationInformationQueryResp.LocationInformationQueryInfo;
import cn.hylexus.jt808.vo.req.TerminalLoctionInfoReportMsg;
import cn.hylexus.jt808.vo.req.TerminalLoctionInfoReportMsg.TerminalLoctionInfo;
import cn.hylexus.jt808.vo.req.TerminalParamQueryMsgResp;
import cn.hylexus.jt808.vo.req.TerminalParamQueryMsgResp.TerminalParametersqQueryInfo;
import cn.hylexus.jt808.vo.req.TerminalRegisterMsg;
import cn.hylexus.jt808.vo.req.TerminalRegisterMsg.TerminalRegInfo;
import cn.hylexus.jt808.vo.resp.ServerCommonRespMsgBody;
import cn.hylexus.jt808.vo.resp.TerminalCommonRespMsg;
import cn.hylexus.jt808.vo.resp.TerminalCommonRespMsg.TerminalCommonRespInfo;

public class MsgDecoder {

	private static final Logger log = LoggerFactory.getLogger(MsgDecoder.class);

	private BitOperator bitOperator;
	private BCD8421Operater bcd8421Operater;

	public MsgDecoder() {
		this.bitOperator = new BitOperator();
		this.bcd8421Operater = new BCD8421Operater();
	}

	public PackageData bytes2PackageData(byte[] data) {
		PackageData ret = new PackageData();

		// 0. 终端套接字地址信息
		// ret.setChannel(msg.getChannel());

		// 1. 16byte 或 12byte 消息头
		MsgHeader msgHeader = this.parseMsgHeaderFromBytes(data);
		ret.setMsgHeader(msgHeader);

		int msgBodyByteStartIndex = 12;
		// 2. 消息体
		// 有子包信息,消息体起始字节后移四个字节:消息包总数(word(16))+包序号(word(16))
		if (msgHeader.isHasSubPackage()) {
			msgBodyByteStartIndex = 16;
		}

		byte[] tmp = new byte[msgHeader.getMsgBodyLength()];
		System.arraycopy(data, msgBodyByteStartIndex, tmp, 0, tmp.length);
		ret.setMsgBodyBytes(tmp);
		// 3. 去掉分隔符之后，最后一位就是校验码
		// int checkSumInPkg =
		// this.bitOperator.oneByteToInteger(data[data.length - 1]);
		int checkSumInPkg = data[data.length - 1];
		//消息包中的最后以为为校验码所以此处的长度为data.length - 1 ,即只取消息头和消息体的数据
		int calculatedCheckSum = this.bitOperator.getCheckSum4JT808(data, 0, data.length - 1);
		ret.setCheckSum(checkSumInPkg);
		if (checkSumInPkg != calculatedCheckSum) {
			log.warn("检验码不一致,msgid:{},pkg:{},calculated:{}", msgHeader.getMsgId(), checkSumInPkg, calculatedCheckSum);
		}
		return ret;
	}
    /**
     * 解析消息头
     * @param data
     * @return
     */
	private MsgHeader parseMsgHeaderFromBytes(byte[] data) {
		MsgHeader msgHeader = new MsgHeader();

		// 1. 消息ID word(16)
		// byte[] tmp = new byte[2];
		// System.arraycopy(data, 0, tmp, 0, 2);
		// msgHeader.setMsgId(this.bitOperator.twoBytesToInteger(tmp));
		msgHeader.setMsgId(this.parseIntFromBytes(data, 0, 2));

		// 2. 消息体属性 word(16)=================>
		// System.arraycopy(data, 2, tmp, 0, 2);
		// int msgBodyProps = this.bitOperator.twoBytesToInteger(tmp);
		int msgBodyProps = this.parseIntFromBytes(data, 2, 2);
		msgHeader.setMsgBodyPropsField(msgBodyProps);
		// [ 0-9 ] 0000,0011,1111,1111(3FF)(消息体长度)
		msgHeader.setMsgBodyLength(msgBodyProps & 0x1ff);
		// [10-12] 0001,1100,0000,0000(1C00)(加密类型)
		msgHeader.setEncryptionType((msgBodyProps & 0xe00) >> 10);
		// [ 13_ ] 0010,0000,0000,0000(2000)(是否有子包)
		msgHeader.setHasSubPackage(((msgBodyProps & 0x2000) >> 13) == 1);
		// [14-15] 1100,0000,0000,0000(C000)(保留位)
		msgHeader.setReservedBit(((msgBodyProps & 0xc000) >> 14) + "");
		// 消息体属性 word(16)<=================

		// 3. 终端手机号 bcd[6]
		// tmp = new byte[6];
		// System.arraycopy(data, 4, tmp, 0, 6);
		// msgHeader.setTerminalPhone(this.bcd8421Operater.bcd2String(tmp));
		msgHeader.setTerminalPhone(this.parseBcdStringFromBytes(data, 4, 6));

		// 4. 消息流水号 word(16) 按发送顺序从 0 开始循环累加
		// tmp = new byte[2];
		// System.arraycopy(data, 10, tmp, 0, 2);
		// msgHeader.setFlowId(this.bitOperator.twoBytesToInteger(tmp));
		msgHeader.setFlowId(this.parseIntFromBytes(data, 10, 2));

		// 5. 消息包封装项
		// 有子包信息
		if (msgHeader.isHasSubPackage()) {
			// 消息包封装项字段
			msgHeader.setPackageInfoField(this.parseIntFromBytes(data, 12, 4));
			// byte[0-1] 消息包总数(word(16))
			// tmp = new byte[2];
			// System.arraycopy(data, 12, tmp, 0, 2);
			// msgHeader.setTotalSubPackage(this.bitOperator.twoBytesToInteger(tmp));
			msgHeader.setTotalSubPackage(this.parseIntFromBytes(data, 12, 2));

			// byte[2-3] 包序号(word(16)) 从 1 开始
			// tmp = new byte[2];
			// System.arraycopy(data, 14, tmp, 0, 2);
			// msgHeader.setSubPackageSeq(this.bitOperator.twoBytesToInteger(tmp));
			msgHeader.setSubPackageSeq(this.parseIntFromBytes(data, 12, 2));
		}
		return msgHeader;
	}

	protected String parseStringFromBytes(byte[] data, int startIndex, int lenth) {
		return this.parseStringFromBytes(data, startIndex, lenth, null);
	}

	private String parseStringFromBytes(byte[] data, int startIndex, int lenth, String defaultVal) {
		try {
			byte[] tmp = new byte[lenth];
			System.arraycopy(data, startIndex, tmp, 0, lenth);
			return new String(tmp, TPMSConsts.string_charset);
		} catch (Exception e) {
			log.error("解析字符串出错:{}", e.getMessage());
			e.printStackTrace();
			return defaultVal;
		}
	}

	private String parseBcdStringFromBytes(byte[] data, int startIndex, int lenth) {
		return this.parseBcdStringFromBytes(data, startIndex, lenth, null);
	}

	private String parseBcdStringFromBytes(byte[] data, int startIndex, int lenth, String defaultVal) {
		try {
			byte[] tmp = new byte[lenth];
			log.info("拷贝的数组长度：{},实际拷贝长度：{},开始的未知：{}",data.length,lenth,startIndex);
			System.arraycopy(data, startIndex, tmp, 0, lenth);
			return this.bcd8421Operater.bcd2String(tmp);
		} catch (Exception e) {
			log.error("解析BCD(8421码)出错:{}", e.getMessage());
			e.printStackTrace();
			return defaultVal;
		}
	}

	private int parseIntFromBytes(byte[] data, int startIndex, int length) {
		return this.parseIntFromBytes(data, startIndex, length, 0);
	}

	private int parseIntFromBytes(byte[] data, int startIndex, int length, int defaultVal) {
		try {
			// 字节数大于4,从起始索引开始向后处理4个字节,其余超出部分丢弃
			final int len = length > 4 ? 4 : length;
			byte[] tmp = new byte[len];
			System.arraycopy(data, startIndex, tmp, 0, len);
			return bitOperator.byteToInteger(tmp);
		} catch (Exception e) {
			log.error("解析整数出错:{}", e.getMessage());
			e.printStackTrace();
			return defaultVal;
		}
	}
    /**
     * 解析终端注册信息
     * @param packageData
     * @return
     */
	public TerminalRegisterMsg toTerminalRegisterMsg(PackageData packageData) {
		TerminalRegisterMsg ret = new TerminalRegisterMsg(packageData);
		byte[] data = ret.getMsgBodyBytes();

		TerminalRegInfo body = new TerminalRegInfo();

		// 1. byte[0-1] 省域ID(WORD)
		// 设备安装车辆所在的省域，省域ID采用GB/T2260中规定的行政区划代码6位中前两位
		// 0保留，由平台取默认值
		body.setProvinceId(this.parseIntFromBytes(data, 0, 2));

		// 2. byte[2-3] 设备安装车辆所在的市域或县域,市县域ID采用GB/T2260中规定的行 政区划代码6位中后四位
		// 0保留，由平台取默认值
		body.setCityId(this.parseIntFromBytes(data, 2, 2));

		// 3. byte[4-8] 制造商ID(BYTE[5]) 5 个字节，终端制造商编码
		// byte[] tmp = new byte[5];
		body.setManufacturerId(this.parseStringFromBytes(data, 4, 5));

		// 4. byte[9-16] 终端型号(BYTE[8]) 八个字节， 此终端型号 由制造商自行定义 位数不足八位的，补空格。
		body.setTerminalType(this.parseStringFromBytes(data, 9, 8));

		// 5. byte[17-23] 终端ID(BYTE[7]) 七个字节， 由大写字母 和数字组成， 此终端 ID由制造 商自行定义
		body.setTerminalId(this.parseStringFromBytes(data, 17, 7));

		// 6. byte[24] 车牌颜色(BYTE) 车牌颜 色按照JT/T415-2006 中5.4.12 的规定
		body.setLicensePlateColor(this.parseIntFromBytes(data, 24, 1));

		// 7. byte[25-x] 车牌(STRING) 公安交 通管理部门颁 发的机动车号牌
		body.setLicensePlate(this.parseStringFromBytes(data, 25, data.length - 25));

		ret.setTerminalRegInfo(body);
		return ret;
	}
	
	/**
	 * 解析位置信息汇报
	 * @param packageData
	 * @return
	 */
	public TerminalLoctionInfoReportMsg toTerminalLoctionInfoReportMsg(PackageData packageData) {
		TerminalLoctionInfoReportMsg ret = new TerminalLoctionInfoReportMsg(packageData);
		byte[] data = ret.getMsgBodyBytes();
		TerminalLoctionInfo body = new TerminalLoctionInfo();

		// 1. byte[0-3]报警标志 (DWORD)
		body.setWarningFlag(this.parseIntFromBytes(data, 0, 4));

		// 2. byte[4-7] 状态
		body.setStatus(this.parseIntFromBytes(data, 4, 4));

		// 3. byte[8-11] 纬度 以度为单位的纬度值乘以 10 的 6 次方，精确到百万分之一度
		int latitude =this.parseIntFromBytes(data, 8, 4);
		body.setLatitude(latitude);

		// 4. byte[12-15] 经度 以度为单位的经度值乘以 10 的 6 次方，精确到百万分之一度 
		int longitude =this.parseIntFromBytes(data, 12, 4);
		body.setLongitude(longitude);
        
		// 5. byte[16-17] 高程   海拔高度，单位为米（m） 
		body.setAltitude(this.parseIntFromBytes(data, 16, 2));
       
		// 6. byte[18-19] 速度    1/10km/h 
		body.setSpeed(this.parseIntFromBytes(data, 18, 2));

		// 7. byte[20-21] 方向   0-359，正北为 0，顺时针 
		body.setCourse(this.parseIntFromBytes(data, 20, 2));
        
		// 8. byte[22-27] YY-MM-DD-hh-mm-ss（GMT+8 时间） 
		body.setTime(this.parseBcdStringFromBytes(data, 22, 6));
		System.out.println(Arrays.toString(data));
		//9. 附加消息
		int attachPosStart = 28;
		if(data.length > attachPosStart) { //当消息长度大于27的时候，即表示还有附加消息的时候
			log.info("报文中含有附加信息，数组的长度:"+data.length);
			int count =0;
			while(attachPosStart<data.length) {
				attachPosStart = generateLocationAdditionalMsg(data, body,attachPosStart);
				count++;
			}
			log.info("一共包含："+count+"条附加信息");
		}
		ret.setTerminalLoctionInfo(body);
		return ret;
	}
	
    /**
     * 生成位置汇报信息的附加信息
     * @param data
     * @param body
     */
	private int generateLocationAdditionalMsg(byte[] data, TerminalLoctionInfo body, int tempPosStart) {
		int msgId = this.parseIntFromBytes(data, tempPosStart, 1);
		int msgLength = this.parseIntFromBytes(data, ++tempPosStart, 1);
		if(0x01 == msgId) {
			body.setMileage(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else if(0xEB == msgId){
			++tempPosStart;
			log.info("自定义消息不做处理");
		}
		int nextPos = tempPosStart+msgLength;
		return nextPos;
	}
	
	/**
	 * 解析查询终端参数应答
	 * @param packageData
	 * @return
	 */
	public TerminalParamQueryMsgResp toTerminalParamQueryMsgResp(PackageData packageData) {
		TerminalParamQueryMsgResp ret = new TerminalParamQueryMsgResp(packageData);
		byte[] data = ret.getMsgBodyBytes();
		TerminalParametersqQueryInfo body = new TerminalParametersqQueryInfo();
        System.out.println(Arrays.toString(data));
		// 1. byte[0-1]应答流⽔号 (WORD)
		body.setReplyFlowId(this.parseIntFromBytes(data, 0, 2));
        // 2. byte[2] 应答参数个数 
		int paramItemCount = this.parseIntFromBytes(data, 2, 1);
		body.setCount(paramItemCount);
		//参数项列表 从第三个字节开始
		int attachPosStart = 3;
		if(data.length > attachPosStart) { //当消息长度大于3的时候，即表示开始读取参数项列表
			int count =0;
			while(attachPosStart<data.length) {
				attachPosStart = generateParamQueryMsgResp(data, body,attachPosStart);
				count++;
			}
			log.info("一共包含："+count+"条参数项信息");
		}
		
		ret.setTerminalParametersqQueryInfo(body);
		return ret;
	}
	
	/**
	 * 解析位置查询应答
	 * @param packageData
	 * @return
	 */
	public LocationInformationQueryResp toLocationInformationQueryResp(PackageData packageData) {
		LocationInformationQueryResp ret = new LocationInformationQueryResp(packageData);
		byte[] data = ret.getMsgBodyBytes();
		LocationInformationQueryInfo body = new LocationInformationQueryInfo();
		
        System.out.println(Arrays.toString(data));
		// 1. byte[0-1]应答流⽔号 (WORD)
		body.setReplyFlowId(this.parseIntFromBytes(data, 0, 2));
		// 2.位置信息汇报 从第二个字节开始
		
		// 2.1 byte[0-3]报警标志 (DWORD)
		body.setWarningFlag(this.parseIntFromBytes(data, 2, 4));
		// 2.2 byte[4-7] 状态
		body.setStatus(this.parseIntFromBytes(data, 6, 4));
		// 2.3 byte[8-11] 纬度 以度为单位的纬度值乘以 10 的 6 次方，精确到百万分之一度
		int latitude =this.parseIntFromBytes(data, 10, 4);
		body.setLatitude(latitude);
		// 2.4 byte[12-15] 经度 以度为单位的经度值乘以 10 的 6 次方，精确到百万分之一度 
		int longitude =this.parseIntFromBytes(data, 14, 4);
		body.setLongitude(longitude);
		// 2.5 byte[16-17] 高程   海拔高度，单位为米（m） 
		body.setAltitude(this.parseIntFromBytes(data, 18, 2));
		// 2.6 byte[18-19] 速度    1/10km/h 
		body.setSpeed(this.parseIntFromBytes(data, 20, 2));
		// 2.7 byte[20-21] 方向   0-359，正北为 0，顺时针 
		body.setCourse(this.parseIntFromBytes(data, 22, 2));
		// 2.8 byte[22-27] YY-MM-DD-hh-mm-ss（GMT+8 时间） 
		body.setTime(this.parseBcdStringFromBytes(data, 24, 6));
		
		//2.9 附加消息
		int attachPosStart = 30;
		if(data.length > attachPosStart) { //当消息长度大于27的时候，即表示还有附加消息的时候
			log.info("报文中含有附加信息，数组的长度:"+data.length);
			int count =0;
			while(attachPosStart<data.length) {
				attachPosStart = generateLocationAdditionalMsg(data, body,attachPosStart);
				count++;
			}
			log.info("一共包含："+count+"条附加信息");
		}
		ret.setLocationInformationQueryInfo(body);
		return ret;
	}
	
	 /**
     *  解析查询终端参数应答
     * @param data
     * @param body
     */
	private int generateParamQueryMsgResp(byte[] data, TerminalParametersqQueryInfo body, int tempPosStart) {
		//参数id DWORD 
		int msgId = this.parseIntFromBytes(data, tempPosStart, 4);
		//参数长度 参数长度
		tempPosStart= tempPosStart+4;
		int msgLength = this.parseIntFromBytes(data, tempPosStart, 1);
		if(0x0001 == msgId) {
//			终端⼼跳发送间隔，单位为（s）
			body.setHeartbeatInterval(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0013 == msgId) {
//			主服务器地址,IP或域名
			body.setServerAddress(this.parseStringFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0018 == msgId) {
//			服务器TCP端口
			body.setServerPort(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0027 == msgId) {
//			休眠时汇报时间间隔，单位为秒（s），>0
			body.setSleepTimeReportInteval(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0029 == msgId) {
//			缺省时间汇报间隔，单位为秒（s），>0
			body.setDefaultTimeReportInteval(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0055 == msgId) {
//			最高速度，单位为千⽶每⼩时（km/h）
			body.setMaxSpeed(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0056 == msgId) {
//			超速持续时间，单位为秒（s）
			body.setOverspeedDuration(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0080 == msgId) {
//			车辆⾥程表读数，1/10km
			body.setMileage(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0081 == msgId) {
//			车辆所在的省域ID，1～255
			body.setProvinceId(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0082 == msgId) {
//			车辆所在的市域ID，1～255
			body.setCityId(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0083 == msgId) {
//			公安交通同管理部门颁发的机动车号牌
			body.setLicensePlate(this.parseStringFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0084 == msgId) {
//			车牌颜色，按照JT/T415—2006中5.4.12的规定
			body.setLicensePlateColor(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0002  == msgId) {
//			TCP 消息应答超时时间，单位为秒（s）  
			body.setLocationReportingStrategy(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0003  == msgId) {
//			TCP 消息重传次数 
			body.setLocationReportingStrategy(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else if(0x0020  == msgId) {
//			位置汇报策略 ，0：定时汇报；1：定距汇报；2：定时和定距汇报 
			body.setLocationReportingStrategy(this.parseIntFromBytes(data, ++tempPosStart, msgLength));
		}else{
			++tempPosStart;
			log.info("消息暂时不解析:"+msgId);
		}
		int nextPos = tempPosStart+msgLength;
		return nextPos;
	}
	
	/**
	 *  解析终端通用应答消息
	 * @param packageData
	 * @return
	 */
	public TerminalCommonRespMsg toTerminalCommonRespMsg(PackageData packageData) {
		TerminalCommonRespMsg ret = new TerminalCommonRespMsg(packageData);
		byte[] data = ret.getMsgBodyBytes();
		TerminalCommonRespInfo body = new TerminalCommonRespInfo();
        //data[0-1] 应答流水号 WORD 对应的平台消息的流水号 
		body.setReplyFlowId(this.parseIntFromBytes(data, 0, 2));
		//data[2-3] 应答 ID WORD 对应的平台消息的 ID 
		body.setReplyId(this.parseIntFromBytes(data, 2, 2));		
		//data[4] 结果 BYTE 0：成功/确认；1：失败；2：消息有误；3：不支持 
		body.setReplyCode(data[4]);
		ret.setTerminalCommonRespInfo(body);
		return ret;
	}
}

package cn.hylexus.jt808.service;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rksp.util.DateUtil;
import com.rksp.util.GsonUtil;
import com.rksp.util.StringUtil;
import com.sharetime.gps.pojo.Gps实时轨迹;
import com.sharetime.gps.pojo.Gps报文;
import com.sharetime.gps.pojo.Gps指令集;
import com.sharetime.gps.pojo.Gps终端设备;
import com.sharetime.gps.pojo.Gps终端设备掉线记录;
import com.sharetime.gps.pojo.Gps车辆;
import com.sharetime.gps.vo.GpsBaseCdt;
import com.sharetime.gps.vo.TerminalParamSettingVo;

import cn.hylexus.jt808.common.GpsServerConsts;
import cn.hylexus.jt808.common.TPMSConsts;
import cn.hylexus.jt808.util.BaseUtil;
import cn.hylexus.jt808.util.BitOperator;
import cn.hylexus.jt808.util.HexStringUtils;
import cn.hylexus.jt808.vo.PackageData;
import cn.hylexus.jt808.vo.PackageData.MsgHeader;
import cn.hylexus.jt808.vo.req.LocationInformationQueryResp;
import cn.hylexus.jt808.vo.req.TerminalLoctionInfoReportMsg;
import cn.hylexus.jt808.vo.req.TerminalLoctionInfoReportMsg.TerminalLoctionInfo;
import cn.hylexus.jt808.vo.req.TerminalParamQueryMsgResp;
import cn.hylexus.jt808.vo.req.TerminalParamQueryMsgResp.TerminalParametersqQueryInfo;
import cn.hylexus.jt808.vo.req.TerminalRegisterMsg;
import cn.hylexus.jt808.vo.req.TerminalRegisterMsg.TerminalRegInfo;
import cn.hylexus.jt808.vo.resp.TerminalRegisterMsgRespBody;
import cn.hylexus.jt808.vo.resp.TerminalCommonRespMsg.TerminalCommonRespInfo;
import rk.stub.gps.GpsBaseSvc;
import rk.stub.gps.GpsBaseSvcStub;
import rk.stub.gps.GpsBusinessSvc;
import rk.stub.gps.GpsBusinessSvcStub;

/**
 * 把接受到的报文信息转换为 DPlatform_GPS系统中对应的实体类
 * 
 * @author cheryl
 *
 */
public class MsgToDPlatformGpsPojoService {
	private final static Logger logger = LoggerFactory.getLogger(MsgToDPlatformGpsPojoService.class);
    
	private static  GpsBaseSvc gpsBaseSvc;
	private static GpsBusinessSvc gpsBusinessSvc;
	
	static {
		 try {
			gpsBaseSvc = new GpsBaseSvcStub(GpsServerConsts.gps_server_base);
			gpsBusinessSvc = new GpsBusinessSvcStub(GpsServerConsts.gps_server_business);
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("服务连接失败");
		}
	}
	/**
	 * 创建终端设备 TerminalRegisterMsg 终端注册信息 authCode 生成的鉴权码
	 * 
	 * @return
	 */
	public String createTerminalDevice(TerminalRegisterMsg msg) {
		GpsBaseCdt cdt = new GpsBaseCdt();
		TerminalRegInfo terminalRegInfo = msg.getTerminalRegInfo();
		String terminalPhone = msg.getMsgHeader().getTerminalPhone();
		String authCode = null;// 鉴权码
		boolean flag ; //操作成功标识符
		try {
			// 1、获取终端
			Gps终端设备 terminal = gpsBaseSvc.getTheTerminalByPhone(terminalPhone);
			cdt = new GpsBaseCdt();
			cdt.setPlatenumber(terminalRegInfo.getLicensePlate());
			// 2、获取车辆
			List<Gps车辆> vehicleList = gpsBaseSvc.getVehicles(cdt);
			// 3、判断终端是否存在
			if (terminal == null) {
				logger.error("终端没有找到");
				return new String(new byte[] { TerminalRegisterMsgRespBody.terminal_not_found });
			}
			// 4、判断车辆是否存在
			if (vehicleList == null || vehicleList.size() <= 0) {
				logger.error("没有找到对应的车辆");
				return new String(new byte[] { TerminalRegisterMsgRespBody.car_not_found });
			}
			// 5、判断车辆是否绑定过终端
			for (Gps车辆  tempvehicle : vehicleList) {
				if (!StringUtil.isEmpty(tempvehicle.get设备序列号())) {
					if (!tempvehicle.get设备序列号().equals(terminalRegInfo.getTerminalId().trim())) {
						logger.error("车辆已经被注册");
						return new String(new byte[] { TerminalRegisterMsgRespBody.car_already_registered },
								"UTF-8");
					} else {
						logger.info("车辆与终端已经绑定成功，无需重复绑定");
						if (terminal.get设备序列号().equals(tempvehicle.get设备序列号())) {
							authCode = terminal.get鉴权码();
							break;
						}
					}
					if (!StringUtil.isEmpty(authCode)) {
						return authCode;
					} else {
						return new String(new byte[] { TerminalRegisterMsgRespBody.car_already_registered },
								"UTF-8");
					}
				}

			}
			
			// 6、判断终端是否 绑定过车辆 (终端参数设置当设置了车牌号的时候会发送终端注册的报文，从而更新终端和车辆的绑定关系)
			Gps车辆 vehicle = terminal.getVehicle();
			List<Gps车辆> vehicles = new ArrayList<Gps车辆>();
			if (vehicle != null) {
				if (terminalRegInfo.getLicensePlate().equals(vehicle.get车牌号())) {
					authCode = terminal.get鉴权码();
				} else {
					vehicle.set设备序列号(null);
					vehicle.set设备id(null);
					vehicles.add(vehicle);
					for (Gps车辆  tempvehicle : vehicleList) { //如果找到报文中车牌号一样的车辆则进行绑定
						if (tempvehicle.get车牌号().equals(terminalRegInfo.getLicensePlate().trim())) {
							tempvehicle.set设备id(terminal.getOid());
							tempvehicle.set设备序列号(terminal.get设备序列号());
							vehicles.add(tempvehicle);
							flag = gpsBaseSvc.updateVehicles(vehicles);
							if(flag) {
								return terminal.get鉴权码();
							}else {
								logger.info("车辆信息更新失败！");
								return "5";
							}
						}
					}
				}
			}

			// 7、 终端以及对应车牌号的车辆还都没有进行绑定
			if (StringUtil.isEmpty(terminal.get制造商ID())) {
				terminal.set制造商ID(terminalRegInfo.getManufacturerId().trim().trim());
			}
			if (StringUtil.isEmpty(terminal.get设备型号())) {
				terminal.set设备型号(terminalRegInfo.getTerminalType().trim());
			}
			//平台新增的时候已经写好
//				terminal.set设备序列号(terminalRegInfo.getTerminalId().trim());
			// 鉴权码,由终端的手机号以及当前时间的毛秒之生成；
			authCode = BaseUtil.generateAuthCode(terminalPhone);
			terminal.set鉴权码(authCode);
			terminal.set设备状态("在线");
			terminal.set设备使用状态("使用");
			vehicle = vehicleList.get(0);
			vehicle.set设备id(terminal.getOid());
//				vehicle.set设备序列号(terminalRegInfo.getTerminalId().trim());
			vehicle.set设备序列号(terminal.get设备序列号());
			flag = gpsBaseSvc.updateTerminalAndVehicle(vehicle, terminal);
			if (flag) {
				return authCode;
			} else {
				logger.info("车辆和终端新更新失败！");
				return "5";
			}

		} catch (RemoteException e) {
			logger.error(GpsServerConsts.gps_server_base + "连接失败！");
			e.printStackTrace();
			// 返回非正常的响应值
			return "5";
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.error("byte[]转换成String异常");
			e.printStackTrace();
			return "5";
		}

	}

	/**
	 * 创建实时轨迹---位置汇报 TerminalLoctionInfoReportMsg 位置汇报信息
	 * 
	 * @return
	 */
	public byte creatLocus(TerminalLoctionInfoReportMsg msg) {
		String oid = null;
		TerminalLoctionInfo terminalLoctionInfo = msg.getTerminalLoctionInfo();
		String phone = msg.getMsgHeader().getTerminalPhone();
		try {
			GpsBaseCdt cdt = new GpsBaseCdt();
			cdt.setSimcn(phone);
			List<Gps终端设备> terminalDeviceList = gpsBaseSvc.getTerminals(cdt);
			if (terminalDeviceList == null || terminalDeviceList.size() <= 0) {
				logger.error("终端没有找到");
			} else {
				Gps实时轨迹 trajectory = setEntity(terminalDeviceList, terminalLoctionInfo, phone);
				oid = gpsBusinessSvc.createTheTrajectory(trajectory);// 添加到数据库
			}

			if (!StringUtil.isEmpty(oid)) {
				return 0;
			} else {
				return 1;
			}

		} catch (RemoteException e) {
			logger.error(GpsServerConsts.gps_server_business + "连接失败！");
			e.printStackTrace();
			return 1;
		}
	}

	/**
	 * 根据手机号查询终端的鉴权码,比较两者是否一致
	 * 
	 * @param terminalPhone
	 * @param authCode
	 * @return
	 */
	public boolean getTheReplyCodeByTerminalPhone(String terminalPhone, String authCode) {
		try {
			GpsBaseCdt cdt = new GpsBaseCdt();
			cdt.setSimcn(terminalPhone);
			List<Gps终端设备> terminalDeviceList = gpsBaseSvc.getTerminals(cdt);
			if (terminalDeviceList != null && terminalDeviceList.size() > 0) {
				Gps终端设备 pojo = terminalDeviceList.get(0);
				String code = pojo.get鉴权码();
				if (authCode.equals(code)) {// 鉴权码一致返回true，否则返回false
					if (pojo.get设备状态().equals("离线")) {
						pojo.set设备状态("在线");
						pojo.set设备使用状态("使用");
						gpsBaseSvc.updateTheTerminal(pojo);
					}
					return true;
				}
			}
			return false;
		} catch (RemoteException e) {
			logger.error(GpsServerConsts.gps_server_business + "axis请求失败！");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 保存终端通用应答消息
	 * 
	 * @param reqHeader
	 * @return
	 */
	public boolean updateGpsDirective(String terminalPhone, TerminalCommonRespInfo info) {
		boolean flag = false;
		try {
			GpsBaseCdt cdt = new GpsBaseCdt();
			cdt.setMessageId(info.getReplyId());
			cdt.setSimcn(terminalPhone);
			cdt.setFlowId(info.getReplyFlowId());
			List<Gps指令集> directiveList = (List<Gps指令集>) gpsBaseSvc.getDirectives(cdt);
			if (directiveList != null && directiveList.size() > 0) {
				Gps指令集 directive = directiveList.get(0);
				directive.set回执时间(DateUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss"));
				if (info.getReplyCode() == 0) {
					directive.set消息回执("成功");
					flag = updateRelativePojo(gpsBaseSvc, directive);
				} else if (info.getReplyCode() == 1) {
					directive.set消息回执("失败");
				} else if (info.getReplyCode() == 2) {
					directive.set消息回执("消息有误");
				} else if (info.getReplyCode() == 3) {
					directive.set消息回执("不支持");
				}
				flag = gpsBaseSvc.updateTheDirective(directive);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 接受到终端通用应答的成功报文后，执行数据库pojo类的更新操作
	 * 
	 * @param gpsBaseSvc
	 * @param directive
	 * @throws RemoteException
	 */
	private boolean updateRelativePojo(GpsBaseSvc gpsBaseSvc, Gps指令集 directive) throws RemoteException {
		boolean flag = false;
		if (directive.get消息id() == TPMSConsts.cmd_terminal_param_settings) {
			if (StringUtil.isEmpty(directive.get消息内容())) {
				return flag;
			}
			TerminalParamSettingVo terminalParamSettingVo = (TerminalParamSettingVo) GsonUtil
					.getObject(directive.get消息内容(), TerminalParamSettingVo.class);
			if (terminalParamSettingVo.getType().equals("terminal")) {
				Gps终端设备 pojo = gpsBaseSvc.getTheTerminal(directive.get关联id());
				List<Gps车辆> vehicles = new ArrayList<Gps车辆>();
				if (!StringUtil.isEmpty(terminalParamSettingVo.getHeartbeatInterval())) {
					pojo.set终端心跳发送间隔(terminalParamSettingVo.getHeartbeatInterval());
				}
				if (!StringUtil.isEmpty(terminalParamSettingVo.getServerAddress())) {
					pojo.set主服务器地址(terminalParamSettingVo.getServerAddress());
				}
				if (!StringUtil.isEmpty(terminalParamSettingVo.getServerPort())) {
					pojo.set服务器TCP端口(terminalParamSettingVo.getServerPort());
				}
				if (!StringUtil.isEmpty(terminalParamSettingVo.getDefaultTimeReportInteval())) {
					pojo.set缺省时间汇报间隔(terminalParamSettingVo.getDefaultTimeReportInteval());
				}
				if (!StringUtil.isEmpty(terminalParamSettingVo.getSleepTimeReportInteval())) {
					pojo.set休眠时汇报时间间隔(terminalParamSettingVo.getSleepTimeReportInteval());
				}
				if (!StringUtil.isEmpty(terminalParamSettingVo.getLicensePlate())) {
					String licensePlate = terminalParamSettingVo.getLicensePlate().trim();
					Gps车辆 bingingVehicle = pojo.getVehicle();
					logger.error("绑定的车辆信息");
					logger.error("原车车辆信息：" + GsonUtil.getJSONString(bingingVehicle));
					if (bingingVehicle != null) {
						// if(!licensePlate.equals(bingingVehicle.get车牌号())) {
						bingingVehicle.set设备id(null);
						bingingVehicle.set设备序列号(null);
						logger.error("新车车辆信息：" + GsonUtil.getJSONString(bingingVehicle));
						vehicles.add(bingingVehicle);
						// }

					}
					GpsBaseCdt cdt = new GpsBaseCdt();
					cdt.setPlatenumber(licensePlate);
					List<Gps车辆> tempVehicles = gpsBaseSvc.getVehicles(cdt);
					if (vehicles != null && vehicles.size() > 0) {
						Gps车辆 vehicle = vehicles.get(0);
						vehicle.set设备id(pojo.getOid());
						vehicle.set设备序列号(pojo.get设备序列号());
						vehicles.add(vehicle);
					}
				}
				if (vehicles != null && vehicles.size() > 0) {
					logger.error("更新车辆" + vehicles.size());
					flag = gpsBaseSvc.updateVehicles(vehicles);
				} else {
					flag = gpsBaseSvc.updateTheTerminal(pojo);
				}
			} else {
				// Gps车辆 pojo = gpsBaseSvc.getTheVehicle(directive.get关联id());
				// if(!StringUtil.isEmpty(terminalParamSettingVo.getMaxSpeed())) {
				// pojo.set速度阈值(terminalParamSettingVo.getMaxSpeed());
				// }
				// if(!StringUtil.isEmpty(terminalParamSettingVo.getOverspeedDuration())) {
				// pojo.set超速持续时间(terminalParamSettingVo.getOverspeedDuration());
				// }
				// if(!StringUtil.isEmpty(terminalParamSettingVo.getLicensePlate())) {
				// pojo.set车牌号(terminalParamSettingVo.getLicensePlate());
				// }
				// if(!StringUtil.isEmpty(terminalParamSettingVo.getLicensePlateColor())) {
				// pojo.set车牌颜色(terminalParamSettingVo.getLicensePlateColor());
				// }
				// flag = gpsBaseSvc.updateTheVehicle(pojo);
			}
		}
		return flag;
	}

	/**
	 * 创建终端设备掉线记录
	 * 
	 * @return
	 */
	public String creatTerminalFacilityDropsRecord(String phone) {
		try {
			GpsBaseCdt cdt = new GpsBaseCdt();
			cdt.setSimcn(phone);
			List<Gps终端设备> terminalDeviceList = gpsBaseSvc.getTerminals(cdt);
			if (terminalDeviceList == null || terminalDeviceList.size() <= 0) {
				logger.error("终端没有找到");
			} else {
				// 设备掉线后修改终端设备状态为离线
				Gps终端设备 terminalDevice = terminalDeviceList.get(0);
				terminalDevice.set设备状态("离线");
				terminalDevice.set设备使用状态("闲置");
				gpsBaseSvc.updateTheTerminal(terminalDevice);
				// 终端设备掉线记录数据库表插入数据
				Gps终端设备掉线记录 dropsRecord = new Gps终端设备掉线记录();
				dropsRecord.setSim卡号(phone);
				dropsRecord.set掉线时间(new Date());
				dropsRecord.set设备id(terminalDevice.getOid());
				dropsRecord.set设备序列号(terminalDevice.get设备序列号());
				gpsBaseSvc.createTerminalFacilityDropsRecord(dropsRecord);// 添加到数据库
			}

		} catch (RemoteException e) {
			logger.error(GpsServerConsts.gps_server_business + "连接失败！");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 创建实时轨迹---位置信息查询应答 LocationInformationQueryResp 位置信息查询应答
	 * 
	 * @return
	 */
	public byte creatLocus(LocationInformationQueryResp msg) {
		try {
			String oid = null;
			TerminalLoctionInfo terminalLoctionInfo = msg.getLocationInformationQueryInfo();
			String phone = msg.getMsgHeader().getTerminalPhone();
			GpsBaseCdt cdt = new GpsBaseCdt();
			// cdt.setMessageId(TPMSConsts.cmd_position_query_resp);
			// cdt.setSimcn(phone);
			// cdt.setFlowId(msg.getLocationInformationQueryInfo().getReplyFlowId());
			// List<Gps指令集> directiveList =(List<Gps指令集>) gpsBaseSvc.getDirectives(cdt);
			// if(directiveList !=null && directiveList.size() >0) {
			// Gps指令集 directive = directiveList.get(0);
			// directive.set回执时间(DateUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss"));
			// directive.set消息回执("成功");
			// boolean flag= gpsBaseSvc.updateTheDirective(directive);
			// if(!flag) {
			// logger.error("终端参数查询指令回执更新失败");
			// }
			// }
			// cdt = new GpsBaseCdt();
			cdt.setSimcn(phone);
			List<Gps终端设备> terminalDeviceList = gpsBaseSvc.getTerminals(cdt);
			if (terminalDeviceList == null || terminalDeviceList.size() <= 0) {
				logger.error("终端没有找到");
			} else {
				Gps实时轨迹 trajectory = setEntity(terminalDeviceList, terminalLoctionInfo, phone);
				oid = gpsBusinessSvc.createTheTrajectory(trajectory);// 添加到数据库
			}

			if (!StringUtil.isEmpty(oid)) {
				return 0;
			} else {
				return 1;
			}
		} catch (RemoteException e) {
			logger.error(GpsServerConsts.gps_server_business + "连接失败！");
			e.printStackTrace();
			return 1;
		}
	}

	/**
	 * 实体类字段进行设置
	 * 
	 * @return
	 */
	public Gps实时轨迹 setEntity(List<Gps终端设备> terminalDeviceList, TerminalLoctionInfo terminalLoctionInfo, String phone)
			throws AxisFault {
		Gps实时轨迹 trajectory = new Gps实时轨迹();
		Gps终端设备 terminalDevice = terminalDeviceList.get(0);
		trajectory.set设备id(terminalDevice.getOid());
		trajectory.set设备序列号(terminalDevice.get设备序列号());
		trajectory.setSim卡号(phone);
		trajectory.set报警标志(String.valueOf(terminalLoctionInfo.getWarningFlag()));
		trajectory.set状态(String.valueOf(terminalLoctionInfo.getStatus()));
		trajectory.set高程(terminalLoctionInfo.getAltitude());
		trajectory.set速度(terminalLoctionInfo.getSpeed());
		trajectory.set方向(terminalLoctionInfo.getCourse());
		int longitude = terminalLoctionInfo.getLongitude();
		int latitude = terminalLoctionInfo.getLatitude();
		trajectory.set经度(longitude);
		trajectory.set纬度(latitude);
		if (longitude != 0 && latitude != 0) {
			double lng = longitude / Math.pow(10, 6);
			double lan = latitude / Math.pow(10, 6);
			String coords = lng + "," + lan;
			String message = BaseUtil.getRealJWD(coords, "1", "5");
			if (!message.equals("error")) {
				String[] arr = message.split(",");
				trajectory.set纠偏经度(Double.valueOf(arr[0]));
				trajectory.set纠偏纬度(Double.valueOf(arr[1]));
			}
		}
        
		if (!StringUtil.isEmpty(String.valueOf(terminalLoctionInfo.getMileage()))) {
			trajectory.set里程(String.valueOf(terminalLoctionInfo.getMileage()));
		}

		// 获取到的字符串日期格式化
		String time = terminalLoctionInfo.getTime();
		Date date = DateUtil.string2Date(time, "yyMMddHHmmss");
		// SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// String str=sdf.format(date);
		trajectory.set上报时间(date);
		trajectory.set系统时间(DateUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss"));
		return trajectory;
	}

	/**
	 * 
	 * @param terminalPhone
	 * @return
	 */
	public boolean changeTerminalLastHeatBeart(String terminalPhone) {
		boolean flag = false;
		try {
			GpsBaseCdt cdt = new GpsBaseCdt();
			cdt.setSimcn(terminalPhone);
			List<Gps终端设备> terminalDeviceList = gpsBaseSvc.getTerminals(cdt);
			if (terminalDeviceList == null || terminalDeviceList.size() <= 0) {
				logger.error("终端没有找到");
			} else {
				Gps终端设备 pojo = terminalDeviceList.get(0);
				pojo.set最后心跳时间(new Date());
				flag = gpsBaseSvc.updateTheTerminal(pojo);
			}
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return flag;
	}

	/**
	 * 清除终端和车辆的绑定关系
	 */
	public boolean clearReplationBetweenVehicleAndTerminal(String terminalPhone) {
		boolean flag = false;
		try {
			Gps终端设备 terminalDevice = gpsBaseSvc.getTheTerminalByPhone(terminalPhone);
			if (terminalDevice == null) {
				logger.error("终端没有找到");
			} else {
				terminalDevice.set鉴权码(null);
				Gps车辆 vehicle = terminalDevice.getVehicle();
				if (vehicle != null) {
					vehicle.set设备id(null);
					vehicle.set设备序列号(null);
				}
				flag = gpsBaseSvc.updateTerminalAndVehicle(vehicle, terminalDevice);
			}
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 更新终端的参数相关的信息
	 * 
	 * @param msg
	 * @return
	 */
	public boolean updateTerminalInfo(TerminalParamQueryMsgResp msg) {
		boolean flag = false;
		try {
			GpsBaseCdt cdt = new GpsBaseCdt();
			// cdt.setMessageId(TPMSConsts.cmd_terminal_param_query);
			// cdt.setSimcn(msg.getMsgHeader().getTerminalPhone());
			// cdt.setFlowId(msg.getTerminalParametersqQueryInfo().getReplyFlowId());
			// List<Gps指令集> directiveList =(List<Gps指令集>) gpsBaseSvc.getDirectives(cdt);
			// if(directiveList !=null && directiveList.size() >0) {
			// Gps指令集 directive = directiveList.get(0);
			// directive.set回执时间(DateUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss"));
			// directive.set消息回执("成功");
			// flag= gpsBaseSvc.updateTheDirective(directive);
			// if(!flag) {
			// logger.error("终端参数查询指令回执更新失败");
			// }
			// }
			// cdt = new GpsBaseCdt();
			cdt.setSimcn(msg.getMsgHeader().getTerminalPhone());
			List<Gps终端设备> terminalDeviceList = gpsBaseSvc.getTerminals(cdt);
			if (terminalDeviceList == null || terminalDeviceList.size() <= 0) {
				logger.error("终端没有找到");
			} else {
				Gps终端设备 pojo = terminalDeviceList.get(0);
				TerminalParametersqQueryInfo info = msg.getTerminalParametersqQueryInfo();
				String heartbeatInterval = String.valueOf(info.getHeartbeatInterval());
				String serverAddress = String.valueOf(info.getServerAddress());
				String serverPort = String.valueOf(info.getServerPort());
				String sleepTimeReportInteval = String.valueOf(info.getSleepTimeReportInteval());
				String defaultTimeReportInteval = String.valueOf(info.getDefaultTimeReportInteval());
				if (!StringUtil.isEmpty(heartbeatInterval)) {
					pojo.set终端心跳发送间隔(heartbeatInterval);
				}
				if (!StringUtil.isEmpty(serverAddress)) {
					pojo.set主服务器地址(serverAddress);
				}
				if (!StringUtil.isEmpty(serverPort)) {
					pojo.set服务器TCP端口(serverPort);
				}
				if (!StringUtil.isEmpty(sleepTimeReportInteval)) {
					pojo.set休眠时汇报时间间隔(sleepTimeReportInteval);
				}
				if (!StringUtil.isEmpty(defaultTimeReportInteval)) {
					pojo.set缺省时间汇报间隔(defaultTimeReportInteval);
				}
				// Gps车辆 vehicle = pojo.getVehicle();
				// if(vehicle!=null) {
				// String maxSpeed = String.valueOf(info.getMaxSpeed());
				// if(!StringUtil.isEmpty(maxSpeed)) {
				// vehicle.set速度阈值(maxSpeed);
				// }
				// String overspeedDuration = String.valueOf(info.getOverspeedDuration());
				//// if(!StringUtil.isEmpty(overspeedDuration)) {
				//// vehicle.set超速持续时间(overspeedDuration);
				//// }
				// flag = gpsBaseSvc.updateTerminalAndVehicle(vehicle, pojo);
				// }else {
				flag = gpsBaseSvc.updateTheTerminal(pojo);
				// }
			}
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return flag;
	}
	
	/**
	 * 保存接受到的指令消息
	 * @param packageData
	 * add by cheryl 2017-12-15
	 * @return
	 */
	public String createTheMessage(PackageData packageData,String type,String messageType,String receiveMsg){
		Gps报文 pojo = new Gps报文();
		String oid = null;
		MsgHeader header = packageData.getMsgHeader();
		pojo.setSim卡号(header.getTerminalPhone());
		System.out.println(DateUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss"));
		pojo.set消息时间(DateUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss"));
		pojo.set消息类型(messageType);
		pojo.set消息id(header.getMsgId());
		pojo.set消息流水号(header.getFlowId());
		//加上消息开始和结束的标识位。
		pojo.set消息内容(receiveMsg);
		pojo.set状态(type);
//		pojo.set备注("消息内容中只包括消息头以及对应的消息体，不包含标识位和对应的校验码");
		try {
			oid = gpsBaseSvc.createTheMessage(pojo);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("报文保存失败");
		} 
		return oid;
	}
}

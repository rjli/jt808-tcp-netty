package cn.hylexus.jt808.vo.req;

import java.util.List;

/**
 * 设置多边形区域
 * 
 * @author cheryl
 */
public class PolygonSettingArea {
	// 区域 ID
	protected int areaID;
	// 区域属性
	protected int areaAttribute;
	// 起始时间
	protected int startTime;
	// 结束时间
	protected int endTime;
	// 最高速度
	protected int maxSpeed;
	// 超速持续时间
	protected int continueTime;
	// 区域总顶点数
	protected int count;
	// 附加消息列表(顶点项)
	protected List<PolygonParamItem> polygonParamItemList;

	public int getAreaID() {
		return areaID;
	}

	public void setAreaID(int areaID) {
		this.areaID = areaID;
	}

	public int getAreaAttribute() {
		return areaAttribute;
	}

	public void setAreaAttribute(int areaAttribute) {
		this.areaAttribute = areaAttribute;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public int getContinueTime() {
		return continueTime;
	}

	public void setContinueTime(int continueTime) {
		this.continueTime = continueTime;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<PolygonParamItem> getPolygonParamItemList() {
		return polygonParamItemList;
	}

	public void setPolygonParamItemList(List<PolygonParamItem> polygonParamItemList) {
		this.polygonParamItemList = polygonParamItemList;
	}

	@Override
	public String toString() {
		return "PolygonSettingInfo [areaID=" + areaID + ", areaAttribute=" + areaAttribute + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", maxSpeed=" + maxSpeed + ", continueTime=" + continueTime + ", count="
				+ count + ", polygonParamItemList=" + polygonParamItemList + "]";
	}

}

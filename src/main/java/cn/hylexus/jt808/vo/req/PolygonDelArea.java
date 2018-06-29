package cn.hylexus.jt808.vo.req;
/**
 * 删除多边形区域
 * @author cheryl
 *
 */

import java.util.Arrays;

public class PolygonDelArea {
	// 区域数， BYTE，
	private int areaCount;
	// 区域ID，如“区域 ID1 区域 ID2......区域 IDn”。
	private byte[] areaIds;

	public int getAreaCount() {
		return areaCount;
	}

	public void setAreaCount(int areaCount) {
		this.areaCount = areaCount;
	}

	public byte[] getAreaIds() {
		return areaIds;
	}

	public void setAreaIds(byte[] areaIds) {
		this.areaIds = areaIds;
	}

	@Override
	public String toString() {
		return "PolygonDelArea [areaCount=" + areaCount + ", areaIds=" + Arrays.toString(areaIds) + "]";
	}

}

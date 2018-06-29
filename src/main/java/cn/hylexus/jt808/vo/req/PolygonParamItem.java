package cn.hylexus.jt808.vo.req;

/**
 * 设置多边形区域 ------>顶点项
 * 
 * @author cheryl
 *
 */
public class PolygonParamItem {

	// 顶点纬度 DWORD
	private int topLatitude;
	// 顶点经度 DWORD
	private int topLongitude;

	public int getTopLatitude() {
		return topLatitude;
	}

	public void setTopLatitude(int topLatitude) {
		this.topLatitude = topLatitude;
	}

	public int getTopLongitude() {
		return topLongitude;
	}

	public void setTopLongitude(int topLongitude) {
		this.topLongitude = topLongitude;
	}

	@Override
	public String toString() {
		return "PolygonParamItem [topLatitude=" + topLatitude + ", topLongitude=" + topLongitude + "]";
	}

}

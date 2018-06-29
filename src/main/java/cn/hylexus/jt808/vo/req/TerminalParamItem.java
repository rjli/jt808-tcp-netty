package cn.hylexus.jt808.vo.req;

import java.util.Arrays;

/**
 * 终端参数设置 ------>参数项
 * @author cheryl
 *
 */
public class TerminalParamItem {

	// 参数ID DWORD
	private int msgId;
	// 参数长度 BYTE
	private int paramLength;
	// 参数值
	private byte[] paramValue;

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public int getParamLength() {
		return paramLength;
	}

	public void setParamLength(int paramLength) {
		this.paramLength = paramLength;
	}

	public byte[] getParamValue() {
		return paramValue;
	}

	public void setParamValue(byte[] paramValue) {
		this.paramValue = paramValue;
	}

	@Override
	public String toString() {
		return "TerminalParamItem [msgId=" + msgId + ", paramLength=" + paramLength + ", paramValue=" + Arrays.toString(paramValue)
				+ "]";
	}
}

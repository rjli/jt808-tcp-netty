package cn.hylexus.jt808.vo.req;
/**
 * 终端参数查询
 * @author cheryl
 *
 */

import java.util.Arrays;

public class TerminalParamQuery {
	// 参数总数n， BYTE，
	private int paramCount;
	// BYTE[4*n] 参数顺序排列，如“参数 ID1 参数 ID2......参数 IDn”。
	private byte[] msgIds;

	public int getParamCount() {
		return paramCount;
	}

	public void setParamCount(int paramCount) {
		this.paramCount = paramCount;
	}

	public byte[] getMsgIds() {
		return msgIds;
	}

	public void setMsgIds(byte[] msgIds) {
		this.msgIds = msgIds;
	}

	@Override
	public String toString() {
		return "TerminalParamQueryInfo [paramCount=" + paramCount + ", msgIds=" + Arrays.toString(msgIds) + "]";
	}
}

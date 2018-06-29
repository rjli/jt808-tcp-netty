package cn.hylexus.jt808.vo.req;
/**
 * 位置附加信息
 * @author cheryl
 *
 */
public class TerminalLocationAdditionalInfoMsg {
    //附加消息id
	private int msgId;
	//附加消息长度
	private int msgCount;
	//附加消息
	private int msgContent;
	public int getMsgId() {
		return msgId;
	}
	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}
	public int getMsgCount() {
		return msgCount;
	}
	public void setMsgCount(int msgCount) {
		this.msgCount = msgCount;
	}
	public int getMsgContent() {
		return msgContent;
	}
	public void setMsgContent(int msgContent) {
		this.msgContent = msgContent;
	}

	
}

package cn.hylexus.jt808.socket;
/**
 * 发送报文的消息类
 * @author cheryl
 *
 */
public class MessageVo {
	private int messageId; //消息id
	private String phone;//终端手机号
	private String paramStr;//报文所需的消息的json str
	
	public int getMessageId() {
		return messageId;
	}
	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getParamStr() {
		return paramStr;
	}
	public void setParamStr(String paramStr) {
		this.paramStr = paramStr;
	}
	
}

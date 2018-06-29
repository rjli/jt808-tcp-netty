package cn.hylexus.jt808.util;
/**
 * 字节数组操作的工具类
 * @author cheryl
 *
 */
public class ByteUtils {
	/**
	 * 截取 字节数组
	 * @param src
	 * @param begin
	 * @param count
	 * @return
	 */
	public static  byte[] subBytes(byte[] src, int begin, int count) {  
	    byte[] bs = new byte[count];  
	    System.arraycopy(src, begin, bs, 0, count);  
	    return bs;  
	}  
}

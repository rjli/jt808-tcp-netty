package cn.hylexus.jt808.util;

public class HexStringUtils {

	private static final char[] DIGITS_HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	protected static char[] encodeHex(byte[] data) {
		int l = data.length;
		char[] out = new char[l << 1];
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = DIGITS_HEX[(0xF0 & data[i]) >>> 4];
			out[j++] = DIGITS_HEX[0x0F & data[i]];
		}
		return out;
	}

	protected static byte[] decodeHex(char[] data) {
		int len = data.length;
		if ((len & 0x01) != 0) {
			throw new RuntimeException("字符个数应该为偶数");
		}
		byte[] out = new byte[len >> 1];
		for (int i = 0, j = 0; j < len; i++) {
			int f = toDigit(data[j], j) << 4;
			j++;
			f |= toDigit(data[j], j);
			j++;
			out[i] = (byte) (f & 0xFF);
		}
		return out;
	}

	protected static int toDigit(char ch, int index) {
		int digit = Character.digit(ch, 16);
		if (digit == -1) {
			throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
		}
		return digit;
	}

	public static String toHexString(byte[] bs) {
		return new String(encodeHex(bs));
	}

	public static String hexString2Bytes(String hex) {
		return new String(decodeHex(hex.toCharArray()));
	}

	public static byte[] chars2Bytes(char[] bs) {
		return decodeHex(bs);
	}

	
	/* 
	  * 把16进制字符串转换成字节数组 
	  * @param hex 
	  * @return 
	  */  
	public static byte[] hexStringToByte(String hex) {  
	    int len = (hex.length() / 2); //除以2是因为十六进制比如a1使用两个字符代表一个byte  
	    byte[] result = new byte[len];  
	    char[] achar = hex.toCharArray();  
	    for (int i = 0; i < len; i++) {  
	    //乘以2是因为十六进制比如a1使用两个字符代表一个byte,pos代表的是数组的位置  
	     //第一个16进制数的起始位置是0第二个是2以此类推   
   	int pos = i * 2;   
	     //<<4位就是乘以16  比如说十六进制的"11",在这里也就是1*16|1,而其中的"|"或运算就相当于十进制中的加法运算   
	    //如00010000|00000001结果就是00010001 而00010000就有点类似于十进制中10而00000001相当于十进制中的1，与是其中的或运算就相当于是10+1(此处说法可能不太对，)  
	     result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));  
	    }  
	    return result;  
	}  
	
	private static byte toByte(char c) {  
	    byte b = (byte) "0123456789ABCDEF".indexOf(c);  
	    return b;  
	}  
	public static void main(String[] args) {
		String s = "abc你好";
		String hex = toHexString(s.getBytes());
		String decode = hexString2Bytes(hex);
		System.out.println("原字符串:" + s);
		System.out.println("十六进制字符串:" + hex);
		System.out.println("还原:" + decode);
	}
}
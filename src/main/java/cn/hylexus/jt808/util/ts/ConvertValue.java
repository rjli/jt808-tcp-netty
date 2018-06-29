package cn.hylexus.jt808.util.ts;

import java.util.Arrays;

public class ConvertValue {
	
	public static void main(String[] args) {
		 int a = 999900000;
		 byte[] b = new byte[4];
		 
		 b[3] = (byte)(a & 0xff);
		 b[2] = (byte)(a & 0xff);
		 b[1] = (byte)(a & 0xff);
		 b[0] = (byte)(a & 0xff);
		 
		 System.out.println(Arrays.toString(b));
		 
		 int ai = 0;
		 for(int i=0;i<b.length;i++){
			 ai += (b[i] & 0xff);
		 }
		 
		 System.out.println(ai);
	}
}

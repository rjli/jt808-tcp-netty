package cn.hylexus.jt808.socket;
import java.io.*;  
import java.net.*;   
public class SocketThread extends Thread {

    private ServerSocket serverSocket = null;  
    
    public SocketThread(ServerSocket serverScoket){  
        try {  
            if(null == serverSocket){  
                this.serverSocket = new ServerSocket(8877);  
                System.out.println("socket start");  
            }  
        } catch (Exception e) {  
            System.out.println("SocketThread创建socket服务出错");  
            e.printStackTrace();  
        }  
  
    }  
    
    public void run(){ 
    	System.out.println("开始启动线程");
        while(!this.isInterrupted()){  
            try {  
                Socket socket = serverSocket.accept();  
                  
                if(null != socket && !socket.isClosed()){    
                	System.out.println("客户端:"+socket.getInetAddress().getLocalHost()+"已连接到服务器");  
                    //处理接受的数据  
                    new SocketOperate(socket).start();  
                }  
//               setSoTimeout(30000) 是表示如果对方连接状态30秒没有收到数据的话强制断开客户端
                socket.setSoTimeout(30000);  
                  
            }catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
    }  
      
      
    public void closeSocketServer(){
    	System.out.println("准备关闭线程");
       try {  
            if(null!=serverSocket && !serverSocket.isClosed())  
            {  
             serverSocket.close();  
            }  
       } catch (IOException e) {  
        // TODO Auto-generated catch block  
        e.printStackTrace();  
       }  
     }  
      
      
}

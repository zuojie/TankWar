import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author pzjay
 * 服务器端:有两个线程，一个是接收UDP数据包的UDP线程；
 * 另一个是主线程，被用来作为TCP通信【玩家离开等消息】的TCP线程
 */

public class TankWarServer 
{
	public static final int UDP_LISTENEING_PORT = 6666;//服务器端监听端口
	private static int cliID = Integer.parseInt(TankProperties.getProperty("cliID"));//为每个客户端分配的ID号
	public static final String SERVER_IP = TankProperties.getProperty("SERVER_IP");
	public static final int TCP_PORT = Integer.parseInt(TankProperties.getProperty("TCP_PORT"));
	public List<ClientAttr> clientOnline = new ArrayList<ClientAttr>();
	/**
	 * 声明一个动态方法【静态的main中没有this对象】，而动态的方法里则有
	 * 然后就可以用这个this对象去new this对象这个外部类的内部类的对象：ClientAttr类
	 */
	public void beginWork()
	{
		/**
		 * 启动监听线程
		 */
		new Thread(new UDPThread()).start();
		ServerSocket ss = null;
		try{
			ss = new ServerSocket(TCP_PORT);
		} catch (IOException ie){
			System.out.println("Port is being used now!");
		}
		while(true)
		{
			Socket sc = null;
			try{
				sc = ss.accept();
				DataInputStream dis = new DataInputStream(sc.getInputStream());
				int udpPort = dis.readInt();
				String IP = sc.getInetAddress().getHostAddress();
				ClientAttr c = new ClientAttr(IP, udpPort);
				clientOnline.add(c);
System.out.println("A client: " + sc.getInetAddress() + ":" + sc.getPort() + " UDP port: " + udpPort);
				
				DataOutputStream dos = new DataOutputStream(sc.getOutputStream());
				/*
				 * 为每个客户端分配一个ID，为防止端口号分配重复，使用前置++【其实由于accept是阻塞型的，
				 * 只有处理完当前的才接受下一个，故没必要区分前置还是后置】
				 */
				dos.writeInt(++ cliID);
			}catch(IOException ie){
				ie.printStackTrace();
			}finally{
				try{
					sc.close();//把断开的客户端所占资源释放
					sc = null;
				}catch(IOException ie){
					ie.printStackTrace();
				}
			}
		}
	}
	public static void main(String[] args)
	{
		new TankWarServer().beginWork();
	}
	/**
	 * 服务器端建立一个内部存储客户端属性的类，存储客户端的UDP端口和IP
	 */
	private class ClientAttr
	{
		String cliIP;
		int cliUDP_PORT;
		public ClientAttr(String IP, int UDP_PORT)
		{
			this.cliIP = IP;
			this.cliUDP_PORT = UDP_PORT;
		}
	}
	/**
	 * 建立服务器端监听类，监听来自客户端的UDP数据包
	 */
	private class UDPThread implements Runnable
	{
		byte[] buf = new byte[1024];
		@Override
		public void run() 
		{
System.out.println("Beginning Listenering!");
			DatagramSocket ds = null;
			try{
				ds = new DatagramSocket(UDP_LISTENEING_PORT);
			}catch(IOException ie){
				System.out.println("Listeneing port " + 
						UDP_LISTENEING_PORT + " has been used!");
			}
			/**
			 * 开始监听
			 */
			while(null != ds)
			{
				/**
				 * 表示把接收到的UDP包中的内容送到buf字节数组中
				 */
				DatagramPacket dp = new DatagramPacket(buf, buf.length);
				try{
					//通过ds接收UDP包，暂存到dp包中
					ds.receive(dp);
					/**
					 * 挨个发给当前在线的玩家【排除自己】；
					 * UDP包封装过程：首先加上目的地地址，然后发送
					 */
					for(int idx = 0; idx < clientOnline.size(); ++ idx)
					{
						ClientAttr ca = clientOnline.get(idx);
						dp.setSocketAddress(new InetSocketAddress(ca.cliIP, ca.cliUDP_PORT));
						ds.send(dp);
					}
System.out.println("Received a packet!");
				}catch(IOException ie){
					ie.printStackTrace();
				}
			}
		}
		
	}
}

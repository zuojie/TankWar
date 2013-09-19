import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 
 * @author pzjay
 * 客户端封装类，TankWar主类将联网事宜全权交给本类管理;
 * 主逻辑是：加进一个客户端后，是通过启动TankWar主线程，然后TankWar主类调用TankWarClient类，
 * 同时每个客户端Tank在TankWar类中都维护一个自己的坦克List，装其他玩家坦克。因此每当有新坦克新加入后，
 * TankWarClient类执行消息发送，更新所有在线玩家的tank list库【不要和服务器的tankOnline库混淆】。
 * 同时接到新坦克的消息后，每个之前加入的tank都要广播一条消息报告自己的位置等，以便让新加入的坦克获取先前玩家
 * 的信息，之所以采用广播，是因为可能有多个玩家同时加入，此时一对一的通信显的有些麻烦，同时广播方式还可对于先前那些
 * 已经存在的玩家但是在其刚加入时没有收到此坦克信息【UDP包丢失什马的】的情况做一个补充。
 */
public class TankWarClient 
{
	private int CLIENT_UDP_PORT;
	public int getCLIENT_UDP_PORT() 
	{
		return CLIENT_UDP_PORT;
	}
	public void setCLIENT_UDP_PORT(int cLIENTUDPPORT) 
	{
		CLIENT_UDP_PORT = cLIENTUDPPORT;
	}
	TankFrame tw = null;
	/**
	 * 用于和服务器端通信用的socket
	 */
	DatagramSocket ds = null;
	
	/**
	 * 构造函数用前置++解释：
	 * 当有多个线程同时new客户端的时候，某一个线程的后置++执行的时间片可能跟另一个线程的new冲突
	 * 造成端口分配冲突，你懂的
	 */
	public TankWarClient(TankFrame tw)
	{
		this.tw = tw;
	}
	/**
	 * 
	 * @param serverIP 服务器端IP端口
	 * @param serverPort 服务器端端口
	 * 由于点击对话框的确定后才连接，故不在构造函数中构造ds
	 * 
	 */
	public void connect(String serverIP, int serverPort)
	{
		try {
			ds = new DatagramSocket(CLIENT_UDP_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		Socket sc = null;
		try{
			sc = new Socket(serverIP, serverPort);
System.out.println("Got U");
			DataOutputStream dos = new DataOutputStream(sc.getOutputStream());//将客户端的UDP端口号发送到服务器端，以便通信用
			dos.writeInt(CLIENT_UDP_PORT);
			
			DataInputStream dis = new DataInputStream(sc.getInputStream());
			tw.tank0.tankID = dis.readInt();//获取服务器端分配给当前客户端主战tank的ID
			/**
			 * 根据坦克ID设定坦克好坏属性：偶数坏，奇数好
			 * 坦克刚出生时默认是好的，连接上服务器之后被服务器重新分配好坏
			 * 属性
			 */
			if(1 == tw.tank0.tankID % 2)  tw.tank0.good = true;
			else  tw.tank0.good = false;
System.out.println("tank ID " + tw.tank0.tankID);

		}catch(UnknownHostException uhe){
			System.out.println("Can't find the specificed host");
		}catch(IOException ie){
			System.out.println("Server is not ready!");
		}finally{
			try{
				if(null != sc)  sc.close();
			}catch(IOException ie){
				ie.printStackTrace();
			}
			/**
			 * 连接上服务器端后，
			 * 启动接收消息线程
			 */
			new Thread(new UDPRecvThread()).start();
		}
		/**
		 * 当前客户端连接时，向服务器发送一个报道包，由服务器通知其他玩家
		 */
		NewTankMsg msg = new NewTankMsg(tw.tank0, tw);
		sendMsg(msg);
	}
	/**
	 * 
	 * @param msg
	 * 实现多态的函数，多态的3 key：继承 + 重写 + 父类引用指向子类对象
	 */
	public void sendMsg(TankMsg msg)//一律传递TankMsg
	{
		msg.sendMsg(ds, TankWarServer.SERVER_IP, 
				TankWarServer.UDP_LISTENEING_PORT);
	}
	/**
	 * 客户端建立一个单独的接收消息线程，用来接收服务器转发的消息
	 */
	private class UDPRecvThread implements Runnable
	{
		byte[] buf = new byte[1024];
		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			while(null != ds)
			{
				DatagramPacket dp = new DatagramPacket(buf, buf.length);
				try{
					ds.receive(dp);
					/**
					 * 对接收到的服务器端的消息包进行解读
					 */
					parseMsg(dp);
System.out.println("Received a packet from server!");
				}catch(IOException ie){
					ie.printStackTrace();
				}
			}
		}
		/**
		 * 
		 * @param dp解析dp包中的数据:
		 * 字节数组流到字节数组流，然后流到基本数据输入流，开始读操作
		 */
		public void parseMsg(DatagramPacket dp)
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(buf, 0, dp.getLength());
			DataInputStream dis = new DataInputStream(bais);
			/**
			 * 把流经过的管道交给消息自己封装的解析函数
			 * 首先解析是什么类型的消息，交给不同的消息处理类
			 */
			try {
				int msgType = dis.readInt();
				switch (msgType)
				{
				case TankMsg.NEW_Tank:
					new NewTankMsg(tw.tank0, tw).parseMsg(dis);
					break;
				case TankMsg.MOVE_Tank:
					new MoveTankMsg(tw.tank0, tw).parseMsg(dis);
					break;
				case TankMsg.NEW_BULLET:
					new NewBulletMsg(tw).parseMsg(dis);
					break;
				case TankMsg.DEAD_TANK:
					new DeadTankMsg(tw).parseMsg(dis);
					break;
				case TankMsg.DEAD_BULLET:
					new DeadBulletMsg(tw).parseMsg(dis);
					break;
				case TankMsg.NEW_TORPEDO:
					new NewTorpedoMsg(tw).parseMsg(dis);
					break;
				case TankMsg.DEAD_TORPEDO:
					new DeadTorpMsg(tw).parseMsg(dis);
					break;
				case TankMsg.REBORN_TANK:
					new RebornTankMsg(tw).parseMsg(dis);
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}

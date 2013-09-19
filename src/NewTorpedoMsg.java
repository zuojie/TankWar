import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * 
 * @author pzjay
 * 产生一颗子弹，发送消息
 */
public class NewTorpedoMsg implements TankMsg 
{
	int msgType = TankMsg.NEW_TORPEDO;
	/**
	 * 地雷的消息解析类，解析过程要跟地雷发生信息交换，
	 * 因此持有地雷的引用
	 */
	Torpedo torpedo;
	TankFrame tw;
	/**
	 * 
	 * @param bullet
	 * 两个构造方法，一个用来发送自己出生/移动的消息，
	 * 另一个用来接收消息
	 */
	//发送用构造函数
	public NewTorpedoMsg(Torpedo torp)
	{
		this.torpedo = torp;
	}
	//接收用构造函数
	public NewTorpedoMsg(TankFrame tw)
	{
		this.tw =tw;
	}
	//图方便，我们可以写一个构造函数完成上面两个功能
	public NewTorpedoMsg(TankFrame tw, Torpedo torp)
	{
		this.torpedo = torp;
		this.tw = tw;
	}
	@Override
	public void sendMsg(DatagramSocket ds, String ip, int udpPort) 
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();//默认32字节大小
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(msgType);
			dos.writeInt(tw.tank0.tankID);
			dos.writeInt(torpedo.id);
			dos.writeInt(torpedo.x);
			dos.writeInt(torpedo.y);
			dos.writeBoolean(tw.tank0.good);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/**
		 * 将字节数组流中的数据转化成纯的字节数组
		 * 然后打包发送
		 */
		byte[] buf = baos.toByteArray();
		try{
		DatagramPacket dp = new DatagramPacket(buf, 
				buf.length, new InetSocketAddress(ip, udpPort));
		ds.send(dp);
		}catch(IOException ie){
			ie.printStackTrace();
		}
	}
	@Override
	public void parseMsg(DataInputStream dis) 
	{
		try{
			int tankID = dis.readInt();
			if(tankID == tw.tank0.tankID)  return;//自己产生的地雷
			int id = dis.readInt();
			int x = dis.readInt();
			int y = dis.readInt();
			boolean good = dis.readBoolean();
			//假如收到的是其他玩家发来的新子弹消息，那么把这个子弹放到自己的子弹库，然后在自己这边重画出来
			boolean exist = false;
			
			for(int idx = 0; idx < tw.torpedos.size(); ++ idx)
			{
				Torpedo torp = tw.torpedos.get(idx);
				if(!torp.isLive())
				{
					exist = true;
					torp.setLive(true);
					break;
				}
				if(torp.id == id && torp.tw.tank0.tankID == tankID)
				{
					exist = true;
					break;
				}
			}
			if(!exist)
			{
				Torpedo torp = new Torpedo(x, y, good, tw);
				torp.id = id;
				tw.torpedos.add(torp);
				tw.tc.sendMsg(new NewTorpedoMsg(tw, torpedo));
			}
		}catch(IOException ie){
			ie.printStackTrace();
		}
	}


}

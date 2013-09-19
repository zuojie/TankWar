import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * 
 * @author pzjay
 * 坦克的消息解析类，由于解析消息的过程中要和坦克
 * 发生交互，故持有tank的引用。当然也可以持有一个大管家，然后间接跟tank
 * 发生交互
 */
public class NewTankMsg implements TankMsg
{
	int msgType = TankMsg.NEW_Tank;
	Tank tk;
	TankFrame tw;
	public NewTankMsg(Tank tk, TankFrame tw)
	{
		this.tw = tw;
		this.tk = tk;
	}
	/**
	 * 
	 * @param ds发送消息的socket
	 * @param ip目的地【server】IP
	 * @param udpPort服务器端的接收端口号
	 * 消息类封装的发送消息函数
	 */
	public void sendMsg(DatagramSocket ds, String ip, int udpPort)
	{
		/**
		 * 要发送数据包内容，先将待发送数据整理到字节数组中
		 * 然后打包发送即可
		 */
		ByteArrayOutputStream baos = new ByteArrayOutputStream();//默认32字节大小
		/**
		 * 由于要发送的是基本数据类型，
		 * 所以还要在外面再包一层基本输出流
		 */
		DataOutputStream dos = new DataOutputStream(baos);
		/**
		 * 发送时先发送消息类型
		 */
		try {
			dos.writeInt(msgType);
			dos.writeInt(tk.tankID);
			dos.writeInt(tk.x);
			dos.writeInt(tk.y);
			dos.writeInt(tk.dir.ordinal());//坦克的方向在枚举类中所在位置【即下标】
			dos.writeBoolean(tk.good);
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
	/**
	 * 
	 * @param dis解析dis流中流过来的数据，对应上面发送的顺序：
	 * 注意此时形成了一个数据发送的环路
	 */
	public void parseMsg(DataInputStream dis) 
	{
		try{
			int id = dis.readInt();
			if(id == tk.tankID)  return;//新生坦克自己也会给自己发消息
			int x = dis.readInt();
			int y = dis.readInt();
			Direction dir = Direction.values()[dis.readInt()];
			boolean good = dis.readBoolean();
			/**
			 * 此时解析的消息仅限于有新坦克加入的消息，新坦克把自己的相关属性发送给
			 * 服务器端，服务器端转发各玩家，到这里进行解析。这里要做的只是把新坦克加入到坦克数组
			 * 中即可，下次repaint时将其画出来;
			 * 续：由于先前加入的玩家也要发送确认消息，所以这里收到的消息就不一定是新玩家的
			 * 消息了，也不能一概的new一个新坦克放到自己的tank库里，要判断一下，假如收到的
			 * 消息是已经在自己库里的坦克发的，无视即可；否则new一个放到自己的tank库中，然后发一条
			 * 广播。
			 */
			boolean exist = false;
			for(int idx = 0; idx < tw.tanks.size(); ++ idx)
			{
				if(id == tw.tanks.get(idx).tankID)
				{
					exist = true;
					break;
				}
			}
			if(!exist)
			{
				Tank tank = new Tank(x, y, dir, good, tw);
				tank.tankID = id;
				tw.tanks.add(tank);
				/**
				 * 发送广播
				 */
				tw.tc.sendMsg(new NewTankMsg(tk, tw));//把自己的属性包装成消息发出去
			}
		}catch(IOException ie){
			ie.printStackTrace();
		}
	}
}

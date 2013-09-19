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
 * 坦克只在转向时才发送消息，告诉其他玩家自己发生转向
 */
public class MoveTankMsg implements TankMsg
{
	int msgType = TankMsg.MOVE_Tank;
	int id;
	/**
	 * 加入坐标控制，使同步性更完美
	 */
	int x, y;
	Direction dir;
	TankFrame tw;
	Tank tk;
	
	public MoveTankMsg(int id, int x, int y, Direction dir, Tank tk) 
	{
		this.id = id;
		this.x = x;
		this.y = y;
		this.dir = dir;
		this.tk = tk;
	}
	public MoveTankMsg(Tank tk, TankFrame tw)
	{	
		this.tk = tk;
		this.tw = tw;
	}

	@Override
	public void parseMsg(DataInputStream dis) 
	{
		try{
			int id = dis.readInt();
			if(id == tk.tankID)  return;//新生坦克自己也会给自己发消息
			int x = dis.readInt();
			int y = dis.readInt();
			Direction dir = Direction.values()[dis.readInt()];
			/**
			 * 此时解析的消息仅限于有新坦克加入的消息，新坦克把自己的相关属性发送给
			 * 服务器端，服务器端转发各玩家，到这里进行解析。这里要做的只是把新坦克加入到坦克数组
			 * 中即可，下次repaint时将其画出来
			 */
			boolean exist = false;//看收到的消息是否是所在tanks数组中的坦克，是的话调整其方向
			for(int i = 0 ; i < tw.tanks.size(); ++ i)
			{
				Tank tank = tw.tanks.get(i);
				if(tank.tankID == id)
				{
					tank.dir = dir;//调向
					tank.x = x;
					tank.y = y;//同步的控制
					exist = true;
					break;
				}
			}
		}catch(IOException ie){
			ie.printStackTrace();
		}
	}

	@Override
	public void sendMsg(DatagramSocket ds, String ip, int udpPort) 
	{
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
			dos.writeInt(x);
			dos.writeInt(y);
			dos.writeInt(tk.dir.ordinal());//坦克的方向在枚举类中所在位置【即下标】
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
}

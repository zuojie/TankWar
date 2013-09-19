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
public class RebornTankMsg implements TankMsg
{
	int msgType = TankMsg.REBORN_TANK;
	Tank tk;
	TankFrame tw;
	public RebornTankMsg(TankFrame tw)
	{
		this.tw = tw;
		this.tk = tw.tank0;
	}
	/**
	 * 
	 * @param tk复活对象
	 * @param tw复活地点
	 */
	public RebornTankMsg(Tank tk, TankFrame tw)
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();//默认32字节大小
		DataOutputStream dos = new DataOutputStream(baos);
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
			//if(null == tk)  System.out.println("sdfadsasdfasdf");
			if(id == tk.tankID)  return;//新生坦克自己也会给自己发消息
			int x = dis.readInt();
			int y = dis.readInt();
			Direction dir = Direction.values()[dis.readInt()];
			boolean good = dis.readBoolean();
			boolean exist = false;
			for(int idx = 0; idx < tw.tanks.size(); ++ idx)
			{
				Tank tk = tw.tanks.get(idx);
				if(id == tk.tankID)
				{
					exist = true;
					tk.setLive(true);//找到指定的坦克，复活之
					break;
				}
			}
			if(!exist)
			{
				Tank tank = new Tank(x, y, dir, good, tw);
				tank.tankID = id;
				tw.tanks.add(tank);
				tw.tc.sendMsg(new RebornTankMsg(tk, tw));//把自己的属性包装成消息发出去
				System.out.println("I am reborned!");
			}
		}catch(IOException ie){
			ie.printStackTrace();
		}
	}
}

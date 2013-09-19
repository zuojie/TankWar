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
public class NewBulletMsg implements TankMsg 
{
	int msgType = TankMsg.NEW_BULLET;
	Bullet bullet;
	TankFrame tw;
	/**
	 * 
	 * @param bullet
	 * 两个构造方法，一个用来发送自己出生/移动的消息，
	 * 另一个用来接收消息
	 */
	//发送用构造函数
	public NewBulletMsg(Bullet bullet)
	{
		this.bullet = bullet;
	}
	//接收用构造函数
	public NewBulletMsg(TankFrame tw)
	{
		this.tw =tw;
	}
	//图方便，我们可以写一个构造函数完成上面两个功能
	public NewBulletMsg(TankFrame tw, Bullet bullet)
	{
		this.bullet = bullet;
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
			dos.writeInt(bullet.id);
			dos.writeInt(bullet.x);
			dos.writeInt(bullet.y);
			dos.writeInt(bullet.dir.ordinal());//坦克的方向在枚举类中所在位置【即下标】
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
			if(tankID == tw.tank0.tankID)  return;//新生坦克自己也会给自己发消息
			int id = dis.readInt();
			int x = dis.readInt();
			int y = dis.readInt();
			Direction dir = Direction.values()[dis.readInt()];
			boolean good = dis.readBoolean();
			//假如收到的是其他玩家发来的新子弹消息，那么把这个子弹放到自己的子弹库，然后在自己这边重画出来
			Bullet bt = new Bullet(x, y, dir, tw, good);
			bt.id = id;
			tw.bullets.add(bt);
		}catch(IOException ie){
			ie.printStackTrace();
		}
	}


}

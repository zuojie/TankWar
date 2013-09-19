import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;


public class DeadTankMsg implements TankMsg 
{
	private static final int msgType = TankMsg.DEAD_TANK;
	int ID;
	TankFrame tw;
	public DeadTankMsg(int  ID)
	{
		this.ID = ID;
	}
	public DeadTankMsg(TankFrame tw)
	{
		this.tw = tw;
	}
	@Override
	public void parseMsg(DataInputStream dis) 
	{
		try{
			int id = dis.readInt();
			//if(id == tw.tank0.tankID)  return;//新生坦克自己也会给自己发消息
			for(int idx = 0; idx < tw.tanks.size(); ++ idx)
			{
				Tank tank = tw.tanks.get(idx);
				if(id == tank.tankID)
				{
					tank.setLive(false);
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
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(msgType);
			dos.writeInt(ID);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

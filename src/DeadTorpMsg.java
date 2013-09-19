import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;


public class DeadTorpMsg implements TankMsg 
{
	private static final int msgType = TankMsg.DEAD_TORPEDO;
	int tankID;
	int id;
	TankFrame tw;
	
	public DeadTorpMsg(int tankID, int id)
	{
		this.tankID = tankID;
		this.id = id;
	}
	public DeadTorpMsg(TankFrame tw)
	{
		this.tw = tw;
	}
	@Override
	public void parseMsg(DataInputStream dis) 
	{
		try{
			int tankID = dis.readInt();
			int id = dis.readInt();
			for(int idx = 0; idx < tw.torpedos.size(); ++ idx)
			{
				Torpedo torp = tw.torpedos.get(idx);
				if(id == torp.id)
				{
					torp.setLive(false);
					torp.tw.torpedos.remove(torp);
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
			dos.writeInt(tankID);
			dos.writeInt(id);
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

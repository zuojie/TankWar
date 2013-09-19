import java.io.DataInputStream;
import java.net.DatagramSocket;

/**
 * 
 * @author pzjay
 * 消息接口，实现多态机制
 * 其内属性默认是public static final，也只能是这种类型
 */
public interface TankMsg 
{
	/**
	 * 消息类型
	 */
	int REBORN_TANK = 0;//坦克重生消息
	int NEW_Tank = 1;//产生一辆新坦克消息
	int MOVE_Tank = 2;//坦克移动消息
	int NEW_BULLET = 3;
	int DEAD_TANK = 4;
	int DEAD_BULLET = 5;
	int NEW_TORPEDO = 6;
	int DEAD_TORPEDO = 7;
	
	public void sendMsg(DatagramSocket ds, String ip, int udpPort);
	public void parseMsg(DataInputStream dis);
}

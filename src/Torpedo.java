
import java.awt.*;
/**
 * 
 * @author pzjay
 * 我方坦克产生地雷
 * 属性：大小和放置位置，死亡标示符，好坏，大管家引用
 * 方法：爆炸，画出自己，碰撞检测
 * X键产生地雷
 */
public class Torpedo
{
	public int x, y, width, height;//地雷属性
	private boolean good, live;
	private static int torpID = 0;//地雷从开始编号
	int id;//当前地雷的编号

	public static TankFrame tw;
	
	private static Toolkit tk = Toolkit.getDefaultToolkit();
	private static Image img = tk.getImage(Torpedo.class.getClassLoader().getResource(
			"Images" + tw.sep + "Torpedo.gif"));
	
	public Torpedo(int x, int y, boolean good, TankFrame tw)
	{
		this.id = ++ torpID;
		this.x = x;
		this.y = y;
		this.tw = tw;
		this.good = good;
		width = height = 20;
		this.live = true;
	}
	
	public void setGood(boolean good) 
	{
		this.good = good;
	}
	public boolean isGood() 
	{
		return good;
	}
	
	public boolean isLive() 
	{
		return live;
	}

	public void setLive(boolean live) 
	{
		this.live = live;
	}
	
	public void drawTor(Graphics g)
	{
		g.drawImage(img, x, y, null);
	}
	
	public Rectangle getRect()
	{
		return new Rectangle(x, y, img.getWidth(null), img.getHeight(null));
	}

}

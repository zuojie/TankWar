
import java.awt.*;

public class Wall
{
	/**
	 * 墙的位置和类型：0. 垂直 1. 水平
	 */
	private int x, y, wallType;
	private static TankFrame tw = null;//大管家
	
	private static Toolkit tk = Toolkit.getDefaultToolkit();
	private static Image[] imgWall = {
		tk.getImage(Wall.class.getClassLoader().getResource("Images" + tw.sep + "wallVertical.gif")),
		tk.getImage(Wall.class.getClassLoader().getResource("Images" + tw.sep + "wallHorizon.gif"))
	};
	public Wall(int x, int y, int wallType, TankFrame tw)
	{
		this.tw = tw;
		this.x = x;
		this.y = y;
		this.wallType = wallType;
	}
	public void drawWall(Graphics g)
	{
		if(0 == wallType)  g.drawImage(imgWall[0], x, y, null);
		else  g.drawImage(imgWall[1], x, y, null);
	}
	public Rectangle getRec()//碰撞检测
	{
		if(0 == wallType)  
			return new Rectangle(x, y, imgWall[0].getWidth(null), imgWall[0].getHeight(null));
		return new Rectangle(x, y, imgWall[1].getWidth(null), imgWall[1].getHeight(null));
	}
}

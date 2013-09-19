import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;


public class Bullet 
{
	public static final int WIDTH = 10, HEIGHT = 10;
	public int x, y;//子弹位置
	private static final int speedX = 10, speedY = 10;//子弹移动速度
	private boolean live = true;//判断子弹是否还活着
	private boolean good;//标示是好坦克发出的子弹还是坏坦克发出的炮弹
	/**
	 * 每辆坦克产生的子弹从1开始编号，知道子弹所属的坦克
	 * 以及子弹的编号，就可以唯一确定一颗子弹了
	 */
	private static int bulletID = 0;//【下面是前置++】从1开始编号
	
	int id;//本颗子弹的id

	Direction dir;//子弹打出的方向肯定随当前坦克移动方向
	public static TankFrame tw;//持有对方引用，判断在主类中子弹是否越界或者碰到坦克，以判断当前子弹是否消失
	
	private static Toolkit tk = Toolkit.getDefaultToolkit();//调用系统的API读入图片，实现跨平台性，为啥用static，你懂的
	/**
	 * 加入子弹图片
	 */
	
	private static Image[] imgBullet = null;
	private static Map<String, Image> imgs = new HashMap<String, Image>();
	static{
		imgBullet = new Image[] {
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "missileU.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "missileD.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "missileL.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "missileR.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "missileLU.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "missileLD.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "missileRU.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "missileRD.gif")),
		};
		/**
		 * 初始化Map
		 * UU, UR, RR, RD, DD, LD, LL, LU, STOP
		 */
		imgs.put("UU", imgBullet[0]);
		imgs.put("DD", imgBullet[1]);
		imgs.put("LL", imgBullet[2]);
		imgs.put("RR", imgBullet[3]);
		imgs.put("LU", imgBullet[4]);
		imgs.put("LD", imgBullet[5]);
		imgs.put("UR", imgBullet[6]);
		imgs.put("RD", imgBullet[7]);
	}
	
	public boolean isGood() 
	{
		return good;
	}
	public void setGood(boolean good) 
	{
		this.good = good;
	}
	
	public Bullet(int x, int y, Direction dir)
	{
		this.x = x;
		this.y = y;
		this.dir = dir;
		this.id = ++ bulletID;
	}
	public Bullet(int x, int y, Direction dir, TankFrame tw, boolean good)
	{
		this(x, y, dir);
		this.tw = tw;
		this.good = good;
	}
	
	public void setLive(boolean live)
	{
		this.live = live;
	}
	public boolean getLive()//获取子弹的当前状态：活或者死 
	{
		return live;
	}
	
	public void drawBullet(Graphics g)//得到一支画笔，坦克画出自己
	{
		if(!live)  return;
		Color c = g.getColor();//保存现场
		if(good)  g.drawImage(imgs.get(dir.toString()), x, y, null);//g.setColor(Color.MAGENTA);
		else
		{
			g.setColor(Color.WHITE);
			g.fillOval(x, y, WIDTH, HEIGHT);
		}
		g.setColor(c);
		Go();
	}
	
	public void Go()//子弹移动
	{
		switch(dir)
		{
		case UU://上
			y -= speedY;
			break;
		case UR://右上
			y -= speedY / Math.sqrt(2.0);
			x += speedX / Math.sqrt(2.0);
			break;
		case RR://右
			x += speedX;
			break;
		case RD:
			x += speedX / Math.sqrt(2.0);
			y += speedY / Math.sqrt(2.0);
			break;
		case DD:
			y += speedY;
			break;
		case LD:
			x -= speedX / Math.sqrt(2.0);
			y += speedY / Math.sqrt(2.0);
			break;
		case LL:
			x -= speedX;
			break;
		case LU:
			x -= speedX / Math.sqrt(2.0);
			y -= speedY / Math.sqrt(2.0);
			break;
		}
		outBoundry();//子弹每次移动后要判断是否越界
	}
	public void outBoundry()//判断子弹是否越界
	{
		if(x < 0 || y < 0 || x > tw.WINDOW_WIDTH || y > tw.WINDOW_HEIGHT)  live = false;
		suicide();
	}
	public void suicide()//自己把自己解决

	{
		if(false == live)  this.tw.bullets.remove(this);
	}
	
	public Rectangle getRect()
	{
		return new Rectangle(x, y, imgBullet[0].getWidth(null), imgBullet[0].getHeight(null));//建立一个包围子弹的边界框，用于碰撞检测
	}
	
	public boolean hitTank(Tank tk)//判断当前子弹是否碰到坦克 tk
	{
		return (this.getRect().intersects(tk.getRect()) && tk.good != this.good);//发生交互，就表示碰撞上了
	}
	
	public boolean hitWall(Wall wall)//判断是否撞墙
	{
		return (this.getRect().intersects(wall.getRec()) && this.live);
	}
}

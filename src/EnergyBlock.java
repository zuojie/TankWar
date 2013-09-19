import java.awt.*;
import java.util.Random;
/**
 * 
 * @author pzjay
 * 血块从左侧随机产生，产生后立即向右侧运动
 * 为了方便访问坦克类里的枚举变量，能量块类持有tank类的引用
 */
public class EnergyBlock//增加血块类
{
	public boolean good;//标示是增加生命值的能量块还是死亡血块,要在主类中访问
	private int speedX = 10;//能量块横向移动的速度
	private boolean live = true;//这个量不允许外部访问
	private int width, height;//血块的大小属性
	public int x, y;//血块的位置，外部类要访问【tank类】
	TankFrame tw = null;//大管家
	public static final double PI = Math.PI;
	Tank tk = null;
	private Random rand = new Random();
	
	private int[] pos = {50, 100, 200, 300, 400, 500, 600, 800, 900};//能量块在左侧这四个方位处产生
	public EnergyBlock(TankFrame tw, boolean good) 
	{
		width = 15;
		height = 15;
		this.tw = tw;
		this.x = 0;//出生时x坐标为0
		this.y = pos[0];//初始血块在第一个位置产生
		this.good = good;
	}
	private int step = 0;//血块飘到第几个位置
	
	public boolean isLive() 
	{
		return live;
	}

	public void setLive(boolean live) 
	{
		this.live = live;
	}
	
	public void drawBlock(Graphics g)
	{
		Color c = g.getColor();
		if(good)  g.setColor(Color.YELLOW);
		else  g.setColor(Color.GRAY);
		g.fillRect(x, y, width, height);
		g.setColor(c);
		move();//能量块画完之后可以移动
	}
	public Rectangle getRec()//碰撞检测
	{
		return new Rectangle(x, y, width, height);
	}
	
	public void move()//血块移动
	{
		if(x >= tw.WIDTH)//当前位置的能量块消失，下个位置产生
			if(rand.nextInt(1000) < 10)//能量块产生概率：1 / 100
			{
				x = 0;
				y = pos[rand.nextInt(pos.length)];
				//y = pos[step ++];
				if(step >= pos.length)  step = 0;
			}
		x += speedX;
	}
	
}

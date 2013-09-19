import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Tank 
{
	//既然是封装类，那么成员通常都不需要也不必要让外部直接访问，只允许通过自己
	//对外提供的方法访问，声明成私有即可
	public static final int WIDTH = 30, HEIGHT = 30;
	public int x=0, y = 0;//坦克出生坐标,因为在主类中子弹产生要获得坦克坐标，故声明称public，当然也可单独写个获取方法
	public int preX, preY;
	private static TankFrame tw = null;//持有对方的引用，为了方便访问对方的变量
	private static final int speedX = 5, speedY = 5;//坦克移动速度，共用量，设成static，省地方
	private boolean bL = false, bR = false, bU = false, bD = false;//方向键标示，为true时为按下
	public boolean good;//增加标示坦克好坏的标示符
	private boolean live = true;//标示当前坦克是否还活着
	public int totalBlood = Integer.parseInt(TankProperties.getProperty("totalBlood"));//总血量
	public int blood = totalBlood;//实时血量；由于要在主类中访问修改，设成public
	private static Random rand = new Random();
	private BloodBar lifeBar = new BloodBar();//坦克的血条
	public int tankID = 0;//网络对战中每辆坦克的ID
	//枚举类型
	public Direction dir = Direction.STOP;//坦克初始静止
	public Direction gun_barrel = Direction.DD;//炮筒方向
	
	private static Toolkit tk = Toolkit.getDefaultToolkit();//调用系统的API读入图片，实现跨平台性，为啥用static，你懂的
	/**
	 * 加入坦克图片
	 * 这里我们把图片数组的初始化放到静态代码区
	 * 这段区域在当前类第一次加载到内存时首先执行
	 * 是一个特殊的函数
	 */
	
	private static Image[] imgTank = null;
	/**
	 * 图片版把坦克方向设为跟炮筒方向一致
	 * 坦克图片8个方向8张，用Map映射方向对应的图片
	 */
	private static Map<String, Image> imgs = new HashMap<String, Image>();
	static{
		imgTank = new Image[] {
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "tankU.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "tankD.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "tankL.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "tankR.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "tankLU.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "tankLD.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "tankRU.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "tankRD.gif")),
		};
		/**
		 * 初始化Map
		 * UU, UR, RR, RD, DD, LD, LL, LU, STOP
		 */
		imgs.put("UU", imgTank[0]);
		imgs.put("DD", imgTank[1]);
		imgs.put("LL", imgTank[2]);
		imgs.put("RR", imgTank[3]);
		imgs.put("LU", imgTank[4]);
		imgs.put("LD", imgTank[5]);
		imgs.put("UR", imgTank[6]);
		imgs.put("RD", imgTank[7]);
	}
	

	private static int enemyStep = rand.nextInt(12) + 3;//敌方坦克随机移动步数:[3, 14]之间

	public boolean isLive() 
	{
		return live;
	}
	public void setLive(boolean live) 
	{
		this.live = live;
	}

	public Tank(int x, int y) 
	{
		this.preX = this.x = x;
		this.preY = this.y = y;
	}
	/**
	 * 
	 * @param x
	 * @param y
	 * @param dir
	 * @param tw
	 * 坦克好坏属性在出生时指定
	 */
	public Tank(int x, int y, Direction dir, boolean good, TankFrame tw)
	{
		this(x, y);//调用上面的构造方法
		this.good = good;
		this.tw = tw;
		this.dir = dir;
	}
	/**
	 * 
	 * @param x
	 * @param y
	 * @param dir
	 * @param tw
	 * 坦克好坏属性由电脑指定
	 */
	public Tank(int x, int y, Direction dir, TankFrame tw)
	{
		this(x, y);
		this.tw = tw;
		this.dir = dir;
	}

	public void drawTank(Graphics g)//得到一支画笔，坦克画出自己
	{
		this.lifeBar.drawBar(g);//画生命条
		this.drawGB(g);//坦克方向和炮筒方向一致，画炮筒的方法适用画坦克
		Go();//敌方坦克一出生就开始随机运动
	}

	public void drawGB(Graphics g)//gun barrel
	{
		Color c = g.getColor();//保存现场
		//画线为炮筒
		int wid = Tank.WIDTH / 2;
		int higt = Tank.HEIGHT / 2;
		g.drawImage(imgs.get(gun_barrel.toString()), x, y, null);//不用switch语句，直接根据方向键，取相应的值
		g.setColor(c);
	}
	public void Go()//坦克移动
	{
		preX = x;
		preY = y;//记录坦克上个位置
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
		case STOP:
			break;
		}
		if(dir != Direction.STOP)  gun_barrel = dir;//时刻调整炮筒方向
		/**
		 * 考虑联网版本的时候，电脑坦克的设定暂时取消
		 */
		/*if(!good)//敌方坦克，随机设定下一步
		{
			Direction[] randDir = Direction.values();//将枚举类型转化为数组
			if(0 == enemyStep)
			{
				dir = randDir[rand.nextInt(randDir.length - 2)];
				enemyStep = rand.nextInt(12) + 3;
				this.fire(false);//坏坦克每次转向都发一颗炮弹
			}
			-- enemyStep;
		}*/
		isTransgress();//限制坦克不能越界
	}

	public void isTransgress()//是否越界
	{
		if(x < 0)  x = 0;
		if(y < 30)  y = 30;
		if(x > tw.WINDOW_WIDTH - imgTank[0].getWidth(null))  x = tw.WINDOW_WIDTH - imgTank[0].getWidth(null);
		if(y > tw.WINDOW_HEIGHT - imgTank[0].getHeight(null))  y = tw.WINDOW_HEIGHT - imgTank[0].getHeight(null);
	}
	/**
	 * 
	 * 产生一颗地雷,将消息发出去
	 */
	public void downLoadTor()
	{
		Torpedo torp = new Torpedo(this.x, this.y, this.good, this.tw);
		tw.torpedos.add(torp);
		
		tw.tc.sendMsg(new NewTorpedoMsg(this.tw, torp));
	}
	
	public void tankMove(KeyEvent ke)//键按下，坦克接到指令，做出响应
	{
		int tpKey = ke.getKeyCode();
		switch(tpKey)//按照上下左右判断按下的是哪个键
		{
		case KeyEvent.VK_UP:
			bU = true;
			break;
		case KeyEvent.VK_DOWN:
			bD = true;
			break;
		case KeyEvent.VK_LEFT:
			bL = true;
			break;
		case KeyEvent.VK_RIGHT:
			bR = true;
			break;
			//以上为移动判断，下面是操作判断：开火等
		case KeyEvent.VK_CONTROL:
			fire(good);
			break;
		case KeyEvent.VK_EQUALS ://敌方加一人
			tw.tanks.add(new Tank(imgTank[0].getWidth(null), imgTank[0].getHeight(null), Direction.DD, false, tw));
			break;
		case KeyEvent.VK_A:
			superFire(1);//以后定义成宏，便于识别
			break;
		case KeyEvent.VK_S:
			superFire(2);
			break;
		case KeyEvent.VK_D:
			superFire(3);
			break;
		case KeyEvent.VK_X://产生地雷
			int totalTorpedo = Integer.parseInt(TankProperties.getProperty("totalTorpedo"));//地图上最多共存的地雷数
			if(tw.torpedos.size() < totalTorpedo)  downLoadTor();//在当前坦克位置处,下一颗地雷【地雷数量限制在5颗以内】
			break;
		}
		setDir();//然后重新设置坦克移动方向
	}

	public void tankStop(KeyEvent ke)//键抬起，坦克接到指令，做出响应
	{
		int tpKey = ke.getKeyCode();
		switch(tpKey)//按照上下左右判断按下的是哪个键
		{
		case KeyEvent.VK_UP:
			bU = false;
			break;
		case KeyEvent.VK_DOWN:
			bD = false;
			break;
		case KeyEvent.VK_LEFT:
			bL = false;
			break;
		case KeyEvent.VK_RIGHT:
			bR = false;
			break;
		}
		setDir();//然后重新设置坦克移动方向
	}

	/**
	 * 若我方坦克在方向键按下时死去，
	 * 那么重生的坦克就会循着这个方向一直运动，
	 * 所以假如坦克死去，所有方向键都默认放开
	 * 我方坦克死亡时，调用本方法
	 */
	public void isDead()
	{
		if(good && !live)  bU = bD = bL = bR = false;
		setDir();
	}
	public void setDir()//根据按键情况设置移动方向
	{
		Direction oldDir = dir;
		if(true == bU)
		{
			if(true == bR)  dir = Direction.UR;//右上
			else if(true == bL)  dir = Direction.LU;//左上
			else  dir = Direction.UU;//上
		}
		else if(false == bU)
		{	
			if(false == bD) 
			{
				if(true == bR)  dir = Direction.RR;
				else if(true == bL)  dir = Direction.LL;
				else  dir = Direction.STOP;
			}
			else if(true == bD)  
			{
				if(true == bR)  dir = Direction.RD;//右下
				else  if(true == bL)  dir = Direction.LD;//左下
				else  dir = Direction.DD;//下
			}
		}
		/**
		 * 发出转向消息，注意调用的是哪一个构造函数
		 */
		if(oldDir != dir)  tw.tc.sendMsg(new MoveTankMsg(tankID, x, y, dir, this));
		//设置完毕后，坦克移动
		Go();
	}
	//开火方法：产生子弹
	public Bullet fire(boolean good)//产生一粒炮弹，放入数组中【good标示是哪方坦克产生的炮弹】
	{
		int x = this.x + (Tank.WIDTH - Bullet.WIDTH) / 2;
		int y = this.y + (Tank.HEIGHT - Bullet.HEIGHT) / 2;//保证子弹从坦克中心打出
		Bullet bt = new Bullet(x, y, this.gun_barrel, this.tw, good); 
		tw.bullets.add(bt);//子弹发射方向跟炮筒方向一致
		/**
		 * 新产生一粒炮弹，给服务器端发送消息
		 */
		tw.tc.sendMsg(new NewBulletMsg(tw, bt));
		return bt;
	}
	//super fire的开火方法
	public Bullet fire1(Direction dir)//因为只有我方坦克才有发射超级炮弹的特权，默认就是好蛋
	{
		int x = this.x + (Tank.WIDTH - Bullet.WIDTH) / 2;
		int y = this.y + (Tank.HEIGHT - Bullet.HEIGHT) / 2;//保证子弹从坦克中心打出
		Bullet bt = new Bullet(x, y, dir, this.tw, good);//传递的不再是炮筒方向,而是8个方向
		tw.bullets.add(bt);//子弹发射方向跟炮筒方向一致
		tw.tc.sendMsg(new NewBulletMsg(tw, bt));
		return bt;
	}
	public void fire2()
	{
		int interval = 20;
		int x = this.x + (Tank.WIDTH - Bullet.WIDTH) / 2;
		int y = this.y + (Tank.HEIGHT - Bullet.HEIGHT) / 2;//中间那颗子弹从坦克中心打出
		Bullet bt = new Bullet(x, y, this.gun_barrel, this.tw, good);
		tw.bullets.add(bt);//子弹发射方向跟炮筒方向一致
		tw.tc.sendMsg(new NewBulletMsg(tw, bt));
		//交替产生两侧的子弹
		switch(gun_barrel)//根据炮筒方向，产生相应炮弹:先产生右侧第一颗，第二颗，然后左侧第一颗，第二颗
		{
		case UU://上
			bt = new Bullet(x + interval, y, this.gun_barrel, this.tw, good);//朝上，y偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x + 2 * interval, y, this.gun_barrel, this.tw, good);//朝上，y偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x - interval, y, this.gun_barrel, this.tw, good);//左侧第一颗
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x - 2 * interval, y, this.gun_barrel, this.tw, good);//左侧第2颗
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			break;
		case UR://右上
			bt = new Bullet(x + (int)(interval / Math.sqrt(2.0)), 
					y + (int)(interval / Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x + (int)(interval * Math.sqrt(2.0)), 
					y + (int)(interval * Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x - (int)(interval / Math.sqrt(2.0)), 
					y - (int)(interval / Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x - (int)(interval * Math.sqrt(2.0)), 
					y - (int)(interval * Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			break;
		case RR://右
			bt = new Bullet(x, y  + interval, this.gun_barrel, this.tw, good);//朝右，x偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x, y  + 2 * interval, this.gun_barrel, this.tw, good);//朝右，x偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x, y  - interval, this.gun_barrel, this.tw, good);//朝右，x偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x, y  - 2 * interval, this.gun_barrel, this.tw, good);//朝右，x偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			break;
		case RD://右下
			bt = new Bullet(x - (int)(interval / Math.sqrt(2.0)), 
					y + (int)(interval / Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x - (int)(interval * Math.sqrt(2.0)), 
					y + (int)(interval * Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x + (int)(interval / Math.sqrt(2.0)), 
					y - (int)(interval / Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x + (int)(interval * Math.sqrt(2.0)), 
					y - (int)(interval * Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			break;
		case DD://下
			bt = new Bullet(x  - interval, y, this.gun_barrel, this.tw, good);//朝下，y偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x  - 2 * interval, y, this.gun_barrel, this.tw, good);//朝下，y偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x  + interval, y, this.gun_barrel, this.tw, good);//朝下，y偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x  + 2 * interval, y, this.gun_barrel, this.tw, good) ;//朝下，y偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			break;
		case LD://左下，同右上
			bt = new Bullet(x + (int)(interval / Math.sqrt(2.0)), 
					y + (int)(interval / Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x + (int)(interval * Math.sqrt(2.0)), 
					y + (int)(interval * Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x - (int)(interval / Math.sqrt(2.0)), 
					y - (int)(interval / Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt =  new Bullet(x - (int)(interval * Math.sqrt(2.0)), 
					y - (int)(interval * Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			break;
		case LL://左，同右
			bt = new Bullet(x, y  + interval, this.gun_barrel, this.tw, good);//朝右，x偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x, y  + 2 * interval, this.gun_barrel, this.tw, good);//朝右，x偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x, y  - interval, this.gun_barrel, this.tw, good);//朝右，x偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x, y  - 2 * interval, this.gun_barrel, this.tw, good);//朝右，x偏移
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			break;
		case LU://左上，同右下
			bt = new Bullet(x - (int)(interval / Math.sqrt(2.0)), 
					y + (int)(interval / Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x - (int)(interval * Math.sqrt(2.0)), 
					y + (int)(interval * Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x + (int)(interval / Math.sqrt(2.0)), 
					y - (int)(interval / Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			bt = new Bullet(x + (int)(interval * Math.sqrt(2.0)), 
					y - (int)(interval * Math.sqrt(2.0)),this.gun_barrel, this.tw, good);
			tw.bullets.add(bt);
			tw.tc.sendMsg(new NewBulletMsg(tw, bt));
			break;
		}
	}
	public void fire3(int x, int y)//因为只有我方坦克才有发射超级炮弹的特权，默认就是好蛋
	{
		Bullet bt = new Bullet(x, y, this.gun_barrel, this.tw, good);//传递的不再是炮筒方向,而是8个方向
		tw.bullets.add(bt);//子弹发射方向跟炮筒方向一致
		tw.tc.sendMsg(new NewBulletMsg(tw, bt));
		
	}
	
	public void superFire(int missleType)//超级炮弹的类型：1为太阳之泪[A]；2为排山倒海[S]，3为地雷torpedo【D】
	{
		switch(missleType)
		{
		case 1:
			Direction[] dirs = Direction.values();
			for(int i = 0; i < 8; ++ i)   tw.bullets.add(fire1(dirs[i]));//这样会朝指定方向连发3颗，因为fire方法里发出一颗同时保存到子弹库一颗
			//然后发出后回到superFire函数又存一颗到子弹库中
			break;
		case 2:
			//造出排山倒海,一排5发针形暗器
			fire2();//和炮筒方向垂直，fire2内部造出5颗子弹
			break;
		case 3://将炮弹的起始位置摆成sin形，然后朝弹筒方向直线前进
			break;///////////////还有bug，暂停使用
		}
	}

	/**
	 * 用图片的属性（长宽）
	 * 来设置碰撞检测的区域
	 * @return
	 */
	public Rectangle getRect()
	{
		return new Rectangle(x, y, imgTank[0].getWidth(null), imgTank[0].getHeight(null));
	}

	public boolean hitWall(Wall wall)//判断是否撞墙
	{
		return (this.getRect().intersects(wall.getRec()) && this.live);
	}

	public boolean hitIlk(Tank tk)//是否撞到同类
	{
		if(tk.live && this.live)
			return this.getRect().intersects(tk.getRect());
		return false;
	}

	public boolean hitEnergyBlock(EnergyBlock eb)//是否碰到能量块
	{
		if(eb.isLive())
			if(this.isLive())
				return this.getRect().intersects(eb.getRec());
		return false;
	}
	
	public boolean hitTor(Torpedo tor)//是否触雷
	{
		if(this.live)
			if(tor.isLive())
				if(this.good != tor.isGood())
					return this.getRect().intersects(tor.getRect());
		return false;
	}
	//坦克撞墙后回到上个位置
	public void back()
	{
		if(good)
		{
			//bU = bD = bL = bR = false;
			dir = Direction.STOP;//我方坦克，停止，直到换另一个方向
		}
		else//敌方坦克，回到上步，自己随机选择下个方向
		{
			x = preX;
			y = preY;

		}
	}
	///////////////////////为坦克增加一个血条类
	private class BloodBar
	{
		public void drawBar(Graphics g)//把血条画在坦克正上方
		{
			Color c = g.getColor();
			g.setColor(Color.RED);
			g.drawRect(x, y - 10, imgTank[0].getWidth(null), 10);//外围的空心条，在坦克正上方
			int leftWIDTH = imgTank[0].getWidth(null) * blood / totalBlood;
			g.fillRect(x, y - 10, leftWIDTH, 10);//内部实心条，根据剩余血量来画
			g.setColor(c);
		}
	}
}

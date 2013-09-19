import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;//由于awt中也有list，所以要知名引入util包中的list

/**
 * @author pzjay
 * @version Beta3.0
 * @see
 * 完善版本。将地雷产生和爆炸的消息以及坦克重生的消息
 * 进行转发
 */
public class TankWar
{
	public static void main(String[] args) 
	{
		new TankFrame().lanchFrame();
	}

}
class TankFrame extends Frame
{
	public static final int WINDOW_WIDTH = 800;
	public static final int WINDOW_HEIGHT = 600;
	private Image vImage = null;//虚拟屏幕上待绘的图像
	public static final String sep = File.separator;
	/**
	 * new一个对话框出来
	 */
	linkDialog linkSetDialog = new linkDialog();
	/**
	 * 建立一个客户端socket连接
	 */
	public TankWarClient tc = new TankWarClient(this);
	//游戏元素
	private Wall wall0 = new Wall(300, 200, 0, this);
	private Wall wall1 = new Wall(100, 400, 1, this);//水平

	private EnergyBlock energy0 = new EnergyBlock(this, true), 
	energy1 = new EnergyBlock(this, false);//生产出一个增加声明的能量块和一个减少生命的能量块
	private Torpedo torpedo = null;//地雷

	Tank tank0 = new Tank(550, 550, Direction.STOP, this);//生产出一辆我方坦克
	List<Crump> crumps = new ArrayList<Crump>();
	List<Bullet>  bullets = new ArrayList<Bullet>();//存放炮弹的数组
	List<Tank> tanks = new ArrayList<Tank>();
	List<Torpedo> torpedos = new ArrayList<Torpedo>();
	/**
	 * 播放的音乐文件路径
	 */
	List<String> musicPath = new ArrayList<String>();
	BkMusic backMusic = new BkMusic(musicPath, true, true);
	/**
	 * 背景图片
	 */
	private static Toolkit tk = Toolkit.getDefaultToolkit();
	private static Image bkImg = tk.getImage(TankWar.class.getClassLoader().getResource(
			"Images" + sep + "Mashimaro.gif"));

	public void lanchFrame()
	{
		/**
		 * 设置地方坦克出生时距离：避免彼此粘连
		 */
		int totalTank = Integer.parseInt(TankProperties.getProperty("totalTank"));//从配置文件获取坦克数量
		int tankSpace = Integer.parseInt(TankProperties.getProperty("tankSpace"));//坦克间平均间距
		/**
		 * 从配置文件获取播放的音乐文件,
		 * 注意配置文件中字符串不用双引号
		 */
		String songFile = TankProperties.getProperty("songFile");
		musicPath.add(songFile);
		//backMusic.start();//////////////////背景音乐暂停播放

		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		this.setLocation(100, 100);
		this.setResizable(false);
		this.setVisible(true);
		this.setBackground(Color.BLACK);
		/**
		 * 为主窗口设计菜单
		 */
		MenuBar mb = new MenuBar();
		Menu linkMenu = new Menu("Connect to server");
		MenuItem linkItem = new MenuItem("连接");
		linkMenu.add(linkItem);
		linkItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				linkSetDialog.setVisible(true);
			}
		});
		mb.add(linkMenu);
		this.setMenuBar(mb);

		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e) 
			{
				System.exit(0);
			}

		});
		this.addKeyListener(new KeyMonitor());//添加键盘监听器类

		new Thread(new FrameThread()).start();//启动重画线程
		/**
		 * 重画线程启动后，发起向服务器端的连接
		 */
		//tc.connect(TankWarServer.SERVER_IP, TankWarServer.TCP_PORT);
	}

	/**
	 * 调用repaint后，会先调用update，再调用paint，为了消除闪烁，
	 *
	 * 我们在update里在paint调用前利用虚拟屏幕
	 * 实现双缓冲
	 */

	public void update(Graphics g)
	{
		if(null == vImage)
		{
			vImage = this.createImage(WINDOW_WIDTH, WINDOW_HEIGHT);
		}
		Graphics vPen = vImage.getGraphics();//获取当前窗口的画笔，用于在在
		//虚拟屏幕上绘制vImage
		Color tpColor = vPen.getColor();
		vPen.setColor(Color.BLACK);//首先设置背景的颜色,用以画完后达到覆盖背景的效果
		vPen.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);//画一个跟窗口等大的矩形，达到窗口重画的目的
		paint(vPen);//借用一下paint方法，完成虚拟图形的绘制
		vPen.setColor(tpColor);
		g.drawImage(vImage, 0, 0, null);//将画好的虚拟图像整体推送到前台屏幕，达到消除闪烁目的
	}
	public void paint(Graphics g) 
	{
		Color c = g.getColor();
		/**
		 * 根据坦克好坏用不用颜色画出其ID
		 */
		if(tank0.good)  g.setColor(Color.BLUE);
		else  g.setColor(Color.GREEN);
		if(tank0.isLive())  g.drawString("id: " + tank0.tankID, tank0.x + 5, tank0.y + 55);
		g.setColor(Color.RED);
		g.drawString("Pz作品，必属赝品", 370, 590);
		g.setColor(c);
		/**
		 * 流氓兔
		 */
		g.drawImage(bkImg, 0, 530, null);
		g.drawImage(bkImg, 740, 530, null);

		for(int i = 0; i < bullets.size(); ++ i)//一粒粒画出来  
		{
			Bullet tp = bullets.get(i);
			if(tp.hitWall(wall0) || tp.hitWall(wall1))  tp.setLive(false);//子弹撞墙就over
			if(false == tp.getLive())  continue;//当前子弹已死，忽略
			/**
			 * 判断自己的主战坦克是否被敌方炮弹击中
			 */
			if(tp.hitTank(tank0) && true == tank0.isLive())//我方坦克被击中，血量减10，敌方的直接over
			{
				tank0.blood -= 10;
				/**
				 * 子弹死亡的消息;注意死亡的坦克是当前类的tank0，
				 * 而死亡的子弹是子弹的大管家的tank0的子弹
				 */
				tp.setLive(false);
				tc.sendMsg(new DeadBulletMsg(tp.tw.tank0.tankID, tp.id));
				if(tank0.blood <= 0)
				{	
					tank0.setLive(false);
					tank0.isDead();
					/**
					 * 发送一条坦克自己死亡的消息
					 */
					tc.sendMsg(new DeadTankMsg(tank0.tankID));
					//GameOver();//弹出提示框
				}
			}
			/**
			 * 然后判断自己方的炮弹是否击中了敌方的坦克，
			 * 倒不是为了处理敌方坦克死亡事件，而是为了处理自己家的子弹的死亡事件
			 */
			for(int j = 0; j < tanks.size(); ++ j)
			{
				Tank tk = tanks.get(j);
				if(tp.hitTank(tk) && true == tk.isLive())//子弹打到坦克
				{
					/**
					 * 这里我们只处理炮弹死亡的消息，因为坦克死亡的消息可由自己处理--上面的部分】
					 */
					//tk.setLive(false);
					//tk.isDead();
					
					tp.setLive(false);
					break;
				}
			}
			if(false == tp.getLive())  continue;//当前子弹已死，忽略
			else tp.drawBullet(g);
		}
		for(int i = 0; i < torpedos.size(); ++ i)
		{
			Torpedo tor = torpedos.get(i);
			if(!tor.isLive())  continue;
			/**
			 * 首先判断自己有没有碰到敌方地雷
			 */
			if(tank0.good != tor.isGood())
				if(tank0.hitTor(tor))
				{
					tank0.setLive(false);
					tor.setLive(false);
					tc.sendMsg(new DeadTorpMsg(tor.tw.tank0.tankID, tor.id));
					
					torpedos.remove(tor);
					crumps.add(new Crump(tank0.x, tank0.y, this));//坦克触雷，引发爆炸

					tank0.setLive(false);
					tank0.isDead();
					/**
					 * 发送一条坦克自己死亡的消息
					 */
					tc.sendMsg(new DeadTankMsg(tank0.tankID));
					break;//不能连着炸
				}
			/**
			 * 然后判断自己坦克库中的其他坦克,
			 * 同子弹的处理，由于自己的死亡事件已在上面处理，这里处理的只是地雷的死亡事件
			 */
			for(int j = 0; j < tanks.size(); ++ j)//判断敌军坦克是否踩雷
			{
				Tank tk = tanks.get(j);
				if(tk.isLive())
					if(tor.isGood() != tk.good)
						if(tk.hitTor(tor))
						{
							tk.setLive(false);
							tk.isDead();
							
							tor.setLive(false);
							tc.sendMsg(new DeadTorpMsg(tor.tw.tank0.tankID, tor.id));
							torpedos.remove(tor);
							crumps.add(new Crump(tk.x, tk.y, this));//坦克触雷，引发爆炸
							break;//不能连着炸
						}
			}
		}
		for(int i = 0; i < crumps.size(); ++ i)//画爆炸
		{
			Crump crump = crumps.get(i);
			if(crump.isLive())  crump.drawCrump(g);
			else  crumps.remove(crump);//一个爆炸周期结束
		}
		if(tank0.hitWall(wall0) || tank0.hitWall(wall1)) tank0.back(); 
		if(tank0.hitEnergyBlock(energy0))//假如我方坦克吃了好能量块，则生命值恢复
		{
			tank0.blood = tank0.totalBlood;
		}
		if(tank0.hitEnergyBlock(energy1))//假如我方坦克吃了坏能量块，则生命值减半
		{
			tank0.blood /= 2;
		}
		Color color = g.getColor();
		g.setColor(Color.BLUE);
		if(tank0.isLive())  tank0.drawTank(g);
		g.setColor(color);
		/**
		 * 画出自己坦克库中的坦克。
		 * 由于自己坦克库好坏坦克均有，故画ID的时候要
		 * 视情况而定
		 */
		for(int i = 0; i < tanks.size(); ++ i)
		{
			Tank tk = tanks.get(i);
			if(tk.isLive())
			{
				Color cc = g.getColor();
				if(tk.good)  g.setColor(Color.BLUE);
				else  g.setColor(Color.GREEN);
				g.drawString("id: " + tk.tankID, tk.x + 5, tk.y + 55);
				tk.drawTank(g);
				g.setColor(cc);
			}
		}
		for(int i = 0; i < torpedos.size(); ++ i)
		{
			Torpedo tor = torpedos.get(i);
			if(tor.isLive())  tor.drawTor(g);
		}
		wall0.drawWall(g);
		wall1.drawWall(g);
		if(energy0.isLive())  energy0.drawBlock(g);
		if(energy1.isLive())  energy1.drawBlock(g);
	}


	//建立一个内部线程类，专门用来重画TankFrame窗口【专一服务，用内部类】
	private class FrameThread implements Runnable
	{
		public void run()
		{
			while(true)
			{
				repaint();//调用外部包装类的重画方法
				try{
					Thread.sleep(50);
				}catch(InterruptedException ie){
					System.out.println("Repaint thread's sleep is interrupted!");
				}
			}
		}
	}
	//建立键盘监听内部类，由于代码较长，不用匿名类；由于要经常访问成员变量且此类跟
	//窗口类之外的类没关系，所以设成内部类
	private class KeyMonitor extends KeyAdapter
	{
		@Override
		public void keyReleased(KeyEvent e) //当坦克还存活的时候才对按键做出响应
		{
			if(tank0.isLive()) tank0.tankStop(e);//键抬起，坦克停止
		}

		@Override
		public void keyPressed(KeyEvent ke) 
		{
			if(tank0.isLive()) tank0.tankMove(ke);
			else if(ke.VK_F2 == ke.getKeyCode())
			{
				tank0.blood = tank0.totalBlood;
				tank0.setLive(true);//复活键
				/**
				 * 然后发送一个自己复活的消息;
				 * 注意内部类访问外部类对象的方法
				 */
				tc.sendMsg(new RebornTankMsg(tank0, TankFrame.this));//传入复活对象和地点
			}
		}
	}


	public void GameOver()
	{
		Frame overF = new TankFrame();
		overF.setSize(200, 100);
		overF.setLocation(400, 400);
		overF.setBackground(Color.RED);
		overF.setTitle("Game Over!");
		overF.setVisible(true);
		overF.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent ae)
			{
				try {
					System.exit(0);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}

		});
	}
	/**
	 * 连接设置对话框内部类.
	 * 对话框类必须指定构造函数，因为对话框内部类未定义无参构造函数
	 */
	private class linkDialog extends Dialog
	{
		Button OKbtn = new Button("OK");
		TextField tfIP = new TextField("127.1", 14);
		TextField tfPort = new TextField("" + TankWarServer.TCP_PORT, 4);
		TextField tfUDP_Port = new TextField(4);
		public linkDialog()
		{
			super(TankFrame.this, true);
			this.setLayout(new FlowLayout());
			this.add(new Label("Server IP"));
			this.add(tfIP);
			this.add(new Label("Server Port"));
			this.add(tfPort);
			this.add(new Label("Client UDP Port"));
			this.add(tfUDP_Port);
			this.add(OKbtn);
			this.setLocation(300, 300);
			this.pack();
			this.addWindowListener(new WindowAdapter()
			{
				/**
				 * 关闭窗口即是设置不显示对话框
				 */
				@Override
				public void windowClosing(WindowEvent e) 
				{
					setVisible(false);
				}

			});
			OKbtn.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					String ip = tfIP.getText().trim();
					int serverPort = Integer.parseInt(tfPort.getText().trim());
					int udpPort = Integer.parseInt(tfUDP_Port.getText().trim());
					tc.setCLIENT_UDP_PORT(udpPort);
					tc.connect(ip, serverPort);
					setVisible(false);
				}
			});
		}
	}
}

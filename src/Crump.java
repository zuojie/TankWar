
import java.awt.*;
import java.io.File;
/**
 * 
 * @author pzjay
 * 爆炸类，当坦克遇上地雷，产生爆炸
 * 属性包括：爆炸发生位置，爆炸生存标示符，大管家
 * 方法包括：画出自己
 */
public class Crump 
{
	int x, y;
	private static TankFrame tw;
	private boolean live = true;
	private int step = 0;
	/**
	 * 加入图片，由于加载图片有个缓冲的过程，所以在爆炸发生前首先在可视化区域外[-1, -1]
	 * 画一次爆炸过程，使图像完全加载到内存
	 */
	private static boolean initImg = false;
	private static Toolkit tk = Toolkit.getDefaultToolkit();//调用系统的API读入图片，实现跨平台性，为啥用static，你懂的
	/**
	 * 加入11张爆炸图片
	 */
	private static Image[] imgCrump = {
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "0.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "1.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "2.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "3.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "4.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "5.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "6.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "7.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "8.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "9.gif")),
		tk.getImage(Crump.class.getClassLoader().getResource("Images" + tw.sep + "10.gif"))
	};
	
	public Crump(int x, int y, TankFrame tw) 
	{
		this.x = x;
		this.y = y;
		this.tw = tw;
	}
	
	public boolean isLive() 
	{
		return live;
	}

	public void setLive(boolean live)
	{
		this.live = live;
	}

	public void drawCrump(Graphics g)
	{
		if(!initImg)
		{
			initImg = true;
			for (int j = 0; j < imgCrump.length; j++) {
				g.drawImage(imgCrump[j], -1, -1, null);
			}
		}
		if(step >= imgCrump.length)
		{
			live = false;
			return;
		}
		g.drawImage(imgCrump[step], x, y, null);
		++ step;
	}
}

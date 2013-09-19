import javax.media.*;//添加外部多媒体类
import java.util.*;
import java.io.*;

/**
 * 
 * @author pzjay
 * 播放类属性包括播放对象
 * 播放文件所在路径
 * 是否循环
 */
public class BkMusic implements ControllerListener
{
	//播放对象
	public Player player;
	public boolean first, loop;
	//public String path;//单一文件的时候用
	public List<String> music;//存放多个音乐文件的路径
	public int musicNo = 0;
	
	public BkMusic(List<String> music, boolean first, boolean loop)
	{
		this.loop = loop;
		this.first = first;
		this.music = music;
	}
	
	public void start()
	{
		try{
			player = Manager.createPlayer(
					new File(music.get(musicNo)).toURI().toURL());
		}catch(Exception e)
		{
			System.out.println("file failed");
			//e.printStackTrace();
		}
		if(null == player)
		{
			System.out.println("播放文件为空");
			return;
		}
		player.addControllerListener(this);
		player.prefetch();//提取媒体内容
	}
	public void controllerUpdate(ControllerEvent ce)
	{
		if(ce instanceof EndOfMediaEvent)
		{
			++ musicNo;//跳到下一首
			if(musicNo >= music.size())  musicNo = 0;//循环播放
			this.start();
			return;
		}
		//假如提取媒体内容结束
		if(ce instanceof PrefetchCompleteEvent)
		{
			player.start();
			return;
		}
		if(ce instanceof RealizeCompleteEvent)
		{
			//实例化结束
		//	pack();
			return;
		}
	}
}

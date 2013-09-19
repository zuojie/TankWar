import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author pzjay
 * TankWar配置管理类，是一个静态类
 */
public class TankProperties 
{
	/**
	 * 静态代码区，调用时只调用一份放到内存
	 * 大伙都能用，并且效率高
	 * 把构造函数声明成私有即可防止其他类中对本类的构造
	 * 因为静态代码只需一份即可
	 * 
	 */
	static String sep = File.separator;
	static Properties props = new Properties();//配置文件管理类
	static{
		try{
			props.load(TankProperties.class.getClassLoader().getResourceAsStream(
					"config" + sep + "tank.properties"));
		}catch(IOException ie){
			ie.printStackTrace();
		}
	}
	/**
	 * 声明成私有起预防保护作用
	 */
	private TankProperties(){};
	/**
	 * 外部调用配置文件管理类的接口
	 */
	public static String getProperty(String key)
	{
		return props.getProperty(key);
	}
}

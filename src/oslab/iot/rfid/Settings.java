package oslab.iot.rfid;

public class Settings {

	   public static final int RSSIMODE = 1; //需判断信号量强度  多个读头同时读取一个RFID情况 
	   public static final int NORSSIMODE = 2; 
	   public static int workMode = RSSIMODE;
	   
	   public static final int SPAN_CONNECT=5*1000;//Reader connect 不断尝试连接周期
	   public static final int SPAN_READ = 2*1000;//RFID Reader读取周期
	 
	   public static final int DELAY_JUDGE = 2*1000; //RFID SOURCE JUDGE延迟时间
	   public static final int SPAN_JUDGE = 4*1000; //RFID SOURCE JUDGE处理周期
	   
	   public static final int SPAN_DEAL_DIFF = 6*1000; //RFID STATUS DEAL CHANGE RFID状态变换信息处理周期
	   
	   public static final int THRESHOLD_IS_OUT = 60*1000; //判断是否离开监听范围
	   
	   public static final int SPAN_ROUTINE = 30*60*1000; //例行插入状态信息  周期 30*60*1000
	   public static final int DELAY_ROUTINE = 30*60*1000; //例行插入状态信息 延迟时间 30*60*1000
	   
	   public static final int ROUTINE_ALL_IN = 1;
	   public static final int ROUTINE_AVAIL_IN = 2;
	   public static int routineMode = ROUTINE_ALL_IN;
	   

}

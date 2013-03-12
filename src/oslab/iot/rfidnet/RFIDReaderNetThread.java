package oslab.iot.rfidnet;

import java.io.*;
import java.util.*;

import oslab.iot.db.DbOperation;
import oslab.iot.rfid.RFIDStatusJudge;
import oslab.iot.rfid.Settings;
import oslab.iot.rfid.SignalData;
import oslab.iot.util.Debug;
import oslab.iot.util.UnicodeFormatter;

/**
 * RFIDReaderNetThread 
 * 使用net口进行传输 
 * 需增加功能 检测读头是否存活   
 * @author cg
 * 在SerialBean上封装的类   
 * 1 构造函数
 * 2 run  功能：1） 判断是否存活 2） 读取数据 
 * 3 read
 * 
 */
public class RFIDReaderNetThread extends Thread {

   //READER 状态
	protected static final int READER_STATUS_ON = 1;
	protected static final int READER_STATUS_OFF= 0;
   // 判断模式  
//   public static int RSSIMODE = 1; //需判断信号量强度  多个读头同时读取一个RFID情况 
//   public static int NORSSIMODE = 2; 
//   public  int mode;       //废掉，在Settings类中统一设置
	

   
   private final String sStart = "RRRRR"; 
   private byte[] readBuffer = new byte[1024];
 
   
   private Hashtable<Integer,Date> ht_rfid_source_data; //时间
   private Hashtable<Integer,Integer> ht_status; 

   private Hashtable<Integer,SignalData> ht_rfid_source_data_New; // 
   private Hashtable<Integer,SignalData> ht_status_new; 

   private NetBean reader;
   protected int reader_status;  //读头状态
   protected int room;    //读头ID 使用room代替
   protected String addr;  //读头 net地址
   protected int port;     //读头 port
   
   private final boolean debug_flag = true;
   private Debug debug = null;
 
   /**
    * constructor  for RFIDReader 
    * @param i
    * 
    */
   public RFIDReaderNetThread(String addr,int port,int room,Hashtable<Integer,Integer> hd_status){
		this.ht_status = hd_status;
		ht_rfid_source_data = new Hashtable<Integer,Date>();
		
//		ht_rfid_source_data_New = new Hashtable<Integer,SignalData>(); //??? no use?
		
		reader = new NetBean();
		
        this.addr = addr;
        this.port = port;
		this.room = room;
		
		
   }
   public RFIDReaderNetThread(String addr,int port,int room,Hashtable<Integer,SignalData> hd_status_new,int mode){
		
	   	this.ht_status_new = hd_status_new;
		ht_rfid_source_data_New = new Hashtable<Integer,SignalData>(); //
		
		reader = new NetBean();
		
		this.addr = addr;
		this.port = port;
		this.room = room;
		
		debug = new Debug(debug_flag);
		
  }
   public void run(){
	   System.out.println("reader  "+this.room+" "+this.addr+" started");
	
	   //1 启动一线程  对已读取的<tag,time>进行判断
	   Timer t = new Timer();
	   if(Settings.workMode == Settings.NORSSIMODE){
		   t.schedule(new RFIDStatusJudge(ht_rfid_source_data,ht_status,room), Settings.DELAY_JUDGE,Settings.SPAN_JUDGE);
	   }else{
		   t.schedule(new RFIDStatusJudge(ht_rfid_source_data_New,ht_status_new), Settings.DELAY_JUDGE,Settings.SPAN_JUDGE);
	   }
	   
	   //2 主线程进行 接收读头数据 
	   byte[] bstart = sStart.getBytes();
	   //2.1 判断是否成功连接读头
	    if(reader.connectHost(addr, port)){
	    	reader_status = READER_STATUS_ON;
	    	readerStatusIntoDB(); // reader status into db 第一次成功
	    	try {
				reader.writePort(bstart);  //发送 RRRRR以启动 读头 
				for (int i = 0; i < bstart.length; i++) {
					System.out.print(UnicodeFormatter.byteToHex(bstart[i]) + "  ");
				}
				System.out.println();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
	    }else
	    {
	    	reader_status = READER_STATUS_OFF;
	    	readerStatusIntoDB(); // reader status into db 第一次失败 
	    }
	   //2.2 读取数据 
	     
	   for(;;){
		   //2.2.1 正常读取  两步 先读数 ； 再等2秒
		   if(READER_STATUS_ON == reader_status){ //如果 在线 那么开始读取 
			   //先判断是否ping通 
			   if(!NetBean.isHostAlive(this.addr)){
				   System.out.println("Reader "+ this.addr+" get out - "+new Date());
				   reader_status = READER_STATUS_OFF; // 如果出错 那么准备重启
				   readerStatusIntoDB(); // reader status into db 成功中失败
				   continue;
			   }
				try {
					this.read(); // 读取数据
				} catch (Exception e1) {
					System.out.println(e1.toString());
					reader_status = READER_STATUS_OFF; // 如果出错 那么准备重启
					readerStatusIntoDB(); // reader status into db 成功中失败
				}
				try {
					Thread.sleep(Settings.SPAN_READ); //2s后再读
				} catch (Exception e) {
				}
		   }else{
			   //2.2.2 非正常情况 不断尝试连接读头
			   try {
					Thread.sleep(Settings.SPAN_CONNECT); //2s后再读
				} catch (Exception e) {
				}
				//尝试连接
				if(reader.connectHost(addr, port)){
					System.out.println("Reader "+ this.addr+" get on - "+new Date());
			    	reader_status = READER_STATUS_ON;
			    	readerStatusIntoDB(); // reader status into db 失败中成功
			    	try {
						reader.writePort(bstart);  //发送 RRRRR以启动 读头 
					} catch (Exception e) {
						// TODO: handle exception
					}
			    }else
			    {
			    	reader_status = READER_STATUS_OFF;
			    }
		   }
	   }
   }
   /**
    *  rfidReader  STatus into db
    */
   public synchronized void readerStatusIntoDB(){
	   DbOperation dbo = new DbOperation();
	   dbo.connOracle();
	   String ssql = "merge into readerstatus a " +
	   		         "using (select "+ this.room + " reader , "+this.reader_status +" status , sysdate checktime from dual) b " +
	   		         "on (a.reader = b.reader)" +
	   		         "when matched then " +
	   		         "    update set a.status = b.status ,a.checktime = b.checktime " +
	   		         "when not matched then " +
	   		         " insert (a.reader,a.status,a.checktime) values(b.reader,b.status,b.checktime) ";
	   dbo.execSqlImmediate(ssql);
	   dbo.closeImmediate();
   }
   public void read() throws Exception{
	   int len = reader.readPort(readBuffer);
	   
	   Parser p = new Parser(readBuffer,len);
	   
	   byte[] msg = new byte[256];
	   int msg_len; 
	   int rfid_flags_count = 0;
	   
	   while((msg_len=p.unpack(msg)) >0 ){
		   if(Settings.workMode == Settings.NORSSIMODE){
			   ht_rfid_source_data.put(new Integer(Parser.getTagID(msg,msg_len)), new Date());
			   rfid_flags_count++;
		   }else{
			   ht_rfid_source_data_New.put(new Integer(Parser.getTagID(msg,msg_len)), new SignalData(new Date(), Parser.getRSSI(msg, msg_len), room));
			   rfid_flags_count++;
		   }
	   }
	   
	   debug.println("*************reading rfid counts "+rfid_flags_count+" , has rfid flags : "+ht_rfid_source_data_New.size());
	   
   }
  
   
}

package oslab.iot.rfid;
import java.util.*;

import oslab.iot.util.Debug;

/**
 * function:
 * 	    将<rfid,date> 转化到 <rfid,room/status>
 * @author David
 * TO BE CONTINUED
 *  <<BUG 1>> 只修改新情况 而未修改常驻情况  需检查 逻辑  find@7.22 
 */

public class RFIDStatusJudge extends TimerTask {
    
	
	private int workMode;
	private static int THREHOLD;  //阀值 不在几秒 来判断在与不在 
	private static int NEARTHREHOLD = 120; //是否临近 
	
	private final boolean debug_flag = false;
	private Debug debug = null;
	
    private Hashtable<Integer,Date> ht_source;
    private Hashtable<Integer,Integer> ht_status;
    

    private Hashtable<Integer, SignalData> ht_source_New; //
    private Hashtable<Integer, SignalData> ht_status_new; //
    private int room;
   
    // 构造函数 
	public RFIDStatusJudge(Hashtable<Integer,Date> ht,Hashtable<Integer,Integer> ht_status,int room){
		THREHOLD = 60;  ///  阀值 *******************************
		this.ht_source = ht;
		this.ht_status = ht_status;
		this.room = room;
		debug = new Debug(debug_flag);
	}
	
	public RFIDStatusJudge(int threhold,Hashtable<Integer,Date> ht,Hashtable<Integer,Integer> ht_status,int room){
		THREHOLD = threhold;
		this.ht_source = ht;
		this.ht_status = ht_status;
		this.room = room;
		debug = new Debug(debug_flag);
	}
	/// to change for  RSSI
	public RFIDStatusJudge(Hashtable<Integer,SignalData> htNew, Hashtable<Integer, SignalData> ht_status_new){
		THREHOLD = 60;  ///  阀值 *******************************
		this.ht_source_New = htNew;
		this.ht_status_new = ht_status_new;
		debug = new Debug(debug_flag);
	}
	
	private boolean isON(Date now,Date s){
		long ms = ( now.getTime() - s.getTime() ) / 1000;
		return  ( ms < THREHOLD ) ? true : false;
		
	}
	private boolean isNear(Date now,Date s){
		long ms = ( now.getTime() - s.getTime() ) / 1000;
		return  ( ms < NEARTHREHOLD  ) ? true : false;
		
	}
	@Override 
	public void run(){
		Date now = new Date();
		Enumeration<Integer> em ;
		
//		if(this.workMode == RFIDReaderNetThread.NORSSIMODE){//NO RSSIMODE 不用管信号量强度 
		if(Settings.workMode == Settings.NORSSIMODE){
			em = this.ht_source.keys();
			while(em.hasMoreElements()){
				Integer key = em.nextElement();
					if(isON(now,this.ht_source.get(key))){
						ht_status.put(key, room);
					}else                         // <<BUG 1>>
					{
						ht_status.put(key, -1);
//						ht_source.remove(key);// 如果本读写头发现此KEY不在了 则在本地管理区删除 bug 
					}
			}
			this.ht_source.clear();  //******处理后删除源数据*****
			
		}else{
			em = this.ht_source_New.keys();
			while(em.hasMoreElements()){
				Integer key = em.nextElement();
					//1 时间判断是否存活
					 if(isON(now,this.ht_source_New.get(key).readTime)){
						//1.1 判断 信号量强度 ===================待定 =============
						 SignalData sd = ht_status_new.get(key);
						 //1.1.1 如果为空 则插入
						//1.1.2.1  如果不为空  旧的信号量强  且时间很近  比如2分钟之内 则不做操作 
						//1.1.2.2  如果不为空  新的信号量强  且时间很近   则插入新值
						//1.1.2.3  如果不为空  时间比较长 则插入新值
						 if(sd == null){  // 无先例
							 ht_status_new.put(key, this.ht_source_New.get(key));
						 }else{  // 有先例
							 if(sd.readerId == this.ht_source_New.get(key).readerId){ //同一 reader情况
							    ht_status_new.get(key).rssi = (sd.rssi+this.ht_source_New.get(key).rssi)/2;
							    ht_status_new.get(key).readTime= this.ht_source_New.get(key).readTime;
							 }else{  //不是同一个reader
								 if(isNear(this.ht_source_New.get(key).readTime, sd.readTime)){ //如果时间比较近
									 if(this.ht_source_New.get(key).rssi>sd.rssi)
										 ht_status_new.put(key, this.ht_source_New.get(key));
								 }else{ //时间比较远
									 ht_status_new.put(key, this.ht_source_New.get(key));
								 }
							 }
						 } //end of is on
						 
					 }else                         // <<BUG 1>>
						{
							ht_status_new.put(key, new SignalData(new Date(), -100, -1));
//							ht_source_New.remove(key);// 如果本读写头发现此KEY不在了 则在本地管理区删除 bug 
						} //end of is NOT on
					  
				}//end of while
			debug.println("------------------ "+ ht_source_New.size()+" rfid source dealed");
			ht_source_New.clear();//******处理后删除源数据*****
			debug.println("================== "+ ht_status_new.size()+" rfid status remains");
			if(ht_source_New.get(14502)==null)
				debug.sysout("--- after status judge -- 14502 in source  is null");
			else
				debug.sysout("--- after status judge -- 14502 in source "+ht_source_New.get(14502).toString());
			if(ht_status_new.get(14502) == null)
				debug.sysout("=== after status judge -- 14502 in status is null");
			else
				debug.sysout("=== after status judge -- 14502 in status "+ht_status_new.get(14502).toString());
		}
		
		
		
		// 将<rfid,date> 转化到 <rfid,room(status)>
		
		
		
		
	}
}

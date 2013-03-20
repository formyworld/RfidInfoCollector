package oslab.iot.rfid;
import java.util.*;

import oslab.iot.util.Debug;

/**
 * function:
 * 	    ��<rfid,date> ת���� <rfid,room/status>
 * @author David
 * TO BE CONTINUED
 *  <<BUG 1>> ֻ�޸������ ��δ�޸ĳ�פ���  ���� �߼�  find@7.22 
 */

public class RFIDStatusJudge extends TimerTask {
    
	
	private int workMode;
	private static int THREHOLD;  //��ֵ ���ڼ��� ���ж����벻�� 
	private static int NEARTHREHOLD = 120; //�Ƿ��ٽ� 
	
	private final boolean debug_flag = false;
	private Debug debug = null;
	
    private Hashtable<Integer,Date> ht_source;
    private Hashtable<Integer,Integer> ht_status;
    

    private Hashtable<Integer, SignalData> ht_source_New; //
    private Hashtable<Integer, SignalData> ht_status_new; //
    private int room;
   
    // ���캯�� 
	public RFIDStatusJudge(Hashtable<Integer,Date> ht,Hashtable<Integer,Integer> ht_status,int room){
		THREHOLD = 60;  ///  ��ֵ *******************************
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
		THREHOLD = 60;  ///  ��ֵ *******************************
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
		
//		if(this.workMode == RFIDReaderNetThread.NORSSIMODE){//NO RSSIMODE ���ù��ź���ǿ�� 
		if(Settings.workMode == Settings.NORSSIMODE){
			em = this.ht_source.keys();
			while(em.hasMoreElements()){
				Integer key = em.nextElement();
					if(isON(now,this.ht_source.get(key))){
						ht_status.put(key, room);
					}else                         // <<BUG 1>>
					{
						ht_status.put(key, -1);
//						ht_source.remove(key);// �������дͷ���ִ�KEY������ ���ڱ��ع�����ɾ�� bug 
					}
			}
			this.ht_source.clear();  //******�����ɾ��Դ����*****
			
		}else{
			em = this.ht_source_New.keys();
			while(em.hasMoreElements()){
				Integer key = em.nextElement();
					//1 ʱ���ж��Ƿ���
					 if(isON(now,this.ht_source_New.get(key).readTime)){
						//1.1 �ж� �ź���ǿ�� ===================���� =============
						 SignalData sd = ht_status_new.get(key);
						 //1.1.1 ���Ϊ�� �����
						//1.1.2.1  �����Ϊ��  �ɵ��ź���ǿ  ��ʱ��ܽ�  ����2����֮�� �������� 
						//1.1.2.2  �����Ϊ��  �µ��ź���ǿ  ��ʱ��ܽ�   �������ֵ
						//1.1.2.3  �����Ϊ��  ʱ��Ƚϳ� �������ֵ
						 if(sd == null){  // ������
							 ht_status_new.put(key, this.ht_source_New.get(key));
						 }else{  // ������
							 if(sd.readerId == this.ht_source_New.get(key).readerId){ //ͬһ reader���
							    ht_status_new.get(key).rssi = (sd.rssi+this.ht_source_New.get(key).rssi)/2;
							    ht_status_new.get(key).readTime= this.ht_source_New.get(key).readTime;
							 }else{  //����ͬһ��reader
								 if(isNear(this.ht_source_New.get(key).readTime, sd.readTime)){ //���ʱ��ȽϽ�
									 if(this.ht_source_New.get(key).rssi>sd.rssi)
										 ht_status_new.put(key, this.ht_source_New.get(key));
								 }else{ //ʱ��Ƚ�Զ
									 ht_status_new.put(key, this.ht_source_New.get(key));
								 }
							 }
						 } //end of is on
						 
					 }else                         // <<BUG 1>>
						{
							ht_status_new.put(key, new SignalData(new Date(), -100, -1));
//							ht_source_New.remove(key);// �������дͷ���ִ�KEY������ ���ڱ��ع�����ɾ�� bug 
						} //end of is NOT on
					  
				}//end of while
			debug.println("------------------ "+ ht_source_New.size()+" rfid source dealed");
			ht_source_New.clear();//******�����ɾ��Դ����*****
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
		
		
		
		// ��<rfid,date> ת���� <rfid,room(status)>
		
		
		
		
	}
}

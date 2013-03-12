package oslab.iot.rfid;

import java.util.Hashtable;
import java.util.Timer;

import oslab.iot.rfidnet.RFIDReaderNetThread;

public class Controller {

	public static String S1435 = "192.168.3.90";
	public static String S1501 = "192.168.2.90";
	public static String S1537 = "192.168.2.70";
	public static String S1616 = "192.168.1.246";
	public static String S1621 = "192.168.1.229";
	
	public static int PORT  = 4001; 
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	    
	    Hashtable<Integer, SignalData> rfid_status_new = new Hashtable<Integer, SignalData>(); //source-get中用到
	    Hashtable<Integer,Integer> rfid_status_old = new Hashtable<Integer,Integer>(); //deal-diff中用到

	    Hashtable<String,String> rfid_reader_room = new Hashtable<String,String>(); 
	    
	    //******开启  rfid 读写 
	    RFIDReaderNetThread n1435 = new RFIDReaderNetThread(S1435, PORT, 1435, rfid_status_new, Settings.RSSIMODE);
	    n1435.start();
	    
	  //******开启 数据存取  变化信息 
	    RFIDStatusDealChange dc = new RFIDStatusDealChange(rfid_reader_room,rfid_status_old, rfid_status_new);
	    dc.start();
	    
	  ///****开启例行数据存入到数据库
	    Timer t_routine = new Timer();
	    t_routine.schedule(new RFIDStatusRoutine(rfid_reader_room, 
	    										rfid_status_new), 
	    					Settings.DELAY_ROUTINE,Settings.SPAN_ROUTINE);
	    
	}

}

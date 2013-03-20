package oslab.iot.rfid;

import java.util.Hashtable;
import java.util.Timer;

import oslab.iot.db.DBOConcurrence;
import oslab.iot.rfidnet.RFIDReaderNetThread;

public class Controller {

	public static String S1435 = "192.168.3.90";
	public static String S1501 = "192.168.2.90";
	public static String S1537 = "192.168.2.70";
	public static String S1616 = "192.168.1.246";
	public static String S1621 = "192.168.1.229";

	public static int PORT = 4001;

	private Hashtable<Integer, SignalData> rfid_status_new = new Hashtable<Integer, SignalData>(); // source-get中用到
	private Hashtable<Integer, Integer> rfid_status_old = new Hashtable<Integer, Integer>(); // deal-diff中用到
	private Hashtable<String, String> rfid_reader_room = new RFIDReaderRoom().get_rfid_reader_room();
	
	
	//============================================================================================

	public Controller() {
		DBOConcurrence.connOracle();
	}

	/**
	 * 开启rfid数据采集设备 这里采用网口进行采集 
	 * 	In: IP地址
	 *  Out: rfid_status_new <rfid,<date,rssi,reader>>
	 */
	public void beginRFIDDeviceReading() {
		// ******开启 rfid 读写
		RFIDReaderNetThread n1435 = new RFIDReaderNetThread(S1435, PORT, 1435,
				rfid_status_new, Settings.RSSIMODE);
		n1435.start();
	}

	/**
	 * 对获取的rfid_status_new进行分析，得出变化信息 
	 * IN: rfid_status_new ,rfid_status_old 
	 * changing info
	 */
	public void analyzeChangingStatus() {

		RFIDStatusDealChange dc = new RFIDStatusDealChange(rfid_reader_room,
				rfid_status_old, rfid_status_new);
		dc.start();
	}

	/**
	 * 以较大的周期（半小时，一小时）去获取每个rfid最新的状态，存储到数据库中 why:以防止出现很久未变化的情况
	 */
	public void routineStoreStatus() {
		Timer t_routine = new Timer();
		t_routine.schedule(new RFIDStatusRoutine(rfid_reader_room,
						rfid_status_new), Settings.DELAY_ROUTINE,
						Settings.SPAN_ROUTINE);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Controller controller = new Controller();
		
		controller.beginRFIDDeviceReading();
//		controller.analyzeChangingStatus();
//		controller.routineStoreStatus();

		/*
		 * Hashtable<Integer, SignalData> rfid_status_new = new
		 * Hashtable<Integer, SignalData>(); //source-get中用到
		 * Hashtable<Integer,Integer> rfid_status_old = new
		 * Hashtable<Integer,Integer>(); //deal-diff中用到
		 * 
		 * Hashtable<String,String> rfid_reader_room = new
		 * Hashtable<String,String>();
		 * 
		 * //******开启 rfid 读写 RFIDReaderNetThread n1435 = new
		 * RFIDReaderNetThread(S1435, PORT, 1435, rfid_status_new,
		 * Settings.RSSIMODE); n1435.start();
		 * 
		 * //******开启 数据存取 变化信息 RFIDStatusDealChange dc = new
		 * RFIDStatusDealChange(rfid_reader_room,rfid_status_old,
		 * rfid_status_new); dc.start();
		 * 
		 * ///****开启例行数据存入到数据库 Timer t_routine = new Timer();
		 * t_routine.schedule(new RFIDStatusRoutine(rfid_reader_room,
		 * rfid_status_new), Settings.DELAY_ROUTINE,Settings.SPAN_ROUTINE);
		 */
	}

}

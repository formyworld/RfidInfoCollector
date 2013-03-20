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

	private Hashtable<Integer, SignalData> rfid_status_new = new Hashtable<Integer, SignalData>(); // source-get���õ�
	private Hashtable<Integer, Integer> rfid_status_old = new Hashtable<Integer, Integer>(); // deal-diff���õ�
	private Hashtable<String, String> rfid_reader_room = new RFIDReaderRoom().get_rfid_reader_room();
	
	
	//============================================================================================

	public Controller() {
		DBOConcurrence.connOracle();
	}

	/**
	 * ����rfid���ݲɼ��豸 ����������ڽ��вɼ� 
	 * 	In: IP��ַ
	 *  Out: rfid_status_new <rfid,<date,rssi,reader>>
	 */
	public void beginRFIDDeviceReading() {
		// ******���� rfid ��д
		RFIDReaderNetThread n1435 = new RFIDReaderNetThread(S1435, PORT, 1435,
				rfid_status_new, Settings.RSSIMODE);
		n1435.start();
	}

	/**
	 * �Ի�ȡ��rfid_status_new���з������ó��仯��Ϣ 
	 * IN: rfid_status_new ,rfid_status_old 
	 * changing info
	 */
	public void analyzeChangingStatus() {

		RFIDStatusDealChange dc = new RFIDStatusDealChange(rfid_reader_room,
				rfid_status_old, rfid_status_new);
		dc.start();
	}

	/**
	 * �Խϴ�����ڣ���Сʱ��һСʱ��ȥ��ȡÿ��rfid���µ�״̬���洢�����ݿ��� why:�Է�ֹ���ֺܾ�δ�仯�����
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
		 * Hashtable<Integer, SignalData>(); //source-get���õ�
		 * Hashtable<Integer,Integer> rfid_status_old = new
		 * Hashtable<Integer,Integer>(); //deal-diff���õ�
		 * 
		 * Hashtable<String,String> rfid_reader_room = new
		 * Hashtable<String,String>();
		 * 
		 * //******���� rfid ��д RFIDReaderNetThread n1435 = new
		 * RFIDReaderNetThread(S1435, PORT, 1435, rfid_status_new,
		 * Settings.RSSIMODE); n1435.start();
		 * 
		 * //******���� ���ݴ�ȡ �仯��Ϣ RFIDStatusDealChange dc = new
		 * RFIDStatusDealChange(rfid_reader_room,rfid_status_old,
		 * rfid_status_new); dc.start();
		 * 
		 * ///****�����������ݴ��뵽���ݿ� Timer t_routine = new Timer();
		 * t_routine.schedule(new RFIDStatusRoutine(rfid_reader_room,
		 * rfid_status_new), Settings.DELAY_ROUTINE,Settings.SPAN_ROUTINE);
		 */
	}

}

package oslab.iot.rfid;

import java.util.*;

import oslab.iot.util.Debug;

/**
 * function: ��<rfid,date> ת���� <rfid,room/status>
 * 
 * @author David TO BE CONTINUED <<BUG 1>> ֻ�޸������ ��δ�޸ĳ�פ��� ���� �߼� find@7.22
 */

public class RFIDStatusJudge extends TimerTask {

	private int workMode;
	private static int THREHOLD; // ��ֵ ���ڼ��� ���ж����벻��
	private static int NEARTHREHOLD = 120; // �Ƿ��ٽ�

	private final boolean debug_flag = true;
	private Debug debug = new Debug(debug_flag);

	private Hashtable<Integer, Date> rfid_read_data_no_rssi;
	private Hashtable<Integer, Integer> rfid_status_no_rssi;

	private Hashtable<Integer, SignalData> rfid_read_data_with_rssi; //
	private Hashtable<Integer, SignalData> rfid_status_with_rssi; //
	private int room;

	// ���캯��
	@Deprecated
	public RFIDStatusJudge(Hashtable<Integer, Date> rfid_read_data_no_rssi,
			Hashtable<Integer, Integer> rfid_status_no_rssi, int room) {
		THREHOLD = 60; // / ��ֵ *******************************
		this.rfid_read_data_no_rssi = rfid_read_data_no_rssi;
		this.rfid_status_no_rssi = rfid_status_no_rssi;
		this.room = room;
	}

	public RFIDStatusJudge(int threhold,
			Hashtable<Integer, Date> rfid_read_data_no_rssi,
			Hashtable<Integer, Integer> rfid_status_no_rssi, int room) {
		THREHOLD = threhold;
		this.rfid_read_data_no_rssi = rfid_read_data_no_rssi;
		this.rfid_status_no_rssi = rfid_status_no_rssi;
		this.room = room;
	}

	// / to change for RSSI
	public RFIDStatusJudge(
			Hashtable<Integer, SignalData> rfid_read_data_with_rssi,
			Hashtable<Integer, SignalData> rfid_status_with_rssi) {
		THREHOLD = 60; // / ��ֵ *******************************
		this.rfid_read_data_with_rssi = rfid_read_data_with_rssi;
		this.rfid_status_with_rssi = rfid_status_with_rssi;
	}

	private boolean isON(Date now, Date s) {
		long ms = (now.getTime() - s.getTime()) / 1000;
		return (ms < THREHOLD) ? true : false;

	}

	private boolean isNear(Date now, Date s) {
		long ms = (now.getTime() - s.getTime()) / 1000;
		return (ms < NEARTHREHOLD) ? true : false;

	}

	/**
	 * statusJudge function: judge from rfid_read_data 1 insert into status 2
	 * refresh rfid_read_data
	 */
	@Deprecated
	public void statusJudge_no_rssi() {
		Date now = new Date();
		Enumeration<Integer> em;
		em = this.rfid_read_data_no_rssi.keys();
		/******************* 1 insert into status ***********************************/
		while (em.hasMoreElements()) {
			Integer key = em.nextElement();
			if (isON(now, this.rfid_read_data_no_rssi.get(key))) {
				rfid_status_no_rssi.put(key, room);
			} else {
				rfid_status_no_rssi.put(key, -1);
			}
		}
		/******************* 2 refresh rfid_read_data ***********************************/
		this.rfid_read_data_no_rssi.clear(); // ******�����ɾ��Դ����*****
	}

	/**
	 * statusJudge function: judge from rfid_read_data 
	 * 1 insert into status 
	 * 2 refresh rfid_read_data
	 */
	public void statusJudge_with_rssi() {
		Date now = new Date();
		Enumeration<Integer> em;
		em = this.rfid_read_data_with_rssi.keys();

		/******************* 1 insert into status ***********************************/
		while (em.hasMoreElements()) {
			Integer key = em.nextElement();

			SignalData old_status = rfid_status_with_rssi.get(key);

			if (old_status == null) { // no old ===> insert
				rfid_status_with_rssi.put(key,
						this.rfid_read_data_with_rssi.get(key));
			} else {
				// old exists & same reader ===> update rssi ,update readtime
				if (old_status.readerId == this.rfid_read_data_with_rssi
						.get(key).readerId) { // ͬһ reader���
					rfid_status_with_rssi.get(key).rssi = (old_status.rssi + this.rfid_read_data_with_rssi
							.get(key).rssi) / 2;
					rfid_status_with_rssi.get(key).readTime = this.rfid_read_data_with_rssi
							.get(key).readTime;
				}
				// old exists & different reader ==> do not change as possible
				else { // ����ͬһ��reader
					if (isNear(this.rfid_read_data_with_rssi.get(key).readTime,
							old_status.readTime)) { // ���ʱ��ȽϽ�
						if (this.rfid_read_data_with_rssi.get(key).rssi > old_status.rssi)
							rfid_status_with_rssi.put(key,
									this.rfid_read_data_with_rssi.get(key));
					} else { // ʱ��Ƚ�Զ
						rfid_status_with_rssi.put(key,
								this.rfid_read_data_with_rssi.get(key));
					}
				}// end of old exists
			}// end of signal of ON status

		}// end of while
		
		//************debug*************************
//		this.debug_reader_status();
		/******************* 2 refresh rfid_read_data ***********************************/
		this.rfid_read_data_with_rssi.clear();
	}
 
	public void debug_reader_status(){
		int tag_test = 14513;
		SignalData sd_reader = this.rfid_read_data_with_rssi.get(tag_test);
		SignalData sd_status = this.rfid_status_with_rssi.get(tag_test);
		String str_reader = null;
		String str_status = null;
		if(sd_reader == null)
			str_reader = "judge -read data: " +tag_test+" no info ";
		else
			str_reader = "judge -read data: " + sd_reader.toString();
		if(sd_status == null)
			str_status = "judge-status data: "+tag_test +" no info";
		else
			str_status = "judge-status data: "+ sd_status.toString();
		debug.println(str_reader	);
		debug.println(str_status);
		
	}
	
	
	@Override
	public void run() {
		/* *****************test debug ***********************************************
		
		SignalData read_data , status;
		String str_read_data , str_status;
		
		read_data = this.rfid_read_data_with_rssi.get(14513);
		status = this.rfid_status_with_rssi.get(14513);
		
		if(read_data == null) 
			str_read_data = " no exists ";
		else
			str_read_data = read_data.toString();
		
		if(status == null) 
			str_status = " no exists ";
		else
			str_status = status.toString();
		
		
		debug.println("=====before judge , 14513 read data:��"+ str_read_data);
		debug.println("=====			      	    status:��"+ str_status);
		
		******************test debug *********************************************** */

		/*********** situation NO rssi **************************************/
		if (Settings.workMode == Settings.NORSSIMODE) {
			this.statusJudge_no_rssi();
		}
		/*********** situation WITH rssi **************************************/
		else {
			this.statusJudge_with_rssi();
		}
		
		/******************test debug ************************************************
		read_data = this.rfid_read_data_with_rssi.get(14513);
		status = this.rfid_status_with_rssi.get(14513);
		
		if(read_data == null) 
			str_read_data = " no exists ";
		else
			str_read_data = read_data.toString();
		
		if(status == null) 
			str_status = " no exists ";
		else
			str_status = status.toString();
		
		
		debug.println("*****after  judge , 14513 read data:��"+ str_read_data);
		debug.println("***** 			      	    status:��"+ str_status);
		******************test debug ************************************************/
	}
}

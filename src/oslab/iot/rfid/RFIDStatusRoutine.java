package oslab.iot.rfid;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.TimerTask;

import oslab.iot.db.DbOperation;
import oslab.iot.util.Debug;

/**
 * ���д洢 ÿ��һ��ʱ��� ������״̬���д洢 �������˶��� ��� newstatus����ֵ ����ʵ��������д洢 ���newstatusû��ֵ
 * ������Ҫ�Ƿ�洢 -1 ֵ
 * 
 * 
 */
public class RFIDStatusRoutine extends TimerTask {

	private Hashtable<Integer, Integer> rfid_status_new_no_rssi; // **��RSSIģʽ�� ��״̬
	private Hashtable<Integer, SignalData> rfid_status_new_with_rssi; // **RSSIģʽ�� ��״̬
	private Hashtable<String, String> rfid_reader_room; // **��Ա��room��rfidreader��Ӧ��
	
	private Hashtable<Integer, Integer> rfid_status_old ;
	
	private final boolean debug_flag = false; // 
	private Debug debug = new Debug(debug_flag);
	
	private DbOperation dbo;

	private Enumeration<Integer> em_int;
	private Enumeration<String> em_str;
	private Integer key;
	private String status;
	private String rfid_reader;
	private String[] s_split;

	/**
	 * 
	 * @param rfid_reader_status
	 * @param newStatus
	 * @param in_mode
	 * @param work_mdoe
	 */
//	@Deprecated
//	public RFIDStatusRoutine(Hashtable<String, String> rfid_reader_status,
//			Hashtable newStatus) {
//		this.rfid_reader_room = rfid_reader_status;
//
//		if (Settings.workMode == Settings.RSSIMODE) {
//			this.newStatus_with_rssi = newStatus;
//		} else {
//			this.newStatus_no_rssi = newStatus;
//		}
//
//		dbo = new DbOperation();
//		dbo.connOracle(); // **********����ֻ����һ������ ����δ�ر� ��δ�ҵ�ͬ���������ݿ�����̵߳�˼·
//							// *&*��Ľ�
//	}

	public RFIDStatusRoutine(Hashtable<Integer, Integer> rfid_status_old ,
			Hashtable<Integer, SignalData> rfid_status_with_rssi,
			Hashtable<String, String> rfid_reader_status){
		this.rfid_reader_room = rfid_reader_status;
		this.rfid_status_old = rfid_status_old;
		this.rfid_status_new_with_rssi = rfid_status_with_rssi;
		dbo = new DbOperation();
		dbo.connOracle();
	}
	
	public void run() {
		new_way_store();
	}
	private void new_way_store(){
		em_int = this.rfid_status_new_with_rssi.keys();
		while(em_int.hasMoreElements()){
			key = em_int.nextElement();
			status = getStatus(key.toString(),
					Integer.toString(this.rfid_status_new_with_rssi.get(key).readerId));
			
			statusInfoIntoDB(key.toString(), status);
		}
		em_int = this.rfid_status_old.keys();
		
		while(em_int.hasMoreElements()){
			key = em_int.nextElement();
			if(!this.rfid_status_new_with_rssi.containsKey(key)){
				status =this.rfid_status_old.get(key).toString();
				statusInfoIntoDB(key.toString(), status);
			}
		}
		
		
	}
	private void old_way_store(){
		if (Settings.workMode == Settings.RSSIMODE) {
			// 1 ��һ��ѭ�� �Բɼ�������Ϣ���в������
			em_int = this.rfid_status_new_with_rssi.keys();
			while (em_int.hasMoreElements()) {
				key = em_int.nextElement();
				status = getStatus(key.toString(),
						Integer.toString(this.rfid_status_new_with_rssi.get(key).readerId));
				
				statusInfoIntoDB(key.toString(), status);
				
			}
			// 2 �ڶ���ѭ�� ��ע��������˽��в������
			if ( Settings.ROUTINE_ALL_IN == Settings.routineMode) {
				em_str = this.rfid_reader_room.keys();
				while (em_str.hasMoreElements()) {
					rfid_reader = em_str.nextElement();
					s_split = rfid_reader.split("-");
					key = new Integer(s_split[0]);
					// �����newstatus�в����� ����� ������� �����κβ���
					if (!rfid_status_new_with_rssi.containsKey(key)) {
						statusInfoIntoDB(key.toString(), "-1");
					}
				}

			}
		} else {
			// 1 ��һ��ѭ�� �Բɼ�������Ϣ���в������
			em_int = this.rfid_status_new_no_rssi.keys();
			while (em_int.hasMoreElements()) {
				key = em_int.nextElement();
				status = getStatus(key.toString(),
						Integer.toString(this.rfid_status_new_no_rssi.get(key)));
				statusInfoIntoDB(key.toString(), status);
				
			}
			// 2 �ڶ���ѭ�� ��ע��������˽��в������
			if ( Settings.ROUTINE_ALL_IN == Settings.routineMode) {
				em_str = this.rfid_reader_room.keys();
				while (em_str.hasMoreElements()) {
					rfid_reader = em_str.nextElement();
					s_split = rfid_reader.split("-");
					key = new Integer(s_split[0]);
					// �����newstatus�в����� ����� ������� �����κβ���
					if (!rfid_status_new_no_rssi.containsKey(key)) {
						statusInfoIntoDB(key.toString(), "-1");
					}
				}
			}

		}
	}

	/**
	 * ��readerId-roomstatus�л����Աʵ�����ڵ�room��Ϣ ���� 1434ĳ�˵Ĳɼ���ϢΪ<rfid,1435>
	 * ����1435���reader�����Լ�⵽1434�������� ������Ҫӳ�䵽<rfid,1434>������ʵ��λ�ñ�
	 * 
	 * @param rfid
	 * @param reader
	 * @return
	 */
	private String getStatus(String rfid, String reader) {
		String room = rfid_reader_room.get(rfid + "-" + reader);
		if (room == null) {
			room = reader;
		}
		return room;
	}
	
	private void statusInfoIntoDB(String rfid, String status) {
		String str_sql;
		// �޸�rfiddata�仯��Ϣ��
		str_sql = "insert into rfiddata(rfid,oldstatus,newstatus,changetime) values( "
				+ rfid + "," + status + "," + status + "," // +newValue.toString()+","
				+ "sysdate )";

		debug.println("dealChange: -data " + new Date() + " - " + str_sql);
		dbo.execSql(str_sql);


	}
	

}

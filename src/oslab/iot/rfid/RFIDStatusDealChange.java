package oslab.iot.rfid;

import java.sql.ResultSet;
import java.util.*;

import oslab.iot.db.DBOConcurrence;
import oslab.iot.db.DbOperation;
import oslab.iot.util.Debug;

/**
 * ����仯��Ϣ newstatus<rfid,signaldata> signaldata<room,time,rssi> VS .
 * oldstatus<rfid,room>
 * 
 * @author David
 * 
 */
public class RFIDStatusDealChange extends Thread {
	private Hashtable<Integer, Integer> oldStatus; // **ԭ״̬
	private Hashtable<Integer, Integer> newStatus_no_rssi; // **��RSSIģʽ�� ��״̬
	private Hashtable<Integer, SignalData> newStatus_with_rssi; // **RSSIģʽ�� ��״̬

	private final boolean debug_flag = false; // cg add
	private Debug debug = new Debug(debug_flag);

	private Hashtable<String, String> rfid_reader_room; // **��Ա��room��rfidreader��Ӧ��

	public RFIDStatusDealChange(Hashtable<String, String> rfid_reader_room,
			Hashtable<Integer, Integer> oldStatus, Hashtable newStatus) {
		this.rfid_reader_room = rfid_reader_room;
		this.oldStatus = oldStatus;

		if (Settings.workMode == Settings.RSSIMODE) {
			this.newStatus_with_rssi = newStatus;
		} else {
			this.newStatus_no_rssi = newStatus;
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

	private void dealDiff() {
		Integer oldValue;
		Integer newValue;
		boolean isOut;
		String str_sql;

		debug.println("DealChang begins here:");

		// �� newStatusΪ���ѭ�������ÿ����״̬
		Enumeration<Integer> em;
		if (Settings.workMode == Settings.RSSIMODE) {
			em = this.newStatus_with_rssi.keys();
		} else {
			em = this.newStatus_no_rssi.keys();
		}
		//
		while (em.hasMoreElements()) {
			// 0 �����״̬
			Integer key = em.nextElement();
			if (Settings.workMode == Settings.RSSIMODE) {
				newValue = this.newStatus_with_rssi.get(key).readerId;
				isOut = this.newStatus_with_rssi.get(key).isOut();
			} else {
				newValue = this.newStatus_no_rssi.get(key);
				isOut = false;// / Ĭ�ϴ�����norssi����£�δ�Ը�������п���
			}

			if (newValue == null)
				break;

			// 1 ��newStatus�����Լ죬�����ʱ����a�����뿪�仯��Ϣ bɾ��new&old״̬��Ϣ
			if (isOut) {
				// a �����뿪�仯��Ϣ
				diffInfoIntoDB(key.toString(),
						getStatus(key.toString(), newValue.toString()), "-1");
				// b ɾ��new&old״̬��Ϣ
				if (Settings.workMode == Settings.RSSIMODE) {
					this.newStatus_with_rssi.remove(key);
				} else {
					this.newStatus_no_rssi.remove(key);
				}
				this.oldStatus.remove(key);
				// ������һ��ѭ��
				break;
			}

			// 2 �����״̬
			oldValue = this.oldStatus.put(key, newValue);
			if (oldValue == null)
				oldValue = -1;
			// 2.1 ����¾�״̬��һ�£������ɱ仯��Ϣ
			if (!newValue.equals(oldValue)) {
				diffInfoIntoDB(key.toString(),
						getStatus(key.toString(), oldValue.toString()),
						getStatus(key.toString(), newValue.toString()));

			}

		}
	}

	/**
	 * 
	 */
	private void dealDiff_no_rssi() {
		Integer oldValue;
		Integer newValue;
		boolean isOut;
		String str_sql;

		debug.println("DealChang begins here:");
		// �� newStatusΪ���ѭ�������ÿ����״̬
		Enumeration<Integer> em;
		em = this.newStatus_no_rssi.keys();
		//
		while (em.hasMoreElements()) {
			// 0 �����״̬
			Integer key = em.nextElement();
/// change here  ============================   2013.3.25 23:46 
			if (Settings.workMode == Settings.RSSIMODE) {
				newValue = this.newStatus_with_rssi.get(key).readerId;
				isOut = this.newStatus_with_rssi.get(key).isOut();
			} else {
				newValue = this.newStatus_no_rssi.get(key);
				isOut = false;// / Ĭ�ϴ�����norssi����£�δ�Ը�������п���
			}

			if (newValue == null)
				break;

			// 1 ��newStatus�����Լ죬�����ʱ����a�����뿪�仯��Ϣ bɾ��new&old״̬��Ϣ
			if (isOut) {
				// a �����뿪�仯��Ϣ
				diffInfoIntoDB(key.toString(),
						getStatus(key.toString(), newValue.toString()), "-1");
				// b ɾ��new&old״̬��Ϣ
				if (Settings.workMode == Settings.RSSIMODE) {
					this.newStatus_with_rssi.remove(key);
				} else {
					this.newStatus_no_rssi.remove(key);
				}
				this.oldStatus.remove(key);
				// ������һ��ѭ��
				break;
			}

			// 2 �����״̬
			oldValue = this.oldStatus.put(key, newValue);
			if (oldValue == null)
				oldValue = -1;
			// 2.1 ����¾�״̬��һ�£������ɱ仯��Ϣ
			if (!newValue.equals(oldValue)) {
				diffInfoIntoDB(key.toString(),
						getStatus(key.toString(), oldValue.toString()),
						getStatus(key.toString(), newValue.toString()));

			}

		}
	}

	private void diffInfoIntoDB(String rfid, String oldStatus, String newStatus) {
		String str_sql;
		// �޸�rfiddata�仯��Ϣ��
		str_sql = "insert into rfiddata(rfid,oldstatus,newstatus,changetime) values( "
				+ rfid + "," + oldStatus + "," + newStatus + "," // +newValue.toString()+","
				+ "sysdate )";

		debug.println("dealChange: -data " + new Date() + " - " + str_sql);
		DBOConcurrence.execSql(str_sql);

		// �޸�rfidstatus ʵʱ״̬��Ϣ��
		str_sql = "merge into rfidstatus a " + "using (select " + rfid
				+ " as rfid ," + newStatus + " as status from dual) b "
				+ "on (a.rfid = b.rfid) " + "when matched then "
				+ "update set a.status = b.status " + "when not matched then "
				+ "insert (a.rfid,a.status) values(b.rfid,b.status) ";

		debug.println("dealChange: -stat " + new Date() + " - " + str_sql);
		DBOConcurrence.execSql(str_sql);

	}

	public void run() {
		for (;;) {
			// 1 to deal the difference
			dealDiff();
			// 2 to sleep
			try {
				Thread.sleep(Settings.SPAN_DEAL_DIFF);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

}

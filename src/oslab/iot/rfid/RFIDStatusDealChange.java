package oslab.iot.rfid;

import java.sql.ResultSet;
import java.util.*;

import oslab.iot.db.DBOConcurrence;
import oslab.iot.db.DbOperation;
import oslab.iot.util.Debug;

/**
 * 处理变化信息 newstatus<rfid,signaldata> signaldata<room,time,rssi> VS .
 * oldstatus<rfid,room>
 * 
 * @author David
 * 
 */
public class RFIDStatusDealChange extends Thread {
	private Hashtable<Integer, Integer> oldStatus; // **原状态
	private Hashtable<Integer, Integer> newStatus_no_rssi; // **非RSSI模式下 新状态
	private Hashtable<Integer, SignalData> newStatus_with_rssi; // **RSSI模式下 新状态

	private final boolean debug_flag = false; // cg add
	private Debug debug = new Debug(debug_flag);

	private Hashtable<String, String> rfid_reader_room; // **人员的room和rfidreader对应表

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
	 * 从readerId-roomstatus中获得人员实际所在的room信息 比如 1434某人的采集信息为<rfid,1435>
	 * 但是1435这个reader还可以检测到1434屋里的情况 所以需要映射到<rfid,1434>这样的实际位置表
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

		// 以 newStatus为外层循环，检查每个新状态
		Enumeration<Integer> em;
		if (Settings.workMode == Settings.RSSIMODE) {
			em = this.newStatus_with_rssi.keys();
		} else {
			em = this.newStatus_no_rssi.keys();
		}
		//
		while (em.hasMoreElements()) {
			// 0 获得新状态
			Integer key = em.nextElement();
			if (Settings.workMode == Settings.RSSIMODE) {
				newValue = this.newStatus_with_rssi.get(key).readerId;
				isOut = this.newStatus_with_rssi.get(key).isOut();
			} else {
				newValue = this.newStatus_no_rssi.get(key);
				isOut = false;// / 默认处理，在norssi情况下，未对该情况进行考虑
			}

			if (newValue == null)
				break;

			// 1 对newStatus进行自检，如果超时，则a生成离开变化信息 b删除new&old状态信息
			if (isOut) {
				// a 生成离开变化信息
				diffInfoIntoDB(key.toString(),
						getStatus(key.toString(), newValue.toString()), "-1");
				// b 删除new&old状态信息
				if (Settings.workMode == Settings.RSSIMODE) {
					this.newStatus_with_rssi.remove(key);
				} else {
					this.newStatus_no_rssi.remove(key);
				}
				this.oldStatus.remove(key);
				// 进行下一个循环
				break;
			}

			// 2 获得老状态
			oldValue = this.oldStatus.put(key, newValue);
			if (oldValue == null)
				oldValue = -1;
			// 2.1 如果新旧状态不一致，则生成变化信息
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
		// 以 newStatus为外层循环，检查每个新状态
		Enumeration<Integer> em;
		em = this.newStatus_no_rssi.keys();
		//
		while (em.hasMoreElements()) {
			// 0 获得新状态
			Integer key = em.nextElement();
/// change here  ============================   2013.3.25 23:46 
			if (Settings.workMode == Settings.RSSIMODE) {
				newValue = this.newStatus_with_rssi.get(key).readerId;
				isOut = this.newStatus_with_rssi.get(key).isOut();
			} else {
				newValue = this.newStatus_no_rssi.get(key);
				isOut = false;// / 默认处理，在norssi情况下，未对该情况进行考虑
			}

			if (newValue == null)
				break;

			// 1 对newStatus进行自检，如果超时，则a生成离开变化信息 b删除new&old状态信息
			if (isOut) {
				// a 生成离开变化信息
				diffInfoIntoDB(key.toString(),
						getStatus(key.toString(), newValue.toString()), "-1");
				// b 删除new&old状态信息
				if (Settings.workMode == Settings.RSSIMODE) {
					this.newStatus_with_rssi.remove(key);
				} else {
					this.newStatus_no_rssi.remove(key);
				}
				this.oldStatus.remove(key);
				// 进行下一个循环
				break;
			}

			// 2 获得老状态
			oldValue = this.oldStatus.put(key, newValue);
			if (oldValue == null)
				oldValue = -1;
			// 2.1 如果新旧状态不一致，则生成变化信息
			if (!newValue.equals(oldValue)) {
				diffInfoIntoDB(key.toString(),
						getStatus(key.toString(), oldValue.toString()),
						getStatus(key.toString(), newValue.toString()));

			}

		}
	}

	private void diffInfoIntoDB(String rfid, String oldStatus, String newStatus) {
		String str_sql;
		// 修改rfiddata变化信息表
		str_sql = "insert into rfiddata(rfid,oldstatus,newstatus,changetime) values( "
				+ rfid + "," + oldStatus + "," + newStatus + "," // +newValue.toString()+","
				+ "sysdate )";

		debug.println("dealChange: -data " + new Date() + " - " + str_sql);
		DBOConcurrence.execSql(str_sql);

		// 修改rfidstatus 实时状态信息表
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

package oslab.iot.rfid;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.TimerTask;

import oslab.iot.db.DbOperation;
import oslab.iot.util.Debug;

/**
 * 例行存储 每过一段时间后 将最新状态进行存储 对所有人都存 如果 newstatus中有值 则按照实际情况进行存储 如果newstatus没有值
 * 则按照需要是否存储 -1 值
 * 
 * 
 */
public class RFIDStatusRoutine extends TimerTask {

	private Hashtable<Integer, Integer> rfid_status_new_no_rssi; // **非RSSI模式下 新状态
	private Hashtable<Integer, SignalData> rfid_status_new_with_rssi; // **RSSI模式下 新状态
	private Hashtable<String, String> rfid_reader_room; // **人员的room和rfidreader对应表
	
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
//		dbo.connOracle(); // **********这里只打开了一个连接 但是未关闭 尚未找到同步各个数据库操作线程的思路
//							// *&*需改进
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
			// 1 第一个循环 对采集到的信息进行插入操作
			em_int = this.rfid_status_new_with_rssi.keys();
			while (em_int.hasMoreElements()) {
				key = em_int.nextElement();
				status = getStatus(key.toString(),
						Integer.toString(this.rfid_status_new_with_rssi.get(key).readerId));
				
				statusInfoIntoDB(key.toString(), status);
				
			}
			// 2 第二个循环 对注册的所有人进行插入操作
			if ( Settings.ROUTINE_ALL_IN == Settings.routineMode) {
				em_str = this.rfid_reader_room.keys();
				while (em_str.hasMoreElements()) {
					rfid_reader = em_str.nextElement();
					s_split = rfid_reader.split("-");
					key = new Integer(s_split[0]);
					// 如果在newstatus中不存在 则插入 如果存在 则不做任何操作
					if (!rfid_status_new_with_rssi.containsKey(key)) {
						statusInfoIntoDB(key.toString(), "-1");
					}
				}

			}
		} else {
			// 1 第一个循环 对采集到的信息进行插入操作
			em_int = this.rfid_status_new_no_rssi.keys();
			while (em_int.hasMoreElements()) {
				key = em_int.nextElement();
				status = getStatus(key.toString(),
						Integer.toString(this.rfid_status_new_no_rssi.get(key)));
				statusInfoIntoDB(key.toString(), status);
				
			}
			// 2 第二个循环 对注册的所有人进行插入操作
			if ( Settings.ROUTINE_ALL_IN == Settings.routineMode) {
				em_str = this.rfid_reader_room.keys();
				while (em_str.hasMoreElements()) {
					rfid_reader = em_str.nextElement();
					s_split = rfid_reader.split("-");
					key = new Integer(s_split[0]);
					// 如果在newstatus中不存在 则插入 如果存在 则不做任何操作
					if (!rfid_status_new_no_rssi.containsKey(key)) {
						statusInfoIntoDB(key.toString(), "-1");
					}
				}
			}

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
	
	private void statusInfoIntoDB(String rfid, String status) {
		String str_sql;
		// 修改rfiddata变化信息表
		str_sql = "insert into rfiddata(rfid,oldstatus,newstatus,changetime) values( "
				+ rfid + "," + status + "," + status + "," // +newValue.toString()+","
				+ "sysdate )";

		debug.println("dealChange: -data " + new Date() + " - " + str_sql);
		dbo.execSql(str_sql);


	}
	

}

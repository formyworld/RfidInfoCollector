package oslab.iot.rfid;

import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.Hashtable;

import oslab.iot.db.DbOperation;
import oslab.iot.util.Debug;

public class RFIDInfoPreparation {
	private static boolean debug_flag = false;
	private static Debug debug = new Debug(debug_flag);
	
	

	public static void init_rfid_reader_room(Hashtable<String, String> rfid_reader_room){
		
		DbOperation dbo = new DbOperation();
		dbo.connOracle();
		// get the real room-reader map
		String s_sql_query = "select a.rfid||'-'||b.reader rfidreader,a.room status  "
				+ "from  userinfo a , roomreader b where a.room = b.room ";
		ResultSet rs = dbo.getResult(s_sql_query);

		try {
			if (rs != null) {
				while (rs.next()) {
					rfid_reader_room.put(rs.getString("rfidreader"),
							rs.getString("status"));

					debug.println(rs.getRow() + " : "
							+ rs.getString("rfidreader") + " - "
							+ rs.getString("status"));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		dbo.closeImmediate();
	}
	public static void init_rfid_old_status(Hashtable<Integer, Integer> rfid_status_old){
		DbOperation dbo = new DbOperation();
		dbo.connOracle();
		// get the real room-reader map
		String s_sql_query = "select a.rfid  "
				+ "from  userinfo a ";
		ResultSet rs = dbo.getResult(s_sql_query);

		try {
			if (rs != null) {
				while (rs.next()) {
					rfid_status_old.put(Integer.parseInt(rs.getString("rfid")),
							-1);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		dbo.closeImmediate();
	}
	
	public static void main(String[] args) {
		Hashtable<Integer, Integer> rfid_status_old = new Hashtable<Integer, Integer>();
		init_rfid_old_status(rfid_status_old);
		Enumeration<Integer> em = rfid_status_old.keys();
		while(em.hasMoreElements()){
			int tag = em.nextElement();
			System.out.println(tag+" --- "+ rfid_status_old.get(tag));
		}
		
	}
}

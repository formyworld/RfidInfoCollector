package oslab.iot.rfid;

import java.sql.ResultSet;
import java.util.Hashtable;

import oslab.iot.db.DbOperation;
import oslab.iot.util.Debug;

public class RFIDReaderRoom {
	private boolean debug_flag = false;
	private Debug debug = new Debug(debug_flag);
	
	
	public Hashtable<String, String> get_rfid_reader_room(){
		
		Hashtable<String, String> rfid_reader_room = new Hashtable<String, String>();
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
		return rfid_reader_room;
	}
	public static void main(String[] args) {
		new RFIDReaderRoom().get_rfid_reader_room();
	}
}

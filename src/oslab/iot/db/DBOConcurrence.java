package oslab.iot.db;

import java.sql.*;
import java.util.Hashtable;

import oslab.iot.util.Debug;

public class DBOConcurrence {

	private static Connection conn;
	private static long sql_count = 0;
	private static boolean debug_flag = false;
	private static Debug debug = new Debug(debug_flag);

	public static void connOracle() {
		// Load the JDBC driver
		try {
			String driverName = "oracle.jdbc.driver.OracleDriver";
			Class.forName(driverName);

			// Create a connection to the database
			String serverName = "192.168.4.193";
			String portNumber = "1521";
			String sid = "orcl";
			String url = "jdbc:oracle:thin:@" + serverName + ":" + portNumber
					+ ":" + sid;
			String username = "iot";
			String password = "oslab";
			conn = DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public static synchronized void execSql(final String ssql) {

		Statement s;
		try {
			s = conn.createStatement();
			s.execute(ssql);
			s.close();

		} catch (final SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sql_count ++;
		if(sql_count>=200){
			debug.println("+++++++ DB execute sqls = "+sql_count+"  +++++++++");
			try {
				conn.commit();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			

	}

	public void execSqlImmediate(final String ssql) {

		Statement s;
		try {
			s = conn.createStatement();
			s.execute(ssql);
			s.close();

		} catch (final SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public ResultSet getResult(final String ssql) {

		Statement s;
		ResultSet rs;
		try {
			s = conn.createStatement();
			rs = s.executeQuery(ssql);

		} catch (final SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return rs;

	}

	public void close() {
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	public void closeImmediate() {
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	public static void main(String[] args) {
		String ssql = "insert into rfiddata(rfid,oldstatus,newstatus,changetime) "
				+ "values(11178,1435,-1,sysdate)";
		DBOConcurrence dbo = new DBOConcurrence();
		dbo.connOracle();
		dbo.execSql(ssql);
		String s_sql_query = "select a.rfid||'-'||b.reader rfidreader,a.room status  "
				+ "from  userinfo a , roomreaders b where a.room = b.room ";
		ResultSet rs = dbo.getResult(s_sql_query);
		Hashtable<String, String> rfid_reader_status = new Hashtable<String, String>();
		try {
			if (rs != null) {
				while (rs.next()) {
					rfid_reader_status.put(rs.getString("rfidreader"),
							rs.getString("status"));
					// System.out.println(rs.getRow()+" : "+rs.getString("rfidreader")+" - "+rs.getInt("status"));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		String room = rfid_reader_status.get("15049-145");
		if (room == null) {
			room = "null";
		}
		System.out.println(room);

		dbo.close();

	}
}

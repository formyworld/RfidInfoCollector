package oslab.iot.rfid;

import java.util.Date;

public class SignalData {
	public Date readTime;
	public float rssi;
	public int   readerId;
	public SignalData(Date readTime,float rssi,int readerId){
		this.readTime = readTime;
		this.rssi 		 = rssi;
		this.readerId	 = readerId;
	}
	
	public boolean isOut(){
		Date now = new Date();
		long ms = ( now.getTime() - readTime.getTime() ) / 1000;
		return  ( ms > Settings.THRESHOLD_IS_OUT  ) ? true : false;
	}
	public String toString(){
		return " room:"+Integer.toString(readerId)+" time:"+readTime.toString()+" rssi: "+Float.toString(rssi);
	}
}

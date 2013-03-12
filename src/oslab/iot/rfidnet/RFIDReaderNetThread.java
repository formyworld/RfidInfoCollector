package oslab.iot.rfidnet;

import java.io.*;
import java.util.*;

import oslab.iot.db.DbOperation;
import oslab.iot.rfid.RFIDStatusJudge;
import oslab.iot.rfid.Settings;
import oslab.iot.rfid.SignalData;
import oslab.iot.util.Debug;
import oslab.iot.util.UnicodeFormatter;

/**
 * RFIDReaderNetThread 
 * ʹ��net�ڽ��д��� 
 * �����ӹ��� ����ͷ�Ƿ���   
 * @author cg
 * ��SerialBean�Ϸ�װ����   
 * 1 ���캯��
 * 2 run  ���ܣ�1�� �ж��Ƿ��� 2�� ��ȡ���� 
 * 3 read
 * 
 */
public class RFIDReaderNetThread extends Thread {

   //READER ״̬
	protected static final int READER_STATUS_ON = 1;
	protected static final int READER_STATUS_OFF= 0;
   // �ж�ģʽ  
//   public static int RSSIMODE = 1; //���ж��ź���ǿ��  �����ͷͬʱ��ȡһ��RFID��� 
//   public static int NORSSIMODE = 2; 
//   public  int mode;       //�ϵ�����Settings����ͳһ����
	

   
   private final String sStart = "RRRRR"; 
   private byte[] readBuffer = new byte[1024];
 
   
   private Hashtable<Integer,Date> ht_rfid_source_data; //ʱ��
   private Hashtable<Integer,Integer> ht_status; 

   private Hashtable<Integer,SignalData> ht_rfid_source_data_New; // 
   private Hashtable<Integer,SignalData> ht_status_new; 

   private NetBean reader;
   protected int reader_status;  //��ͷ״̬
   protected int room;    //��ͷID ʹ��room����
   protected String addr;  //��ͷ net��ַ
   protected int port;     //��ͷ port
   
   private final boolean debug_flag = true;
   private Debug debug = null;
 
   /**
    * constructor  for RFIDReader 
    * @param i
    * 
    */
   public RFIDReaderNetThread(String addr,int port,int room,Hashtable<Integer,Integer> hd_status){
		this.ht_status = hd_status;
		ht_rfid_source_data = new Hashtable<Integer,Date>();
		
//		ht_rfid_source_data_New = new Hashtable<Integer,SignalData>(); //??? no use?
		
		reader = new NetBean();
		
        this.addr = addr;
        this.port = port;
		this.room = room;
		
		
   }
   public RFIDReaderNetThread(String addr,int port,int room,Hashtable<Integer,SignalData> hd_status_new,int mode){
		
	   	this.ht_status_new = hd_status_new;
		ht_rfid_source_data_New = new Hashtable<Integer,SignalData>(); //
		
		reader = new NetBean();
		
		this.addr = addr;
		this.port = port;
		this.room = room;
		
		debug = new Debug(debug_flag);
		
  }
   public void run(){
	   System.out.println("reader  "+this.room+" "+this.addr+" started");
	
	   //1 ����һ�߳�  ���Ѷ�ȡ��<tag,time>�����ж�
	   Timer t = new Timer();
	   if(Settings.workMode == Settings.NORSSIMODE){
		   t.schedule(new RFIDStatusJudge(ht_rfid_source_data,ht_status,room), Settings.DELAY_JUDGE,Settings.SPAN_JUDGE);
	   }else{
		   t.schedule(new RFIDStatusJudge(ht_rfid_source_data_New,ht_status_new), Settings.DELAY_JUDGE,Settings.SPAN_JUDGE);
	   }
	   
	   //2 ���߳̽��� ���ն�ͷ���� 
	   byte[] bstart = sStart.getBytes();
	   //2.1 �ж��Ƿ�ɹ����Ӷ�ͷ
	    if(reader.connectHost(addr, port)){
	    	reader_status = READER_STATUS_ON;
	    	readerStatusIntoDB(); // reader status into db ��һ�γɹ�
	    	try {
				reader.writePort(bstart);  //���� RRRRR������ ��ͷ 
				for (int i = 0; i < bstart.length; i++) {
					System.out.print(UnicodeFormatter.byteToHex(bstart[i]) + "  ");
				}
				System.out.println();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
	    }else
	    {
	    	reader_status = READER_STATUS_OFF;
	    	readerStatusIntoDB(); // reader status into db ��һ��ʧ�� 
	    }
	   //2.2 ��ȡ���� 
	     
	   for(;;){
		   //2.2.1 ������ȡ  ���� �ȶ��� �� �ٵ�2��
		   if(READER_STATUS_ON == reader_status){ //��� ���� ��ô��ʼ��ȡ 
			   //���ж��Ƿ�pingͨ 
			   if(!NetBean.isHostAlive(this.addr)){
				   System.out.println("Reader "+ this.addr+" get out - "+new Date());
				   reader_status = READER_STATUS_OFF; // ������� ��ô׼������
				   readerStatusIntoDB(); // reader status into db �ɹ���ʧ��
				   continue;
			   }
				try {
					this.read(); // ��ȡ����
				} catch (Exception e1) {
					System.out.println(e1.toString());
					reader_status = READER_STATUS_OFF; // ������� ��ô׼������
					readerStatusIntoDB(); // reader status into db �ɹ���ʧ��
				}
				try {
					Thread.sleep(Settings.SPAN_READ); //2s���ٶ�
				} catch (Exception e) {
				}
		   }else{
			   //2.2.2 ��������� ���ϳ������Ӷ�ͷ
			   try {
					Thread.sleep(Settings.SPAN_CONNECT); //2s���ٶ�
				} catch (Exception e) {
				}
				//��������
				if(reader.connectHost(addr, port)){
					System.out.println("Reader "+ this.addr+" get on - "+new Date());
			    	reader_status = READER_STATUS_ON;
			    	readerStatusIntoDB(); // reader status into db ʧ���гɹ�
			    	try {
						reader.writePort(bstart);  //���� RRRRR������ ��ͷ 
					} catch (Exception e) {
						// TODO: handle exception
					}
			    }else
			    {
			    	reader_status = READER_STATUS_OFF;
			    }
		   }
	   }
   }
   /**
    *  rfidReader  STatus into db
    */
   public synchronized void readerStatusIntoDB(){
	   DbOperation dbo = new DbOperation();
	   dbo.connOracle();
	   String ssql = "merge into readerstatus a " +
	   		         "using (select "+ this.room + " reader , "+this.reader_status +" status , sysdate checktime from dual) b " +
	   		         "on (a.reader = b.reader)" +
	   		         "when matched then " +
	   		         "    update set a.status = b.status ,a.checktime = b.checktime " +
	   		         "when not matched then " +
	   		         " insert (a.reader,a.status,a.checktime) values(b.reader,b.status,b.checktime) ";
	   dbo.execSqlImmediate(ssql);
	   dbo.closeImmediate();
   }
   public void read() throws Exception{
	   int len = reader.readPort(readBuffer);
	   
	   Parser p = new Parser(readBuffer,len);
	   
	   byte[] msg = new byte[256];
	   int msg_len; 
	   int rfid_flags_count = 0;
	   
	   while((msg_len=p.unpack(msg)) >0 ){
		   if(Settings.workMode == Settings.NORSSIMODE){
			   ht_rfid_source_data.put(new Integer(Parser.getTagID(msg,msg_len)), new Date());
			   rfid_flags_count++;
		   }else{
			   ht_rfid_source_data_New.put(new Integer(Parser.getTagID(msg,msg_len)), new SignalData(new Date(), Parser.getRSSI(msg, msg_len), room));
			   rfid_flags_count++;
		   }
	   }
	   
	   debug.println("*************reading rfid counts "+rfid_flags_count+" , has rfid flags : "+ht_rfid_source_data_New.size());
	   
   }
  
   
}

package oslab.iot.rfid;

public class Settings {

	   public static final int RSSIMODE = 1; //���ж��ź���ǿ��  �����ͷͬʱ��ȡһ��RFID��� 
	   public static final int NORSSIMODE = 2; 
	   public static int workMode = RSSIMODE;
	   
	   public static final int SPAN_CONNECT=5*1000;//Reader connect ���ϳ�����������
	   public static final int SPAN_READ = 4*1000;//RFID Reader��ȡ����
	   public static final int DELAY_JUDGE = 5*1000; //RFID SOURCE JUDGE�ӳ�ʱ��
	   public static final int SPAN_JUDGE = 10*1000; //RFID SOURCE JUDGE��������
	   
	   public static final int SPAN_DEAL_DIFF = 10*1000; //RFID STATUS DEAL CHANGE RFID״̬�任��Ϣ��������
	   
	   public static final int THRESHOLD_IS_OUT = 5*60*1000; //�ж��Ƿ��뿪������Χ
	   
	   public static final int SPAN_ROUTINE = 30*60*1000; //���в���״̬��Ϣ  ����
	   public static final int DELAY_ROUTINE = 30*60*1000; //���в���״̬��Ϣ �ӳ�ʱ��
	   public static final int ROUTINE_ALL_IN = 1;
	   public static final int ROUTINE_AVAIL_IN = 2;
	   public static int routineMode = ROUTINE_ALL_IN;
	   

}
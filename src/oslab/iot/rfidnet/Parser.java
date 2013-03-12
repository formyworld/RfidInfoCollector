package oslab.iot.rfidnet;
/**
 * 
 * @author CG
 *  FUNCTION 
 *     1  UNPACK INTO MSG
 *     2  GET MSG 
 */
public class Parser {
   private byte[] data;
   private int cur;
   private int length;
   private final byte sep = 0x7f;
   private final byte escape = (byte)0x81;
   private final static int NORMAL_MSG = 17;
   private final static int MAYBE_MSG  = 20;
   
   public Parser(byte[] data,int length){
	   this.data = data;
	   this.length = length;
	   cur =0;
   }
   /**
    *  unpack 
    * @param msg
    */
   public int unpack(byte[] msg){
	   //check the length remaining  , assert > 6
	   if( length - cur + 1 < 6)
	      return 0;
	   //while 
	   int istart =0;
	   int iend = 0;
	   for (int i = cur ;i <length;i++){
		   // the first two SEP
		   if ( (iend==0 || iend == 1) && data[i]!= sep ){
			   iend = 0;
			   continue;
		   }
		   // FILL CONTENT
		   msg[iend++] = data[i];
		   if (iend > 256) {
			   return 0;
		   }
		   //ending 
		   if(iend > 2 && data[i]== sep && data[i-1] == sep && iend -2 >= istart ){
			   if (iend > 4){
				   cur = ++i;
				   return iend;
			   }else{
				   iend = 2;
			   }
			   
		   }
		   // deal with escape
		   if (data[i]== escape && data[i-1]== sep && iend >2){
			   iend --;
			   istart = iend;
		   }
	   }
	   return 0;
   }
	public static int getTagID(byte[] msg,int len){
		if(len == NORMAL_MSG)
		   return (msg[10]&0xff)*256+(msg[9]&0xff); 
		else
			return (msg[13]&0xff)*256+(msg[12]&0xff);
	}
	public static float getRSSI(byte[] msg,int len){
		Byte b ;
		if(len == NORMAL_MSG)
			b = new Byte(msg[12]);
		else
			b = new Byte(msg[15]);
		
		float fRssi = b.floatValue();
        //¶ÔRSSI½øÐÐ»»Ëã
        if (fRssi >= 128)
        {
            fRssi = (fRssi - 256) / 2 - 79;
        }
        else
        {
            fRssi = fRssi / 2 - 79;
        }
        return fRssi;
	}
}

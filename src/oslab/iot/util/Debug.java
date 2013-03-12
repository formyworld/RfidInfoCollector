package oslab.iot.util;

public class Debug {

	/**
	 * @param args
	 */
	private boolean flag ;
	public Debug(boolean flag){
		this.flag = flag;
	}
	public void print(String str){
		if(flag)
			System.out.print(str+" ");
	}
	
	public void println(String str){
		if(flag)
			System.out.println(str+" ");
	}

}

package oslab.iot.rfidnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import oslab.iot.util.Debug;

public class NetBean {
	private Socket s;
	private String addr;
	private int port;
	private InputStream in;
	private OutputStream out;
	private final String sStart = "RRRRR";
	private final boolean debug_flag = false;
	private Debug debug = null;

	/**
	 * isHostAlive 采用Iaddresss.isreachable 的的方法 ********
	 * 
	 * @param addr
	 * @return
	 */
	public static boolean isHostAlive(final String addr) {
		boolean ret;
		InetAddress i_addr;
		try {
			i_addr = InetAddress.getByName(addr);
			ret = i_addr.isReachable(5000);
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}

	public NetBean() {
		debug = new Debug(debug_flag);
	}

	/**
	 * connectHost
	 * 
	 * @param addr
	 * @param port
	 * @return
	 */
	public boolean connectHost(String addr, int port) {
		this.addr = addr;
		this.port = port;
		closeHost();
		try {
			InetSocketAddress sa = new InetSocketAddress(addr, port);
			s = new Socket();
			s.connect(sa);
			in = s.getInputStream();
			out = s.getOutputStream();
		} catch (Exception e) {
			debug.println("CONNECT FAILED -- "+addr+" : "+port);
			return false;
		}
		debug.println("CONNECT SUCCESS -- "+addr+" : "+port);
		return true;
	}

	/**
	 * closeHost
	 */
	public void closeHost() {
		if (s != null) {
			try {
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public boolean isConnected() {
		if (s == null)
			return false;
		else
			return s.isConnected();
	}

	public void writePort(byte[] bytes) throws IOException {
		out.write(bytes);
		out.flush();
		if (debug_flag) {
			System.out.println("sending " + bytes.toString());
		}
	}

	public int readPort(byte[] b) throws Exception {
		int i = 0;

		i = (int) in.read(b);
		if (debug_flag) {
			System.out.println(i);
		}

		return i;

	}

	public static void main(String[] args) {

		NetBean nb = new NetBean();
		String addr = "192.168.3.90";
		byte[] ret = new byte[1024];
		int port = 4001;
		// ------------------------------------
		// -----------------------------------

		boolean st;
		int l;
		st = nb.connectHost(addr, port); // 1 connect
		if (st) {
			System.out.println("on  " + new Date());
			while (true) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				if (st) {
					try {
						l = nb.readPort(ret); // 2 read
						System.out.println("get " + l + " bytes - "
								+ new Date());
						System.out.println(isHostAlive(addr));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						st = false;
						System.out
								.println("offf ===============================");
					}
				}
			}
		} else {
			System.out.println("off  " + new Date());
		}

	}
}
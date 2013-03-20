package oslab.iot.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class ReadProperties {

	private Properties prop = null;

	public ReadProperties(String filename) {

		try {
			InputStream in = new FileInputStream(filename);

			prop = new Properties();

			prop.load(in);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getProp(String key) {
		return prop.getProperty(key);
	}

}

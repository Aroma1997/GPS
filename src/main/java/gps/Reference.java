package gps;

import java.io.InputStream;
import java.util.Properties;

import com.google.common.base.Throwables;

public class Reference {
	static {
		Properties prop = new Properties();

		try {
			InputStream stream = Reference.class.getResourceAsStream("reference.properties");
			prop.load(stream);
			stream.close();
		} catch (Exception e) {
			Throwables.propagate(e);
		}

		VERSION = prop.getProperty("version");
	}

	public static final String MOD_ID = "GPS";
	public static final String MOD_NAME = MOD_ID;
	public static final String VERSION;

}

/**
 * The code of the GPS mod and all related materials like textures is licensed under the
 * GNU GENERAL PUBLIC LICENSE Version 3.
 * <p>
 * See https://github.com/Aroma1997/GPS/blob/master/license.txt for more information.
 */
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

	public static final String MOD_ID = "gps";
	public static final String MOD_NAME = "GPS";
	public static final String VERSION;

}

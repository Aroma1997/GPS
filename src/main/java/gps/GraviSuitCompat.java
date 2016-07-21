package gps;

import java.lang.reflect.Field;

public class GraviSuitCompat {
	public static boolean graviSuitEnabled = false;
	private static Class graviSuite = null;
	private static Field hudPos = null;
	private static int prevHudPos = 1;
	
	public static void init() {
		try {
			graviSuite = Class.forName("gravisuite.GraviSuite");
			hudPos = graviSuite.getDeclaredField("hudPos");
			prevHudPos = hudPos.getInt(null);
			graviSuitEnabled = true;
		} catch (Throwable e) {}
	}
	
	public static void toggleHudPos(boolean displaying) {
		if (prevHudPos == 2 || prevHudPos == 4) return;
		try {
			if (displaying) {
				hudPos.set(null, 4);
			} else {
				hudPos.set(null, prevHudPos);
			}
		} catch (Throwable e) {}
	}
}

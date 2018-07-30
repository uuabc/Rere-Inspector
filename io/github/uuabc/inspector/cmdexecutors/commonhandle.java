package io.github.uuabc.inspector.cmdexecutors;

import java.text.SimpleDateFormat;

public class commonhandle {
	public static String parseTime(final String args_input) {
		long time = 0;
		long w = 0;
		long d = 0;
		long h = 0;
		long m = 0;
		long s = 0;
		String datestring = null;

		String i = args_input.trim().toLowerCase();

		if (i.startsWith("t:")) {
			i = i.replaceAll("t:", "");
			i = i.replaceAll("y", "y:");
			i = i.replaceAll("m", "m:");
			i = i.replaceAll("w", "w:");
			i = i.replaceAll("d", "d:");
			i = i.replaceAll("h", "h:");
			i = i.replaceAll("s", "s:");

			final String[] arr = i.split(":");
			for (String i1: arr) {
				if (i1.endsWith("w")) {
					final String i2 = i1.replaceAll("[^0-9]", "");
					if (i2.length() > 0) {
						w = Long.parseLong(i2);
					}
				} else if (i1.endsWith("d")) {
					final String i2 = i1.replaceAll("[^0-9]", "");
					if (i2.length() > 0) {
						d = Long.parseLong(i2);
					}
				} else if (i1.endsWith("h")) {
					final String i2 = i1.replaceAll("[^0-9]", "");
					if (i2.length() > 0 ) {
						h =  Long.parseLong(i2);
					}
				} else if (i1.endsWith("m")) {
					final String i2 = i1.replaceAll("[^0-9]", "");
					if (i2.length() > 0) {
						m = Long.parseLong(i2);
					}
				} else if (i1.endsWith("s")) {
					final String i2 = i1.replaceAll("[^0-9]", "");
					if (i2.length() > 0) {
						s = Long.parseLong(i2);
					}
				}
			}
			time = w * 7 * 24 * 60 * 60 + d * 24 * 60 * 60 + h * 60 * 60 + m * 60 + s;

			if (time  != 0) {
				long ntime = System.currentTimeMillis() - time * 1000;
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				datestring = df.format(ntime);
			}

			
		}
		
		return datestring;
	}
}

package co.aoscp.miservices.shared;

import android.os.Build;

public class Bits {

	public static String getKey() {
		if (!"Poundcake".equals(Build.LUNA.VERSION_CODE)) {
            return null;
        }
		return "c3bbf6fb258c4175d78548a3a3bcddc3";
	}

	public static String getSecret() {
		if (!"Poundcake".equals(Build.LUNA.VERSION_CODE)) {
            return null;
        }
		return "6XcxeirzpHb5A6pSaZxyrj0YFT25mmxo4YqLTnnb";
	}
}
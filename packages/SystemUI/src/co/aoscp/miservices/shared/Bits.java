/*
 * Copyright 2019 CypherOS
 *
 * MiServices is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MiServices is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MiServices.  If not, see <http://www.gnu.org/licenses/>.
 */
package co.aoscp.miservices.shared;

import android.os.Build;

public class Bits {

	public static String getKey() {
		if (!"Poundcake".equals(Build.LUNA.VERSION_CODE)) {
            return null;
        }
		return "e49c241cdaa4257f" + "d4607ef93aa9ca7d";
	}

	public static String getSecret() {
		if (!"Poundcake".equals(Build.LUNA.VERSION_CODE)) {
            return null;
        }
		return "TomUVHpZeWvtH2JIUqEXhdp" + "DyY8bizyS6AGJ8Rds";
	}
}

/**
 * Copyright (C) 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.client.common;

import android.util.Log;
import java.io.Serializable;

/**
 * Class to manage update versions
 */
public class VersionManager implements Serializable {

    private static final String TAG = "VersionManager";

    private int mMajor = 0;
    private int mMinor = 0;
    private int mMaintenance = 0;

    private String mDate = "0";

    public VersionManager(String version) {
        this(version.split("-")[0], version.split("-")[1]);
    }

    public VersionManager(String version, String date) {

        try {
            String[] parts = version.split("\\.");
            mMajor = Integer.parseInt(parts[0]);
            if (parts.length > 1) {
                mMinor = Integer.parseInt(parts[1]);
            }
            if (parts.length > 2) {
                mMaintenance = Integer.parseInt(parts[2]);
			}	
			mDate = date;
            if (Constants.DEBUG) Log.d(TAG, "got version: " + mMajor + "." + mMinor + "." + mMaintenance);
            if (Constants.DEBUG) Log.d(TAG, "got date: " + mDate);
        } catch (NumberFormatException ex) {
            // malformed version, write the log and continue
            // C derped something for sure
            ex.printStackTrace();
			Log.d(TAG, "Whhhhhhhhyyyy?");
        }
    }

    public static int compare(VersionManager v1, VersionManager v2) {
        if (v1.getMajor() != v2.getMajor()) {
            return v1.getMajor() < v2.getMajor() ? -1 : 1;
        }
        if (v1.getMinor() != v2.getMinor()) {
            return v1.getMinor() < v2.getMinor() ? -1 : 1;
        }
        if (v1.getMaintenance() != v2.getMaintenance()) {
            return v1.getMaintenance() < v2.getMaintenance() ? -1 : 1;
        }
        if (!v1.getDate().equals(v2.getDate())) {
            return v1.getDate().compareTo(v2.getDate());
        }
        return 0;
    }

    public int getMajor() {
        return mMajor;
    }

    public int getMinor() {
        return mMinor;
    }

    public int getMaintenance() {
        return mMaintenance;
    }

    public String getDate() {
        return mDate;
    }

    public boolean isEmpty() {
        return mMajor == 0;
    }

    public String toString() {
        return mMajor + "." + mMinor + (mMaintenance > 0 ? "." + mMaintenance : "");
    }
}
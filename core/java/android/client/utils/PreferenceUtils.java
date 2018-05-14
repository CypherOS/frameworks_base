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
package android.client.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtils {
    public static final String PROPERTY_FIRST_BOOT = "firstBoot";
    public static final String PROPERTY_LAST_CHECK = "lastCheck";
    public static final String DOWNLOAD_ROM_ID = "download_rom_id";
    public static final String DOWNLOAD_ROM_MD5 = "download_rom_md5";
    public static final String DOWNLOAD_ROM_FILENAME = "download_rom_filaname";

    public static String getPreference(Context context, String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    public static int getPreference(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue);
    }

    public static boolean getPreference(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }

    public static void setPreference(Context context, String preference, String value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(preference, value);
        editor.apply();
    }

    public static void setPreference(Context context, String preference, int value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(preference, value);
        editor.apply();
    }

    public static void setPreference(Context context, String preference, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(preference, value);
        editor.apply();
    }

    public static void removePreference(Context context, String preference) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(preference);
        editor.apply();
    }

    public static void setDownloadRomId(Context context, Long id, String md5, String fileName) {
        if (id == null) {
            removePreference(context, DOWNLOAD_ROM_ID);
            removePreference(context, DOWNLOAD_ROM_MD5);
            removePreference(context, DOWNLOAD_ROM_FILENAME);
        } else {
            setPreference(context, DOWNLOAD_ROM_ID, String.valueOf(id));
            setPreference(context, DOWNLOAD_ROM_MD5, md5);
            setPreference(context, DOWNLOAD_ROM_FILENAME, fileName);
        }
    }
}
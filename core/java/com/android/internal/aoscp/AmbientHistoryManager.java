/*
 * Copyright (C) 2018 PixelExperience
 * Copyright (C) 2019 CypherOS
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>.
 */

package com.android.internal.aoscp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.UserHandle;

import java.util.ArrayList;
import java.util.List;

public class AmbientPlayHistoryManager {
	
	private static final Uri HISTORY_PROVIDER = Uri.parse("content://co.aoscp.miservices.providers.ambient/history");
    private static final String KEY_ID = "_id";
    private static final String KEY_TIMESTAMP = "ts";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_SONG = "song";
    private static final String[] PROJECTION = {KEY_ID, KEY_TIMESTAMP, KEY_SONG, KEY_ARTIST};
    private static String ACTION_SONG_MATCH = "co.aoscp.miservices.ambient.play.SONG_MATCH";
    public static Intent INTENT_SONG_MATCH = new Intent(ACTION_SONG_MATCH);

    public static void addSong(String song, String artist, Context context) {
        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, System.currentTimeMillis());
        values.put(KEY_SONG, song);
        values.put(KEY_ARTIST, artist);
        context.getContentResolver().insert(HISTORY_PROVIDER, values);
		context.sendBroadcastAsUser(INTENT_SONG_MATCH, UserHandle.CURRENT);
    }

    public static List<AmbientHistoryData> getSongs(Context context) {
        List<AmbientHistoryData> result = new ArrayList<>();
        try (Cursor c = context.getContentResolver().query(HISTORY_PROVIDER, PROJECTION, null, null, null)) {
            if (c != null) {
                while (c.moveToNext()) {
                    result.add(new AmbientHistoryData(c.getInt(0), c.getLong(1), c.getString(2), c.getString(3)));
                }
            }
        }
        return result;
    }

    public static void deleteSong(int id, Context context) {
        context.getContentResolver().delete(Uri.parse(HISTORY_PROVIDER + "/" + id), null, null);
    }

    public static void deleteAll(Context context) {
        context.getContentResolver().delete(HISTORY_PROVIDER, null, null);
    }
}

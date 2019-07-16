/*
 * Copyright (C) 2019 CypherOS
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
 * limitations under the License
 */

package com.android.systemui.ambientplay;

import java.util.ArrayList;
import java.util.List;

class PlayApi {

    public Metadata metadata;
    public Status status;

    public static class Artist {
        public String name;
    }

    public static class Metadata {
        public List<Song> humming = new ArrayList();
        public List<Song> music = new ArrayList();
    }

    public static class Song {
        public List<Artist> artists = new ArrayList();
        public String title;

        public String getPrimaryArtistName() {
            return ((Artist) this.artists.get(0)).name;
        }
    }

    public static class Status {
        public int code;

        public boolean isSuccess() {
            return this.code == 0;
        }
    }
}
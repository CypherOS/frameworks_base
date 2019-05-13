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
 * limitations under the License.
 */

package com.android.systemui.aoscp.privacy;

public class LiveOpItem {

    private int mCode;
    private String mPackageName;
    private String mState;
    private long mTimeStarted;
    private int mUid;

    public LiveOpItem(int code, int uid, String packageName, long timeStarted) {
        this.mCode = code;
        this.mUid = uid;
        this.mPackageName = packageName;
        this.mTimeStarted = timeStarted;
        StringBuilder sb = new StringBuilder();
        sb.append("LiveOpItem(");
        sb.append("Op code=");
        sb.append(code);
        String str2 = ", ";
        sb.append(str2);
        sb.append("UID=");
        sb.append(uid);
        sb.append(str2);
        sb.append("Package name=");
        sb.append(packageName);
        sb.append(")");
        this.mState = sb.toString();
    }

    public int getCode() {
        return this.mCode;
    }

    public int getUid() {
        return this.mUid;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String toString() {
        return this.mState;
    }
}
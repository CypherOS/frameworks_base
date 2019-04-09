package com.android.systemui.appops;

public class AppOpItem {
    private int mCode;
    private String mPackageName;
    private String mState;
    private long mTimeStarted;
    private int mUid;

    public AppOpItem(int code, int uid, String packageName, long timeStarted) {
        this.mCode = code;
        this.mUid = uid;
        this.mPackageName = packageName;
        this.mTimeStarted = timeStarted;
        StringBuilder sb = new StringBuilder();
        sb.append("AppOpItem(");
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

package com.android.systemui.privacy;

import kotlin.jvm.internal.Intrinsics;

public class PrivacyItem {

    private PrivacyApplication application;
    private PrivacyType privacyType;

	public PrivacyItem(PrivacyType privacyType, PrivacyApplication privacyApplication) {
        this.privacyType = privacyType;
        this.application = privacyApplication;
    }

    public boolean equals(Object obj) {
        if (this != obj) {
            if (obj instanceof PrivacyItem) {
                PrivacyItem privacyItem = (PrivacyItem) obj;
                if (Intrinsics.areEqual(this.privacyType, privacyItem.privacyType) && Intrinsics.areEqual(this.application, privacyItem.application)) {
                }
            }
            return false;
        }
        return true;
    }

    public int hashCode() {
        PrivacyType privacyType = this.privacyType;
        int i = 0;
        int hashCode = (privacyType != null ? privacyType.hashCode() : 0) * 31;
        if (this.application != null) {
            i = this.application.hashCode();
        }
        return hashCode + i;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PrivacyItem(privacyType=");
        sb.append(this.privacyType);
        sb.append(", application=");
        sb.append(this.application);
        sb.append(")");
        return sb.toString();
    }

    public PrivacyType getPrivacyType() {
        return this.privacyType;
    }

    public PrivacyApplication getApplication() {
        return this.application;
    }
}

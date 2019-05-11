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
				if (this.privacyType.equals(privacyItem.privacyType) && this.application.equals(privacyItem.application)) {
					return true;
                }
            }
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hashCode = (this.privacyType != null ? this.privacyType.hashCode() : 0) * 31;
        if (this.application != null) {
			return hashCode + this.application.hashCode();
        }
        return 0;
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

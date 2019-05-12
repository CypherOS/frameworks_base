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

package com.android.systemui.aoscp.privacy.utils;

import java.io.Serializable;

public final class OpPair<A, B> implements Serializable {
    private final A first;
    private final B second;

    public boolean equals(Object obj) {
        if (this != obj) {
            if (obj instanceof OpPair) {
                OpPair pair = (OpPair) obj;
				if (this.first.equals(pair.first) && this.second.equals(pair.second)) {
					return true;
				}
            }
            return false;
        }
        return true;
    }

    public int hashCode() {
        Object obj = this.first;
		int hashCodeA = (obj != null ? obj.hashCode() : 0) * 31;
        int hashCodeB = 0;
        Object obj2 = this.second;
        if (obj2 != null) {
            hashCodeB = obj2.hashCode();
        }
        return hashCodeA + hashCodeB;
    }

    public OpPair(A a, B b) {
        this.first = a;
        this.second = b;
    }

    public final B getSecond() {
        return this.second;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(this.first);
        sb.append(", ");
        sb.append(this.second);
        sb.append(')');
        return sb.toString();
    }
}
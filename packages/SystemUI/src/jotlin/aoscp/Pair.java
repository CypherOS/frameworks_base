package jotlin.aoscp;

import java.io.Serializable;

public final class Pair<First, Second> implements Serializable {

    private final First first;
    private final Second second;

    public boolean equals(Object obj) {
        if (this != obj) {
			if (obj instanceof Pair) {
                Pair pair = (Pair) obj;
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
        int code = 0;
        int hashCode = (obj != null ? obj.hashCode() : 0) * 31;
        Object obj2 = this.second;
        if (obj2 != null) {
            code = obj2.hashCode();
        }
        return hashCode + code;
    }

    public Pair(First first, Second second) {
        this.first = first;
        this.second = second;
    }

    public final First getFirst() {
        return this.first;
    }

    public final Second getSecond() {
        return this.second;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('(');
        stringBuilder.append(this.first);
        stringBuilder.append(", ");
        stringBuilder.append(this.second);
        stringBuilder.append(')');
        return stringBuilder.toString();
    }
}

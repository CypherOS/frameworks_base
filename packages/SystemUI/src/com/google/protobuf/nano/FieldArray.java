package com.google.protobuf.nano;

public final class FieldArray implements Cloneable {
    private static final FieldData DELETED = new FieldData();
    private FieldData[] mData;
    private int[] mFieldNumbers;
    private boolean mGarbage;
    private int mSize;

    FieldArray() {
        this(10);
    }

    FieldArray(int initialCapacity) {
        this.mGarbage = false;
        initialCapacity = idealIntArraySize(initialCapacity);
        this.mFieldNumbers = new int[initialCapacity];
        this.mData = new FieldData[initialCapacity];
        this.mSize = 0;
    }

    /* renamed from: gc */
    private void m10gc() {
        int n = this.mSize;
        int[] keys = this.mFieldNumbers;
        FieldData[] values = this.mData;
        int o = 0;
        for (int i = 0; i < n; i++) {
            FieldData val = values[i];
            if (val != DELETED) {
                if (i != o) {
                    keys[o] = keys[i];
                    values[o] = val;
                    values[i] = null;
                }
                o++;
            }
        }
        this.mGarbage = false;
        this.mSize = o;
    }

    int size() {
        if (this.mGarbage) {
            m10gc();
        }
        return this.mSize;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof FieldArray)) {
            return false;
        }
        FieldArray other = (FieldArray) o;
        if (size() != other.size()) {
            return false;
        }
        if (!(arrayEquals(this.mFieldNumbers, other.mFieldNumbers, this.mSize) && arrayEquals(this.mData, other.mData, this.mSize))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        if (this.mGarbage) {
            m10gc();
        }
        int result = 17;
        for (int i = 0; i < this.mSize; i++) {
            result = this.mData[i].hashCode() + (31 * ((31 * result) + this.mFieldNumbers[i]));
        }
        return result;
    }

    private int idealIntArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }

    private int idealByteArraySize(int need) {
        for (int i = 4; i < 32; i++) {
            if (need <= (1 << i) - 12) {
                return (1 << i) - 12;
            }
        }
        return need;
    }

    private boolean arrayEquals(int[] a, int[] b, int size) {
        for (int i = 0; i < size; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean arrayEquals(FieldData[] a, FieldData[] b, int size) {
        for (int i = 0; i < size; i++) {
            if (!a[i].equals(b[i])) {
                return false;
            }
        }
        return true;
    }

    public final FieldArray clone() {
        int size = size();
        FieldArray clone = new FieldArray(size);
        int i = 0;
        System.arraycopy(this.mFieldNumbers, 0, clone.mFieldNumbers, 0, size);
        while (true) {
            int i2 = i;
            if (i2 < size) {
                if (this.mData[i2] != null) {
                    clone.mData[i2] = this.mData[i2].clone();
                }
                i = i2 + 1;
            } else {
                clone.mSize = size;
                return clone;
            }
        }
    }
}

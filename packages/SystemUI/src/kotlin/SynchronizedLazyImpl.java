package kotlin;

import java.io.Serializable;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: LazyJVM.kt */
final class SynchronizedLazyImpl<T> implements Lazy<T>, Serializable {
    private volatile Object _value;
    private Function0<? extends T> initializer;
    private final Object lock;

    public SynchronizedLazyImpl(Function0<? extends T> function0, Object obj) {
        Intrinsics.checkParameterIsNotNull(function0, "initializer");
        this.initializer = function0;
        this._value = UNINITIALIZED_VALUE.INSTANCE;
        if (obj == null) {
            obj = this;
        }
        this.lock = obj;
    }

    public /* synthetic */ SynchronizedLazyImpl(Function0 function0, Object obj, int i, DefaultConstructorMarker defaultConstructorMarker) {
        if ((i & 2) != 0) {
            obj = null;
        }
        this(function0, obj);
    }

    public T getValue() {
        UNINITIALIZED_VALUE uninitialized_value = this._value;
        if (uninitialized_value != UNINITIALIZED_VALUE.INSTANCE) {
            return uninitialized_value;
        }
        T t;
        synchronized (this.lock) {
            t = this._value;
            if (t == UNINITIALIZED_VALUE.INSTANCE) {
                Function0 function0 = this.initializer;
                if (function0 != null) {
                    t = function0.invoke();
                    this._value = t;
                    this.initializer = null;
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
        }
        return t;
    }

    public boolean isInitialized() {
        return this._value != UNINITIALIZED_VALUE.INSTANCE;
    }

    public String toString() {
        return isInitialized() ? String.valueOf(getValue()) : "Lazy value not initialized yet.";
    }

    private final Object writeReplace() {
        return new InitializedLazyImpl(getValue());
    }
}

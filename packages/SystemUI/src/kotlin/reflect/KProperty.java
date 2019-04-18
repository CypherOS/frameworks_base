package kotlin.reflect;

/* compiled from: KProperty.kt */
public interface KProperty<R> extends KCallable<R> {

    /* compiled from: KProperty.kt */
    public interface Accessor<R> {
    }

    /* compiled from: KProperty.kt */
    public interface Getter<R> extends Accessor<R>, KFunction<R> {
    }
}

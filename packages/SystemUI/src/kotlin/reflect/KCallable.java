package kotlin.reflect;

/* compiled from: KCallable.kt */
public interface KCallable<R> extends KAnnotatedElement {
    R call(Object... objArr);
}

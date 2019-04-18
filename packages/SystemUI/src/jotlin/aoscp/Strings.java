package jotlin.aoscp;

import java.lang.reflect.Method;

public class Strings {

    public static <T> void appendElement(Appendable appendable, T t, Method<? super T, ? extends CharSequence> method) {
        if (method != null) {
            appendable.append((CharSequence) method.invoke(t));
            return;
        }
        if (t != null ? t instanceof CharSequence : true) {
            appendable.append((CharSequence) t);
        } else if (t instanceof Character) {
            appendable.append(((Character) t).charValue());
        } else {
            appendable.append(String.valueOf(t));
        }
    }
}
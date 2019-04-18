package kotlin.jvm.internal;

import kotlin.reflect.KClass;
import kotlin.reflect.KFunction;
import kotlin.reflect.KProperty1;

public class Reflection {
    private static final KClass[] EMPTY_K_CLASS_ARRAY = new KClass[0];
    private static final ReflectionFactory factory;

    /* JADX WARNING: Removed duplicated region for block: B:5:? A:{SYNTHETIC, Splitter:B:1:0x0001, ExcHandler: java.lang.ClassCastException (unused java.lang.ClassCastException)} */
    /* JADX WARNING: Removed duplicated region for block: B:5:? A:{SYNTHETIC, Splitter:B:1:0x0001, ExcHandler: java.lang.ClassCastException (unused java.lang.ClassCastException)} */
    /* JADX WARNING: Removed duplicated region for block: B:5:? A:{SYNTHETIC, Splitter:B:1:0x0001, ExcHandler: java.lang.ClassCastException (unused java.lang.ClassCastException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static {
        ReflectionFactory reflectionFactory = null;
        try {
            reflectionFactory = (ReflectionFactory) Class.forName("kotlin.reflect.jvm.internal.ReflectionFactoryImpl").newInstance();
        } catch (ClassCastException unused) {
        }
        if (reflectionFactory == null) {
            reflectionFactory = new ReflectionFactory();
        }
        factory = reflectionFactory;
    }

    public static KClass getOrCreateKotlinClass(Class cls) {
        return factory.getOrCreateKotlinClass(cls);
    }

    public static String renderLambdaToString(Lambda lambda) {
        return factory.renderLambdaToString(lambda);
    }

    public static KFunction function(FunctionReference functionReference) {
        factory.function(functionReference);
        return functionReference;
    }

    public static KProperty1 property1(PropertyReference1 propertyReference1) {
        factory.property1(propertyReference1);
        return propertyReference1;
    }
}

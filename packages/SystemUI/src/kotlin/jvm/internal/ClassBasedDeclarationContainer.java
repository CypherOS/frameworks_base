package kotlin.jvm.internal;

import kotlin.reflect.KDeclarationContainer;

/* compiled from: ClassBasedDeclarationContainer.kt */
public interface ClassBasedDeclarationContainer extends KDeclarationContainer {
    Class<?> getJClass();
}

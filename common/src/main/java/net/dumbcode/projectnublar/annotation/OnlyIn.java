package net.dumbcode.projectnublar.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Loader-neutral marker for client-only members (replaces the NeoForge
// distmarker annotations so common has no loader imports). It carries no
// loader enforcement on either platform.
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface OnlyIn {

    Dist value();

    enum Dist {
        CLIENT,
        DEDICATED_SERVER
    }
}

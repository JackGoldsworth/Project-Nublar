package net.dumbcode.projectnublar.platform;

import java.util.ServiceLoader;

// ServiceLoader bridge to the active loader's platform implementation
// (neoforge/fabric module each provides META-INF/services entries).
public class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz).findFirst()
            .orElseThrow(() -> new IllegalStateException("No service implementation found for " + clazz.getName()));
    }
}

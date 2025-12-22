package xyz.overdyn.dyngui;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.overdyn.dyngui.scheduler.TaskScheduler;

public interface DynGui {

    @NotNull JavaPlugin getPlugin();

    TaskScheduler createTaskScheduler();

    void dispose();

    boolean isSupportedPlaceholder();

    /**
     * Initializes the global DynGui instance.
     * This should be called exactly once, typically from your plugin's onEnable().
     */
    static void init(@NotNull DynGui instance) {
        if (Holder.INSTANCE != null) {
            throw new IllegalStateException("DynGui has already been initialized");
        }
        Holder.INSTANCE = instance;
    }

    /**
     * Returns the global DynGui instance.
     *
     * @throws IllegalStateException if init(...) has not been called yet
     */
    static @NotNull DynGui getInstance() {
        DynGui ref = Holder.INSTANCE;
        if (ref == null) {
            throw new IllegalStateException(
                    "DynGui is not initialized. Call DynGui.init(...) in your plugin's onEnable()."
            );
        }
        return ref;
    }

    final class Holder {
        static volatile DynGui INSTANCE;

        private Holder() {
        }
    }
}

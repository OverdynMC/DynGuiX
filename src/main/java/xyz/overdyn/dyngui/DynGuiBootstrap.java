package xyz.overdyn.dyngui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.overdyn.dyngui.dupe.ItemMarker;
import xyz.overdyn.dyngui.listener.GuiListener;
import xyz.overdyn.dyngui.manager.SessionManager;
import xyz.overdyn.dyngui.scheduler.TaskScheduler;
import xyz.overdyn.dyngui.scheduler.TaskSchedulerImpl;

public class DynGuiBootstrap implements DynGui {

    @Getter
    private final JavaPlugin plugin;
    @Getter
    private final boolean supportedPlaceholder;
    private final GuiListener listener;

    private DynGuiBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
        this.listener = new GuiListener();
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        ItemMarker.init(plugin);
        this.supportedPlaceholder = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;

    }

    public static void init(JavaPlugin javaPlugin) {
        DynGui.init(new DynGuiBootstrap(javaPlugin));
    }

    public void dispose() {
        HandlerList.unregisterAll(listener);
        SessionManager.dispose();
        DynGui.Holder.INSTANCE = null;
    }

    @Override
    public TaskScheduler createTaskScheduler() {
        return new TaskSchedulerImpl(this);
    }
}

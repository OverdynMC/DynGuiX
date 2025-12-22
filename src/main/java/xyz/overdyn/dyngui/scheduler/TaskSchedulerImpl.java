package xyz.overdyn.dyngui.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.overdyn.dyngui.DynGui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class TaskSchedulerImpl implements TaskScheduler {

    private final JavaPlugin PLUGIN;
    private final BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    private final Set<BukkitTask> tasks = Collections.synchronizedSet(new HashSet<>());

    public TaskSchedulerImpl(DynGui dynGui) {
        this.PLUGIN = dynGui.getPlugin();
    }

    private void register(BukkitTask task) {
        if (task != null) tasks.add(task);
    }

    @Override
    public @NotNull BukkitTask runTask(@NotNull Runnable task) {
        BukkitTask t = SCHEDULER.runTask(PLUGIN, task);
        register(t);
        return t;
    }

    @Override
    public @NotNull BukkitTask runTaskAsync(@NotNull Runnable task) {
        BukkitTask t = SCHEDULER.runTaskAsynchronously(PLUGIN, task);
        register(t);
        return t;
    }

    @Override
    public void runTask(@NotNull Consumer<BukkitTask> action) {
        BukkitTask t = SCHEDULER.runTask(PLUGIN, () -> {});
        register(t);
        action.accept(t);
    }

    @Override
    public void runTaskAsync(@NotNull Consumer<BukkitTask> action) {
        BukkitTask t = SCHEDULER.runTaskAsynchronously(PLUGIN, () -> {});
        register(t);
        action.accept(t);
    }

    @Override
    public @NotNull BukkitTask runTask(@NotNull Runnable task, long delay) {
        BukkitTask t = SCHEDULER.runTaskLater(PLUGIN, task, delay);
        register(t);
        return t;
    }

    @Override
    public @NotNull BukkitTask runTaskAsync(@NotNull Runnable task, long delay) {
        BukkitTask t = SCHEDULER.runTaskLaterAsynchronously(PLUGIN, task, delay);
        register(t);
        return t;
    }

    @Override
    public void runTask(@NotNull Consumer<BukkitTask> action, long delay) {
        BukkitTask t = SCHEDULER.runTaskLater(PLUGIN, () -> {}, delay);
        register(t);
        action.accept(t);
    }

    @Override
    public void runTaskAsync(@NotNull Consumer<BukkitTask> action, long delay) {
        BukkitTask t = SCHEDULER.runTaskLaterAsynchronously(PLUGIN, () -> {}, delay);
        register(t);
        action.accept(t);
    }

    @Override
    public @NotNull BukkitTask runTask(@NotNull Runnable task, long delay, long period) {
        BukkitTask t = SCHEDULER.runTaskTimer(PLUGIN, task, delay, period);
        register(t);
        return t;
    }

    @Override
    public @NotNull BukkitTask runTaskAsync(@NotNull Runnable task, long delay, long period) {
        BukkitTask t = SCHEDULER.runTaskTimerAsynchronously(PLUGIN, task, delay, period);
        register(t);
        return t;
    }

    @Override
    public void runTask(@NotNull Consumer<BukkitTask> action, long delay, long period) {
        BukkitTask t = SCHEDULER.runTaskTimer(PLUGIN, () -> {}, delay, period);
        register(t);
        action.accept(t);
    }

    @Override
    public void runTaskAsync(@NotNull Consumer<BukkitTask> action, long delay, long period) {
        BukkitTask t = SCHEDULER.runTaskTimerAsynchronously(PLUGIN, () -> {}, delay, period);
        register(t);
        action.accept(t);
    }

    @Override
    public @NotNull Set<BukkitTask> getTasks() {
        return Collections.unmodifiableSet(tasks);
    }

    @Override
    public void cancelAll() {
        synchronized (tasks) {
            for (BukkitTask task : tasks) {
                if (!task.isCancelled()) task.cancel();
            }
            tasks.clear();
        }
    }

    @Override
    public void cancel(@NotNull BukkitTask task) {
        task.cancel();
        tasks.remove(task);
    }

    @Override
    public void cleanup() {
        tasks.removeIf(BukkitTask::isCancelled);
    }
}

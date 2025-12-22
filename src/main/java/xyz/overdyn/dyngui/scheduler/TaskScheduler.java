package xyz.overdyn.dyngui.scheduler;

import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Consumer;

/**
 * High-level abstraction over the Bukkit scheduler.
 *
 * <p>This scheduler:
 * <ul>
 *   <li>Wraps common scheduling operations (sync / async, delayed, repeating)</li>
 *   <li>Tracks created tasks so they can be cancelled in bulk</li>
 *   <li>Provides basic cleanup utilities for cancelled tasks</li>
 * </ul>
 */
public interface TaskScheduler {

    /**
     * Run a task on the main server thread on the next tick.
     *
     * @param task runnable to execute
     * @return created BukkitTask
     */
    @NotNull BukkitTask runTask(@NotNull Runnable task);

    /**
     * Run a task asynchronously.
     *
     * @param task runnable to execute
     * @return created BukkitTask
     */
    @NotNull BukkitTask runTaskAsync(@NotNull Runnable task);

    /**
     * Create and register a task on the main thread and pass it to the given action.
     * The runnable itself is a no-op; the task object is the main purpose.
     *
     * @param action consumer that receives the created task
     */
    void runTask(@NotNull Consumer<BukkitTask> action);

    /**
     * Create and register an asynchronous task and pass it to the given action.
     * The runnable itself is a no-op; the task object is the main purpose.
     *
     * @param action consumer that receives the created task
     */
    void runTaskAsync(@NotNull Consumer<BukkitTask> action);

    /**
     * Run a task on the main thread after a delay.
     *
     * @param task  runnable to execute
     * @param delay delay in ticks
     * @return created BukkitTask
     */
    @NotNull BukkitTask runTask(@NotNull Runnable task, long delay);

    /**
     * Run a task asynchronously after a delay.
     *
     * @param task  runnable to execute
     * @param delay delay in ticks
     * @return created BukkitTask
     */
    @NotNull BukkitTask runTaskAsync(@NotNull Runnable task, long delay);

    /**
     * Create a delayed task on the main thread and pass it to the given action.
     * The runnable itself is a no-op; the task object is the main purpose.
     *
     * @param action consumer that receives the created task
     * @param delay  delay in ticks
     */
    void runTask(@NotNull Consumer<BukkitTask> action, long delay);

    /**
     * Create a delayed asynchronous task and pass it to the given action.
     * The runnable itself is a no-op; the task object is the main purpose.
     *
     * @param action consumer that receives the created task
     * @param delay  delay in ticks
     */
    void runTaskAsync(@NotNull Consumer<BukkitTask> action, long delay);

    /**
     * Run a repeating task on the main thread.
     *
     * @param task   runnable to execute
     * @param delay  initial delay in ticks
     * @param period repeat period in ticks
     * @return created BukkitTask
     */
    @NotNull BukkitTask runTask(@NotNull Runnable task, long delay, long period);

    /**
     * Run a repeating asynchronous task.
     *
     * @param task   runnable to execute
     * @param delay  initial delay in ticks
     * @param period repeat period in ticks
     * @return created BukkitTask
     */
    @NotNull BukkitTask runTaskAsync(@NotNull Runnable task, long delay, long period);

    /**
     * Create a repeating task on the main thread and pass it to the given action.
     * The runnable itself is a no-op; the task object is the main purpose.
     *
     * @param action consumer that receives the created task
     * @param delay  initial delay in ticks
     * @param period repeat period in ticks
     */
    void runTask(@NotNull Consumer<BukkitTask> action, long delay, long period);

    /**
     * Create a repeating asynchronous task and pass it to the given action.
     * The runnable itself is a no-op; the task object is the main purpose.
     *
     * @param action consumer that receives the created task
     * @param delay  initial delay in ticks
     * @param period repeat period in ticks
     */
    void runTaskAsync(@NotNull Consumer<BukkitTask> action, long delay, long period);

    /**
     * Returns an unmodifiable view of all tracked tasks.
     *
     * @return set of tracked tasks
     */
    @NotNull Set<BukkitTask> getTasks();

    /**
     * Cancels all tracked tasks and clears the internal collection.
     */
    void cancelAll();

    /**
     * Cancels a single task and removes it from the internal collection.
     *
     * @param task task to cancel
     */
    void cancel(@NotNull BukkitTask task);

    /**
     * Removes all tasks from the internal collection that are already cancelled.
     * Does not cancel any tasks by itself.
     */
    void cleanup();
}

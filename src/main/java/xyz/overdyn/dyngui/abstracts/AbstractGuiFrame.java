package xyz.overdyn.dyngui.abstracts;

import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.overdyn.dyngui.items.GuiItem;
import xyz.overdyn.dyngui.policy.GuiPolicy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract GUI Frame.
 *
 * <p>
 * Provides ready-to-use animation and transformation utilities
 * for GUI layers without enforcing any lifecycle or animation flow.
 * </p>
 *
 * <p>
 * All tasks are automatically cancelled when GUI closes.
 * </p>
 */
public abstract class AbstractGuiFrame extends AbstractGuiLayer {

    private final List<BukkitTask> tasks = new ArrayList<>();

    {
        onClose(e -> stopAllFrames());
    }

    /* ========================================================= */
    /* ===================== CONSTRUCTORS ====================== */
    /* ========================================================= */

    public AbstractGuiFrame(InventoryType type, @NotNull Component title, GuiPolicy policy) {
        super(type, title, policy);
    }

    public AbstractGuiFrame(@NotNull Component title, GuiPolicy policy) {
        super(title, policy);
    }

    public AbstractGuiFrame(int size, @NotNull Component title, GuiPolicy policy) {
        super(size, title, policy);
    }

    /* ========================================================= */
    /* ===================== SCHEDULER UTILS ================== */
    /* ========================================================= */

    /** Schedule a single task after a delay (ticks) */
    protected final void after(long delay, Runnable action) {
        BukkitTask task = scheduler.runTask(() -> {
            if (!isOpen()) return;
            action.run();
        }, delay);
        tasks.add(task);
    }

    /** Schedule a repeating task with delay and period */
    protected final void every(long delay, long period, Runnable action) {
        BukkitTask task = scheduler.runTask(() -> {
            if (!isOpen()) return;
            action.run();
        }, delay, period);
        tasks.add(task);
    }

    /** Stop all scheduled tasks for this frame */
    protected final void stopAllFrames() {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
    }

    /* ========================================================= */
    /* ===================== SLOT OPERATIONS ================== */
    /* ========================================================= */

    /** Instantly register GuiItem */
    protected final void register(GuiItem item) {
        for (int slot : item.getSlots()) {
            unregisterSlotOnly(slot);
        }
        registerItem(item);
    }

    /** Instantly clear slots */
    protected final void clearSlots(Collection<Integer> slots) {
        slots.forEach(slot -> {
            unregisterSlotOnly(slot);
            getInventory().clear(slot);
        });
    }

    /** Move single GuiItem from one slot to another */
    protected final void moveItem(int from, int to) {
        GuiItem item = getItem(from).clone();
        if (item == null) return;

        unregisterSlotOnly(from);
        unregisterItem(from);
        item.clearSlots();
        item.addSlot(to);
        registerItem(item);
    }

    /** Swap two GuiItems */
    protected final void swapItems(int a, int b) {
        GuiItem itemA = getItem(a).clone();
        GuiItem itemB = getItem(b).clone();

        if (itemA != null) unregisterSlotOnly(a);
        if (itemB != null) unregisterSlotOnly(b);

        if (itemA != null) {
            itemA.clearSlots();
            itemA.addSlot(b);
            registerItem(itemA);
        }
        if (itemB != null) {
            itemB.clearSlots();
            itemB.addSlot(a);
            registerItem(itemB);
        }
    }

    /** Move multiple items with optional delay between each */
    protected final void moveMultiple(List<Integer> fromSlots, List<Integer> toSlots, long delay) {
        int size = Math.min(fromSlots.size(), toSlots.size());
        for (int i = 0; i < size; i++) {
            int from = fromSlots.get(i);
            int to = toSlots.get(i);
            after(i * delay, () -> moveItem(from, to));
        }
    }

    /** Swap multiple pairs of items with optional delay */
    protected final void swapMultiple(List<Integer> slotsA, List<Integer> slotsB, long delay) {
        int size = Math.min(slotsA.size(), slotsB.size());
        for (int i = 0; i < size; i++) {
            int a = slotsA.get(i);
            int b = slotsB.get(i);
            after(i * delay, () -> swapItems(a, b));
        }
    }

    /* ========================================================= */
    /* ===================== ANIMATIONS ======================= */
    /* ========================================================= */

    /** Fill slots sequentially with GuiItem and period */
    protected final void fillSequential(List<Integer> slots, GuiItem item, long period) {
        Iterator<Integer> it = slots.iterator();
        every(0, period, () -> {
            if (!it.hasNext()) {
                stopAllFrames();
                return;
            }
            int slot = it.next();
            GuiItem copy = item.clone();
            copy.clearSlots();
            copy.addSlot(slot);
            register(copy);
        });
    }

    /** Clear slots sequentially */
    protected final void clearSequential(List<Integer> slots, long period) {
        Iterator<Integer> it = slots.iterator();
        every(0, period, () -> {
            if (!it.hasNext()) {
                stopAllFrames();
                return;
            }
            unregisterItem(it.next());
        });
    }

    /** Blink GuiItem in slot periodically */
    protected final void blink(int slot, long period) {
        GuiItem original = getItem(slot);
        if (original == null) return;

        every(0, period, new Runnable() {
            boolean visible = true;

            @Override
            public void run() {
                if (!isOpen()) {
                    stopAllFrames();
                    return;
                }
                if (visible) unregisterItem(slot);
                else registerItem(original.clone());
                visible = !visible;
            }
        });
    }

    /** Wave animation across slots sequentially */
    protected final void wave(List<Integer> slots, long period) {
        Iterator<Integer> it = slots.iterator();
        every(0, period, new Runnable() {
            Integer prev = null;

            @Override
            public void run() {
                if (!it.hasNext()) {
                    stopAllFrames();
                    return;
                }
                if (prev != null) unregisterItem(prev);
                prev = it.next();
                GuiItem item = getItem(slots.get(0));
                if (item != null) {
                    registerItem(item.clone().clearSlots().addSlot(prev));
                }
            }
        });
    }

    /** Wind / spiral animation */
    protected final void windAnimation(List<List<Integer>> layers, GuiItem item, long step) {
        long tick = 0;
        for (List<Integer> layer : layers) {
            for (int slot : layer) {
                long currentTick = tick;
                after(currentTick, () -> {
                    GuiItem copy = item.clone();
                    copy.clearSlots();
                    copy.addSlot(slot);
                    register(copy);
                });
            }
            tick += step;
        }
    }
}

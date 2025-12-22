package xyz.overdyn.dyngui.abstracts;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.overdyn.dyngui.DynGui;
import xyz.overdyn.dyngui.manager.SessionManager;
import xyz.overdyn.dyngui.policy.GuiPolicy;
import xyz.overdyn.dyngui.policy.sections.InteractionPolicy;
import xyz.overdyn.dyngui.scheduler.TaskScheduler;
import xyz.overdyn.dyngui.tools.InventoryTitleUpdater;

import java.util.Objects;

/**
 * Base abstract GUI class.
 *
 * <p>
 * Represents a single, stateful GUI instance bound to exactly one
 * {@link Inventory} and at most one {@link Player} at any given time.
 * </p>
 *
 * <p>
 * <b>Design contract:</b>
 * </p>
 * <ul>
 *     <li>One {@code AbstractGui} instance is intended for one logical GUI container</li>
 *     <li>One GUI instance may be opened by only one player simultaneously</li>
 *     <li>Reusing the same instance for multiple players is explicitly forbidden</li>
 * </ul>
 *
 * <p>
 * The class is responsible for:
 * </p>
 * <ul>
 *     <li>Inventory creation and rebuilding</li>
 *     <li>GUI open / close lifecycle</li>
 *     <li>Viewer ownership enforcement</li>
 *     <li>Session registration and cleanup</li>
 *     <li>Scheduled task lifecycle management</li>
 * </ul>
 *
 * <p>
 * <b>Threading:</b> All methods must be invoked from the Bukkit main thread.
 * </p>
 */
public abstract class AbstractGui implements InventoryHolder {

    /**
     * Scheduler bound to this GUI instance.
     *
     * <p>
     * All scheduled tasks are automatically cancelled when the GUI is closed.
     * </p>
     */
    protected final TaskScheduler scheduler =
            DynGui.getInstance().createTaskScheduler();

    /**
     * Inventory size for chest-based inventories.
     *
     * <p>
     * Always a multiple of 9 in range [9; 54].
     * </p>
     */
    private int size;

    /**
     * Inventory type, or {@code null} if a chest inventory is used.
     */
    private @Nullable InventoryType type;

    /**
     * Current GUI title.
     */
    private @NotNull Component title;

    /**
     * Backing Bukkit inventory instance.
     */
    private Inventory inventory;

    /**
     * Policy defining allowed interactions and behavior.
     */
    private GuiPolicy policy;

    /**
     * Current viewer of this GUI.
     *
     * <p>
     * {@code null} if the GUI is not open.
     * </p>
     */
    private @Nullable Player viewer;

    /**
     * Creates a chest-based GUI with default size (54).
     *
     * @param title  GUI title
     * @param policy GUI policy
     */
    public AbstractGui(@NotNull Component title, @NotNull GuiPolicy policy) {
        this(54, null, title, policy);
    }

    /**
     * Creates a chest-based GUI with a custom size.
     *
     * @param size   inventory size (must be divisible by 9)
     * @param title  GUI title
     * @param policy GUI policy
     */
    public AbstractGui(int size, @NotNull Component title, @NotNull GuiPolicy policy) {
        this(size, null, title, policy);
    }

    /**
     * Creates a GUI based on a specific {@link InventoryType}.
     *
     * @param type   inventory type
     * @param title  GUI title
     * @param policy GUI policy
     */
    public AbstractGui(@NotNull InventoryType type, @NotNull Component title, @NotNull GuiPolicy policy) {
        this(type.getDefaultSize(), type, title, policy);
    }

    /**
     * Internal base constructor.
     *
     * @param size   inventory size
     * @param type   inventory type or {@code null} for chest inventory
     * @param title  GUI title
     * @param policy GUI policy
     */
    private AbstractGui(
            int size,
            @Nullable InventoryType type,
            @NotNull Component title,
            @NotNull GuiPolicy policy
    ) {
        validateSize(size);

        this.type = type;
        this.size = size;
        this.title = title;
        this.policy = policy;

        this.inventory = createInventory();
    }

    /**
     * Creates a new Bukkit inventory instance based on current size, type and title.
     *
     * @return newly created inventory
     */
    private Inventory createInventory() {
        return type != null
                ? Bukkit.createInventory(this, type, title)
                : Bukkit.createInventory(this, size, title);
    }

    /**
     * Rebuilds the inventory and reopens it for the current viewer, if present.
     *
     * <p>
     * Intended exclusively for structural changes such as inventory size
     * or type modification.
     * </p>
     */
    protected final void rebuildAndReopen() {
        if (viewer == null || !viewer.isOnline()) {
            inventory = createInventory();
            return;
        }

        Player current = viewer;
        close();

        inventory = createInventory();
        open(current);
    }

    /**
     * Opens this GUI for the specified entity.
     *
     * <p>
     * This GUI instance may only be opened by a single player.
     * Attempting to open it for another player will result in an exception.
     * </p>
     *
     * @param entity viewer entity (must be a {@link Player})
     *
     * @throws IllegalArgumentException if entity is not a player
     * @throws IllegalStateException    if the GUI is already bound to another player
     */
    public void open(@NotNull HumanEntity entity) {
        DynGui.getInstance(); //no-op;

        if (!(entity instanceof Player player)) {
            throw new IllegalArgumentException("GUI can only be opened for Player");
        }

        if (viewer != null && viewer != player) {
            throw new IllegalStateException(
                    "This GUI instance is already bound to another player: " + viewer.getName()
            );
        }

        if (viewer == player) {
            return;
        }

        this.viewer = player;
        player.openInventory(inventory);
        SessionManager.register(player, this);
    }

    /**
     * Closes this GUI for the current viewer, if any.
     */
    public final void close() {
        if (viewer == null) return;
        viewer.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
    }

    /**
     * Closes this GUI for the specified entity.
     *
     * @param player entity whose inventory should be closed
     */
    public final void close(@NotNull HumanEntity player) {
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
    }

    /**
     * Handles the GUI close lifecycle.
     *
     * <p>
     * This method is expected to be called by the GUI event dispatcher
     * when an {@link InventoryCloseEvent} occurs.
     * </p>
     *
     * @param player player who closed the GUI
     */
    public final void handleClose(@NotNull Player player) {
        if (!Objects.equals(viewer, player)) return;

        if (policy.interaction().isEnabled(
                InteractionPolicy.InteractionType.UPDATE_AFTER_CLOSE
        )) {
            Bukkit.getScheduler().runTaskLater(
                    DynGui.getInstance().getPlugin(),
                    player::updateInventory,
                    1
            );
        }

        scheduler.cancelAll();
        viewer = null;
        SessionManager.unregister(player);
    }

    /**
     * Checks whether this GUI is currently open for its viewer.
     *
     * @return {@code true} if the GUI is open and active
     */
    public final boolean isOpen() {
        return viewer != null
                && viewer.isOnline()
                && viewer.getOpenInventory().getTopInventory() == inventory;
    }

    /**
     * Handles a Bukkit event related to this GUI.
     *
     * @param event Bukkit event
     */
    public abstract void handleEvent(@NotNull Event event);

    /**
     * Updates the GUI title without rebuilding the inventory.
     *
     * @param newTitle new title component
     * @return {@code true} if the title was successfully updated
     */
    public final boolean updateTitle(@NotNull Component newTitle) {
        this.title = newTitle;

        if (!isOpen()) return false;

        boolean result = InventoryTitleUpdater.updateTitle(viewer, title);
        viewer.updateInventory();
        return result;
    }

    /**
     * Updates the GUI title using the current title value.
     *
     * @return {@code true} if the title was successfully updated
     */
    public final boolean updateTitle() {
        return updateTitle(getTitle());
    }

    /**
     * Returns the backing Bukkit inventory.
     *
     * @return inventory instance
     */
    @Override
    public final @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * Returns the configured inventory size.
     *
     * @return inventory size
     */
    public final int getSize() {
        return size;
    }

    /**
     * Sets a new inventory size.
     *
     * <p>
     * Requires {@link #rebuildAndReopen()} to take effect.
     * </p>
     *
     * @param size new inventory size
     */
    public final void setSize(int size) {
        validateSize(size);
        this.size = size;
    }

    /**
     * Returns the inventory type.
     *
     * @return inventory type or {@code null}
     */
    public final @Nullable InventoryType getType() {
        return type;
    }

    /**
     * Sets a new inventory type.
     *
     * <p>
     * Requires {@link #rebuildAndReopen()} to take effect.
     * </p>
     *
     * @param type new inventory type
     */
    public final void setType(@Nullable InventoryType type) {
        this.type = type;
    }

    /**
     * Returns the current GUI title.
     *
     * @return title component
     */
    public final @NotNull Component getTitle() {
        return title;
    }

    /**
     * Sets a new GUI title.
     *
     * @param title new title component
     */
    public final void setTitle(@NotNull Component title) {
        this.title = title;
    }

    /**
     * Returns the current viewer.
     *
     * @return viewer or {@code null}
     */
    public final @Nullable Player getViewer() {
        return viewer;
    }

    /**
     * Returns the active GUI policy.
     *
     * @return GUI policy
     */
    public final @NotNull GuiPolicy getPolicy() {
        return policy;
    }

    /**
     * Sets a new GUI policy.
     *
     * @param policy new policy
     */
    public final void setPolicy(@NotNull GuiPolicy policy) {
        this.policy = policy;
    }

    /**
     * Validates inventory size constraints.
     *
     * @param size inventory size
     */
    private void validateSize(int size) {
        if (size % 9 != 0 || size < 9 || size > 54) {
            throw new IllegalArgumentException(
                    "Invalid inventory size: " + size +
                            ". Allowed sizes: 9, 18, 27, 36, 45, 54."
            );
        }
    }
}

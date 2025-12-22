package xyz.overdyn.dyngui.abstracts;

import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.overdyn.dyngui.items.GuiItem;
import xyz.overdyn.dyngui.items.ItemWrapper;
import xyz.overdyn.dyngui.policy.GuiPolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A paginated extension of {@link AbstractGuiLayer} that manages multiple pages of {@link ItemWrapper}s.
 * <p>
 * Each page consists of a list of {@link ItemWrapper}-based controllers that are registered when
 * the page is opened and unregistered when switching away.
 * <p>
 * Fixed elements (like navigation or borders) are handled separately and remain persistent.
 * This structure ensures no dynamic slot conflicts and allows manual page navigation.
 *
 * <p>Page switching is explicit and safe. If conflicts arise (e.g., same slot used on multiple
 * pages and not cleared), they must be resolved by design.</p>
 */
@ApiStatus.Experimental
@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class AbstractGuiPaginator extends AbstractGuiLayer {

    /**
     * Internal list of defined GUI pages.
     */
    protected final List<Page> pages = new ArrayList<>(); //TODO: Возможно сделать, чтобы можно было какую-то получить страницу и получить, допустим, определенный какой-то слот и уже предмет там обновить или что-то такое.

    /**
     * Currently active page index. -1 if no page is active.
     */
    protected int currentPage = -1;

    /**
     * Constructs a paginated GUI with the given component title and policy.
     *
     * @param title     The GUI title
     * @param guiPolicy The policy to apply to this GUI
     */
    public AbstractGuiPaginator(@NotNull Component title, GuiPolicy guiPolicy) {
        super(title, guiPolicy);
    }

    /**
     * Constructs a paginated GUI with the given inventory type, title, and policy.
     *
     * @param type      The type of inventory
     * @param title     The GUI title
     * @param guiPolicy The policy to apply to this GUI
     */
    public AbstractGuiPaginator(InventoryType type, @NotNull Component title, GuiPolicy guiPolicy) {
        super(type, title, guiPolicy);
    }

    /**
     * Constructs a paginated GUI with the given inventory size, title, and policy.
     *
     * @param size      The size of the inventory (must be multiple of 9, max 54)
     * @param title     The GUI title
     * @param guiPolicy The policy to apply to this GUI
     */
    public AbstractGuiPaginator(int size, @NotNull Component title, GuiPolicy guiPolicy) {
        super(size, title, guiPolicy);
    }

    /**
     * Returns the number of registered pages.
     *
     * @return total number of pages
     */
    @Contract(pure = true)
    public final int pages() {
        return pages.size();
    }

    /**
     * Returns the index of the currently active page.
     *
     * @return index of current page, or -1 if no page is active
     */
    @Contract(pure = true)
    public final int currentPage() {
        return currentPage;
    }

    /**
     * Adds a new page defined by one or more {@link ItemWrapper}s.
     *
     * @param builderConsumers Item wrappers to add to the new page
     */
    public final void addPage(@NotNull GuiItem... builderConsumers) {
        pages.add(new Page(Arrays.asList(builderConsumers)));
    }

    /**
     * Adds a new page defined by a list of {@link ItemWrapper}s.
     *
     * @param builderConsumers Item wrappers to add to the new page
     */
    public final void addPage(@NotNull List<GuiItem> builderConsumers) {
        pages.add(new Page(builderConsumers));
    }

    /**
     * Navigates to the next page, if available.
     *
     * @return true if page changed, false otherwise
     */
    public boolean nextPage() {
        if (currentPage < pages.size() - 1) {
            openPage(currentPage + 1);
            return true;
        }
        return false;
    }

    /**
     * Navigates to the previous page, if available.
     *
     * @return true if page changed, false otherwise
     */
    public boolean previousPage() {
        if (currentPage > 0) {
            openPage(currentPage - 1);
            return true;
        }
        return false;
    }

    /**
     * Opens a specific page, unregistering items from the previous one
     * and registering items of the target page.
     *
     * @param pageIndex target page index (0-based)
     */
    public void openPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= pages.size()) {
            return;
        }

        // Unregister current page items
        if (currentPage >= 0 && currentPage < pages.size()) {
            Page current = pages.get(currentPage);
            for (GuiItem item : current.controllers()) {
                unregisterItem(item);
            }
        }

        // Register new page items
        Page target = pages.get(pageIndex);
        for (GuiItem item : target.controllers()) {
            registerItem(item);
        }

        this.currentPage = pageIndex;
    }

    /**
     * Represents a single GUI page containing a list of item controllers.
     *
     * <p>Items in this page are only active when the page is open.
     * They are registered/unregistered with the inventory dynamically.</p>
     *
     * @param controllers list of item wrappers for this page
     */
    public record Page(List<GuiItem> controllers) {

        /**
         * Creates an empty page.
         */
        public Page() {
            this(new ArrayList<>());
        }

        /**
         * Creates a page with a defensive copy of the given controllers.
         *
         * @param controllers list of items to copy
         */
        public Page(List<GuiItem> controllers) {
            this.controllers = new ArrayList<>(controllers);
        }

        /**
         * Adds an item to this page.
         *
         * @param wrapper item to add
         */
        public void addItem(@NotNull GuiItem wrapper) {
            controllers.add(wrapper);
        }

        /**
         * Removes an item from this page.
         *
         * @param wrapper item to remove
         */
        public void removeItem(@NotNull GuiItem wrapper) {
            controllers.remove(wrapper);
        }

        /**
         * Clears all items from this page.
         */
        public void clear() {
            controllers.clear();
        }

        /**
         * Gets the item registered at a given slot, or null if not found.
         *
         * @param slot slot index
         * @return item wrapper or null
         */
        public GuiItem getBySlot(int slot) {
            for (GuiItem item : controllers) {
                if (item.getSlots().contains(slot)) {
                    return item;
                }
            }
            return null;
        }

        /**
         * Replaces an item by slot with a new item.
         *
         * @param slot    slot to replace
         * @param newItem new item to assign
         * @return true if replaced, false if no item was found
         */
        public boolean replaceItem(int slot, @NotNull GuiItem newItem) {
            GuiItem old = getBySlot(slot);
            if (old == null) return false;

            controllers.remove(old);
            controllers.add(newItem);
            return true;
        }
    }
}
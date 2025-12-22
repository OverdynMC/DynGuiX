package xyz.overdyn.dyngui.abstracts;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import xyz.overdyn.dyngui.items.GuiItem;
import xyz.overdyn.dyngui.policy.GuiPolicy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract GUI implementation with paginated content handling.
 *
 * <p>Extends {@link AbstractGuiPaginator} and adds:</p>
 * <ul>
 *     <li>Support for dynamically populated paginated items</li>
 *     <li>Slot allocation for where items may appear</li>
 *     <li>Per-page logic hook before displaying content</li>
 *     <li>Efficient removal by key</li>
 * </ul>
 *
 * <p>To use this, populate content via {@link #addItemToContent(GuiItem)},
 * specify allowed slot via {@link #setAllowedSlots(int...)}, and call {@link #open(Player)}.</p>
 */
public abstract class AbstractGuiContent extends AbstractGuiPaginator {

    /**
     * All registered content items across pages.
     */
    private final List<GuiItem> content = new ArrayList<>();

    /**
     * Slots allowed for placing content items (per page).
     */
    private int[] allowedSlots;

    /**
     * Constructs a paginated GUI with the given component title and policy.
     *
     * @param title      GUI title as Adventure component
     * @param guiPolicy  GUI behavior policy
     */
    public AbstractGuiContent(@NotNull Component title, GuiPolicy guiPolicy) {
        super(title, guiPolicy);
    }

    /**
     * Constructs a paginated GUI with the given inventory type, title and policy.
     *
     * @param type       Bukkit inventory type
     * @param title      GUI title
     * @param guiPolicy  GUI behavior policy
     */
    public AbstractGuiContent(InventoryType type, @NotNull Component title, GuiPolicy guiPolicy) {
        super(type, title, guiPolicy);
    }

    /**
     * Constructs a paginated GUI with the given size, title and policy.
     *
     * @param size       inventory size in slot
     * @param title      GUI title
     * @param guiPolicy  GUI behavior policy
     */
    public AbstractGuiContent(int size, @NotNull Component title, GuiPolicy guiPolicy) {
        super(size, title, guiPolicy);
    }

    /**
     * Adds a new {@link GuiItem} to the paginated content pool.
     * Call {@link #open(Player)} again to refresh the UI.
     *
     * @param wrapper item to add
     */
    public void addItemToContent(GuiItem wrapper) {
        content.add(wrapper);
    }

    /**
     * Specifies which slot are used to render paginated content.
     * This must be called before {@link #open(Player)}.
     *
     * @param slots array of allowed inventory slot
     */
    public void setAllowedSlots(int... slots) {
        this.allowedSlots = slots;
    }

    /**
     * Opens a specific page and runs post-page logic.
     *
     * @param page target page index
     */
    @Override
    public void openPage(int page) {
        super.openPage(page);
        everyPageLogic();
    }

    /**
     * Removes all items from content (and visible pages) that match the given key.
     *
     * @param key unique key to remove by
     * @return number of items removed
     */
    public int removeContentByKey(@NotNull String key) {
        int page = currentPage();

        List<GuiItem> toRemove = new ArrayList<>();
        for (GuiItem item : content) {
            if (key.equals(item.key())) {
                toRemove.add(item);
            }
        }

        if (toRemove.isEmpty()) return 0;

        int removedCount = 0;

        for (GuiItem item : toRemove) {
            if (!pages.isEmpty()) {
                List<GuiItem> pageItems = pages.get(page).controllers();

                if (pageItems.contains(item)) {
                    pageItems.remove(item);
                    content.remove(item);
                    for (int slot : item.getSlots()) {
                        unregisterItem(slot);
                    }

                    removedCount++;
                }

                for (Page pageS : pages) {
                    pageS.controllers().remove(item);
                }
            }
        }
        return removedCount;
    }

    /**
     * Generates paginated pages from the current {@link #content} list.
     * Items are assigned into {@link #allowedSlots}.
     */
    private void generatePages() {
        pages.clear();

        int perPage = allowedSlots.length;
        int totalItems = content.size();
        int totalPages = (int) Math.ceil((double) totalItems / perPage);

        for (int p = 0; p < totalPages; p++) {
            List<GuiItem> items = new ArrayList<>();

            int from = p * perPage;
            int to = Math.min(from + perPage, totalItems);
            int index = 0;

            for (int i = from; i < to; i++) {
                int slot = allowedSlots[index++];
                var item = content.get(i);
                item.setSlots(Collections.singleton(slot));

                items.add(item);
            }

            addPage(items);
        }
    }

    /**
     * Hook called after each {@link #openPage(int)} call.
     * Subclasses can override to implement per-page logic (e.g. updating footer).
     */
    protected void everyPageLogic() {}

    /**
     * Regenerates pages and opens the GUI for the given player.
     *
     * @param player player to open for
     */
    public final void open(@NotNull Player player) {
        generatePages();
        super.open(player);
        openPage(0);
    }
}

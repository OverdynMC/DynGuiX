package xyz.overdyn.dyngui.items;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.overdyn.dyngui.dupe.ItemMarker;
import xyz.overdyn.dyngui.placeholder.Placeholder;
import xyz.overdyn.dyngui.placeholder.context.PlaceholderContextImpl;

import java.util.*;
import java.util.function.Consumer;

/**
 * GUI-обвязка вокруг {@link ItemWrapper}.
 *
 * Здесь:
 * - слоты
 * - key
 * - metadata
 * - placeholderEngine (персонализирует имя/лор под игрока)
 * - clickHandler
 *
 * Сам предмет (material, name, lore, model data, флаги, глинт) лежит в {@link ItemWrapper}.
 */
@Getter
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class GuiItem implements Cloneable {

    private final ItemWrapper itemWrapper;

    private final Collection<Integer> slots = new ArrayList<>();
    private boolean marker;
    private boolean update;
    private String key;
    private final Map<String, Object> metadata = new HashMap<>();
    private final Map<String, Object> metadataPlaceholder = new HashMap<>();

    private @Nullable Placeholder placeholderEngine;
    private @Nullable Consumer<InventoryClickEvent> clickHandler;
    private ItemMeta cachedMeta;

    public GuiItem(@NotNull ItemWrapper item) {
        this.marker = true;
        this.itemWrapper = Objects.requireNonNull(item, "item");
    }

    public GuiItem(@NotNull ItemStack baseStack) {
        this(new ItemWrapper(baseStack));
    }

    public GuiItem(@NotNull Material material) {
        this(new ItemWrapper(material));
    }

    public GuiItem(@NotNull Material material, int amount) {
        this(new ItemWrapper(material, amount));
    }

    /* ========= slot / key / metadata ========= */

    public GuiItem setSlots(@NotNull Collection<Integer> slots) {
        this.slots.clear();
        this.slots.addAll(slots);
        return this;
    }

    public GuiItem addSlot(int slot) {
        this.slots.add(slot);
        return this;
    }

    public GuiItem addSlots(@NotNull Collection<Integer> slots) {
        this.slots.addAll(slots);
        return this;
    }

    public GuiItem remSlot(int slot) {
        this.slots.remove(slot);
        return this;
    }

    public GuiItem remSlots(@NotNull Collection<Integer> slots) {
        this.slots.removeAll(slots);
        return this;
    }

    public GuiItem clearSlots() {
        this.slots.clear();
        return this;
    }

    public GuiItem setMarker(boolean marker) {
        this.marker = marker;
        return this;
    }

    public boolean isMarker() {
        return marker;
    }

    public String key() {
        return key;
    }

    public GuiItem key(@Nullable String key) {
        this.key = key;
        return this;
    }

    /**
     * Sets whether this GUI item should automatically update its display name and lore.
     *
     * <p>If {@code true}, the item's {@link ItemWrapper#displayName()} and {@link ItemWrapper#lore()}
     * will be refreshed to reflect any changes, including placeholder replacements or dynamic content updates.</p>
     *
     * <p>If {@code false}, the item's display name and lore remain static and will not be updated
     * automatically, even if placeholders or dynamic values change.</p>
     *
     * @param update {@code true} to enable automatic updating of display name and lore, {@code false} to disable
     * @return this {@link GuiItem} instance for chaining
     */
    public GuiItem setUpdate(boolean update) {
        this.update = update;
        return this;
    }

    public GuiItem set(@NotNull String name, @Nullable Object value) {
        metadata.put(name, value);
        return this;
    }

    @Nullable
    public Object get(@NotNull String name) {
        return metadata.get(name);
    }

    @Nullable
    public <T> T getAs(@NotNull String name, @NotNull Class<T> type) {
        Object o = metadata.get(name);
        if (o == null) return null;
        return type.cast(o);
    }

    public <T> T getOrDefault(@NotNull String name, @Nullable T def, @NotNull Class<T> type) {
        Object o = metadata.get(name);
        return o == null ? def : type.cast(o);
    }

    public boolean has(@NotNull String name) {
        return metadata.containsKey(name);
    }

    public GuiItem remove(@NotNull String name) {
        metadata.remove(name);
        return this;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    /* ========= placeholder / click ========= */

    public GuiItem placeholderEngine(Placeholder placeholderEngine) {
        this.placeholderEngine = placeholderEngine;
        return this;
    }

    public GuiItem onClick(@Nullable Consumer<InventoryClickEvent> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        if (clickHandler != null) clickHandler.accept(event);
    }

    /* ========= Работа с ItemStack ========= */

    /**
     * Базовый ItemStack без персонализации (без плейсхолдеров).
     * То, что лежит в ItemWrapper как есть.
     */
    public ItemStack baseItemStack() {
        if (isMarker()) return ItemMarker.mark(itemWrapper.itemStack());
        return itemWrapper.itemStack();
    }

    /**
     * Рендер предмета под конкретного игрока: прогоняем имя/лор через Placeholder.
     * Возвращает НОВЫЙ ItemStack, не трогает базовый.
     */
    public ItemStack render(@Nullable OfflinePlayer player) {
        itemWrapper.update();

        PlaceholderContextImpl context = new PlaceholderContextImpl(player, metadataPlaceholder);

        if (cachedMeta == null) cachedMeta = itemWrapper.itemStack().getItemMeta();
        if (cachedMeta == null) return baseItemStack();

        Component name = itemWrapper.displayName();
        List<Component> lore = itemWrapper.lore() != null ? new ArrayList<>(itemWrapper.lore()) : null;

        if (placeholderEngine != null) {
            if (name != null) name = placeholderEngine.process(name, context);
            if (lore != null) lore = placeholderEngine.process(lore, context);
        }

        if (name != null) cachedMeta.displayName(name);
        if (lore != null) cachedMeta.lore(lore);

        itemWrapper.itemStack().setItemMeta(cachedMeta);

        return baseItemStack();
    }


    /**
     * Удобный алиас: если player == null или плейсхолдеров нет – вернёт базовый стек,
     * иначе – персонализированный.
     */
    public ItemStack itemStack(@Nullable OfflinePlayer player) {
        if (player == null || placeholderEngine == null) {
            return baseItemStack();
        }
        return render(player);
    }

    /* ========= Сахар поверх ItemWrapper, если хочешь =================== */

    public GuiItem name(@Nullable Component name) {
        this.itemWrapper.displayName(name);
        return this;
    }

    public GuiItem lore(@Nullable List<Component> lore) {
        this.itemWrapper.lore(lore);
        return this;
    }

    /* ========= clone ========= */

    @Override
    public GuiItem clone() {
        try {
            GuiItem clone = (GuiItem) super.clone();
            // клон предмета
            ItemWrapper clonedItem = this.itemWrapper.clone();
            // заменить ссылку на клон
            // (field item final, поэтому небольшой обход)
            return new GuiItem(clonedItem)
                    .placeholderEngine(this.placeholderEngine)
                    .onClick(this.clickHandler)
                    .key(this.key)
                    .setSlots(this.slots)
                    .applyMetadata(this.metadata);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    private GuiItem applyMetadata(Map<String, Object> meta) {
        this.metadata.putAll(meta);
        return this;
    }

}

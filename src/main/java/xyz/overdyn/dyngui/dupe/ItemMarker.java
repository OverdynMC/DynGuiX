package xyz.overdyn.dyngui.dupe;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemMarker {

    private static NamespacedKey KEY;

    private ItemMarker() {}

    public static void init(@NotNull JavaPlugin plugin) {
        KEY = new NamespacedKey(plugin, "marked_item");
    }

    public static @Nullable ItemStack mark(@Nullable ItemStack item) {
        if (item == null || KEY == null) return item;
        item = item.clone();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isMarked(@Nullable ItemStack item) {
        if (item == null || KEY == null) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        var pdc = meta.getPersistentDataContainer();
        return pdc.has(KEY, PersistentDataType.BYTE);
    }

    public static @Nullable ItemStack unmark(@Nullable ItemStack item) {
        if (item == null || KEY == null) return item;
        item = item.clone();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.getPersistentDataContainer().remove(KEY);
        item.setItemMeta(meta);
        return item;
    }
}

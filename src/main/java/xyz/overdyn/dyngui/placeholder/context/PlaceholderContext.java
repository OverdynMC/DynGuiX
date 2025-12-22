package xyz.overdyn.dyngui.placeholder.context;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

public interface PlaceholderContext {

    @Nullable
    OfflinePlayer player();

    @Nullable Object get(String key);

    void set(String key, Object value);
}

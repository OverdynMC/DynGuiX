package xyz.overdyn.dyngui.placeholder.context;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PlaceholderContextImpl implements PlaceholderContext {

    private final OfflinePlayer player;
    private final Map<String, Object> data = new HashMap<>();

    public PlaceholderContextImpl(@Nullable OfflinePlayer player) {
        this.player = player;
    }

    public PlaceholderContextImpl(@Nullable OfflinePlayer player, Map<String, Object> data) {
        this.player = player;
        this.data.putAll(data);
    }

    @Override
    public OfflinePlayer player() {
        return player;
    }

    @Override
    public Object get(String key) {
        return data.get(key);
    }

    @Override
    public void set(String key, Object value) {
        data.put(key, value);
    }
}

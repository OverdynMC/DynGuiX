package xyz.overdyn.dyngui.manager;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import xyz.overdyn.dyngui.abstracts.AbstractGui;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class SessionManager {

    public final Map<Player, AbstractGui> sessions = new HashMap<>();

    public void register(Player player, AbstractGui gui) {
        sessions.put(player, gui);
    }

    public void unregister(Player player) {
        sessions.remove(player);
    }

    public AbstractGui get(Player player) {
        return sessions.get(player);
    }

    public boolean has(Player player) {
        return sessions.containsKey(player);
    }

    public void dispose() {
        var snapshot = new HashMap<>(sessions);
        sessions.clear();
        snapshot.forEach((player, gui) -> gui.close(player));
    }


    public void clear() {
        sessions.clear();
    }

}

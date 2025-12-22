package xyz.overdyn.dyngui.tools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Utility for updating inventory titles using NMS packets.
 * Supports versions 1.16.4+ with proper fallback mechanisms.
 */
public class InventoryTitleUpdater {
    
    private static final ServerVersion SERVER_VERSION = detectServerVersion();
    
    /**
     * Updates the title of a player's currently open inventory.
     *
     * @param player the player whose inventory title to update
     * @param newTitle the new title component
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateTitle(Player player, Component newTitle) {
        return updateTitle(player, newTitle, null);
    }
    
    /**
     * Updates the title of a player's currently open inventory.
     *
     * @param player the player whose inventory title to update
     * @param newTitle the new title component
     * @param plugin plugin instance for logging (optional)
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateTitle(Player player, Component newTitle, JavaPlugin plugin) {
        if (player == null || !player.isOnline() || !SERVER_VERSION.isSupported()) {
            return false;
        }
        
        try {
            Object nmsComponent = convertToNmsComponent(newTitle);
            sendOpenScreenPacket(player, nmsComponent);
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Failed to update inventory title: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Sends the ClientboundOpenScreenPacket to update the inventory title.
     */
    private static void sendOpenScreenPacket(Player player, Object nmsComponent) throws Exception {
        Object serverPlayer = player.getClass().getMethod("getHandle").invoke(player);
        Object containerMenu = serverPlayer.getClass().getField("containerMenu").get(serverPlayer);
        
        int containerId = containerMenu.getClass().getField("containerId").getInt(containerMenu);
        Object menuType = containerMenu.getClass().getMethod("getType").invoke(containerMenu);
        
        Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundOpenScreenPacket");
        Class<?> componentInterface = Class.forName("net.minecraft.network.chat.Component");
        
        Object packet = packetClass
                .getConstructor(int.class, menuType.getClass(), componentInterface)
                .newInstance(containerId, menuType, nmsComponent);
        
        Object connection = serverPlayer.getClass().getField("connection").get(serverPlayer);
        connection.getClass()
                .getMethod("send", Class.forName("net.minecraft.network.protocol.Packet"))
                .invoke(connection, packet);
    }
    
    /**
     * Converts Adventure Component to NMS Component.
     */
    private static Object convertToNmsComponent(Component component) throws Exception {
        try {
            Class<?> paperAdventure = Class.forName("io.papermc.paper.adventure.PaperAdventure");
            return paperAdventure.getMethod("asVanilla", Component.class).invoke(null, component);
        } catch (Exception e) {
            try {
                String nmsVersion = SERVER_VERSION.getNmsVersion();
                Class<?> craftChatMessage = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".util.CraftChatMessage");
                return craftChatMessage.getMethod("fromAdventure", Component.class).invoke(null, component);
            } catch (Exception e2) {
                String plainText = PlainTextComponentSerializer.plainText().serialize(component);
                Class<?> componentClass = Class.forName("net.minecraft.network.chat.Component");
                return componentClass.getMethod("literal", String.class).invoke(null, plainText);
            }
        }
    }
    
    /**
     * Detects the current server version.
     */
    private static ServerVersion detectServerVersion() {
        try {
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            String nmsVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
            
            if (nmsVersion.startsWith("v1_")) {
                ServerVersion version = ServerVersion.fromNmsVersion(nmsVersion);
                if (!version.isError()) {
                    return version;
                }
            }
            
            return ServerVersion.fromBukkitVersion(Bukkit.getBukkitVersion());
        } catch (Exception e) {
            return ServerVersion.ERROR;
        }
    }
    
    /**
     * Checks if inventory title updates are supported on this server version.
     *
     * @return true if supported, false otherwise
     */
    public static boolean isSupported() {
        return SERVER_VERSION.isSupported();
    }
    
    /**
     * Gets the current detected server version.
     *
     * @return the server version
     */
    public static ServerVersion getCurrentVersion() {
        return SERVER_VERSION;
    }
}
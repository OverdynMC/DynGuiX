package xyz.overdyn.dyngui.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.overdyn.dyngui.DynGui;
import xyz.overdyn.dyngui.abstracts.AbstractGui;
import xyz.overdyn.dyngui.dupe.ItemMarker;

public class GuiListener implements Listener {

    @EventHandler
    public void onClick(@NotNull InventoryClickEvent event) {
        dispatch(event);
    }

    @EventHandler
    public void onDrag(@NotNull InventoryDragEvent event) {
        dispatch(event);
    }

    @EventHandler
    public void onOpen(@NotNull InventoryOpenEvent event) {
        dispatch(event);
    }

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent event) {
        dispatch(event);
    }

    private void dispatch(@NotNull InventoryEvent event) {
        var gui = getHolder(event.getInventory());
        if (gui == null) return;

        gui.handleEvent(event);
    }

    @EventHandler
    public void onPickup(@NotNull final EntityPickupItemEvent event) {
        if (!ItemMarker.isMarked(event.getItem().getItemStack())) {
            return;
        }

        event.getItem().remove();
    }

    @EventHandler
    public void onDrop(@NotNull final PlayerDropItemEvent event) {
        if (!ItemMarker.isMarked(event.getItemDrop().getItemStack())) {
            return;
        }

        event.getItemDrop().remove();
    }

    @EventHandler
    public void onLogin(@NotNull final PlayerJoinEvent event) {
        DynGui.getInstance().getPlugin().getServer().getScheduler().runTaskLater(
                DynGui.getInstance().getPlugin(),
                () -> {
                    for (final ItemStack itemStack : event.getPlayer().getInventory().getContents()) {
                        if (itemStack == null) continue;
                        if (!ItemMarker.isMarked(itemStack)) continue;

                        event.getPlayer().getInventory().remove(itemStack);
                    }
                },
                10L
        );
    }

    @Nullable
    private static AbstractGui getHolder(Inventory inventory) {
        if (inventory == null) return null;

        var holder = inventory.getHolder();
        if (holder instanceof AbstractGui gui) {
            return gui;
        }
        return null;
    }
}

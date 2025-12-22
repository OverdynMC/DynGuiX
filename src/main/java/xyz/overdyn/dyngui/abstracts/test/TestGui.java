package xyz.overdyn.dyngui.abstracts.test;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import xyz.overdyn.dyngui.abstracts.AbstractGuiLayer;
import xyz.overdyn.dyngui.items.GuiItem;
import xyz.overdyn.dyngui.items.ItemWrapper;
import xyz.overdyn.dyngui.policy.GuiPolicy;

public class TestGui extends AbstractGuiLayer {

    public TestGui() {
        super(InventoryType.ANVIL, Component.text("dede"), GuiPolicy.Factories.HIGHEST);

        var item = new GuiItem(
            ItemWrapper
                .builder(Material.ITEM_FRAME)
                .amount(3)
                .displayName(Component.text("Test"))
                .build()
        )
        .addSlot(10)
        .setMarker(false);


        onEvent(InventoryClickEvent.class, e -> {

        });

        item.getItemWrapper().displayName(Component.text("Test"));
        item.render(null);
        item.getItemWrapper().update();
    }
}

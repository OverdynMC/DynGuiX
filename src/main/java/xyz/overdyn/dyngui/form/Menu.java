package xyz.overdyn.dyngui.form;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryType;
import xyz.overdyn.dyngui.form.requirements.ViewRequirement;

import java.util.List;

/**
 * Represents a GUI menu configuration.
 *
 * <p>The {@code Menu} record defines all structural and permission-based
 * metadata for constructing a GUI. This includes title, size, type,
 * open conditions, and its associated buttons.</p>
 *
 * <p>This model is typically deserialized from a configuration file
 * (e.g., YAML or JSON) and consumed by the GUI engine at runtime.</p>
 */
public record Menu(
        String id,
        String title,
        FileConfiguration config,
        int size,
        InventoryType inventoryType,
        List<String> openCommands, //actions
        List<ViewRequirement> open_requirement,
        int update_interval,
        int refresh_interval,
        List<String> close_commands,
        List<Button> buttons
) {
}

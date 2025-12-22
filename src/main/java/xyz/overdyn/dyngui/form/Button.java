package xyz.overdyn.dyngui.form;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import xyz.overdyn.dyngui.form.requirements.ViewRequirement;

import java.util.List;
import java.util.Map;

/**
 * Represents a configurable button in a GUI form.
 *
 * <p>A button may contain visual configuration (slot, display name, lore, etc.),
 * visibility requirements, commands to execute on click, and custom metadata.</p>
 *
 * <p>Supports dynamic conditions via {@link ViewRequirement} and
 * complex interaction behavior via {@link ButtonCommand}.</p>
 */
public record Button(

        String id,
        ConfigurationSection itemSection,
        int slot,
        String displayName,
        List<String> lore,
        int priority,
        int amount,
        int customModelData,
        List<ItemFlag> itemFlags,
        ItemStack itemStack,
        List<ViewRequirement> viewRequirements,
        List<ButtonCommand> buttonCommands,
        boolean update,
        boolean placeholder,
        Map<String, Object> customValues

) {
}

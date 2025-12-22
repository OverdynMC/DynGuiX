package xyz.overdyn.dyngui.form;

import org.bukkit.event.inventory.ClickType;
import xyz.overdyn.dyngui.form.requirements.ClickRequirement;

import java.util.List;

/**
 * Represents a clickable action configuration for a GUI button.
 *
 * <p>A {@code ButtonCommand} defines how a button should react to specific click types,
 * what actions it should perform, and what requirements must be satisfied before executing.</p>
 *
 * <p>Multiple {@code ButtonCommand}s can be assigned to a {@link Button}, allowing fine-grained control
 * over interaction types (e.g., left-click, right-click, shift-click) and conditional logic.</p>
 */
public record ButtonCommand(
        boolean anyClick,
        ClickType clickType,
        List<String> actions,
        List<ClickRequirement> clickRequirements
) {
}

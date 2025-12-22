package xyz.overdyn.dyngui.form.requirements;

import org.bukkit.event.inventory.ClickType;

import java.util.List;

/**
 * A requirement that validates a GUI click action based on the {@link ClickType}
 * and other optional constraints like permissions or expected input/output values.
 *
 * <p>Used to restrict access to specific GUI actions depending on how a player clicks
 * (e.g., left/right/middle click), what they input or output, or what permissions they have.</p>
 *
 * @param anyClick       whether to allow any click type (overrides {@code clickType} if true)
 * @param clickType      specific {@link ClickType} to match against if {@code anyClick} is false
 * @param type           logical type of requirement (custom identifier, e.g., "permission", "match", etc.)
 * @param input          expected input string (used in type-specific logic)
 * @param output         expected output string (used in type-specific logic)
 * @param permission     required permission to pass the requirement
 * @param deny_commands  list of commands to execute if the requirement is not met
 */
public record ClickRequirement(
        boolean anyClick,
        ClickType clickType,
        String type,
        String input,
        String output,
        String permission,
        List<String> success,
        List<String> deny_commands
) implements Requirement { }

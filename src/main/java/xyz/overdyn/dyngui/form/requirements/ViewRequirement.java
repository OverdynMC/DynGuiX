package xyz.overdyn.dyngui.form.requirements;

import java.util.List;

/**
 * A requirement that determines whether a GUI element can be rendered (visible) to the player.
 *
 * <p>Used primarily to conditionally hide/show items in GUIs depending on player state,
 * permissions, placeholders, or inventory conditions.</p>
 *
 * <p>Supported requirement types:</p>
 * <ul>
 *     <li><b>"has permission"</b> — player has the given permission.</li>
 *     <li><b>"!has permission"</b> — player <i>does not</i> have the given permission.</li>
 *     <li><b>"string equals"</b> — {@code input.equalsIgnoreCase(output)}</li>
 *     <li><b>"!string equals"</b> — {@code !input.equalsIgnoreCase(output)}</li>
 *     <li><b>"math"</b> / <b>"javascript"</b> — evaluates numeric/logical expression</li>
 * </ul>
 */
public record ViewRequirement(
        String type,
        String input,
        String output,
        String permission,
        List<String> success,
        List<String> deny_commands
) implements Requirement { }

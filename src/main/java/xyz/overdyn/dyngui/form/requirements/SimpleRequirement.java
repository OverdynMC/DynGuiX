package xyz.overdyn.dyngui.form.requirements;

import java.util.List;

/**
 * A basic implementation of the {@link Requirement} interface
 * that supports permission, string comparison, and action-based checks.
 *
 * <p>This record is used for GUI elements that conditionally appear,
 * become clickable, or trigger logic based on the defined conditions.</p>
 *
 * <p>Supported requirement types:</p>
 * <ul>
 *     <li><b>"has permission"</b> — player has the specified permission.</li>
 *     <li><b>"!has permission"</b> — player <i>does not</i> have the permission.</li>
 *     <li><b>"string equals"</b> — {@code input.equalsIgnoreCase(output)} (after placeholders).</li>
 *     <li><b>"!string equals"</b> — {@code !input.equalsIgnoreCase(output)}.</li>
 *     <li><b>"math"</b> / <b>"javascript"</b> — evaluates comparison: {@code %value% >= 10}, etc.</li>
 * </ul>
 *
 * <p>Actions allow controlling GUI logic:</p>
 * <ul>
 *     <li>{@code actions} — triggered if requirement passes</li>
 *     <li>{@code denyActions} — triggered if requirement fails</li>
 * </ul>
 */
public record SimpleRequirement(
        String type,
        String input,
        String output,
        String permission,
        List<String> success,
        List<String> deny_commands
) implements Requirement { }

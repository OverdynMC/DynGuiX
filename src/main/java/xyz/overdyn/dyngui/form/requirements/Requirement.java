package xyz.overdyn.dyngui.form.requirements;

/**
 * Base interface for defining GUI interaction requirements.
 *
 * <p>
 * A requirement represents a logical condition that must be met
 * before a GUI element (such as a button or form field) becomes active
 * or executable by the player.
 * </p>
 *
 * <p>Supported requirement types include:</p>
 * <ul>
 *     <li><b>"has permission"</b> – player must have a specific permission node.</li>
 *     <li><b>"!has permission"</b> – player must NOT have the specified permission node.</li>
 *     <li><b>"string equals"</b> – case-insensitive comparison of input and output.</li>
 *     <li><b>"!string equals"</b> – inverse of the above: input must not equal output.</li>
 *     <li><b>"javascript"</b> / <b>"math"</b> – mathematical or logical expression (e.g. {@code %level% >= 10}).</li>
 * </ul>
 *
 * <p>Placeholders (e.g. from PlaceholderAPI) are supported in {@code input} and {@code output}.</p>
 *
 * <h3>Examples:</h3>
 * <pre>
 * type: "has permission"
 * permission: "treexclans.admin"
 *
 * type: "string equals"
 * input: "%player_name%"
 * output: "JetBy"
 *
 * type: "math"
 * input: "%player_level% >= 10"
 * </pre>
 */
public interface Requirement {

    /**
     * Returns the type of this requirement.
     *
     * <p>Allowed values:</p>
     * <ul>
     *     <li><b>"has permission"</b></li>
     *     <li><b>"!has permission"</b></li>
     *     <li><b>"string equals"</b></li>
     *     <li><b>"!string equals"</b></li>
     *     <li><b>"javascript"</b> or <b>"math"</b></li>
     * </ul>
     *
     * @return the requirement type.
     */
    String type();

    /**
     * The permission string required or forbidden for this requirement.
     *
     * <p>Used only when {@link #type()} is:</p>
     * <ul>
     *     <li>"has permission"</li>
     *     <li>"!has permission"</li>
     * </ul>
     *
     * @return the permission node, or {@code null} if not applicable.
     */
    String permission();

    /**
     * The dynamic input value to compare or evaluate.
     *
     * <p>Supported with:</p>
     * <ul>
     *     <li>"string equals"</li>
     *     <li>"!string equals"</li>
     *     <li>"math"</li>
     *     <li>"javascript"</li>
     * </ul>
     *
     * <p>Placeholders can be used (e.g. {@code %player_name%}).</p>
     *
     * @return the input string to be resolved and used in validation.
     */
    String input();

    /**
     * The expected value used for comparison with {@link #input()}.
     *
     * <p>Used only when {@link #type()} is:</p>
     * <ul>
     *     <li>"string equals"</li>
     *     <li>"!string equals"</li>
     * </ul>
     *
     * <p>Placeholders can be used (e.g. {@code %target_name%}).</p>
     *
     * @return the target value for comparison.
     */
    String output();
}

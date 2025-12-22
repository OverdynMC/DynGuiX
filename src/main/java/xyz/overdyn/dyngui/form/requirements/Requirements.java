package xyz.overdyn.dyngui.form.requirements;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

/**
 * Utility class for evaluating GUI {@link Requirement}s.
 *
 * <p>This class handles checking permissions, string comparisons,
 * and mathematical or logical expressions using placeholder-aware input.</p>
 *
 * <p>Supported requirement types:</p>
 * <ul>
 *     <li><b>"has permission"</b></li>
 *     <li><b>"!has permission"</b></li>
 *     <li><b>"string equals"</b></li>
 *     <li><b>"!string equals"</b></li>
 *     <li><b>"math"</b> / <b>"javascript"</b></li>
 * </ul>
 */
@UtilityClass
public class Requirements {

    /**
     * Evaluates a {@link Requirement} for a specific player.
     *
     * @param player The player to check
     * @param req    The requirement instance to evaluate
     * @return {@code true} if the requirement is met, otherwise {@code false}
     */
    public boolean check(Player player, Requirement req) {
        return checkInternal(player, req.type(), req.permission(), req.input(), req.output());
    }

    /**
     * Internal requirement handler.
     *
     * <p>This method performs the actual parsing and comparison logic.
     * All string inputs are processed with PlaceholderAPI (via {@link PlaceholderAPI} wrapper).</p>
     *
     * @param player     The target player
     * @param type       The requirement type (e.g. "has permission", "math")
     * @param permission The permission node, if applicable
     * @param input      The input string (may contain placeholders)
     * @param output     The comparison target string (may contain placeholders)
     * @return {@code true} if the requirement is satisfied
     */
    public boolean checkInternal(Player player,
                                  String type,
                                  String permission,
                                  String input,
                                  String output) {

        if (!type.equalsIgnoreCase("permission") && input==null) return true;
        String parsedInput = PlaceholderAPI.setPlaceholders(player, input);

        String parsedOutput = "";
        if (output!=null) {
            parsedOutput = PlaceholderAPI.setPlaceholders(player, output);
        }
        return switch (type.toLowerCase()) {
            case "has permission" -> player.hasPermission(permission);
            case "!has permission" -> !player.hasPermission(permission);
            case "string equals" -> parsedInput.equalsIgnoreCase(parsedOutput);
            case "!string equals" -> !parsedInput.equalsIgnoreCase(parsedOutput);
            case "javascript", "math" -> evalJavascriptLike(player, parsedInput);
            default -> false;
        };
    }

    /**
     * Evaluates a simplified JavaScript-like or math expression.
     *
     * <p>The input must be a 3-part space-separated expression, e.g. {@code "5 >= 3"} or {@code "%level% < 10"}.</p>
     *
     * <p>All parts are resolved through PlaceholderAPI prior to parsing.
     * If operands are numeric, numerical comparison is applied; otherwise, string comparison is used.</p>
     *
     * @param player The player for placeholder resolution
     * @param input  The expression string
     * @return {@code true} if the expression evaluates to true
     */
    private boolean evalJavascriptLike(Player player, String input) {
        String[] args = input.split(" ");
        if (args.length < 3) return false;

        args[0] = PlaceholderAPI.setPlaceholders(player, args[0]);
        args[2] = PlaceholderAPI.setPlaceholders(player, args[2]);

        try {
            double x = Double.parseDouble(args[0]);
            double x1 = Double.parseDouble(args[2]);
            return switch (args[1]) {
                case ">" -> x > x1;
                case ">=" -> x >= x1;
                case "==" -> x == x1;
                case "!=" -> x != x1;
                case "<=" -> x <= x1;
                case "<" -> x < x1;
                default -> false;
            };
        } catch (NumberFormatException e) {
            return switch (args[1]) {
                case "==" -> args[0].equals(args[2]);
                case "!=" -> !args[0].equals(args[2]);
                default -> false;
            };
        }
    }
}

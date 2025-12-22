package xyz.overdyn.dyngui.placeholder;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import xyz.overdyn.dyngui.placeholder.context.PlaceholderContext;

import javax.annotation.RegEx;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Placeholder engine interface for dynamic text replacement within {@link Component} structures.
 *
 * <p>This API now uses {@link PlaceholderContext} instead of raw player access, allowing
 * resolvers to read not only player reference but also attached metadata.</p>
 *
 * <p>Useful for GUI systems, MiniMessage integration, lore/title formatting, etc.</p>
 */
@SuppressWarnings({"unused"})
public interface Placeholder {

    /**
     * Registers a placeholder with a dynamic value resolver from context.
     *
     * @param placeholder key literal, e.g., {@code "player_name"}
     * @param resolver    function producing resolved value based on context
     */
    void register(@NotNull String placeholder,
                  @NotNull Function<@NotNull PlaceholderContext, @NotNull String> resolver);

    /**
     * Registers a static placeholder that always returns the same value.
     *
     * @param placeholder key literal
     * @param resolver    resolved text value
     */
    void register(@NotNull String placeholder, @NotNull String resolver);

    /**
     * Registers a placeholder resolver from generic object.
     *
     * <p>If object is {@link String}, resolver is static.
     * If object is {@link Function}, resolver is context-driven.</p>
     *
     * @param placeholder key literal
     * @param resolver    resolver object
     */
    void register(@NotNull String placeholder, @NotNull Object resolver);

    /**
     * Registers a regex-based placeholder using raw text pattern.
     *
     * @param pattern  regex pattern literal
     * @param resolver function receiving matched segment + context
     */
    void registerRegex(@NotNull @RegEx String pattern,
                       @NotNull BiFunction<@NotNull String, @NotNull PlaceholderContext, @NotNull String> resolver);

    /**
     * Registers a regex-based placeholder using compiled pattern.
     *
     * @param pattern  compiled regex
     * @param resolver function receiving matched segment + context
     */
    void registerRegex(@NotNull Pattern pattern,
                       @NotNull BiFunction<@NotNull String, @NotNull PlaceholderContext, @NotNull String> resolver);

    /**
     * Imports all registered placeholders from another {@link Placeholder} engine.
     *
     * @param placeholderEngine another engine instance
     */
    void addAll(@NotNull Placeholder placeholderEngine);

    @NotNull
    String processString(@NotNull String input, @NotNull PlaceholderContext context);

    default List<String> processStrings(@NotNull List<String> input,
                                 @NotNull PlaceholderContext context) {
        return input.stream()
                .map(component -> processString(component, context))
                .collect(Collectors.toList());
    }



    /**
     * Applies registered placeholders to a list of Adventure components.
     *
     * @param input   component list
     * @param context resolution context with player + metadata
     * @return processed component list
     */
    @NotNull
    List<Component> process(@NotNull List<Component> input, @NotNull PlaceholderContext context);

    /**
     * Applies registered placeholders to a single component.
     *
     * @param input   component
     * @param context resolution context
     * @return processed component
     */
    @NotNull
    Component process(@NotNull Component input, @NotNull PlaceholderContext context);

    /**
     * Factory method for obtaining a new placeholder engine.
     *
     * @return new engine implementation
     */
    @NotNull
    static Placeholder of() {
        return new PlaceholderImpl();
    }
}

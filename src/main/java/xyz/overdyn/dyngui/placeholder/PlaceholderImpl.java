package xyz.overdyn.dyngui.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.overdyn.dyngui.DynGui;
import xyz.overdyn.dyngui.placeholder.context.PlaceholderContext;

import javax.annotation.RegEx;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings({"unused"})
public class PlaceholderImpl implements Placeholder {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");

    /** simple %key% replacements */
    private final Map<Pattern, @NotNull Function<PlaceholderContext, String>> literalPlaceholders = new LinkedHashMap<>();

    /** regex replacements */
    private final Map<Pattern, @NotNull BiFunction<String, PlaceholderContext, String>> regexPlaceholders = new LinkedHashMap<>();

    @Override
    public void register(@NotNull String placeholder,
                         @NotNull Function<PlaceholderContext, String> resolver) {
        literalPlaceholders.put(Pattern.compile(placeholder, Pattern.LITERAL), resolver);
    }

    @Override
    public void register(@NotNull String placeholder, @NotNull String resolver) {
        register(placeholder, ctx -> resolver);
    }

    @Override
    public void register(@NotNull String placeholder, @NotNull Object resolver) {
        register(placeholder, String.valueOf(resolver));
    }

    @Override
    public void registerRegex(@NotNull @RegEx String pattern,
                              @NotNull BiFunction<String, PlaceholderContext, String> resolver) {
        registerRegex(Pattern.compile(pattern), resolver);
    }

    @Override
    public void registerRegex(@NotNull Pattern pattern,
                              @NotNull BiFunction<String, PlaceholderContext, String> resolver) {
        regexPlaceholders.put(pattern, resolver);
    }

    @Override
    public void addAll(@NotNull Placeholder placeholderEngine) {
        PlaceholderImpl engine = (PlaceholderImpl) placeholderEngine;

        this.regexPlaceholders.putAll(engine.regexPlaceholders);
        this.literalPlaceholders.putAll(engine.literalPlaceholders);
    }

    @Override
    public @NotNull String processString(@NotNull String input,
                                   @NotNull PlaceholderContext context) {

        if (input == null || input.isEmpty()) {
            return input;
        }

        String current = input;

        // literal placeholders
        for (Map.Entry<Pattern, Function<PlaceholderContext, String>> entry : literalPlaceholders.entrySet()) {
            current = entry.getKey()
                    .matcher(current)
                    .replaceAll(entry.getValue().apply(context));
        }

        // regex placeholders
        for (Map.Entry<Pattern, BiFunction<String, PlaceholderContext, String>> entry : regexPlaceholders.entrySet()) {
            Pattern pattern = entry.getKey();
            BiFunction<String, PlaceholderContext, String> resolver = entry.getValue();

            current = pattern.matcher(current).replaceAll(match ->
                    resolver.apply(match.group(), context)
            );
        }

        // PlaceholderAPI (если есть игрок)
        Object p = context.player();
        if (p instanceof OfflinePlayer player && DynGui.getInstance().isSupportedPlaceholder()) {
            current = PlaceholderAPI.setPlaceholders(player, current);
        }

        return current;
    }


    @Override
    public @NotNull List<Component> process(@NotNull List<Component> input,
                                            @NotNull PlaceholderContext context) {
        return input.stream()
                .map(component -> process(component, context))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull Component process(@NotNull Component input,
                                      @NotNull PlaceholderContext context) {
        Component current = input;

        for (Map.Entry<Pattern, Function<PlaceholderContext, String>> entry : literalPlaceholders.entrySet()) {
            Pattern pattern = entry.getKey();
            Function<PlaceholderContext, String> resolver = entry.getValue();

            current = current.replaceText(builder -> builder
                    .match(pattern)
                    .replacement(resolver.apply(context))
            );
        }

        for (Map.Entry<Pattern, BiFunction<String, PlaceholderContext, String>> entry : regexPlaceholders.entrySet()) {
            Pattern pattern = entry.getKey();
            BiFunction<String, PlaceholderContext, String> resolver = entry.getValue();

            current = current.replaceText(TextReplacementConfig.builder()
                    .match(pattern)
                    .replacement(match -> {
                        match.content(resolver.apply(match.content(), context));
                        return match;
                    })
                    .build());
        }

        current = applyPapi(current, context);

        return current;
    }

    private Component applyPapi(@NotNull Component component,
                                @NotNull PlaceholderContext context) {
        Object p = context.player();
        if (!(p instanceof OfflinePlayer player)) return component;

        if (!xyz.overdyn.dyngui.DynGui.getInstance().isSupportedPlaceholder())
            return component;

        return component.replaceText(TextReplacementConfig.builder()
                .match(PLACEHOLDER_PATTERN)
                .replacement(match -> {
                    String placeholder = match.content();
                    String parsed = PlaceholderAPI.setPlaceholders(player, placeholder);

                    match.content(parsed);
                    return match;
                })
                .build());
    }
}
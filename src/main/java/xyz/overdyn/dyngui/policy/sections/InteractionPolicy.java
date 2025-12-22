package xyz.overdyn.dyngui.policy.sections;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Defines the contract for interaction behavior policies within the Treex GUI framework.
 * Implementations provide configurable options for handling GUI lifecycle events,
 * such as closing rules and inventory synchronization.
 *
 * <p>This interface ensures API consumers can interact with policies in a type-safe,
 * extensible manner without tight coupling to concrete implementations.</p>
 */
public interface InteractionPolicy {

    /**
     * Checks if the specified interaction type is enabled in this policy.
     *
     * @param type the interaction type to query
     * @return true if enabled, false otherwise
     */
    boolean isEnabled(InteractionType type);

    /**
     * Returns an immutable map of all interaction types and their enabled states.
     *
     * @return a map of interaction types to boolean values
     */
    Map<InteractionType, Boolean> getValues();

    /**
     * Creates a mutable builder for modifying or extending this policy.
     *
     * @return a new builder pre-populated with this policy's values
     */
    Builder asBuilder();

    /**
     * The interaction types supported by this policy.
     */
    enum InteractionType {
        /**
         * Forces the GUI to close automatically when the plugin is disabled.
         * <p>
         * This prevents players from remaining inside a menu that no longer
         * functions after reloads or shutdowns. Highly recommended for menus
         * that register listeners or depend on plugin logic dynamically.
         * </p>
         */
        CLOSE_ON_DISABLE,

        /**
         * Refreshes the player's inventory after they close the GUI normally.
         * <p>
         * Useful for preventing visual desynchronization, where the inventory
         * might still show ghost items or outdated states after interacting
         * with a GUI. This is a player-focused consistency improvement.
         * </p>
         */
        UPDATE_AFTER_CLOSE,

        /**
         * Refreshes the player's inventory if the GUI is closed because the
         * plugin was disabled or forcibly shut down.
         * <p>
         * This ensures that the player's inventory remains consistent even
         * during plugin reloads, preventing incorrect or outdated visual states.
         * </p>
         */
        UPDATE_AFTER_DISABLE
    }

    /**
     * Predefined presets for common interaction scenarios, providing
     * quick-start configurations for different levels of safety and consistency.
     */
    enum InteractionPreset {
        /**
         * The lightest level of interaction handling.
         * <p>
         * Performs no forced closing or inventory refreshing.
         * Ideal for decorative, static, or informational GUIs
         * where no synchronization is required.
         * </p>
         */
        LOWEST,

        /**
         * Low-level interaction control.
         * <p>
         * Applies minimal safety measures. Refreshes the player's
         * inventory after a normal close, reducing visual desync
         * without enforcing strict behavior.
         * </p>
         */
        LOW,

        /**
         * Balanced mid-level handling.
         * <p>
         * Ensures stable behavior without excessive control.
         * Recommended for standard GUIs that require predictable
         * inventory updates during normal usage.
         * </p>
         */
        MEDIUM,

        /**
         * High-level interaction safety.
         * <p>
         * Ensures the GUI closes on plugin disable and refreshes
         * the inventory after normal closes. Appropriate for menus
         * that impact gameplay state or modify player items.
         * </p>
         */
        HIGH,

        /**
         * Maximum interaction safety and consistency.
         * <p>
         * Forces GUI closing on plugin shutdown and refreshes the
         * player's inventory after both normal and forced closure.
         * Designed for critical, data-sensitive GUIs.
         * </p>
         */
        HIGHEST
    }

    /**
     * A fluent builder interface for constructing {@link InteractionPolicy} instances.
     * Supports enabling/disabling specific types and preset-based initialization.
     */
    interface Builder {
        /**
         * Enables the specified interaction type.
         *
         * @param type the type to enable
         * @return this builder for chaining
         */
        Builder enable(InteractionType type);

        /**
         * Disables the specified interaction type.
         *
         * @param type the type to disable
         * @return this builder for chaining
         */
        Builder disable(InteractionType type);

        /**
         * Initializes the builder with values from a preset.
         *
         * @param preset the preset to apply
         * @return this builder for chaining
         */
        default Builder fromPreset(InteractionPreset preset) {
            return switch (preset) {
                case LOWEST -> this;
                case LOW, MEDIUM -> this.enable(InteractionType.UPDATE_AFTER_CLOSE);
                case HIGH -> this
                        .enable(InteractionType.CLOSE_ON_DISABLE)
                        .enable(InteractionType.UPDATE_AFTER_CLOSE);
                case HIGHEST -> this
                        .enable(InteractionType.CLOSE_ON_DISABLE)
                        .enable(InteractionType.UPDATE_AFTER_CLOSE)
                        .enable(InteractionType.UPDATE_AFTER_DISABLE);
            };
        }

        /**
         * Applies a consumer to all interaction types, allowing bulk configuration.
         *
         * @param configurator a function that takes each type and a setter for its state
         * @return this builder for chaining
         */
        default Builder configureAll(BiConsumer<InteractionType, Consumer<Boolean>> configurator) {
            Arrays.stream(InteractionType.values()).forEach(type -> configurator.accept(type, enabled -> {
                if (enabled) enable(type);
                else disable(type);
            }));
            return this;
        }

        /**
         * Builds an immutable {@link InteractionPolicy} from the current configuration.
         *
         * @return a new policy instance
         */
        InteractionPolicy build();
    }

    /**
     * Factory methods for common presets and custom builders.
     */
    final class Factories {
        private Factories() {} // Non-instantiable

        /**
         * Creates a new empty builder.
         *
         * @return a fresh builder instance
         */
        public static Builder builder() {
            return new Default.DefaultBuilder();
        }

        /**
         * Creates a policy from the given preset.
         *
         * @param preset the preset to use
         * @return a new policy instance
         */
        public static InteractionPolicy fromPreset(InteractionPreset preset) {
            return builder().fromPreset(preset).build();
        }

        public static InteractionPolicy lowest() {
            return fromPreset(InteractionPreset.LOWEST);
        }

        public static InteractionPolicy low() {
            return fromPreset(InteractionPreset.LOW);
        }

        public static InteractionPolicy medium() {
            return fromPreset(InteractionPreset.MEDIUM);
        }

        public static InteractionPolicy high() {
            return fromPreset(InteractionPreset.HIGH);
        }

        public static InteractionPolicy highest() {
            return fromPreset(InteractionPreset.HIGHEST);
        }
    }

    /**
         * Default implementation of {@link InteractionPolicy}.
         * Uses an {@link EnumMap} for efficient, type-safe storage.
         */
    record Default(EnumMap<InteractionType, Boolean> values) implements InteractionPolicy {
            public Default(EnumMap<InteractionType, Boolean> values) {
                this.values = new EnumMap<>(values);
            }

            @Override
            public boolean isEnabled(InteractionType type) {
                return values.getOrDefault(type, false);
            }

            @Override
            public Map<InteractionType, Boolean> getValues() {
                return Map.copyOf(values);
            }

            @Override
            public Builder asBuilder() {
                Builder b = Factories.builder();
                values.forEach((type, enabled) -> {
                    if (enabled) b.enable(type);
                    else b.disable(type);
                });
                return b;
            }

            /**
             * Default builder implementation for {@link Default}.
             */
            static final class DefaultBuilder implements Builder {
                private final EnumMap<InteractionType, Boolean> values = new EnumMap<>(InteractionType.class);

                @Override
                public Builder enable(InteractionType type) {
                    values.put(type, true);
                    return this;
                }

                @Override
                public Builder disable(InteractionType type) {
                    values.put(type, false);
                    return this;
                }

                @Override
                public InteractionPolicy build() {
                    return new Default(values);
                }
            }
        }
}
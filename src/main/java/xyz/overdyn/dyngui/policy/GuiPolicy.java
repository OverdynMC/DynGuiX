package xyz.overdyn.dyngui.policy;

import xyz.overdyn.dyngui.policy.sections.InteractionPolicy;

/**
 * Defines the contract for GUI behavioral policies in the Treex GUI framework.
 * A policy orchestrates multiple independent sections (e.g., interaction, animation)
 * to define comprehensive GUI behavior.
 *
 * <p>This interface promotes loose coupling, allowing API consumers to work with
 * policies abstractly while enabling easy extension through composition.</p>
 *
 * <p>Currently, policies include only interaction handling, but the design supports
 * future sections via builder extensibility.</p>
 */
public interface GuiPolicy {

    /**
     * Retrieves the interaction policy section.
     *
     * @return the interaction policy
     */
    InteractionPolicy interaction();

    static GuiPolicy fromPreset(GuiPreset preset) {
        return Factories.fromPreset(preset);
    }

    /**
     * Creates a mutable builder for modifying or extending this policy.
     *
     * @return a new builder pre-populated with this policy's sections
     */
    Builder asBuilder();

    /**
     * Predefined presets for common GUI behavior profiles, balancing
     * safety, performance, and consistency.
     */
    enum GuiPreset {
        /**
         * The minimal behavior profile.
         * <p>
         * Applies no automatic synchronization, no closing rules,
         * and no forced refresh logic. Intended for lightweight or
         * decorative GUIs where full control is unnecessary.
         * </p>
         */
        LOWEST,

        /**
         * Light behavioral guarantees.
         * <p>
         * Updates the player’s inventory after normal close events,
         * providing mild consistency without enforcing strict control.
         * Suitable for frequently opened menus that must remain responsive.
         * </p>
         */
        LOW,

        /**
         * Balanced and stable behavior profile.
         * <p>
         * Ensures predictable interaction handling and inventory safety
         * without heavy restrictions. Recommended for the majority of GUIs.
         * </p>
         */
        MEDIUM,

        /**
         * High-consistency profile.
         * <p>
         * Forces the GUI to close properly during plugin shutdown and
         * refreshes player inventory after normal use. Suitable for menus
         * modifying gameplay data.
         * </p>
         */
        HIGH,

        /**
         * Maximum safety and synchronization.
         * <p>
         * Guarantees full refresh behavior after both normal and forced closes.
         * Best suited for critical or data-sensitive GUIs where consistency is
         * required under all circumstances.
         * </p>
         */
        HIGHEST
    }

    /**
     * A fluent builder interface for constructing {@link GuiPolicy} instances.
     * Supports section-specific configuration and preset-based initialization.
     */
    interface Builder {
        /**
         * Configures the interaction policy section.
         *
         * @param interaction the interaction policy to set
         * @return this builder for chaining
         */
        Builder interaction(InteractionPolicy interaction);

        /**
         * Initializes the builder with values from a preset.
         *
         * @param preset the preset to apply
         * @return this builder for chaining
         */
        default Builder fromPreset(GuiPreset preset) {
            return switch (preset) {
                case LOWEST -> interaction(InteractionPolicy.Factories.lowest());
                case LOW -> interaction(InteractionPolicy.Factories.low());
                case MEDIUM -> interaction(InteractionPolicy.Factories.medium());
                case HIGH -> interaction(InteractionPolicy.Factories.high());
                case HIGHEST -> interaction(InteractionPolicy.Factories.highest());
            };
        }

        /**
         * Builds an immutable {@link GuiPolicy} from the current configuration.
         *
         * @return a new policy instance
         */
        GuiPolicy build();
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
        public static GuiPolicy fromPreset(GuiPreset preset) {
            return builder().fromPreset(preset).build();
        }

        // Convenience methods for presets
        public static final GuiPolicy LOWEST = fromPreset(GuiPreset.LOWEST);
        public static final GuiPolicy LOW = fromPreset(GuiPreset.LOW);
        public static final GuiPolicy MEDIUM = fromPreset(GuiPreset.MEDIUM);
        public static final GuiPolicy HIGH = fromPreset(GuiPreset.HIGH);
        public static final GuiPolicy HIGHEST = fromPreset(GuiPreset.HIGHEST);
    }

    /**
         * Default implementation of {@link GuiPolicy}.
         * Composes sections immutably for thread-safety and performance.
         */
    record Default(InteractionPolicy interaction) implements GuiPolicy {
            public Default(InteractionPolicy interaction) {
                this.interaction = (interaction != null) ? interaction : InteractionPolicy.Factories.lowest();
            }

            @Override
            public Builder asBuilder() {
                return Factories.builder().interaction(interaction);
            }

            /**
             * Default builder implementation for {@link Default}.
             */
            static final class DefaultBuilder implements Builder {
                private InteractionPolicy interaction;

                @Override
                public Builder interaction(InteractionPolicy interaction) {
                    this.interaction = interaction;
                    return this;
                }

                @Override
                public GuiPolicy build() {
                    return new Default(interaction);
                }
            }
        }
}
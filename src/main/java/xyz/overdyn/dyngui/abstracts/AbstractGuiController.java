package xyz.overdyn.dyngui.abstracts;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.*;
import org.jetbrains.annotations.NotNull;
import xyz.overdyn.dyngui.policy.GuiPolicy;

import java.util.*;
import java.util.function.Consumer;

/**
 * Controller abstraction for click-based GUI menus.
 *
 * <p>
 * Extends {@link AbstractGui} and provides a flexible framework for:
 * </p>
 * <ul>
 *     <li>Per-slot click handlers for fine-grained control over interactions</li>
 *     <li>Global event listeners (click, drag, open, close)</li>
 *     <li>Automatic event registration and dispatching</li>
 * </ul>
 *
 * <p>
 * Designed for GUIs with static or semi-static layouts where slots have
 * predefined behavior, but can also handle dynamic interactions.
 * </p>
 *
 * <p>
 * This class internally manages a mapping of event types to handlers and
 * slot-specific click consumers. Subclasses should primarily focus on
 * setting up handlers and GUI behavior without manually handling low-level
 * event registration.
 * </p>
 */
@Getter
public abstract class AbstractGuiController extends AbstractGui {

    /**
     * Custom event handlers for this GUI instance.
     *
     * <p>
     * Maps an event class to a list of consumer callbacks.
     * Used internally to dispatch events to registered listeners.
     * </p>
     */
    private final Map<Class<? extends Event>, List<Consumer<? extends Event>>> customHandlers = new HashMap<>();

    /**
     * Slot-specific click handlers.
     *
     * <p>
     * The key is the raw slot index (including both top and player inventory slots),
     * and the value is a {@link Consumer} of {@link InventoryClickEvent} executed
     * when that slot is clicked.
     * </p>
     */
    private final Map<Integer, Consumer<InventoryClickEvent>> clickHandlersBySlot = new HashMap<>();

    {
        onClick();
        onClose(event -> handleClose((Player) event.getPlayer()));
    }

    /**
     * Constructs a controller GUI with a default chest size (54) and a specified policy.
     *
     * @param title  GUI title component
     * @param policy interaction policy for this GUI
     */
    public AbstractGuiController(@NotNull Component title, GuiPolicy policy) {
        super(title, policy);
    }

    /**
     * Constructs a controller GUI using a specific inventory type.
     *
     * @param type   inventory type (e.g., CHEST, HOPPER)
     * @param title  GUI title component
     * @param policy interaction policy for this GUI
     */
    public AbstractGuiController(InventoryType type, @NotNull Component title, GuiPolicy policy) {
        super(type, title, policy);
    }

    /**
     * Constructs a controller GUI with a specified inventory size.
     *
     * @param size   inventory size (must be multiple of 9)
     * @param title  GUI title component
     * @param policy interaction policy for this GUI
     */
    public AbstractGuiController(int size, @NotNull Component title, GuiPolicy policy) {
        super(size, title, policy);
    }

    /**
     * Dispatches an event to all registered handlers for its class.
     *
     * <p>
     * This method is final and should not be overridden. For custom behavior,
     * register handlers via {@link #onEvent(Class, Consumer)}.
     * </p>
     *
     * @param event the Bukkit event to handle
     */
    @Override @SuppressWarnings("unchecked")
    public final void handleEvent(@NotNull Event event) {
        List<Consumer<? extends Event>> handlers = customHandlers.get(event.getClass());
        if (handlers != null) {
            for (var handler : handlers) {
                ((Consumer<Event>) handler).accept(event);
            }
        }
    }

    /**
     * Registers a custom event handler for this GUI instance.
     *
     * <p>
     * Allows subclasses or external modules to register arbitrary Bukkit events
     * with a consumer callback.
     * </p>
     *
     * @param eventClass event type to listen for
     * @param handler    consumer callback invoked when the event occurs
     * @param <T>        event type
     */
    public <T extends Event> void onEvent(Class<T> eventClass, Consumer<T> handler) {
        customHandlers.computeIfAbsent(eventClass, it -> new ArrayList<>()).add(handler);
    }

    /**
     * Initializes the default click dispatcher.
     *
     * <p>
     * This internal method routes {@link InventoryClickEvent}s to their
     * slot-specific handlers. Can be overridden if more complex click logic
     * is required, but generally left as is.
     * </p>
     */
    public final void onClick() {
        onEvent(InventoryClickEvent.class, event -> {
            final int slot = event.getRawSlot();
            final var slotHandler = clickHandlersBySlot.get(slot);
            if (slotHandler != null) {
                slotHandler.accept(event);
            }
        });
    }

    /**
     * Registers a click handler for a specific slot.
     *
     * @param rawSlot raw slot index
     * @param handler click consumer executed on click
     */
    public void setSlotHandler(int rawSlot, @NotNull Consumer<InventoryClickEvent> handler) {
        clickHandlersBySlot.put(rawSlot, handler);
    }

    /**
     * Registers the same click handler for multiple slots.
     *
     * @param rawSlots collection of slot indices
     * @param handler  click consumer executed for each slot
     */
    public void setSlotHandlers(@NotNull Collection<Integer> rawSlots,
                                @NotNull Consumer<InventoryClickEvent> handler) {
        for (int slot : rawSlots) {
            clickHandlersBySlot.put(slot, handler);
        }
    }

    /**
     * Registers multiple slot-specific handlers at once.
     *
     * @param handlers map of slot index -> click consumer
     */
    public void setSlotHandlers(@NotNull Map<Integer, Consumer<InventoryClickEvent>> handlers) {
        clickHandlersBySlot.putAll(handlers);
    }

    /**
     * Retrieves the click handler for a specific slot.
     *
     * @param rawSlot raw slot index
     * @return the consumer handler, or null if none is set
     */
    public Consumer<InventoryClickEvent> getSlotHandler(int rawSlot) {
        return clickHandlersBySlot.get(rawSlot);
    }

    /**
     * Removes the click handler for a specific slot.
     *
     * @param rawSlot raw slot index
     */
    public void removeSlotHandler(int rawSlot) {
        clickHandlersBySlot.remove(rawSlot);
    }

    /**
     * Removes handlers from multiple slots.
     *
     * @param rawSlots collection of slot indices to clear
     */
    public void removeSlotHandlers(@NotNull Collection<Integer> rawSlots) {
        for (int slot : rawSlots) {
            clickHandlersBySlot.remove(slot);
        }
    }

    /**
     * Checks whether a slot has a registered click handler.
     *
     * @param rawSlot raw slot index
     * @return true if a handler is registered, false otherwise
     */
    public boolean hasSlotHandler(int rawSlot) {
        return clickHandlersBySlot.containsKey(rawSlot);
    }

    /**
     * Adds a handler to execute when the GUI is opened.
     *
     * @param handler consumer invoked on {@link InventoryOpenEvent}
     */
    public void onOpen(@NotNull Consumer<InventoryOpenEvent> handler) {
        onEvent(InventoryOpenEvent.class, handler);
    }

    /**
     * Adds a handler to execute when the GUI is closed.
     *
     * @param handler consumer invoked on {@link InventoryCloseEvent}
     */
    public void onClose(@NotNull Consumer<InventoryCloseEvent> handler) {
        onEvent(InventoryCloseEvent.class, handler);
    }

    /**
     * Adds a handler to execute when an item drag occurs within the GUI.
     *
     * @param handler consumer invoked on {@link InventoryDragEvent}
     */
    public void onDrag(@NotNull Consumer<InventoryDragEvent> handler) {
        onEvent(InventoryDragEvent.class, handler);
    }

    /**
     * Adds a global click handler for all inventory clicks in this GUI.
     *
     * @param handler consumer invoked on {@link InventoryClickEvent}
     */
    public void onClick(@NotNull Consumer<InventoryClickEvent> handler) {
        onEvent(InventoryClickEvent.class, handler);
    }
}

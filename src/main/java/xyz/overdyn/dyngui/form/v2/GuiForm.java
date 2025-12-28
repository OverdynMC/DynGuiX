package xyz.overdyn.dyngui.form.v2;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class GuiForm {

    @Getter
    private final InventoryUI menu = new InventoryUI();

    public void loadGui(FileConfiguration config) {
        menuLoad(config);
        Bukkit.getLogger().info("loaded");
    }

    public void menuLoad(@NotNull FileConfiguration configuration) {
        try {
            menu.with(InventoryUI.MenuProperty.MENU_ID, configuration.getString("id", UUID.randomUUID().toString()));
            menu.with(InventoryUI.MenuProperty.TITLE, configuration.getString("title", "Empty title"));
            menu.with(InventoryUI.MenuProperty.UPDATE_INTERVAL, configuration.getInt("update_interval", -1));
            menu.with(InventoryUI.MenuProperty.REFRESH_INTERVAL, configuration.getInt("refresh_interval", -1));

            InventoryType inventoryType = null;
            if (configuration.contains("inventoryType")) {
                try {
                    inventoryType = InventoryType.valueOf(configuration.getString("inventoryType").toUpperCase());
                    menu.with(InventoryUI.MenuProperty.TYPE, inventoryType);
                } catch (Exception ignored) {
                    Bukkit.getLogger().warning("Не удалось определить InventoryType: " + configuration.getString("inventory"));
                }
            }

            if (inventoryType == null) {
                if (configuration.contains("size")) {
                    int size = configuration.getInt("size", 6);
                    if (size < 1) size = 1;
                    if (size > 6) size = 6;
                    size = size * 9;
                    menu.with(InventoryUI.MenuProperty.SIZE, size);
                } else {
                    Bukkit.getLogger().warning("size не указаны, меню будет без size!");
                }
            }

            menu.with(InventoryUI.MenuProperty.OPEN_COMMANDS, configuration.getStringList("open_commands"));
            menu.with(InventoryUI.MenuProperty.CLOSE_COMMANDS, configuration.getStringList("close_commands"));

            menu.with(InventoryUI.MenuProperty.BUTTONS,
                parseButtons(configuration).stream()
                    .collect(Collectors.toMap(
                        b -> b.meta(InventoryUI.Button.ButtonProperty.ID),
                        b -> b,
                        (first, second) -> first
                    ))
            );

        } catch (Exception e) {
            Bukkit.getLogger().severe("Error parsing menu: " + e.getMessage());
        }
    }

    private Map<String, Object> parseCustomValues(ConfigurationSection section) {
        ConfigurationSection custom = section.getConfigurationSection("custom_value");
        if (custom == null) return Map.of();

        Map<String, Object> map = new HashMap<>();
        for (String key : custom.getKeys(true)) {
            Object value = custom.get(key);
            map.put(key, value);
        }
        return map;
    }

    private List<ItemFlag> parseItemFlags(ConfigurationSection section) {
        List<ItemFlag> flags = new ArrayList<>();

        List<String> rawFlags = section.getStringList("item_flags");
        for (String name : rawFlags) {
            try {
                ItemFlag flag = ItemFlag.valueOf(name.toUpperCase());
                flags.add(flag);
            } catch (IllegalArgumentException ignored) {
                Bukkit.getLogger().warning("[DynGUI] Unknown ItemFlag: " + name);
            }
        }

        return flags;
    }

    private Map<Enchantment, Integer> parseEnchantments(ConfigurationSection section) {
        Map<Enchantment, Integer> map = new HashMap<>();

        ConfigurationSection enchSection = section.getConfigurationSection("enchantments");
        if (enchSection == null) return map;

        for (String enchKey : enchSection.getKeys(false)) {
            try {
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchKey.toLowerCase()));
                int level = enchSection.getInt(enchKey, 1);

                if (enchantment != null) map.put(enchantment, level);
            } catch (Exception ignored) {}
        }
        return map;
    }

    private List<InventoryUI.Button> parseButtons(FileConfiguration config) {
        List<InventoryUI.Button> buttons = new ArrayList<>();
        var items = config.getConfigurationSection("items");

        if (items != null) {
            for (var key : items.getKeys(false)) {
                var item = items.getConfigurationSection(key);
                if (item != null) {
                    var displayName = item.getString("display_name");
                    var displayLore = item.getStringList("display_lore");
                    var slots = parseSlots(item.get("slot"));
                    var amount = item.getInt("amount", 1);
                    var customModelData = item.getInt("custom_model_data", -1);
                    Map<Enchantment, Integer> enchantments = parseEnchantments(item);
                    int priority = item.getInt("priority", 0);
                    boolean update = item.getBoolean("update", false);
                    boolean placeholder = item.getBoolean("placeholder", false);
                    List<ItemFlag> itemFlags = parseItemFlags(item);

                    String material = PlaceholderAPI.setPlaceholders(null, item.getString("material", "STONE"));

                    for (var slot : slots) {
                        buttons.add(
                            new InventoryUI.Button()
                                .with(InventoryUI.Button.ButtonProperty.ID, key)
                                .with(InventoryUI.Button.ButtonProperty.DISPLAY_NAME, displayName)
                                .with(InventoryUI.Button.ButtonProperty.DISPLAY_LORE, displayLore)
                                .with(InventoryUI.Button.ButtonProperty.SLOT, slot)
                                .with(InventoryUI.Button.ButtonProperty.AMOUNT, amount)
                                .with(InventoryUI.Button.ButtonProperty.CUSTOM_MODEL_DATA, customModelData)
                                .with(InventoryUI.Button.ButtonProperty.ITEM_ENCHANTMENTS, enchantments)
                                .with(InventoryUI.Button.ButtonProperty.PRIORITY, priority)
                                .with(InventoryUI.Button.ButtonProperty.UPDATE, update)
                                .with(InventoryUI.Button.ButtonProperty.PLACEHOLDER, placeholder)
                                .with(InventoryUI.Button.ButtonProperty.ITEM_FLAGS, itemFlags)
                                .with(InventoryUI.Button.ButtonProperty.MATERIAL, material)
                                .with(InventoryUI.Button.ButtonProperty.CUSTOM_VALUES, parseCustomValues(item))
                                .with(InventoryUI.Button.ButtonProperty.VIEW_REQUIREMENTS, parseViewRequirements0(item))
                                .with(InventoryUI.Button.ButtonProperty.COMMANDS, parseClickCommands0(item))

                        );
                    }
                }
            }
        }

        return List.of();
    }

    private List<InventoryUI.Command> parseClickCommands0(ConfigurationSection itemSection) {
        List<InventoryUI.Command> buttonCommands = new ArrayList<>();
        if (itemSection.contains("left_click_commands")) {
            buttonCommands.add(new InventoryUI.Command(false, ClickType.LEFT, itemSection.getStringList("left_click_commands"),
                    parseClickRequirements0(itemSection, "left_click_requirements", ClickType.LEFT, false)));
        }
        if (itemSection.contains("right_click_commands")) {
            buttonCommands.add(new InventoryUI.Command(false, ClickType.RIGHT, itemSection.getStringList("right_click_commands"),
                    parseClickRequirements0(itemSection, "right_click_requirements", ClickType.RIGHT, false)));
        }
        if (itemSection.contains("shift_left_click_commands")) {
            buttonCommands.add(new InventoryUI.Command(false, ClickType.SHIFT_LEFT, itemSection.getStringList("shift_left_click_commands"),
                    parseClickRequirements0(itemSection, "shift_left_click_requirements", ClickType.SHIFT_LEFT, false)));
        }
        if (itemSection.contains("shift_right_click_commands")) {
            buttonCommands.add(new InventoryUI.Command(false, ClickType.SHIFT_RIGHT, itemSection.getStringList("shift_right_click_commands"),
                    parseClickRequirements0(itemSection, "shift_right_click_requirements", ClickType.SHIFT_RIGHT, false)));
        }
        if (itemSection.contains("click_commands")) {
            buttonCommands.add(new InventoryUI.Command(true, ClickType.UNKNOWN, itemSection.getStringList("click_commands"),
                    parseClickRequirements0(itemSection, "click_requirements", ClickType.UNKNOWN, true)));
        }
        if (itemSection.contains("drop_commands")) {
            buttonCommands.add(new InventoryUI.Command(false, ClickType.DROP, itemSection.getStringList("drop_commands"),
                    parseClickRequirements0(itemSection, "drop_requirements", ClickType.DROP, false)));
        }
        return buttonCommands;
    }

    private List<InventoryUI.SimpleRequirement> parseViewRequirements0(ConfigurationSection itemSection) {
        List<InventoryUI.SimpleRequirement> requirements = new ArrayList<>();
        ConfigurationSection requirementsSection = itemSection.getConfigurationSection("view_requirements.requirements");
        if (requirementsSection == null) {
            return requirements;
        }
        for (String key : requirementsSection.getKeys(false)) {
            ConfigurationSection section = requirementsSection.getConfigurationSection(key);
            if (section == null) continue;
            requirements.add(new InventoryUI.SimpleRequirement(
                    section.getString("type"),
                    section.getString("input"),
                    section.getString("output"),
                    section.getString("permission"),
                    section.getStringList("success_commands"),
                    section.getStringList("deny_commands")
            ));
        }
        return requirements;
    }

    private List<InventoryUI.ClickRequirement> parseClickRequirements0(ConfigurationSection itemSection, String name, ClickType clickType, boolean anyClick) {
        List<InventoryUI.ClickRequirement> requirements = new ArrayList<>();
        ConfigurationSection requirementsSection = itemSection.getConfigurationSection(name + ".requirements");
        if (requirementsSection == null) {
            return requirements;
        }
        for (String key : requirementsSection.getKeys(false)) {
            ConfigurationSection section = requirementsSection.getConfigurationSection(key);
            if (section == null) continue;
            requirements.add(new InventoryUI.ClickRequirement(anyClick, clickType,
                    section.getString("type"),
                    section.getString("input"),
                    section.getString("output"),
                    section.getString("permission"),
                    section.getStringList("success_commands"),
                    section.getStringList("deny_commands")));
        }
        return requirements;
    }

    private List<Integer> parseSlots(Object slotObject) {
        List<Integer> slots = new ArrayList<>();
        if (slotObject instanceof Integer) {
            slots.add((Integer) slotObject);
        } else if (slotObject instanceof String) {
            String slotString = ((String) slotObject).trim();
            slots.addAll(parseSlotString(slotString));
        } else if (slotObject instanceof List<?>) {
            for (Object obj : (List<?>) slotObject) {
                if (obj instanceof Integer) {
                    slots.add((Integer) obj);
                } else if (obj instanceof String) {
                    slots.addAll(parseSlotString((String) obj));
                }
            }
        } else {
            Bukkit.getLogger().warning("Unknown slot format: " + slotObject);
        }
        return slots;
    }

    private List<Integer> parseSlotString(String slotString) {
        List<Integer> slots = new ArrayList<>();
        if (slotString.contains("-")) {
            try {
                String[] range = slotString.split("-");
                int start = Integer.parseInt(range[0].trim());
                int end = Integer.parseInt(range[1].trim());
                for (int i = start; i <= end; i++) {
                    slots.add(i);
                }
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Error parsing slot range: " + slotString);
            }
        } else {
            try {
                slots.add(Integer.parseInt(slotString));
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Error parsing single slot: " + slotString);
            }
        }
        return slots;
    }
}
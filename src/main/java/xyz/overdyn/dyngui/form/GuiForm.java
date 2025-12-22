package xyz.overdyn.dyngui.form;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import xyz.overdyn.dyngui.form.requirements.ClickRequirement;
import xyz.overdyn.dyngui.form.requirements.ViewRequirement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiForm {

    @Getter
    private Menu menu;

    public void loadGui(FileConfiguration config) {
        menuLoad(config);
        Bukkit.getLogger().info("loaded");
    }

    public void menuLoad(@NotNull FileConfiguration configuration) {
        try {
            String menuId = configuration.getString("id"); //template "report_main"
            String title = configuration.getString("title", "Empty title");

            int update_interval = configuration.getBoolean("update") ? configuration.getInt("update_interval", -1) : -1;
            int refresh_interval = configuration.getBoolean("refresh") ? configuration.getInt("refresh_interval", -1) : -1;
            int size = configuration.getInt("size", 6);
            if (size < 9) {
                if (size <= 6 && size>0) {
                    size = size * 9;
                } else {
                    size = 9;
                }
            }

            InventoryType inventoryType = InventoryType.valueOf(configuration.getString("inventory", "CHEST"));
            List<String> openCommandsActions = configuration.getStringList("open_commands");
            List<String> closeCommandsActions = configuration.getStringList("close_commands");

            List<Button> buttons = getButtons(configuration);

            menu = new Menu(menuId, title, configuration, size, inventoryType, openCommandsActions, parseViewRequirements(configuration), update_interval, refresh_interval, closeCommandsActions, buttons);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error parsing menu: " + e.getMessage());
        }
    }

    private Map<String, Object> parseCustomValues(ConfigurationSection section) {
        ConfigurationSection custom = section.getConfigurationSection("custom_value");
        if (custom == null) return Map.of(); // пусто

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

    private List<Button> getButtons(FileConfiguration config) {
        List<Button> buttons = new ArrayList<>();
        ConfigurationSection itemsSection = config.getConfigurationSection("Items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection != null) {
                    String displayName = itemSection.getString("display_name", "empty");
                    List<String> lore = itemSection.getStringList("display_lore");
                    List<Integer> slots = parseSlots(itemSection.get("slot"));
                    int amount = itemSection.getInt("amount", 1);
                    int customModelData = itemSection.getInt("custom_model_data", 0);
                    Map<Enchantment, Integer> enchantments = parseEnchantments(itemSection);
                    int priority = itemSection.getInt("priority", 0);
                    boolean update = itemSection.getBoolean("update", false);
                    boolean placeholder = itemSection.getBoolean("placeholder", false);
                    List<ItemFlag> itemFlags = parseItemFlags(itemSection);
//                    String type = itemSection.getString("type", "default");
                    String rgb = itemSection.getString("color", "WHITE");

                    String material = PlaceholderAPI.setPlaceholders(null, itemSection.getString("material", "STONE"));
                    ItemStack itemStack;
                    if (material.startsWith("basehead-")) {
                        try {
                            itemStack = SkullCreator.itemFromBase64(material.replace("basehead-", ""));
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("Error creating custom skull: " + e.getMessage());
                            itemStack = new ItemStack(SkullCreator.createSkull());
                        }
                    } else {
                        itemStack = new ItemStack(Material.valueOf(material));
                    }
                    itemStack.setAmount(amount);
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        meta.addItemFlags(ItemFlag.HIDE_DYE);
                        meta.setDisplayName(displayName);
                        meta.setLore(lore);
                        meta.addItemFlags(itemFlags.toArray(ItemFlag[]::new));
                        meta.setCustomModelData(customModelData);

                        enchantments.forEach((ench, level) -> meta.addEnchant(ench, level, true));

                        if (meta instanceof LeatherArmorMeta lam) {
                            lam.setColor(getColorByName(rgb));
                        }
                        if (meta instanceof PotionMeta potionMeta) {
                            potionMeta.setColor(getColorByName(rgb));
                        }
                        itemStack.setItemMeta(meta);
                    }

                    for (Integer slot : slots) {
                        buttons.add(new Button(key, itemsSection, slot, displayName,
                                lore, priority, amount, customModelData,
                                itemFlags,
                                itemStack, parseViewRequirements(itemSection),
                                parseClickCommands(itemSection),
                                update, placeholder,
                                parseCustomValues(itemSection)
                        ));
                    }
                }
            }
        }
        return buttons;
    }

    private List<ButtonCommand> parseClickCommands(ConfigurationSection itemSection) {
        List<ButtonCommand> buttonCommands = new ArrayList<>();
        if (itemSection.contains("left_click_commands")) {
            buttonCommands.add(new ButtonCommand(false, ClickType.LEFT, itemSection.getStringList("left_click_commands"),
                    parseClickRequirements(itemSection, "left_click_requirements", ClickType.LEFT, false)));
        }
        if (itemSection.contains("right_click_commands")) {
            buttonCommands.add(new ButtonCommand(false, ClickType.RIGHT, itemSection.getStringList("right_click_commands"),
                    parseClickRequirements(itemSection, "right_click_requirements", ClickType.RIGHT, false)));
        }
        if (itemSection.contains("shift_left_click_commands")) {
            buttonCommands.add(new ButtonCommand(false, ClickType.SHIFT_LEFT, itemSection.getStringList("shift_left_click_commands"),
                    parseClickRequirements(itemSection, "shift_left_click_requirements", ClickType.SHIFT_LEFT, false)));
        }
        if (itemSection.contains("shift_right_click_commands")) {
            buttonCommands.add(new ButtonCommand(false, ClickType.SHIFT_RIGHT, itemSection.getStringList("shift_right_click_commands"),
                    parseClickRequirements(itemSection, "shift_right_click_requirements", ClickType.SHIFT_RIGHT, false)));
        }
        if (itemSection.contains("click_commands")) {
            buttonCommands.add(new ButtonCommand(true, ClickType.UNKNOWN, itemSection.getStringList("click_commands"),
                    parseClickRequirements(itemSection, "click_requirements", ClickType.UNKNOWN, true)));
        }
        if (itemSection.contains("drop_commands")) {
            buttonCommands.add(new ButtonCommand(false, ClickType.DROP, itemSection.getStringList("drop_commands"),
                    parseClickRequirements(itemSection, "drop_requirements", ClickType.DROP, false)));
        }
        return buttonCommands;
    }

    private List<ViewRequirement> parseViewRequirements(ConfigurationSection itemSection) {
        List<ViewRequirement> requirements = new ArrayList<>();
        ConfigurationSection requirementsSection = itemSection.getConfigurationSection("view_requirements.requirements");
        if (requirementsSection == null) {
            return requirements;
        }
        for (String key : requirementsSection.getKeys(false)) {
            ConfigurationSection section = requirementsSection.getConfigurationSection(key);
            if (section == null) continue;
            requirements.add(new ViewRequirement(
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

    private List<ClickRequirement> parseClickRequirements(ConfigurationSection itemSection, String name, ClickType clickType, boolean anyClick) {
        List<ClickRequirement> requirements = new ArrayList<>();
        ConfigurationSection requirementsSection = itemSection.getConfigurationSection(name + ".requirements");
        if (requirementsSection == null) {
            return requirements;
        }
        for (String key : requirementsSection.getKeys(false)) {
            ConfigurationSection section = requirementsSection.getConfigurationSection(key);
            if (section == null) continue;
            requirements.add(new ClickRequirement(anyClick, clickType,
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

    private static @NotNull Color getColorByName(@NotNull String name) {
        try {
            Field field = Color.class.getDeclaredField(name.toUpperCase());

            Object o = field.get(null);
            if (o instanceof Color color) {
                return color;
            }
        } catch (Exception ignored) {
        }

        return Color.WHITE;
    }
}
package xyz.overdyn.dyngui.abstracts.test;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.overdyn.dyngui.abstracts.AbstractGuiLayer;
import xyz.overdyn.dyngui.form.Button;
import xyz.overdyn.dyngui.form.ButtonCommand;
import xyz.overdyn.dyngui.form.GuiForm;
import xyz.overdyn.dyngui.form.requirements.ClickRequirement;
import xyz.overdyn.dyngui.form.requirements.Requirements;
import xyz.overdyn.dyngui.form.requirements.ViewRequirement;
import xyz.overdyn.dyngui.items.GuiItem;
import xyz.overdyn.dyngui.items.ItemWrapper;
import xyz.overdyn.dyngui.policy.GuiPolicy;

import java.util.*;

public class FormedGui extends AbstractGuiLayer {

    public FormedGui(@NotNull GuiForm form) {
        super(
                form.getMenu().size(),
                Component.text(form.getMenu().title()),
                GuiPolicy.Factories.HIGHEST
        );

        registerButtons(form);
    }

    /**
     * Группируем кнопки по слотам, выбираем по приоритету и view-условиям.
     */
    private void registerButtons(GuiForm form) {

        // Группируем кнопки по слоту
        Map<Integer, List<Button>> buttonsBySlot = new HashMap<>();

        for (Button btn : form.getMenu().buttons()) {
            buttonsBySlot
                    .computeIfAbsent(btn.slot(), k -> new ArrayList<>())
                    .add(btn);
        }

        // Обрабатываем каждый слот отдельно
        for (Map.Entry<Integer, List<Button>> entry : buttonsBySlot.entrySet()) {

            int slot = entry.getKey();
            List<Button> slotButtons = entry.getValue();

            // Больший priority — важнее
            slotButtons.sort(Comparator.comparingInt(Button::priority).reversed());

            Button selected = null;

            // Ищем первую кнопку, у которой все ViewRequirement прошли
            outer:
            for (Button btn : slotButtons) {
                for (ViewRequirement req : btn.viewRequirements()) {
                    if (!Requirements.check(getViewer(), req)) {
                        // эта кнопка не подходит
                        continue outer;
                    }
                }
                selected = btn;
                break;
            }

            // Если ничего не подошло — слот остаётся пустым
            if (selected != null) {
                registerButton(selected);
            }
        }
    }

    /**
     * Регистрирует одну кнопку в GUI.
     */
    private void registerButton(Button btn) {

        ItemStack base = btn.itemStack();

        // Lore -> List<Component> с плейсхолдерами
        List<Component> loreComponents = new ArrayList<>();
        for (String line : btn.lore()) {
            loreComponents.add(Component.text(PlaceholderAPI.setPlaceholders(getViewer(), line)));
        }

        ItemWrapper builder = ItemWrapper.builder(base.getType())
                .amount(base.getAmount())
                .displayName(
                        Component.text(
                                PlaceholderAPI.setPlaceholders(getViewer(), btn.displayName())
                        )
                )
                .lore(loreComponents).build();

        if (btn.customModelData() > 0) {
            builder.customModelData(btn.customModelData());
        }

        GuiItem guiItem = new GuiItem(builder).addSlot(btn.slot());

        guiItem.onClick(event -> {
            event.setCancelled(true);
            ClickType click = event.getClick();

            for (ButtonCommand cmd : btn.buttonCommands()) {
                // Проверяем тип клика
                if (!(cmd.anyClick() || cmd.clickType() == click)) {
                    continue;
                }

                boolean allowed = true;

                // Проверяем click-requirements
                for (ClickRequirement req : cmd.clickRequirements()) {
                    if (req.anyClick() || req.clickType() == click) {
                        if (!Requirements.check(getViewer(), req)) {
                            executeDenied(req, btn);
                            allowed = false;
                            break;
                        } else {
                            executeAllowed(req, btn);
                        }
                    }
                }

                if (allowed) {
                    executeActions(cmd, btn);
                    break;
                }
            }
        });

        registerItem(guiItem);
    }

    // ==== Action Execution Bridges (заглушки — сюда подключишь свою систему действий) ====

    private void executeDenied(ClickRequirement req, Button button) {
        req.deny_commands().forEach(str -> getViewer().sendMessage("Denied: " + str));
    }

    private void executeAllowed(ClickRequirement req, Button button) {
        req.success().forEach(str -> getViewer().sendMessage("Allowed: " + str));
    }

    private void executeActions(ButtonCommand cmd, Button button) {
        cmd.actions().forEach(str -> getViewer().sendMessage("EXEC: " + str));
    }
}

package dev.kirpichvisuals.hud;

import dev.kirpichvisuals.screen.HudEditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/** Единый набор HUD-модулей: им пользуются и HUD, и экран перетаскивания. */
public final class HudManager {
    public static final TargetHud TARGET = new TargetHud();
    public static final PotionHud POTIONS = new PotionHud();
    public static final CooldownHud COOLDOWNS = new CooldownHud();

    /** Порядок = порядок отрисовки (последний сверху). */
    public static final List<HudModule> ALL = List.of(TARGET, POTIONS, COOLDOWNS);

    private HudManager() {
    }

    public static void renderAll(DrawContext ctx) {
        // пока открыт экран перетаскивания, он сам рисует блоки, иначе они дублировались бы
        if (MinecraftClient.getInstance().currentScreen instanceof HudEditorScreen) {
            return;
        }
        for (HudModule module : ALL) {
            module.render(ctx);
        }
        HotbarGlow.render(ctx);
    }
}

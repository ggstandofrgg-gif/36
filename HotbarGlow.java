package dev.kirpichvisuals.hud;

import dev.kirpichvisuals.config.KirpichConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Цветная рамка выбранного слота хотбара, плавно переезжающая между слотами. */
public final class HotbarGlow {
    private static float shown = -1f;

    private HotbarGlow() {
    }

    public static void render(DrawContext ctx) {
        KirpichConfig cfg = KirpichConfig.get();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!cfg.hotbarGlow || mc.player == null || mc.world == null
                || mc.options.hudHidden || mc.player.isSpectator()) {
            shown = -1f;
            return;
        }

        int slot = mc.player.getInventory().selectedSlot;
        if (shown < 0f) {
            shown = slot;
        }
        shown += (slot - shown) * 0.35f;

        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int rgb = KirpichConfig.ACCENTS[cfg.accent];

        // ванильный хотбар: 182x22 по центру внизу, слот шириной 20
        int x = Math.round(sw / 2f - 92f + shown * 20f);
        int y = sh - 23;

        ctx.fill(x + 1, y + 1, x + 23, y + 23, (0x30 << 24) | rgb);
        HudUtil.outline(ctx, x, y, 24, 24, HudUtil.opaque(rgb));
        ctx.fill(x + 3, y - 2, x + 21, y - 1, HudUtil.opaque(rgb));
    }
}

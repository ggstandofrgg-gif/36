package dev.flyvisuals.hud;

import dev.flyvisuals.config.Anchor;
import dev.flyvisuals.config.FlyConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

/** Базовый класс HUD-модуля: масштаб, привязка к углу экрана, проверка включённости. */
public abstract class HudModule {
    protected static final int MARGIN = 6;
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    protected abstract boolean enabled(FlyConfig cfg);

    protected abstract Anchor anchor(FlyConfig cfg);

    /** Обновить состояние перед отрисовкой. false — модуль сейчас не показывать. */
    protected abstract boolean prepare();

    protected abstract int width();

    protected abstract int height();

    protected abstract void draw(DrawContext ctx, int x, int y, int accentRgb);

    public final void render(DrawContext ctx) {
        FlyConfig cfg = FlyConfig.get();
        if (!enabled(cfg) || mc.player == null || mc.world == null || mc.options.hudHidden) {
            return;
        }
        if (!prepare()) {
            return;
        }

        float scale = FlyConfig.SCALES[cfg.scalePreset];
        int sw = (int) (ctx.getScaledWindowWidth() / scale);
        int sh = (int) (ctx.getScaledWindowHeight() / scale);
        int[] pos = anchor(cfg).place(sw, sh, width(), height(), MARGIN);

        MatrixStack matrices = ctx.getMatrices();
        matrices.push();
        matrices.scale(scale, scale, 1f);
        draw(ctx, pos[0], pos[1], FlyConfig.ACCENTS[cfg.accent]);
        matrices.pop();
    }
}

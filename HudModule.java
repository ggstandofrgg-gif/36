package dev.kirpichvisuals.hud;

import dev.kirpichvisuals.config.Anchor;
import dev.kirpichvisuals.config.KirpichConfig;
import dev.kirpichvisuals.config.HudPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

/** Базовый класс HUD-модуля: масштаб, позиция (угол или свободная), проверка включённости. */
public abstract class HudModule {
    protected static final int MARGIN = 6;
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    public abstract boolean enabled(KirpichConfig cfg);

    protected abstract Anchor anchor(KirpichConfig cfg);

    /** Свободная позиция модуля в конфиге. */
    public abstract HudPos position(KirpichConfig cfg);

    /** Обновить состояние перед отрисовкой. false - модуль сейчас не показывать. */
    protected abstract boolean prepare();

    public abstract int width();

    protected abstract int height();

    protected abstract void draw(DrawContext ctx, int x, int y, int accentRgb);

    /** Высота блока с тестовыми данными в режиме перетаскивания. */
    public abstract int previewHeight();

    /** Рисует блок с тестовыми данными, чтобы его можно было двигать, даже когда он скрыт. */
    public abstract void drawPreview(DrawContext ctx, int x, int y, int accentRgb);

    /**
     * Левый верхний угол блока размером w*h. sw/sh уже поделены на масштаб HUD.
     * Если пользователь двигал блок, берётся его позиция, иначе выбранный угол.
     */
    public final int[] locate(KirpichConfig cfg, int sw, int sh, int w, int h) {
        HudPos p = position(cfg);
        if (p.custom) {
            int x = Math.max(0, Math.min(sw - w, Math.round(p.fx * sw)));
            int y = Math.max(0, Math.min(sh - h, Math.round(p.fy * sh)));
            return new int[]{x, y};
        }
        return anchor(cfg).place(sw, sh, w, h, MARGIN);
    }

    public final void render(DrawContext ctx) {
        KirpichConfig cfg = KirpichConfig.get();
        if (!enabled(cfg) || mc.player == null || mc.world == null || mc.options.hudHidden) {
            return;
        }
        if (!prepare()) {
            return;
        }

        float scale = KirpichConfig.SCALES[cfg.scalePreset];
        int sw = (int) (ctx.getScaledWindowWidth() / scale);
        int sh = (int) (ctx.getScaledWindowHeight() / scale);
        int[] pos = locate(cfg, sw, sh, width(), height());

        MatrixStack matrices = ctx.getMatrices();
        matrices.push();
        matrices.scale(scale, scale, 1f);
        draw(ctx, pos[0], pos[1], KirpichConfig.ACCENTS[cfg.accent]);
        matrices.pop();
    }
}

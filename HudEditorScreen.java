package dev.kirpichvisuals.screen;

import dev.kirpichvisuals.config.KirpichConfig;
import dev.kirpichvisuals.hud.HudManager;
import dev.kirpichvisuals.hud.HudModule;
import dev.kirpichvisuals.hud.HudUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/**
 * Режим перетаскивания: все включённые блоки HUD показываются с тестовыми данными,
 * их можно двигать мышью или пальцем. Блок прилипает к краям и к центру экрана.
 */
public class HudEditorScreen extends Screen {
    private static final int SNAP = 3;
    private static final int EDGE = 6;

    private final Screen parent;
    private final KirpichConfig cfg = KirpichConfig.get();

    private HudModule dragged;
    private int grabX;
    private int grabY;

    public HudEditorScreen(Screen parent) {
        super(Text.translatable("kirpichvisuals.edit.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("kirpichvisuals.edit.reset"), button -> {
            cfg.resetPositions();
            cfg.save();
        }).dimensions(width / 2 - 102, height - 28, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                .dimensions(width / 2 + 2, height - 28, 100, 20)
                .build());
    }

    private float scale() {
        return KirpichConfig.SCALES[cfg.scalePreset];
    }

    private int scaledWidth() {
        return (int) (width / scale());
    }

    private int scaledHeight() {
        return (int) (height / scale());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // фон рисует сам Screen.render
        super.render(context, mouseX, mouseY, delta);

        float s = scale();
        int sw = scaledWidth();
        int sh = scaledHeight();
        int accent = KirpichConfig.ACCENTS[cfg.accent];

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.scale(s, s, 1f);
        for (HudModule module : HudManager.ALL) {
            if (!module.enabled(cfg)) {
                continue;
            }
            int w = module.width();
            int h = module.previewHeight();
            int[] p = module.locate(cfg, sw, sh, w, h);
            module.drawPreview(context, p[0], p[1], accent);
            int frame = module == dragged ? 0xFFFFFFFF : 0x90FFFFFF;
            HudUtil.outline(context, p[0] - 1, p[1] - 1, w + 2, h + 2, frame);
        }
        matrices.pop();

        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("kirpichvisuals.edit.hint"),
                width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // кнопки Сброс и Готово важнее блоков
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) {
            return false;
        }

        float s = scale();
        double x = mouseX / s;
        double y = mouseY / s;
        int sw = scaledWidth();
        int sh = scaledHeight();

        // с конца, чтобы выбрать верхний из перекрывающихся блоков
        for (int i = HudManager.ALL.size() - 1; i >= 0; i--) {
            HudModule module = HudManager.ALL.get(i);
            if (!module.enabled(cfg)) {
                continue;
            }
            int w = module.width();
            int h = module.previewHeight();
            int[] p = module.locate(cfg, sw, sh, w, h);
            if (x >= p[0] && x < p[0] + w && y >= p[1] && y < p[1] + h) {
                dragged = module;
                grabX = (int) (x - p[0]);
                grabY = (int) (y - p[1]);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragged != null && button == 0) {
            float s = scale();
            int sw = scaledWidth();
            int sh = scaledHeight();
            int w = dragged.width();
            int h = dragged.previewHeight();

            int nx = (int) (mouseX / s) - grabX;
            int ny = (int) (mouseY / s) - grabY;

            // прилипание к краям и центру
            nx = snap(nx, EDGE);
            nx = snap(nx, sw - w - EDGE);
            nx = snap(nx, (sw - w) / 2);
            ny = snap(ny, EDGE);
            ny = snap(ny, sh - h - EDGE);
            ny = snap(ny, (sh - h) / 2);

            nx = Math.max(0, Math.min(Math.max(0, sw - w), nx));
            ny = Math.max(0, Math.min(Math.max(0, sh - h), ny));

            dragged.position(cfg).set(nx / (float) sw, ny / (float) sh);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragged != null) {
            dragged = null;
            cfg.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private static int snap(int value, int target) {
        return Math.abs(value - target) <= SNAP ? target : value;
    }

    @Override
    public void close() {
        cfg.save();
        client.setScreen(parent);
    }
}

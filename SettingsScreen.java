package dev.kirpichvisuals.screen;

import dev.kirpichvisuals.config.Anchor;
import dev.kirpichvisuals.config.KirpichConfig;
import dev.kirpichvisuals.config.HudPos;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Supplier;

/** Экран настроек с вкладками. Открывается клавишей (по умолчанию Right Shift). */
public class SettingsScreen extends Screen {
    public enum Tab {
        HUD,
        EFFECTS,
        PERF
    }

    private static final int[] COUNTS = {4, 8, 12, 16, 24, 32};
    private static final int[] DISTANCES = {24, 32, 48, 64, 96, 128};
    private static final int[] FPS_LIMITS = {0, 30, 45, 60, 90};
    private static final int FIRST_ROW_Y = 56;

    private final Screen parent;
    private final Tab tab;
    private final KirpichConfig cfg = KirpichConfig.get();

    public SettingsScreen(Screen parent) {
        this(parent, Tab.HUD);
    }

    public SettingsScreen(Screen parent, Tab tab) {
        super(Text.translatable("kirpichvisuals.title"));
        this.parent = parent;
        this.tab = tab;
    }

    @Override
    protected void init() {
        addTabs();
        switch (tab) {
            case HUD -> initHud();
            case EFFECTS -> initEffects();
            case PERF -> initPerf();
        }
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close())
                .dimensions(width / 2 - 100, height - 28, 200, 20)
                .build());
    }

    private void addTabs() {
        Tab[] tabs = Tab.values();
        int tabWidth = 100;
        int gap = 4;
        int total = tabs.length * (tabWidth + gap) - gap;
        int startX = width / 2 - total / 2;
        for (int i = 0; i < tabs.length; i++) {
            Tab target = tabs[i];
            ButtonWidget button = ButtonWidget.builder(
                    Text.translatable("kirpichvisuals.tab." + target.name()),
                    b -> client.setScreen(new SettingsScreen(parent, target))
            ).dimensions(startX + i * (tabWidth + gap), 28, tabWidth, 20).build();
            button.active = target != tab;
            addDrawableChild(button);
        }
    }

    private void initHud() {
        option(0, 0, () -> toggle("kirpichvisuals.opt.target", cfg.targetHud), () -> cfg.targetHud = !cfg.targetHud);
        option(0, 1, () -> toggle("kirpichvisuals.opt.potions", cfg.potionHud), () -> cfg.potionHud = !cfg.potionHud);
        option(0, 2, () -> toggle("kirpichvisuals.opt.cooldowns", cfg.cooldownHud), () -> cfg.cooldownHud = !cfg.cooldownHud);
        option(0, 3, () -> toggle("kirpichvisuals.opt.hotbar", cfg.hotbarGlow), () -> cfg.hotbarGlow = !cfg.hotbarGlow);
        option(0, 4, () -> value("kirpichvisuals.opt.accent", Text.translatable("kirpichvisuals.accent." + cfg.accent)),
                () -> cfg.accent = (cfg.accent + 1) % KirpichConfig.ACCENTS.length);
        option(0, 5, () -> value("kirpichvisuals.opt.scale", Text.literal(Math.round(KirpichConfig.SCALES[cfg.scalePreset] * 100) + "%")),
                () -> cfg.scalePreset = (cfg.scalePreset + 1) % KirpichConfig.SCALES.length);

        option(1, 0, () -> value("kirpichvisuals.opt.anchor_target", anchorText(cfg.targetPos, cfg.targetAnchor)),
                () -> cfg.targetAnchor = cycleAnchor(cfg.targetPos, cfg.targetAnchor));
        option(1, 1, () -> value("kirpichvisuals.opt.anchor_potions", anchorText(cfg.potionPos, cfg.potionAnchor)),
                () -> cfg.potionAnchor = cycleAnchor(cfg.potionPos, cfg.potionAnchor));
        option(1, 2, () -> value("kirpichvisuals.opt.anchor_cooldowns", anchorText(cfg.cooldownPos, cfg.cooldownAnchor)),
                () -> cfg.cooldownAnchor = cycleAnchor(cfg.cooldownPos, cfg.cooldownAnchor));

        // новый экземпляр, чтобы подписи кнопок обновились после возврата
        addDrawableChild(ButtonWidget.builder(Text.translatable("kirpichvisuals.opt.edit"),
                        b -> client.setScreen(new HudEditorScreen(new SettingsScreen(parent, Tab.HUD))))
                .dimensions(columnX(1), rowY(3), 150, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("kirpichvisuals.opt.reset"), b -> {
                    cfg.resetPositions();
                    cfg.save();
                    client.setScreen(new SettingsScreen(parent, Tab.HUD));
                })
                .dimensions(columnX(1), rowY(4), 150, 20)
                .build());
    }

    private void initEffects() {
        option(0, 0, () -> toggle("kirpichvisuals.opt.hits", cfg.hitParticles), () -> cfg.hitParticles = !cfg.hitParticles);
        option(0, 1, () -> value("kirpichvisuals.opt.style", Text.translatable("kirpichvisuals.style." + cfg.particleStyle.name())),
                () -> cfg.particleStyle = cfg.particleStyle.next());
        option(0, 2, () -> value("kirpichvisuals.opt.count", Text.literal(String.valueOf(cfg.particleCount))),
                () -> cfg.particleCount = nextOf(COUNTS, cfg.particleCount));
        option(0, 3, () -> toggle("kirpichvisuals.opt.burst", cfg.hitBurst), () -> cfg.hitBurst = !cfg.hitBurst);
        option(0, 4, () -> value("kirpichvisuals.opt.burst_style", Text.translatable("kirpichvisuals.style." + cfg.burstStyle.name())),
                () -> cfg.burstStyle = cfg.burstStyle.next());
        option(0, 5, () -> toggle("kirpichvisuals.opt.jump", cfg.jumpCircle), () -> cfg.jumpCircle = !cfg.jumpCircle);
        option(0, 6, () -> value("kirpichvisuals.opt.jump_style", Text.translatable("kirpichvisuals.style." + cfg.jumpStyle.name())),
                () -> cfg.jumpStyle = cfg.jumpStyle.next());

        option(1, 0, () -> toggle("kirpichvisuals.opt.aura", cfg.targetAura), () -> cfg.targetAura = !cfg.targetAura);
        option(1, 1, () -> value("kirpichvisuals.opt.aura_style", Text.translatable("kirpichvisuals.style." + cfg.auraStyle.name())),
                () -> cfg.auraStyle = cfg.auraStyle.next());
        option(1, 2, () -> value("kirpichvisuals.opt.aura_mode", Text.translatable("kirpichvisuals.aura." + cfg.auraMode.name())),
                () -> cfg.auraMode = cfg.auraMode.next());
        option(1, 3, () -> toggle("kirpichvisuals.opt.ambient", cfg.ambientParticles), () -> cfg.ambientParticles = !cfg.ambientParticles);
        option(1, 4, () -> value("kirpichvisuals.opt.ambient_style", Text.translatable("kirpichvisuals.style." + cfg.ambientStyle.name())),
                () -> cfg.ambientStyle = cfg.ambientStyle.next());
    }

    private void initPerf() {
        option(0, 0, () -> toggle("kirpichvisuals.opt.optimize", cfg.optimizeParticles), () -> cfg.optimizeParticles = !cfg.optimizeParticles);
        option(1, 0, () -> value("kirpichvisuals.opt.distance", Text.literal(String.valueOf(cfg.cullDistance))),
                () -> cfg.cullDistance = nextOf(DISTANCES, cfg.cullDistance));
        option(0, 1, () -> value("kirpichvisuals.opt.lowfps",
                        cfg.lowFpsThreshold == 0 ? Text.translatable("kirpichvisuals.value.off") : Text.literal(cfg.lowFpsThreshold + " FPS")),
                () -> cfg.lowFpsThreshold = nextOf(FPS_LIMITS, cfg.lowFpsThreshold));
    }

    private int columnX(int column) {
        return width / 2 - 154 + column * 158;
    }

    private int rowY(int row) {
        return FIRST_ROW_Y + row * 22;
    }

    private void option(int column, int row, Supplier<Text> label, Runnable onClick) {
        addDrawableChild(ButtonWidget.builder(label.get(), button -> {
            onClick.run();
            button.setMessage(label.get());
            cfg.save();
        }).dimensions(columnX(column), rowY(row), 150, 20).build());
    }

    private static Text anchorText(HudPos pos, Anchor anchor) {
        return pos.custom
                ? Text.translatable("kirpichvisuals.anchor.CUSTOM")
                : Text.translatable("kirpichvisuals.anchor." + anchor.name());
    }

    /** Если блок стоял на свободной позиции, первое нажатие возвращает его в угол, дальше угол листается. */
    private static Anchor cycleAnchor(HudPos pos, Anchor anchor) {
        if (pos.custom) {
            pos.reset();
            return anchor;
        }
        return anchor.next();
    }

    private static MutableText toggle(String key, boolean on) {
        MutableText state = on
                ? Text.translatable("options.on").formatted(Formatting.GREEN)
                : Text.translatable("options.off").formatted(Formatting.RED);
        return Text.translatable(key).append(Text.literal(": ")).append(state);
    }

    private static MutableText value(String key, Text value) {
        return Text.translatable(key).append(Text.literal(": ")).append(value.copy().formatted(Formatting.AQUA));
    }

    /** Следующее значение по кругу; если текущего нет в списке, берётся первое. */
    private static int nextOf(int[] values, int current) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) {
                return values[(i + 1) % values.length];
            }
        }
        return values[0];
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // фон рисует сам Screen.render, повторный вызов renderBackground ломает блюр
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        cfg.save();
        client.setScreen(parent);
    }
}

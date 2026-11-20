package dev.flyvisuals.hud;

import dev.flyvisuals.config.Anchor;
import dev.flyvisuals.config.FlyConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Список активных эффектов с таймером и полоской оставшегося времени. */
public class PotionHud extends HudModule {
    private static final int W = 118;
    private static final int ROW = 18;
    private static final int MAX_ROWS = 8;

    private final List<StatusEffectInstance> effects = new ArrayList<>();
    /** Самая большая длительность, которую мы видели у эффекта, нужна для полоски. */
    private final Map<RegistryEntry<StatusEffect>, Integer> maxDuration = new HashMap<>();

    @Override
    protected boolean enabled(FlyConfig cfg) {
        return cfg.potionHud;
    }

    @Override
    protected Anchor anchor(FlyConfig cfg) {
        return cfg.potionAnchor;
    }

    @Override
    protected int width() {
        return W;
    }

    @Override
    protected int height() {
        return effects.size() * ROW + 6;
    }

    @Override
    protected boolean prepare() {
        effects.clear();
        Set<RegistryEntry<StatusEffect>> present = new HashSet<>();

        for (StatusEffectInstance inst : mc.player.getStatusEffects()) {
            effects.add(inst);
            present.add(inst.getEffectType());
            maxDuration.merge(inst.getEffectType(), inst.isInfinite() ? 1 : inst.getDuration(), Math::max);
        }
        maxDuration.keySet().retainAll(present);

        // бесконечные в конец, остальные по возрастанию оставшегося времени
        effects.sort(Comparator.comparingInt(e -> e.isInfinite() ? Integer.MAX_VALUE : e.getDuration()));
        while (effects.size() > MAX_ROWS) {
            effects.remove(effects.size() - 1);
        }
        return !effects.isEmpty();
    }

    @Override
    protected void draw(DrawContext ctx, int x, int y, int accentRgb) {
        HudUtil.panel(ctx, x, y, W, height(), accentRgb);

        int i = 0;
        for (StatusEffectInstance inst : effects) {
            RegistryEntry<StatusEffect> entry = inst.getEffectType();
            StatusEffect effect = entry.value();
            int ry = y + 3 + i * ROW;

            int color = HudUtil.opaque(effect.getColor());
            ctx.fill(x + 6, ry + 1, x + 14, ry + 9, color);

            String name = effect.getName().getString();
            if (inst.getAmplifier() > 0) {
                name += " " + HudUtil.roman(inst.getAmplifier() + 1);
            }
            name = mc.textRenderer.trimToWidth(name, W - 62);
            int nameColor = effect.isBeneficial() ? HudUtil.TEXT : HudUtil.TEXT_BAD;
            ctx.drawText(mc.textRenderer, name, x + 18, ry + 1, nameColor, true);

            String time = inst.isInfinite() ? "∞" : HudUtil.time(inst.getDuration());
            int timeColor = (!inst.isInfinite() && inst.getDuration() <= 100) ? HudUtil.TEXT_BAD : HudUtil.TEXT_DIM;
            int tw = mc.textRenderer.getWidth(time);
            ctx.drawText(mc.textRenderer, time, x + W - 6 - tw, ry + 1, timeColor, true);

            float fraction = 1f;
            if (!inst.isInfinite()) {
                int max = Math.max(1, maxDuration.getOrDefault(entry, inst.getDuration()));
                fraction = inst.getDuration() / (float) max;
            }
            HudUtil.bar(ctx, x + 6, ry + 11, W - 12, 2, fraction, color);
            i++;
        }
    }
}

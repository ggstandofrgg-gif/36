package dev.flyvisuals.hud;

import dev.flyvisuals.config.Anchor;
import dev.flyvisuals.config.FlyConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

/** Информация о цели под прицелом: имя, здоровье с плавной анимацией, броня, поглощение. */
public class TargetHud extends HudModule {
    private static final int W = 124;
    private static final int H = 36;
    private static final int BAR_W = W - 12;
    private static final long KEEP_MS = 2500L;

    private LivingEntity target;
    private long lastSeen;
    private long lastFrame;
    private float shownHealth;

    @Override
    protected boolean enabled(FlyConfig cfg) {
        return cfg.targetHud;
    }

    @Override
    protected Anchor anchor(FlyConfig cfg) {
        return cfg.targetAnchor;
    }

    @Override
    protected int width() {
        return W;
    }

    @Override
    protected int height() {
        return H;
    }

    @Override
    protected boolean prepare() {
        long now = System.currentTimeMillis();
        Entity looking = mc.targetedEntity;

        if (looking instanceof LivingEntity living && living.isAlive() && living != mc.player) {
            if (living != target) {
                shownHealth = living.getHealth();
            }
            target = living;
            lastSeen = now;
        }

        if (target == null || target.isRemoved() || now - lastSeen > KEEP_MS) {
            target = null;
            return false;
        }

        // плавное движение полоски, не зависящее от частоты кадров
        float dt = lastFrame == 0 ? 0f : Math.min(0.25f, (now - lastFrame) / 1000f);
        lastFrame = now;
        float k = 1f - (float) Math.exp(-10f * dt);
        shownHealth += (target.getHealth() - shownHealth) * k;
        return true;
    }

    @Override
    protected void draw(DrawContext ctx, int x, int y, int accentRgb) {
        LivingEntity t = target;
        float max = Math.max(1f, t.getMaxHealth());
        float ratio = Math.max(0f, Math.min(1f, shownHealth / max));

        HudUtil.panel(ctx, x, y, W, H, accentRgb);

        String name = mc.textRenderer.trimToWidth(t.getName().getString(), W - 12);
        ctx.drawText(mc.textRenderer, name, x + 6, y + 4, HudUtil.TEXT, true);

        HudUtil.bar(ctx, x + 6, y + 16, BAR_W, 6, ratio, HudUtil.healthColor(ratio));

        String hp = String.format("%.1f / %.1f", t.getHealth(), max);
        ctx.drawText(mc.textRenderer, hp, x + 6, y + 25, HudUtil.TEXT_DIM, true);

        float absorption = t.getAbsorptionAmount();
        if (absorption > 0f) {
            String ab = String.format(" +%.1f", absorption);
            int hpWidth = mc.textRenderer.getWidth(hp);
            ctx.drawText(mc.textRenderer, ab, x + 6 + hpWidth, y + 25, 0xFFFFD740, true);
        }

        int armor = t.getArmor();
        if (armor > 0) {
            String a = Text.translatable("flyvisuals.hud.armor").getString() + " " + armor;
            int aw = mc.textRenderer.getWidth(a);
            ctx.drawText(mc.textRenderer, a, x + W - 6 - aw, y + 25, HudUtil.opaque(accentRgb), true);
        }
    }
}

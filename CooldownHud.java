package dev.kirpichvisuals.hud;

import dev.kirpichvisuals.config.Anchor;
import dev.kirpichvisuals.config.KirpichConfig;
import dev.kirpichvisuals.config.HudPos;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Перезарядки предметов (жемчуг, яблоки, щит...) и заряд атаки. */
public class CooldownHud extends HudModule {
    private static final int W = 104;
    private static final int ROW = 20;

    private static final Item[] TRACKED = {
            Items.ENDER_PEARL,
            Items.GOLDEN_APPLE,
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.CHORUS_FRUIT,
            Items.SHIELD,
            Items.WIND_CHARGE,
            Items.TRIDENT
    };

    private record Entry(ItemStack icon, float fill, boolean charging) {
    }

    private final ItemStack[] stacks = new ItemStack[TRACKED.length];
    private final ItemStack attackIcon = new ItemStack(Items.IRON_SWORD);
    private final List<Entry> entries = new ArrayList<>();

    public CooldownHud() {
        for (int i = 0; i < TRACKED.length; i++) {
            stacks[i] = new ItemStack(TRACKED[i]);
        }
    }

    @Override
    public boolean enabled(KirpichConfig cfg) {
        return cfg.cooldownHud;
    }

    @Override
    protected Anchor anchor(KirpichConfig cfg) {
        return cfg.cooldownAnchor;
    }

    @Override
    public HudPos position(KirpichConfig cfg) {
        return cfg.cooldownPos;
    }

    @Override
    public int width() {
        return W;
    }

    @Override
    protected int height() {
        return entries.size() * ROW + 4;
    }

    @Override
    public int previewHeight() {
        return 2 * ROW + 4;
    }

    @Override
    protected boolean prepare() {
        entries.clear();

        float charge = mc.player.getAttackCooldownProgress(0f);
        if (charge < 1f) {
            entries.add(new Entry(attackIcon, charge, true));
        }

        for (ItemStack stack : stacks) {
            float remaining = mc.player.getItemCooldownManager().getCooldownProgress(stack, 0f);
            if (remaining > 0f) {
                entries.add(new Entry(stack, remaining, false));
            }
        }
        return !entries.isEmpty();
    }

    @Override
    protected void draw(DrawContext ctx, int x, int y, int accentRgb) {
        drawEntries(ctx, x, y, accentRgb, entries);
    }

    @Override
    public void drawPreview(DrawContext ctx, int x, int y, int accentRgb) {
        drawEntries(ctx, x, y, accentRgb, List.of(
                new Entry(attackIcon, 0.4f, true),
                new Entry(stacks[0], 0.6f, false)
        ));
    }

    private void drawEntries(DrawContext ctx, int x, int y, int accentRgb, List<Entry> list) {
        HudUtil.panel(ctx, x, y, W, list.size() * ROW + 4, accentRgb);

        int i = 0;
        for (Entry e : list) {
            int ry = y + 2 + i * ROW;
            ctx.drawItem(e.icon(), x + 5, ry + 2);

            int barColor = e.charging() ? HudUtil.opaque(accentRgb) : HudUtil.lerpColor(0xFF5252, 0xFFD740, 1f - e.fill());
            HudUtil.bar(ctx, x + 26, ry + 7, 46, 6, e.fill(), barColor);

            String pct = Math.round(e.fill() * 100f) + "%";
            ctx.drawText(mc.textRenderer, pct, x + 77, ry + 6, HudUtil.TEXT_DIM, true);
            i++;
        }
    }
}

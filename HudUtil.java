package dev.flyvisuals.hud;

import net.minecraft.client.gui.DrawContext;

/** Мелкие помощники для отрисовки HUD. */
public final class HudUtil {
    public static final int PANEL_BG = 0xB0101216;
    public static final int BAR_BG = 0xFF2A2D33;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT_DIM = 0xFFB0B6BE;
    public static final int TEXT_BAD = 0xFFFF8A80;

    private HudUtil() {
    }

    public static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    /** Тёмная панель с цветной полоской-акцентом слева. */
    public static void panel(DrawContext ctx, int x, int y, int w, int h, int accentRgb) {
        ctx.fill(x, y, x + w, y + h, PANEL_BG);
        ctx.fill(x, y, x + 2, y + h, opaque(accentRgb));
    }

    /** Горизонтальная полоска: фон + заполнение долей 0..1. */
    public static void bar(DrawContext ctx, int x, int y, int w, int h, float fraction, int fillColor) {
        ctx.fill(x, y, x + w, y + h, BAR_BG);
        int fw = Math.round(w * Math.max(0f, Math.min(1f, fraction)));
        if (fw > 0) {
            ctx.fill(x, y, x + fw, y + h, fillColor);
        }
    }

    /** Плавный переход цвета a -> b, t в диапазоне 0..1. */
    public static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = Math.round(ar + (br - ar) * t);
        int g = Math.round(ag + (bg - ag) * t);
        int bl = Math.round(ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    /** Красный -> жёлтый -> зелёный по доле здоровья. */
    public static int healthColor(float ratio) {
        int red = 0xFF5252, yellow = 0xFFD740, green = 0x69F0AE;
        return ratio < 0.5f
                ? lerpColor(red, yellow, ratio * 2f)
                : lerpColor(yellow, green, (ratio - 0.5f) * 2f);
    }

    public static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(n);
        };
    }

    public static String time(int ticks) {
        int s = Math.max(0, ticks) / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }
}

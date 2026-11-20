package dev.kirpichvisuals.config;

/** Куда на экране прикрепляется HUD-модуль. */
public enum Anchor {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER;

    /**
     * Возвращает {x, y} левого верхнего угла блока размером w*h.
     * Размеры экрана sw/sh уже поделены на масштаб HUD.
     */
    public int[] place(int sw, int sh, int w, int h, int margin) {
        return switch (this) {
            case TOP_LEFT -> new int[]{margin, margin};
            case TOP_RIGHT -> new int[]{sw - w - margin, margin};
            // слева снизу в ваниле живёт чат, поэтому поднимаем выше
            case BOTTOM_LEFT -> new int[]{margin, sh - h - 50};
            case BOTTOM_RIGHT -> new int[]{sw - w - margin, sh - h - margin};
            case CENTER -> new int[]{(sw - w) / 2, sh / 2 + 28};
        };
    }

    public Anchor next() {
        Anchor[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}

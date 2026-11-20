package dev.kirpichvisuals.config;

/**
 * Свободная позиция HUD-блока. Хранится как доля экрана (0..1) для левого верхнего угла,
 * поэтому не ломается при смене размера окна и масштаба интерфейса.
 */
public class HudPos {
    /** false - блок стоит в углу, выбранном через Anchor; true - в точке (fx, fy). */
    public boolean custom = false;
    public float fx = 0f;
    public float fy = 0f;

    public void set(float fx, float fy) {
        this.custom = true;
        this.fx = fx;
        this.fy = fy;
    }

    public void reset() {
        this.custom = false;
    }

    /** Приводит значения из файла к допустимым. */
    public void sanitize() {
        if (Float.isNaN(fx) || Float.isNaN(fy)) {
            reset();
            fx = 0f;
            fy = 0f;
            return;
        }
        fx = Math.max(0f, Math.min(1f, fx));
        fy = Math.max(0f, Math.min(1f, fy));
    }
}

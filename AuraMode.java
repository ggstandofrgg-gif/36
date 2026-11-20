package dev.kirpichvisuals.config;

/** Форма ауры вокруг цели. */
public enum AuraMode {
    /** Кольцо-нимб над головой. */
    RING,
    /** Спираль вдоль тела снизу вверх. */
    SPIRAL,
    /** Несколько частиц, вращающихся вокруг груди. */
    ORBIT;

    public AuraMode next() {
        AuraMode[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}

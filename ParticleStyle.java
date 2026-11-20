package dev.kirpichvisuals.config;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

/** Вид частиц для эффектов. Используются только ванильные частицы. */
public enum ParticleStyle {
    CRIT,
    STARS,
    SPARKS,
    ENCHANT,
    FLAME,
    SOUL,
    TOTEM,
    HEART,
    SNOWFLAKE,
    GLOW,
    HAPPY;

    public ParticleEffect effect() {
        return switch (this) {
            case CRIT -> ParticleTypes.CRIT;
            case STARS -> ParticleTypes.END_ROD;
            case SPARKS -> ParticleTypes.ELECTRIC_SPARK;
            case ENCHANT -> ParticleTypes.ENCHANTED_HIT;
            case FLAME -> ParticleTypes.FLAME;
            case SOUL -> ParticleTypes.SOUL_FIRE_FLAME;
            case TOTEM -> ParticleTypes.TOTEM_OF_UNDYING;
            case HEART -> ParticleTypes.HEART;
            case SNOWFLAKE -> ParticleTypes.SNOWFLAKE;
            case GLOW -> ParticleTypes.GLOW;
            case HAPPY -> ParticleTypes.HAPPY_VILLAGER;
        };
    }

    public ParticleStyle next() {
        ParticleStyle[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}

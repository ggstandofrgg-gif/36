package dev.kirpichvisuals.effects;

import dev.kirpichvisuals.config.KirpichConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Box;

/** Аура из частиц вокруг цели, которую ты только что ударил. Держится 3 секунды. */
public final class TargetAura {
    private static final long DURATION_TICKS = 60L;

    private static Entity target;
    private static long until;

    private TargetAura() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(TargetAura::tick);
    }

    /** Запоминает цель; вызывается при ударе. */
    public static void mark(Entity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) {
            return;
        }
        target = entity;
        until = mc.world.getTime() + DURATION_TICKS;
    }

    private static void tick(MinecraftClient mc) {
        KirpichConfig cfg = KirpichConfig.get();
        Entity t = target;
        if (t == null || !cfg.targetAura || mc.world == null || mc.player == null || mc.isPaused()) {
            return;
        }

        long now = mc.world.getTime();
        if (t.isRemoved() || !t.isAlive() || now > until) {
            target = null;
            return;
        }

        ParticleEffect effect = cfg.auraStyle.effect();
        Box box = t.getBoundingBox();
        double cx = (box.minX + box.maxX) / 2.0;
        double cz = (box.minZ + box.maxZ) / 2.0;
        double height = box.getLengthY();
        double radius = Math.max(box.getLengthX(), box.getLengthZ()) * 0.7 + 0.15;
        int count = EffectUtil.scaled(3);

        for (int i = 0; i < count; i++) {
            double offset = (Math.PI * 2.0 * i) / count;
            switch (cfg.auraMode) {
                case RING -> {
                    double a = now * 0.35 + offset;
                    EffectUtil.emit(mc.world, effect,
                            cx + Math.cos(a) * radius, box.maxY + 0.25, cz + Math.sin(a) * radius,
                            0.0, 0.0, 0.0);
                }
                case SPIRAL -> {
                    double a = now * 0.45 + offset;
                    double rise = (now * 0.06 + (double) i / count) % 1.0;
                    EffectUtil.emit(mc.world, effect,
                            cx + Math.cos(a) * radius, box.minY + rise * height, cz + Math.sin(a) * radius,
                            0.0, 0.0, 0.0);
                }
                case ORBIT -> {
                    double a = now * 0.25 + offset;
                    double y = box.minY + height * 0.5 + Math.sin(now * 0.2 + i) * height * 0.3;
                    EffectUtil.emit(mc.world, effect,
                            cx + Math.cos(a) * radius * 1.2, y, cz + Math.sin(a) * radius * 1.2,
                            0.0, 0.0, 0.0);
                }
            }
        }
    }
}

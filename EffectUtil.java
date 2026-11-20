package dev.kirpichvisuals.effects;

import dev.kirpichvisuals.config.KirpichConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.World;

/** Общие помощники для собственных эффектов частиц. */
public final class EffectUtil {
    private EffectUtil() {
    }

    /** Сколько частиц реально создавать: при низком FPS и включённой оптимизации меньше. */
    public static int scaled(int count) {
        KirpichConfig cfg = KirpichConfig.get();
        if (!cfg.optimizeParticles) {
            return Math.max(1, count);
        }
        int level = ParticleOptimizer.thinningLevel(cfg, MinecraftClient.getInstance().getCurrentFps());
        int n = level == 0 ? count : (level == 1 ? count / 2 : count / 4);
        return Math.max(1, n);
    }

    /** Создаёт собственную частицу в обход фильтра дальности и прореживания. */
    public static void emit(World world, ParticleEffect effect,
                            double x, double y, double z, double vx, double vy, double vz) {
        ParticleOptimizer.bypass = true;
        try {
            world.addParticle(effect, x, y, z, vx, vy, vz);
        } finally {
            ParticleOptimizer.bypass = false;
        }
    }
}

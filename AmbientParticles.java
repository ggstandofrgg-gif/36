package dev.kirpichvisuals.effects;

import dev.kirpichvisuals.config.KirpichConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.random.Random;

/** Лёгкие частицы, плавающие в воздухе вокруг игрока. Подпадают под оптимизацию частиц. */
public final class AmbientParticles {
    private AmbientParticles() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(AmbientParticles::tick);
    }

    private static void tick(MinecraftClient mc) {
        KirpichConfig cfg = KirpichConfig.get();
        ClientPlayerEntity player = mc.player;
        if (!cfg.ambientParticles || player == null || mc.world == null || mc.isPaused()) {
            return;
        }
        if (mc.world.getTime() % 4 != 0) {
            return;
        }

        ParticleEffect effect = cfg.ambientStyle.effect();
        Random random = mc.world.getRandom();
        for (int i = 0; i < 2; i++) {
            double x = player.getX() + (random.nextDouble() - 0.5) * 12.0;
            double y = player.getY() + random.nextDouble() * 4.0;
            double z = player.getZ() + (random.nextDouble() - 0.5) * 12.0;
            // без обхода фильтра: оптимизатор может их прореживать
            mc.world.addParticle(effect, x, y, z,
                    (random.nextDouble() - 0.5) * 0.02, 0.01 + random.nextDouble() * 0.02, (random.nextDouble() - 0.5) * 0.02);
        }
    }
}

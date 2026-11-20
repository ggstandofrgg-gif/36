package dev.kirpichvisuals.effects;

import dev.kirpichvisuals.config.KirpichConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleEffect;

/** Кольцо частиц, которое расходится от ног игрока в момент прыжка. */
public final class JumpCircle {
    private static boolean wasOnGround = true;

    private JumpCircle() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(JumpCircle::tick);
    }

    private static void tick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null) {
            wasOnGround = true;
            return;
        }
        if (mc.isPaused()) {
            return;
        }

        boolean onGround = player.isOnGround();
        boolean jumped = wasOnGround && !onGround && player.getVelocity().y > 0.1;
        wasOnGround = onGround;

        KirpichConfig cfg = KirpichConfig.get();
        if (!cfg.jumpCircle || !jumped) {
            return;
        }

        ParticleEffect effect = cfg.jumpStyle.effect();
        int points = EffectUtil.scaled(28);
        double speed = 0.22;
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0 * i) / points;
            EffectUtil.emit(mc.world, effect,
                    player.getX(), player.getY() + 0.08, player.getZ(),
                    Math.cos(angle) * speed, 0.0, Math.sin(angle) * speed);
        }
    }
}

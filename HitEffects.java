package dev.kirpichvisuals.effects;

import dev.kirpichvisuals.config.KirpichConfig;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** Эффекты при ударе и регистрация остальных эффектов. Чисто визуально, на урон не влияет. */
public final class HitEffects {
    private HitEffects() {
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // событие общее для клиента и встроенного сервера, нужна только клиентская сторона
            MinecraftClient mc = MinecraftClient.getInstance();
            if (world.isClient() && player == mc.player && entity instanceof LivingEntity) {
                TargetAura.mark(entity);
                spawn(world, entity, mc);
            }
            return ActionResult.PASS;
        });

        JumpCircle.register();
        TargetAura.register();
        AmbientParticles.register();
    }

    private static void spawn(World world, Entity target, MinecraftClient mc) {
        KirpichConfig cfg = KirpichConfig.get();
        if (mc.player == null) {
            return;
        }
        // слабые удары без заряда не украшаем, иначе спам кликами засыпает экран
        if (mc.player.getAttackCooldownProgress(0.5f) < 0.5f) {
            return;
        }

        Box box = target.getBoundingBox();
        double cx = (box.minX + box.maxX) / 2.0;
        double cy = box.minY + box.getLengthY() * 0.6;
        double cz = (box.minZ + box.maxZ) / 2.0;
        Random random = world.getRandom();

        if (cfg.hitParticles) {
            ParticleEffect effect = cfg.particleStyle.effect();
            int count = EffectUtil.scaled(cfg.particleCount);
            for (int i = 0; i < count; i++) {
                double ox = (random.nextDouble() - 0.5) * box.getLengthX();
                double oy = (random.nextDouble() - 0.5) * box.getLengthY() * 0.6;
                double oz = (random.nextDouble() - 0.5) * box.getLengthZ();
                double vx = (random.nextDouble() - 0.5) * 0.5;
                double vy = random.nextDouble() * 0.4;
                double vz = (random.nextDouble() - 0.5) * 0.5;
                EffectUtil.emit(world, effect, cx + ox, cy + oy, cz + oz, vx, vy, vz);
            }
        }

        if (cfg.hitBurst) {
            // шар частиц: равномерно по сфере, летят наружу
            ParticleEffect effect = cfg.burstStyle.effect();
            int count = EffectUtil.scaled(24);
            double speed = 0.18;
            for (int i = 0; i < count; i++) {
                double y = random.nextDouble() * 2.0 - 1.0;
                double phi = random.nextDouble() * Math.PI * 2.0;
                double r = Math.sqrt(1.0 - y * y);
                EffectUtil.emit(world, effect, cx, cy, cz,
                        r * Math.cos(phi) * speed, y * speed, r * Math.sin(phi) * speed);
            }
        }
    }
}

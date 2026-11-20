package dev.flyvisuals;

import dev.flyvisuals.config.FlyConfig;
import dev.flyvisuals.effects.HitEffects;
import dev.flyvisuals.hud.CooldownHud;
import dev.flyvisuals.hud.PotionHud;
import dev.flyvisuals.hud.TargetHud;
import dev.flyvisuals.screen.SettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlyVisualsClient implements ClientModInitializer {
    public static final String MOD_ID = "flyvisuals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding openSettingsKey;

    @Override
    public void onInitializeClient() {
        FlyConfig.get(); // загрузить конфиг сразу

        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.flyvisuals.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.categories.flyvisuals"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettingsKey.wasPressed()) {
                client.setScreen(new SettingsScreen(client.currentScreen));
            }
        });

        TargetHud targetHud = new TargetHud();
        PotionHud potionHud = new PotionHud();
        CooldownHud cooldownHud = new CooldownHud();

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            targetHud.render(context);
            potionHud.render(context);
            cooldownHud.render(context);
        });

        HitEffects.register();
        LOGGER.info("FlyVisuals загружен");
    }
}

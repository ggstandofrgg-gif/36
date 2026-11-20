package dev.kirpichvisuals;

import dev.kirpichvisuals.config.KirpichConfig;
import dev.kirpichvisuals.effects.HitEffects;
import dev.kirpichvisuals.hud.HudManager;
import dev.kirpichvisuals.screen.SettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KirpichVisualsClient implements ClientModInitializer {
    public static final String MOD_ID = "kirpichvisuals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding openSettingsKey;

    @Override
    public void onInitializeClient() {
        KirpichConfig.get(); // загрузить конфиг сразу

        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kirpichvisuals.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.categories.kirpichvisuals"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettingsKey.wasPressed()) {
                client.setScreen(new SettingsScreen(client.currentScreen));
            }
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> HudManager.renderAll(context));

        HitEffects.register();
        LOGGER.info("KirpichVisuals загружен");
    }
}

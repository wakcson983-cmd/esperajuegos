package com.esperajuegos.client;

import com.esperajuegos.ScreenStatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class EsperaJuegosClient implements ClientModInitializer {

    public static boolean enabled = false;
    public static int count = 0;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ScreenStatePayload.ID, (payload, context) -> {
            enabled = payload.enabled();
            count = payload.count();
            MinecraftClient mc = context.client();
            if (enabled) {
                if (!(mc.currentScreen instanceof WaitingScreen)) {
                    mc.setScreen(new WaitingScreen());
                }
            } else if (mc.currentScreen instanceof WaitingScreen) {
                mc.setScreen(null);
            }
        });

        // Si algo cierra la pantalla mientras está activada, se vuelve a abrir.
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (enabled && mc.player != null && mc.currentScreen == null) {
                mc.setScreen(new WaitingScreen());
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            enabled = false;
            count = 0;
            ChatLog.clear();
        });

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, timestamp) -> ChatLog.add(message));
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                ChatLog.add(message);
            }
        });
    }
}

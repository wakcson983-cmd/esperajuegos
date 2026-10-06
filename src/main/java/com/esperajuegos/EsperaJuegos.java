package com.esperajuegos;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EsperaJuegos implements ModInitializer {

    public static final String MOD_ID = "esperajuegos";
    public static final int MAX_PLAYERS = 100;

    /** Jugadores que tienen la pantalla activada. */
    private static final Set<UUID> ENABLED = ConcurrentHashMap.newKeySet();
    /** Si se activó para todos (@a), los jugadores que entren después también la reciben. */
    private static volatile boolean allMode = false;

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(ScreenStatePayload.ID, ScreenStatePayload.CODEC);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            for (String root : new String[]{"screan", "screen"}) {
                dispatcher.register(CommandManager.literal(root)
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("games")
                                .then(CommandManager.literal("players")
                                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                                .then(CommandManager.literal("on")
                                                        .executes(ctx -> run(ctx, true)))
                                                .then(CommandManager.literal("off")
                                                        .executes(ctx -> run(ctx, false)))))));
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            if (allMode) {
                ENABLED.add(player.getUuid());
            }
            if (ENABLED.contains(player.getUuid())) {
                ServerPlayNetworking.send(player,
                        new ScreenStatePayload(true, server.getPlayerManager().getCurrentPlayerCount()));
            }
            broadcastCount(server, null);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity leaving = handler.player;
            ENABLED.remove(leaving.getUuid());
            broadcastCount(server, leaving);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ENABLED.clear();
            allMode = false;
        });
    }

    private static int run(CommandContext<ServerCommandSource> ctx, boolean on) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(ctx, "targets");
        MinecraftServer server = ctx.getSource().getServer();
        int total = server.getPlayerManager().getCurrentPlayerCount();

        // Si el selector alcanzó a todos los jugadores conectados (@a), se recuerda para los que entren después.
        if (targets.size() >= total) {
            allMode = on;
        }

        for (ServerPlayerEntity player : targets) {
            if (on) {
                ENABLED.add(player.getUuid());
            } else {
                ENABLED.remove(player.getUuid());
            }
            ServerPlayNetworking.send(player, new ScreenStatePayload(on, total));
        }

        final int n = targets.size();
        ctx.getSource().sendFeedback(() -> Text.literal(
                "Pantalla de minijuegos " + (on ? "activada" : "desactivada") + " para " + n + " jugador(es)."), true);
        return n;
    }

    /** Envía el contador actualizado a todos los que tienen la pantalla abierta. */
    private static void broadcastCount(MinecraftServer server, ServerPlayerEntity leaving) {
        int count = server.getPlayerManager().getCurrentPlayerCount() - (leaving != null ? 1 : 0);
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (p != leaving && ENABLED.contains(p.getUuid())) {
                ServerPlayNetworking.send(p, new ScreenStatePayload(true, Math.max(0, count)));
            }
        }
    }
}

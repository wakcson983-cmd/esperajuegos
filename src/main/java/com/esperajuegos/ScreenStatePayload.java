package com.esperajuegos;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Servidor -> cliente: activa/desactiva la pantalla y envía el número de jugadores conectados. */
public record ScreenStatePayload(boolean enabled, int count) implements CustomPayload {

    public static final CustomPayload.Id<ScreenStatePayload> ID =
            new CustomPayload.Id<>(Identifier.of(EsperaJuegos.MOD_ID, "screen_state"));

    public static final PacketCodec<RegistryByteBuf, ScreenStatePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, ScreenStatePayload::enabled,
            PacketCodecs.INTEGER, ScreenStatePayload::count,
            ScreenStatePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}

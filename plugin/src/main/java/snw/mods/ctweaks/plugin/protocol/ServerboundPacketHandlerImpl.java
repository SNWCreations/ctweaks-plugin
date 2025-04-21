package snw.mods.ctweaks.plugin.protocol;

import lombok.RequiredArgsConstructor;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.handler.ServerboundPacketHandler;

@RequiredArgsConstructor
public class ServerboundPacketHandlerImpl implements ServerboundPacketHandler {
    private final ServerPlayer owner;

    public void close() {
    }
}

package snw.mods.ctweaks.plugin.protocol;

import lombok.RequiredArgsConstructor;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.handler.ServerboundPacketHandler;
import snw.mods.ctweaks.protocol.packet.c2s.ServerboundWindowPropertiesPacket;

import static snw.mods.ctweaks.plugin.util.Logging.debug;

@RequiredArgsConstructor
public class ServerboundPacketHandlerImpl implements ServerboundPacketHandler {
    private final ServerPlayer owner;

    public void close() {
    }

    @Override
    public void handleWindowProperties(ServerboundWindowPropertiesPacket packet) {
        debug(() -> "Updating screen properties of player " + owner.getHandle().getName());
        owner.getScreen().update(packet);
    }
}

package snw.mods.ctweaks.plugin.net;

import lombok.RequiredArgsConstructor;
import snw.mods.ctweaks.plugin.event.PlayerModReadyEvent;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.handler.ServerboundPacketHandler;
import snw.mods.ctweaks.protocol.packet.c2s.ServerboundReadyPacket;
import snw.mods.ctweaks.protocol.packet.c2s.ServerboundWindowPropertiesPacket;

import static snw.mods.ctweaks.plugin.util.Logging.debug;

@RequiredArgsConstructor
public class ServerboundPacketHandlerImpl implements ServerboundPacketHandler {
    private final ServerPlayer owner;

    public void close() {
    }

    @Override
    public void handleReady(ServerboundReadyPacket packet) {
        if (!owner.modReady) {
            owner.modReady = true;
            new PlayerModReadyEvent(owner).callEvent();
        } else {
            debug(() -> "Client sent ready packet while it is in ready state");
        }
    }

    @Override
    public void handleWindowProperties(ServerboundWindowPropertiesPacket packet) {
        debug(() -> "Updating screen properties of player " + owner.getHandle().getName());
        owner.getScreen().update(packet);
    }
}

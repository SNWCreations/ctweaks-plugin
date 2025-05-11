package snw.mods.ctweaks.plugin.net;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import snw.mods.ctweaks.object.IntKeyed;
import snw.mods.ctweaks.object.pos.PlanePosition;
import snw.mods.ctweaks.plugin.event.PlayerModReadyEvent;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.handler.ServerboundPacketHandler;
import snw.mods.ctweaks.protocol.packet.c2s.ServerboundObjectUpdatedPacket;
import snw.mods.ctweaks.protocol.packet.c2s.ServerboundReadyPacket;
import snw.mods.ctweaks.protocol.packet.c2s.ServerboundSetObjectPlanePosPacket;
import snw.mods.ctweaks.protocol.packet.c2s.ServerboundWindowPropertiesPacket;

import java.util.HashMap;
import java.util.Map;

import static snw.mods.ctweaks.plugin.util.Logging.debug;

@Slf4j
@RequiredArgsConstructor
public class ServerboundPacketHandlerImpl implements ServerboundPacketHandler {
    private final ServerPlayer owner;
    private final Map<String, Runnable> afterUpdateCallbacks = new HashMap<>();

    public void registerAfterUpdateCallback(String nonce, Runnable callback) {
        afterUpdateCallbacks.put(nonce, callback);
    }

    public void close() {
        afterUpdateCallbacks.clear();
    }

    @Override
    public void handleObjectUpdated(ServerboundObjectUpdatedPacket packet) {
        String nonce = packet.getNonce();
        Runnable callback = afterUpdateCallbacks.remove(nonce);
        if (callback != null) {
            callback.run();
        } else {
            log.error("Could not find a callback which is bound to nonce {}", nonce);
        }
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
    public void handleSetPlanePos(ServerboundSetObjectPlanePosPacket packet) {
        IntKeyed.Descriptor descriptor = packet.getTargetDescriptor();
        IntKeyed target = owner.getScreen().lookupObject(descriptor);
        if (target instanceof PlanePosition.Setter setter) {
            setter.setPosition(packet.getNewPosition());
        } else {
            log.error("Attempted to update plane position of an object ({}) which does not have plane position", descriptor);
        }
    }

    @Override
    public void handleWindowProperties(ServerboundWindowPropertiesPacket packet) {
        debug(() -> "Updating screen properties of player " + owner.getHandle().getName());
        owner.getScreen().update(packet);
    }
}

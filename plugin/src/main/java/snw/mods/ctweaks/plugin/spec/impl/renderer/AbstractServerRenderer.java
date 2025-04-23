package snw.mods.ctweaks.plugin.spec.impl.renderer;

import lombok.Getter;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundRemoveRendererPacket;
import snw.mods.ctweaks.render.Renderer;

import static snw.lib.protocol.util.PacketHelper.newNonce;
import static snw.mods.ctweaks.plugin.util.Logging.debug;

public abstract class AbstractServerRenderer implements Renderer {
    @Getter
    private final int id;
    protected final ServerPlayer owner;
    private boolean removed;

    protected AbstractServerRenderer(ServerPlayer owner, int id) {
        this.owner = owner;
        this.id = id;
    }

    @Override
    public void remove() {
        if (!removed) {
            removed = true;
            owner.sendPacket(() -> new ClientboundRemoveRendererPacket(this.id, newNonce()));
            debug(() -> "Removed renderer with ID " + this.id);
        } else {
            debug(() -> "Attempted to remove a dead renderer with ID " + this.id);
        }
    }

    public abstract void sendAdditionalAddPackets();

    public abstract void sendFullUpdate();
}

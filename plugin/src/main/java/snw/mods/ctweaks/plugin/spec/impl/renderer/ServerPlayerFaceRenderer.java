package snw.mods.ctweaks.plugin.spec.impl.renderer;

import lombok.Getter;
import lombok.NonNull;
import snw.mods.ctweaks.object.pos.PlanePosition;
import snw.mods.ctweaks.plugin.spec.impl.AbstractUpdater;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundUpdatePlayerFaceRendererPacket;
import snw.mods.ctweaks.render.PlayerFaceRenderer;

import java.util.UUID;

import static java.util.Objects.requireNonNullElse;
import static snw.lib.protocol.util.PacketHelper.newNonce;

@Getter
public class ServerPlayerFaceRenderer extends AbstractServerRenderer implements PlayerFaceRenderer {
    private UUID target;
    private PlanePosition position;
    private int size;

    public ServerPlayerFaceRenderer(ServerPlayer owner, int id, UUID target, PlanePosition position, int size) {
        super(owner, id);
        this.target = target;
        this.position = position;
        this.size = size;
    }

    @Override
    public void sendAdditionalAddPackets() {
        sendFullUpdate();
    }

    @Override
    public void sendFullUpdate() {
        owner.sendPacket(() -> new ClientboundUpdatePlayerFaceRendererPacket(getId(), target, position, size, newNonce()));
    }

    @Override
    public Updater newUpdater() {
        return new UpdaterImpl();
    }

    class UpdaterImpl extends AbstractUpdater implements Updater {
        private UUID target;
        private PlanePosition position;
        private Integer size;

        @Override
        public Updater setTarget(@NonNull UUID target) {
            this.target = target;
            return this;
        }

        @Override
        public Updater setPosition(@NonNull PlanePosition position) {
            this.position = position;
            return this;
        }

        @Override
        public Updater setSize(int size) {
            this.size = size;
            return this;
        }

        @Override
        public void update() throws IllegalStateException {
            super.update();
            ServerPlayerFaceRenderer.this.target = requireNonNullElse(this.target, ServerPlayerFaceRenderer.this.target);
            ServerPlayerFaceRenderer.this.position = requireNonNullElse(this.position, ServerPlayerFaceRenderer.this.position);
            ServerPlayerFaceRenderer.this.size = requireNonNullElse(this.size, ServerPlayerFaceRenderer.this.size);
            owner.sendPacket(() -> new ClientboundUpdatePlayerFaceRendererPacket(
                    getId(),
                    ServerPlayerFaceRenderer.this.target,
                    ServerPlayerFaceRenderer.this.position,
                    ServerPlayerFaceRenderer.this.size,
                    newNonce()
            ));
        }
    }
}

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
    private float scale = 1.0F;

    public ServerPlayerFaceRenderer(ServerPlayer owner, int id, UUID target, PlanePosition position, float scale) {
        super(owner, id);
        this.target = target;
        this.position = position;
        this.scale = scale;
    }

    @Override
    public void sendAdditionalAddPackets() {
        sendFullUpdate();
    }

    @Override
    public void sendFullUpdate() {
        owner.sendPacket(() -> new ClientboundUpdatePlayerFaceRendererPacket(getId(), target, position, scale, newNonce()));
    }

    @Override
    public Updater newUpdater() {
        return new UpdaterImpl();
    }

    class UpdaterImpl extends AbstractUpdater implements Updater {
        private UUID target;
        private PlanePosition position;
        private Float scale;

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
        public Updater setScale(float scale) {
            this.scale = scale;
            return this;
        }

        @Override
        public void update() throws IllegalStateException {
            super.update();
            ServerPlayerFaceRenderer.this.target = requireNonNullElse(this.target, ServerPlayerFaceRenderer.this.target);
            ServerPlayerFaceRenderer.this.position = requireNonNullElse(this.position, ServerPlayerFaceRenderer.this.position);
            ServerPlayerFaceRenderer.this.scale = requireNonNullElse(this.scale, ServerPlayerFaceRenderer.this.scale);
            owner.sendPacket(() -> new ClientboundUpdatePlayerFaceRendererPacket(
                    getId(),
                    ServerPlayerFaceRenderer.this.target,
                    ServerPlayerFaceRenderer.this.position,
                    ServerPlayerFaceRenderer.this.scale,
                    newNonce()
            ));
        }
    }
}

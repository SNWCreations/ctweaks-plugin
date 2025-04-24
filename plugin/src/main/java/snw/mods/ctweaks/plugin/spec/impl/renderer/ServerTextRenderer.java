package snw.mods.ctweaks.plugin.spec.impl.renderer;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import snw.mods.ctweaks.object.pos.PlanePosition;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundUpdateTextRendererPacket;
import snw.mods.ctweaks.render.TextRenderer;

import java.util.Objects;

import static snw.lib.protocol.util.PacketHelper.newNonce;

@ToString
@Getter
public class ServerTextRenderer extends AbstractServerRenderer implements TextRenderer {
    private PlanePosition position;
    private Component text;

    public ServerTextRenderer(ServerPlayer owner, int id, PlanePosition position) {
        super(owner, id);
        this.position = position;
    }

    @Override
    public Updater newUpdater() {
        return new UpdaterImpl();
    }

    @Override
    public void sendAdditionalAddPackets() {
        owner.sendPacket(() -> new ClientboundUpdateTextRendererPacket(getId(), null, position, newNonce()));
    }

    @Override
    public void sendFullUpdate() {
        owner.sendPacket(() -> new ClientboundUpdateTextRendererPacket(getId(), this.text, position, newNonce()));
    }

    class UpdaterImpl implements Updater {
        private boolean used;
        private Component text;
        private PlanePosition position;

        @Override
        public Updater setText(Component text) {
            this.text = text;
            return this;
        }

        @Override
        public Updater setPosition(@NonNull PlanePosition position) {
            this.position = position;
            return this;
        }

        @Override
        public void update() throws IllegalStateException {
            Preconditions.checkState(!used, "Updates from this updater was already applied");
            used = true;
            ServerTextRenderer.this.text = Objects.requireNonNullElse(this.text, ServerTextRenderer.this.text);
            ServerTextRenderer.this.position = Objects.requireNonNullElse(this.position, ServerTextRenderer.this.position);
            owner.sendPacket(() -> new ClientboundUpdateTextRendererPacket(getId(), text, position, newNonce()));
        }
    }
}

package snw.mods.ctweaks.plugin.spec.impl.renderer;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import snw.mods.ctweaks.object.pos.PlanePosition;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundUpdateTextRendererPacket;
import snw.mods.ctweaks.render.TextRenderer;

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

    class UpdaterImpl implements Updater {
        private boolean used;
        @Setter
        private Component text;
        @Setter
        private @NonNull PlanePosition position;

        @Override
        public void update() throws IllegalStateException {
            Preconditions.checkState(!used, "Updates from this updater was already applied");
            used = true;
            if (text != null) {
                ServerTextRenderer.this.text = text;
            }
            ServerTextRenderer.this.position = position;
            owner.sendPacket(() -> new ClientboundUpdateTextRendererPacket(getId(), text, position, newNonce()));
        }
    }
}

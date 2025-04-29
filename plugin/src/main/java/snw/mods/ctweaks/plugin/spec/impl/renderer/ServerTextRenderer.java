package snw.mods.ctweaks.plugin.spec.impl.renderer;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import snw.mods.ctweaks.object.pos.PlanePosition;
import snw.mods.ctweaks.plugin.spec.impl.AbstractUpdater;
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
    private float scale;
    private boolean noShadow;

    public ServerTextRenderer(ServerPlayer owner, int id, PlanePosition position, @Nullable Component text, float scale) {
        super(owner, id);
        this.position = position;
        this.text = text;
        this.scale = scale;
    }

    @Override
    public Updater newUpdater() {
        return new UpdaterImpl();
    }

    @Override
    public void sendAdditionalAddPackets() {
        sendFullUpdate();
    }

    @Override
    public void sendFullUpdate() {
        owner.sendPacket(() -> new ClientboundUpdateTextRendererPacket(getId(), text, position, noShadow, scale, newNonce()));
    }

    class UpdaterImpl extends AbstractUpdater implements Updater {
        private Component text;
        private PlanePosition position;
        private Float scale;
        private Boolean noShadow;

        @Override
        public Updater setText(Component text) {
            ensureNotApplied();
            this.text = text;
            return this;
        }

        @Override
        public Updater setPosition(@NonNull PlanePosition position) {
            ensureNotApplied();
            this.position = position;
            return this;
        }

        @Override
        public Updater setNoShadow(boolean noShadow) {
            this.noShadow = noShadow;
            return this;
        }

        @Override
        public Updater setScale(float scale) {
            ensureNotApplied();
            this.scale = scale;
            return this;
        }

        @Override
        public void update() throws IllegalStateException {
            super.update();
            ServerTextRenderer.this.text = Objects.requireNonNullElse(this.text, ServerTextRenderer.this.text);
            ServerTextRenderer.this.position = Objects.requireNonNullElse(this.position, ServerTextRenderer.this.position);
            ServerTextRenderer.this.noShadow = Objects.requireNonNullElse(this.noShadow, ServerTextRenderer.this.noShadow);
            ServerTextRenderer.this.scale = Objects.requireNonNullElse(this.scale, ServerTextRenderer.this.scale);
            owner.sendPacket(() -> new ClientboundUpdateTextRendererPacket(
                    getId(),
                    this.text,
                    this.position,
                    this.noShadow,
                    this.scale,
                    newNonce()
            ));
        }
    }
}

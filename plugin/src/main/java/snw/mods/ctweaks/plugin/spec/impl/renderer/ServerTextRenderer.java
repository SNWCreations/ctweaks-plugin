package snw.mods.ctweaks.plugin.spec.impl.renderer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import snw.mods.ctweaks.object.pos.PlanePosition;
import snw.mods.ctweaks.plugin.spec.PlanePosInternalSetter;
import snw.mods.ctweaks.plugin.spec.impl.AbstractUpdater;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundUpdateTextRendererPacket;
import snw.mods.ctweaks.render.TextRenderer;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import static snw.lib.protocol.util.PacketHelper.newNonce;
import static snw.mods.ctweaks.ModConstants.UNIT_AS_INT;

@ToString
@Getter
public class ServerTextRenderer extends AbstractServerRenderer implements TextRenderer, PlanePosInternalSetter {
    private PlanePosition position;
    private Component text;
    private float scale = 1.0F;
    private boolean noShadow;
    private int outlineColor = UNIT_AS_INT;

    public ServerTextRenderer(ServerPlayer owner, int id) {
        super(owner, id);
    }

    public ServerTextRenderer(ServerPlayer owner, int id, PlanePosition position, Component text) {
        this(owner, id);
        this.position = position;
        this.text = text;
    }

    @Override
    public Updater newUpdater() {
        return new UpdaterImpl();
    }

    @Override
    public String sendFullUpdate() {
        String nonce = newNonce();
        owner.sendPacket(() -> new ClientboundUpdateTextRendererPacket(getId(), text, position, noShadow, scale, outlineColor, nonce));
        return nonce;
    }

    @Override
    public void setPosition(PlanePosition position) {
        newUpdater().setPosition(position).update();
    }

    @Override
    public void setPositionInternal(PlanePosition position) {
        this.position = position;
    }

    class UpdaterImpl extends AbstractUpdater implements Updater {
        private Component text;
        private PlanePosition position;
        private Float scale;
        private Boolean noShadow;
        private Integer outlineColor;

        @Override
        public Updater setText(Component text) {
            ensureNotApplied();
            this.text = text;
            return this;
        }

        @Override
        public Updater setPosition(PlanePosition position) {
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
        public Updater setOutlineColor(int outlineColor) {
            ensureNotApplied();
            this.outlineColor = outlineColor;
            return this;
        }

        @Override
        public void update() throws IllegalStateException {
            super.update();
            ServerTextRenderer.this.text = Objects.requireNonNullElse(this.text, ServerTextRenderer.this.text);
            ServerTextRenderer.this.position = this.position;
            ServerTextRenderer.this.noShadow = Objects.requireNonNullElse(this.noShadow, ServerTextRenderer.this.noShadow);
            ServerTextRenderer.this.scale = Objects.requireNonNullElse(this.scale, ServerTextRenderer.this.scale);
            ServerTextRenderer.this.outlineColor = Objects.requireNonNullElse(this.outlineColor, ServerTextRenderer.this.outlineColor);
            owner.sendPacket(() -> new ClientboundUpdateTextRendererPacket(
                    getId(),
                    this.text,
                    this.position,
                    this.noShadow,
                    this.scale,
                    this.outlineColor,
                    newNonce()
            ));
        }
    }

    @RequiredArgsConstructor
    public static class BuilderImpl implements Builder {
        private final ServerPlayer owner;
        private final IntSupplier idGetter;
        private final Consumer<ServerTextRenderer> buildCallback;

        private @Nullable Component text;
        private PlanePosition position;
        private boolean noShadow;
        private @Nullable Integer outlineColor;
        private float scale = 1.0F;

        @Override
        public TextRenderer build() {
            owner.ensureOnline();

            ServerTextRenderer result = new ServerTextRenderer(owner, idGetter.getAsInt());
            result.text = this.text;
            result.position = this.position;
            result.noShadow = this.noShadow;
            result.outlineColor = Objects.requireNonNullElse(this.outlineColor, UNIT_AS_INT);
            result.scale = this.scale;

            buildCallback.accept(result);
            return result;
        }

        @Override
        public Builder setText(Component text) {
            this.text = text;
            return this;
        }

        @Override
        public Builder setPosition(PlanePosition position) {
            this.position = position;
            return this;
        }

        @Override
        public Builder setNoShadow(boolean noShadow) {
            this.noShadow = noShadow;
            return this;
        }

        @Override
        public Builder setOutlineColor(int outlineColor) {
            this.outlineColor = outlineColor;
            return this;
        }

        @Override
        public Builder setScale(float scale) {
            this.scale = scale;
            return this;
        }
    }
}

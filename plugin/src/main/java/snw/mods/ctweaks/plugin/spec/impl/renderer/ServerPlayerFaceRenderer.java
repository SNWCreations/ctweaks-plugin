package snw.mods.ctweaks.plugin.spec.impl.renderer;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import snw.mods.ctweaks.object.pos.PlanePosition;
import snw.mods.ctweaks.plugin.spec.impl.AbstractUpdater;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundUpdatePlayerFaceRendererPacket;
import snw.mods.ctweaks.render.PlayerFaceRenderer;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import static java.util.Objects.requireNonNullElse;
import static snw.lib.protocol.util.PacketHelper.newNonce;

@Getter
public class ServerPlayerFaceRenderer extends AbstractServerRenderer implements PlayerFaceRenderer {
    private UUID target;
    private @Nullable PlanePosition position;
    private int size = 12;

    public ServerPlayerFaceRenderer(ServerPlayer owner, int id) {
        super(owner, id);
    }

    public ServerPlayerFaceRenderer(ServerPlayer owner, int id, UUID target, @Nullable PlanePosition position) {
        this(owner, id);
        this.target = target;
        this.position = position;
    }

    @Override
    public String sendFullUpdate() {
        String nonce = newNonce();
        owner.sendPacket(() -> new ClientboundUpdatePlayerFaceRendererPacket(getId(), target, position, size, nonce));
        return nonce;
    }

    @Override
    public Updater newUpdater() {
        return new UpdaterImpl();
    }

    private static void validateSize(int size) throws IllegalArgumentException {
        Preconditions.checkArgument(size > 0 && size % 12 == 0, "size must be positive and multiply of 12");
    }

    @Override
    public void setPosition(PlanePosition position) {
        newUpdater().setPosition(position).update();
    }

    class UpdaterImpl extends AbstractUpdater implements Updater {
        private UUID target;
        private @Nullable PlanePosition position;
        private Integer size;

        @Override
        public Updater setTarget(@NonNull UUID target) {
            this.target = target;
            return this;
        }

        @Override
        public Updater setPosition(@Nullable PlanePosition position) {
            this.position = position;
            return this;
        }

        @Override
        public Updater setSize(int size) {
            validateSize(size);
            this.size = size;
            return this;
        }

        @Override
        public void update() throws IllegalStateException {
            super.update();
            ServerPlayerFaceRenderer.this.target = requireNonNullElse(this.target, ServerPlayerFaceRenderer.this.target);
            ServerPlayerFaceRenderer.this.position = this.position;
            ServerPlayerFaceRenderer.this.size = requireNonNullElse(this.size, ServerPlayerFaceRenderer.this.size);
            owner.sendPacket(() -> new ClientboundUpdatePlayerFaceRendererPacket(
                    getId(),
                    this.target,
                    this.position,
                    this.size,
                    newNonce()
            ));
        }
    }

    @RequiredArgsConstructor
    public static class BuilderImpl implements Builder {
        private final ServerPlayer owner;
        private final IntSupplier idGetter;
        private final Consumer<ServerPlayerFaceRenderer> buildCallback;

        private UUID target;
        private @Nullable PlanePosition position;
        private @Nullable Integer size;

        @Override
        public PlayerFaceRenderer build() {
            owner.ensureOnline();
            Preconditions.checkNotNull(target, "target cannot be null");

            ServerPlayerFaceRenderer result = new ServerPlayerFaceRenderer(owner, idGetter.getAsInt());
            result.target = this.target;
            result.position = this.position;
            result.size = Objects.requireNonNullElse(this.size, result.size);

            buildCallback.accept(result);
            return result;
        }

        @Override
        public Builder setTarget(@NonNull UUID target) {
            this.target = target;
            return this;
        }

        @Override
        public Builder setPosition(@Nullable PlanePosition position) {
            this.position = position;
            return this;
        }

        @Override
        public Builder setSize(int size) {
            validateSize(size);
            this.size = size;
            return this;
        }
    }
}

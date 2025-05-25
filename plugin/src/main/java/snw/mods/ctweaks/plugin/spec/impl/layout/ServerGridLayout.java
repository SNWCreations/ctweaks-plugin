package snw.mods.ctweaks.plugin.spec.impl.layout;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import snw.lib.protocol.packet.Packet;
import snw.mods.ctweaks.object.pos.PlanePosition;
import snw.mods.ctweaks.object.range.Rectangle;
import snw.mods.ctweaks.plugin.spec.impl.AbstractUpdater;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.handler.ClientboundPacketHandler;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundUpdateGridLayoutPacket;
import snw.mods.ctweaks.render.layout.GridLayout;
import snw.mods.ctweaks.render.layout.LayoutElement;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntSupplier;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;
import static snw.lib.protocol.util.PacketHelper.newNonce;
import static snw.mods.ctweaks.plugin.net.ServerPacketHelper.mapToDescriptor;
import static snw.mods.ctweaks.plugin.util.Checker.ensureCanAddLayoutElement;

@Getter
public class ServerGridLayout extends AbstractServerLayout implements GridLayout {
    private PlanePosition position;
    private int rowSpacing;
    private int columnSpacing;
    private int rowCount = 1;
    private int columnCount = 1;
    private @Nullable Rectangle range;

    public ServerGridLayout(ServerPlayer owner, int id) {
        super(owner, id);
    }

    @Override
    protected Packet<ClientboundPacketHandler> createChildrenClearedPacket() {
        return new ClientboundUpdateGridLayoutPacket(getId(), emptyList(),
                null, null, null, null, null, null, newNonce());
    }

    @Override
    public String sendFullUpdate() {
        String nonce = newNonce();
        owner.sendPacket(() -> new ClientboundUpdateGridLayoutPacket(
                getId(),
                mapToDescriptor(this.children),
                this.position,
                Optional.ofNullable(this.range),
                this.rowSpacing,
                this.columnSpacing,
                this.rowCount,
                this.columnCount,
                nonce
        ));
        return nonce;
    }

    @Override
    public GridLayout.Updater newUpdater() {
        return new UpdaterImpl();
    }

    @Override
    public void setPosition(PlanePosition position) {
        newUpdater().setPosition(position).update();
    }

    public class UpdaterImpl extends AbstractUpdater implements GridLayout.Updater {
        private @Nullable List<LayoutElement> updatedChildren;
        private @Nullable Integer rowSpacing;
        private @Nullable Integer columnSpacing;
        private @Nullable Integer rowCount;
        private @Nullable Integer columnCount;
        private @Nullable PlanePosition position;
        private @Nullable Rectangle range;
        private @Nullable Runnable afterUpdateCallback;

        private List<LayoutElement> getUpdatedChildren() {
            if (updatedChildren == null) {
                List<LayoutElement> knownChildren = ServerGridLayout.this.children;
                updatedChildren = new ArrayList<>(Objects.requireNonNullElseGet(knownChildren, Collections::emptyList));
            }
            return updatedChildren;
        }

        @Override
        public GridLayout.Updater removeChild(int index) {
            getUpdatedChildren().remove(index);
            return this;
        }

        @Override
        public GridLayout.Updater removeChild(LayoutElement element) {
            getUpdatedChildren().remove(element);
            return this;
        }

        @Override
        public GridLayout.Updater addChild(LayoutElement element) {
            ensureCanAddLayoutElement(ServerGridLayout.this, element);
            getUpdatedChildren().add(element);
            return this;
        }

        @Override
        public GridLayout.Updater addChild(int index, LayoutElement element) {
            ensureCanAddLayoutElement(ServerGridLayout.this, element);
            getUpdatedChildren().add(index, element);
            return this;
        }

        @Override
        public GridLayout.Updater setChild(int index, LayoutElement element) {
            ensureCanAddLayoutElement(ServerGridLayout.this, element);
            getUpdatedChildren().set(index, element);
            return this;
        }

        @Override
        public GridLayout.Updater setRowSpacing(@Range(from = 0, to = Integer.MAX_VALUE) int rowSpacing) {
            this.rowSpacing = rowSpacing;
            return this;
        }

        @Override
        public GridLayout.Updater setColumnSpacing(@Range(from = 0, to = Integer.MAX_VALUE) int columnSpacing) {
            this.columnSpacing = columnSpacing;
            return this;
        }

        @Override
        public GridLayout.Updater setRowCount(@Range(from = 1, to = Integer.MAX_VALUE) int rowCount) {
            this.rowCount = rowCount;
            return this;
        }

        @Override
        public GridLayout.Updater setColumnCount(@Range(from = 1, to = Integer.MAX_VALUE) int columnCount) {
            this.columnCount = columnCount;
            return this;
        }

        @Override
        public GridLayout.Updater setPosition(PlanePosition position) {
            this.position = position;
            return this;
        }

        @Override
        public GridLayout.Updater setRange(@Nullable Rectangle range) {
            this.range = range;
            return this;
        }

        @Override
        public GridLayout.Updater setAfterUpdateCallback(Runnable callback) {
            this.afterUpdateCallback = callback;
            return this;
        }

        @Override
        public void update() throws IllegalStateException {
            super.update();
            ServerGridLayout.this.children = requireNonNullElse(this.updatedChildren, ServerGridLayout.this.children);
            ServerGridLayout.this.rowSpacing = requireNonNullElse(this.rowSpacing, ServerGridLayout.this.rowSpacing);
            ServerGridLayout.this.columnSpacing = requireNonNullElse(this.columnSpacing, ServerGridLayout.this.columnSpacing);
            ServerGridLayout.this.rowCount = requireNonNullElse(this.rowCount, ServerGridLayout.this.rowCount);
            ServerGridLayout.this.columnCount = requireNonNullElse(this.columnCount, ServerGridLayout.this.columnCount);
            ServerGridLayout.this.position = requireNonNullElse(this.position, ServerGridLayout.this.position);
            ServerGridLayout.this.range = this.range;
            String nonce = newNonce();
            owner.sendPacket(() -> new ClientboundUpdateGridLayoutPacket(
                    getId(),
                    mapToDescriptor(this.updatedChildren),
                    this.position,
                    this.range == null ? null : Optional.of(this.range),
                    this.rowSpacing, this.columnSpacing, this.rowCount, this.columnCount, nonce));
            if (this.afterUpdateCallback != null) {
                owner.registerAfterUpdateCallback(nonce, this.afterUpdateCallback);
            }
        }
    }

    public static class BuilderImpl implements GridLayout.Builder {
        private final ServerPlayer owner;
        private final IntSupplier idProvider;
        private final Function<ServerGridLayout, String> builtCallback;
        private @Nullable List<LayoutElement> elements;
        private @Nullable Integer rowSpacing;
        private @Nullable Integer columnSpacing;
        private @Nullable Integer rowCount;
        private @Nullable Integer columnCount;
        private PlanePosition position;
        private @Nullable Rectangle range;
        private @Nullable Runnable afterUpdateCallback;

        public BuilderImpl(ServerPlayer owner, IntSupplier idProvider, Function<ServerGridLayout, String> builtCallback) {
            this.owner = owner;
            this.idProvider = idProvider;
            this.builtCallback = builtCallback;
        }

        private List<LayoutElement> getElements() {
            return requireNonNullElseGet(elements, () -> elements = new ArrayList<>());
        }

        @Override
        public GridLayout build() {
            owner.ensureOnline();
            Preconditions.checkNotNull(position, "position cannot be null");

            ServerGridLayout result = new ServerGridLayout(owner, idProvider.getAsInt());
            result.children = this.elements;
            result.rowSpacing = requireNonNullElse(this.rowSpacing, result.rowSpacing);
            result.columnSpacing = requireNonNullElse(this.columnSpacing, result.columnSpacing);
            result.rowCount = requireNonNullElse(this.rowCount, result.rowCount);
            result.columnCount = requireNonNullElse(this.columnCount, result.columnCount);
            result.position = this.position;
            result.range = this.range;
            String nonce = builtCallback.apply(result);
            if (this.afterUpdateCallback != null) {
                owner.registerAfterUpdateCallback(nonce, this.afterUpdateCallback);
            }
            return result;
        }

        @Override
        public GridLayout.Builder addChild(LayoutElement element) {
            getElements().add(element);
            return this;
        }

        @Override
        public GridLayout.Builder addChild(int index, LayoutElement element) {
            getElements().add(index, element);
            return this;
        }

        @Override
        public GridLayout.Builder setChild(int index, LayoutElement element) {
            getElements().set(index, element);
            return this;
        }

        @Override
        public GridLayout.Builder setRowSpacing(@Range(from = 0, to = Integer.MAX_VALUE) int rowSpacing) {
            this.rowSpacing = rowSpacing;
            return this;
        }

        @Override
        public GridLayout.Builder setColumnSpacing(@Range(from = 0, to = Integer.MAX_VALUE) int columnSpacing) {
            this.columnSpacing = columnSpacing;
            return this;
        }

        @Override
        public GridLayout.Builder setRowCount(@Range(from = 1, to = Integer.MAX_VALUE) int rowCount) {
            this.rowCount = rowCount;
            return this;
        }

        @Override
        public GridLayout.Builder setColumnCount(@Range(from = 1, to = Integer.MAX_VALUE) int columnCount) {
            this.columnCount = columnCount;
            return this;
        }

        @Override
        public GridLayout.Builder setPosition(@NonNull PlanePosition position) {
            this.position = position;
            return this;
        }

        @Override
        public GridLayout.Builder setRange(@Nullable Rectangle range) {
            this.range = range;
            return this;
        }

        @Override
        public GridLayout.Builder setAfterUpdateCallback(Runnable callback) {
            this.afterUpdateCallback = callback;
            return this;
        }
    }
}

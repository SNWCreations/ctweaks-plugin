package snw.mods.ctweaks.plugin.spec.impl.layout;

import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import snw.lib.protocol.packet.Packet;
import snw.mods.ctweaks.plugin.spec.ServerSideObject;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.handler.ClientboundPacketHandler;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundArrangeLayoutPacket;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundRemoveLayoutPacket;
import snw.mods.ctweaks.render.layout.Layout;
import snw.mods.ctweaks.render.layout.LayoutElement;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static snw.lib.protocol.util.PacketHelper.newNonce;
import static snw.mods.ctweaks.plugin.util.Logging.debug;

public abstract class AbstractServerLayout implements Layout, ServerSideObject {
    protected final ServerPlayer owner;
    @Getter
    private final int id;
    protected @Nullable List<LayoutElement> children;
    private boolean removed;

    protected AbstractServerLayout(ServerPlayer owner, int id) {
        this.owner = owner;
        this.id = id;
    }

    @Override
    public void arrangeElements(@Nullable Runnable onFinish) {
        if (!removed) {
            String nonce = newNonce();
            owner.sendPacket(() -> new ClientboundArrangeLayoutPacket(describe(), nonce));
            owner.registerAfterUpdateCallback(nonce, onFinish);
        }
    }

    @Override
    public List<LayoutElement> getChildren() {
        return children == null ? Collections.emptyList() : Collections.unmodifiableList(children);
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        if (children != null) {
            children.forEach(visitor);
        }
    }

    @Override
    @MustBeInvokedByOverriders
    public void clear() {
        if (children != null) {
            children = null;
            owner.sendPacket(this::createChildrenClearedPacket);
        }
    }

    protected abstract Packet<ClientboundPacketHandler> createChildrenClearedPacket();

    @Override
    public void remove() {
        if (!removed) {
            removed = true;
            owner.getScreen().removeLayout(this);
            owner.sendPacket(() -> new ClientboundRemoveLayoutPacket(this.id, newNonce()));
            debug(() -> "Removed layout with ID " + this.id);
        } else {
            debug(() -> "Attempted to remove a dead layout with ID " + this.id);
        }
    }
}

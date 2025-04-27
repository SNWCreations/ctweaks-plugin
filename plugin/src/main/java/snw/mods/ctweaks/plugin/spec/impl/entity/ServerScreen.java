package snw.mods.ctweaks.plugin.spec.impl.entity;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;
import snw.mods.ctweaks.entity.Screen;
import snw.mods.ctweaks.object.pos.PlanePosition;
import snw.mods.ctweaks.plugin.spec.impl.renderer.AbstractServerRenderer;
import snw.mods.ctweaks.plugin.spec.impl.renderer.ServerTextRenderer;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundAddRendererPacket;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundClearRendererPacket;
import snw.mods.ctweaks.render.Renderer;
import snw.mods.ctweaks.render.TextRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static snw.lib.protocol.util.PacketHelper.newNonce;

@RequiredArgsConstructor
public class ServerScreen implements Screen {
    @Getter
    private final ServerPlayer owner;
    private final List<AbstractServerRenderer> renderers = new ArrayList<>();
    private int nextRendererId;

    @Override
    public TextRenderer addTextRenderer(@NonNull PlanePosition position) {
        owner.ensureOnline();
        final int newId = nextRendererId++;
        final ServerTextRenderer result = new ServerTextRenderer(getOwner(), newId, position);
        owner.sendPacket(() -> new ClientboundAddRendererPacket(newId, result.getType(), newNonce()));
        onRendererAdded(result);
        return result;
    }

    private void onRendererAdded(AbstractServerRenderer renderer) {
        renderer.sendAdditionalAddPackets();
        renderers.add(renderer);
    }

    @ApiStatus.Internal
    public void removeRenderer(AbstractServerRenderer renderer) {
        renderers.remove(renderer);
    }

    @Override
    public @UnmodifiableView Collection<Renderer> getRenderers() {
        return Collections.unmodifiableList(renderers);
    }

    @Override
    public void clearRenderers() {
        renderers.clear();
        sendClearRendererPacket();
    }

    private void sendClearRendererPacket() {
        getOwner().sendPacket(() -> new ClientboundClearRendererPacket(newNonce()));
    }

    public void sendFullUpdate() {
        sendClearRendererPacket();
        for (AbstractServerRenderer renderer : renderers) {
            getOwner().sendPacket(() -> new ClientboundAddRendererPacket(renderer.getId(), renderer.getType(), newNonce()));
            renderer.sendFullUpdate();
        }
    }
}

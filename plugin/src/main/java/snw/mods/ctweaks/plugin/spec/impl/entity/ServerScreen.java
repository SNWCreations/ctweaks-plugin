package snw.mods.ctweaks.plugin.spec.impl.entity;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import snw.mods.ctweaks.entity.Screen;
import snw.mods.ctweaks.object.pos.PlanePosition;
import snw.mods.ctweaks.plugin.event.PlayerWindowPropertiesUpdateEvent;
import snw.mods.ctweaks.plugin.spec.impl.renderer.AbstractServerRenderer;
import snw.mods.ctweaks.plugin.spec.impl.renderer.ServerPlayerFaceRenderer;
import snw.mods.ctweaks.plugin.spec.impl.renderer.ServerTextRenderer;
import snw.mods.ctweaks.protocol.packet.c2s.ServerboundWindowPropertiesPacket;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundAddRendererPacket;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundClearRendererPacket;
import snw.mods.ctweaks.render.PlayerFaceRenderer;
import snw.mods.ctweaks.render.Renderer;
import snw.mods.ctweaks.render.TextRenderer;

import java.util.*;

import static snw.lib.protocol.util.PacketHelper.newNonce;
import static snw.mods.ctweaks.ModConstants.UNIT_AS_INT;

@RequiredArgsConstructor
public class ServerScreen implements Screen {
    @Getter
    private final ServerPlayer owner;
    private final List<AbstractServerRenderer> renderers = new ArrayList<>();
    private int nextRendererId;
    @Getter
    private int width = UNIT_AS_INT;
    @Getter
    private int height = UNIT_AS_INT;
    @Getter
    private boolean nowFullScreen;

    @Override
    public TextRenderer addTextRenderer(@NonNull PlanePosition position, @Nullable Component text, float scale) {
        owner.ensureCanOperate();
        final int newId = nextRendererId++;
        final ServerTextRenderer result = new ServerTextRenderer(getOwner(), newId, position, text, scale);
        onRendererAdded(result);
        return result;
    }

    @Override
    public PlayerFaceRenderer addPlayerFaceRenderer(UUID target, PlanePosition position, int size) {
        owner.ensureCanOperate();
        final int newId = nextRendererId++;
        final ServerPlayerFaceRenderer result = new ServerPlayerFaceRenderer(getOwner(), newId, target, position, size);
        onRendererAdded(result);
        return result;
    }

    private void onRendererAdded(AbstractServerRenderer renderer) {
        owner.sendPacket(() -> new ClientboundAddRendererPacket(renderer.getId(), renderer.getType(), newNonce()));
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

    public void update(ServerboundWindowPropertiesPacket packet) {
        this.width = packet.getWidth();
        this.height = packet.getHeight();
        this.nowFullScreen = packet.isNowFullScreen();
        new PlayerWindowPropertiesUpdateEvent(owner).callEvent();
    }
}

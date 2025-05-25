package snw.mods.ctweaks.plugin.spec.impl.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import snw.mods.ctweaks.entity.Screen;
import snw.mods.ctweaks.object.IntKeyed;
import snw.mods.ctweaks.object.pos.PlanePosition;
import snw.mods.ctweaks.plugin.event.PlayerWindowPropertiesUpdateEvent;
import snw.mods.ctweaks.plugin.spec.impl.layout.AbstractServerLayout;
import snw.mods.ctweaks.plugin.spec.impl.layout.ServerGridLayout;
import snw.mods.ctweaks.plugin.spec.impl.renderer.AbstractServerRenderer;
import snw.mods.ctweaks.plugin.spec.impl.renderer.ServerPlayerFaceRenderer;
import snw.mods.ctweaks.plugin.spec.impl.renderer.ServerTextRenderer;
import snw.mods.ctweaks.protocol.packet.c2s.ServerboundWindowPropertiesPacket;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundAddLayoutPacket;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundAddRendererPacket;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundClearLayoutPacket;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundClearRendererPacket;
import snw.mods.ctweaks.render.PlayerFaceRenderer;
import snw.mods.ctweaks.render.Renderer;
import snw.mods.ctweaks.render.TextRenderer;
import snw.mods.ctweaks.render.layout.GridLayout;
import snw.mods.ctweaks.render.layout.Layout;
import snw.mods.ctweaks.render.layout.LinearLayout;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static snw.lib.protocol.util.PacketHelper.newNonce;
import static snw.mods.ctweaks.ModConstants.UNIT_AS_INT;

@RequiredArgsConstructor
public class ServerScreen implements Screen {
    @Getter
    private final ServerPlayer owner;
    private final Int2ObjectMap<AbstractServerRenderer> renderers = new Int2ObjectLinkedOpenHashMap<>();
    private final Int2ObjectMap<AbstractServerLayout> layouts = new Int2ObjectLinkedOpenHashMap<>();
    private final AtomicInteger rendererIdGenerator = new AtomicInteger();
    @Getter
    private int width = UNIT_AS_INT;
    @Getter
    private int height = UNIT_AS_INT;
    @Getter
    private boolean nowFullScreen;

    @Override
    public TextRenderer addTextRenderer() {
        return addTextRenderer(null, null);
    }

    @Override
    public TextRenderer addTextRenderer(@Nullable Component text, @Nullable PlanePosition position) {
        owner.ensureCanOperate();
        final int newId = rendererIdGenerator.getAndIncrement();
        final ServerTextRenderer result = new ServerTextRenderer(getOwner(), newId, position, text);
        onRendererAdded(result);
        return result;
    }

    @Override
    public TextRenderer.Builder textRendererBuilder() {
        owner.ensureCanOperate();
        return new ServerTextRenderer.BuilderImpl(owner, rendererIdGenerator::getAndIncrement, this::onRendererAdded);
    }

    @Override
    public PlayerFaceRenderer addPlayerFaceRenderer(UUID target) {
        return addPlayerFaceRenderer(target, null);
    }

    @Override
    public PlayerFaceRenderer addPlayerFaceRenderer(UUID target, @Nullable PlanePosition position) {
        owner.ensureCanOperate();
        final int newId = rendererIdGenerator.getAndIncrement();
        final ServerPlayerFaceRenderer result = new ServerPlayerFaceRenderer(getOwner(), newId, target, position);
        onRendererAdded(result);
        return result;
    }

    @Override
    public PlayerFaceRenderer.Builder playerFaceRendererBuilder() {
        owner.ensureCanOperate();
        return new ServerPlayerFaceRenderer.BuilderImpl(owner, rendererIdGenerator::getAndIncrement, this::onRendererAdded);
    }

    @Override
    public GridLayout.Builder gridLayoutBuilder() {
        return new ServerGridLayout.BuilderImpl(owner, rendererIdGenerator::getAndIncrement, this::onLayoutAdded);
    }

    @Override
    public LinearLayout.Builder linearLayoutBuilder() {
        throw new UnsupportedOperationException("not implemented yet"); // todo implement linear layout
    }

    private String onRendererAdded(AbstractServerRenderer renderer) {
        owner.sendPacket(() -> new ClientboundAddRendererPacket(renderer.describe(), newNonce()));
        String nonce = renderer.sendFullUpdate();
        renderers.put(renderer.getId(), renderer);
        return nonce;
    }

    private String onLayoutAdded(AbstractServerLayout layout) {
        owner.sendPacket(() -> new ClientboundAddLayoutPacket(layout.describe(), newNonce()));
        String nonce = layout.sendFullUpdate();
        layouts.put(layout.getId(), layout);
        return nonce;
    }

    @ApiStatus.Internal
    public void removeRenderer(AbstractServerRenderer renderer) {
        renderers.values().remove(renderer);
    }

    @ApiStatus.Internal
    public void removeLayout(AbstractServerLayout layout) {
        layouts.values().remove(layout);
    }

    @Override
    public @UnmodifiableView Collection<? extends Renderer> getRenderers() {
        return renderers.values().stream().toList();
    }

    @Override
    public @UnmodifiableView Collection<? extends Layout> getLayouts() {
        return layouts.values().stream().toList();
    }

    @Override
    public void clearRenderers() {
        renderers.clear();
        sendClearRendererPacket();
    }

    @Override
    public void clearLayouts() {
        layouts.clear();
        sendClearLayoutPacket();
    }

    private void sendClearRendererPacket() {
        getOwner().sendPacket(() -> new ClientboundClearRendererPacket(newNonce()));
    }

    private void sendClearLayoutPacket() {
        getOwner().sendPacket(() -> new ClientboundClearLayoutPacket(newNonce()));
    }

    public void sendFullUpdate() {
        sendClearRendererPacket();
        sendClearLayoutPacket();
        for (AbstractServerRenderer renderer : renderers.values()) {
            getOwner().sendPacket(() -> new ClientboundAddRendererPacket(renderer.describe(), newNonce()));
            renderer.sendFullUpdate();
        }
        for (AbstractServerLayout layout : layouts.values()) {
            getOwner().sendPacket(() -> new ClientboundAddLayoutPacket(layout.describe(), newNonce()));
            layout.sendFullUpdate();
        }
    }

    public @Nullable IntKeyed lookupObject(IntKeyed.Descriptor descriptor) {
        int id = descriptor.id();
        Key type = descriptor.type();
        Int2ObjectMap<? extends IntKeyed> registry;
        if (type.equals(Renderer.TYPE)) {
            registry = renderers;
        } else if (type.equals(Layout.TYPE)) {
            registry = layouts;
        } else {
            throw new IllegalArgumentException("Unknown object type " + type);
        }
        return registry.get(id);
    }

    public void update(ServerboundWindowPropertiesPacket packet) {
        this.width = packet.getWidth();
        this.height = packet.getHeight();
        this.nowFullScreen = packet.isNowFullScreen();
        new PlayerWindowPropertiesUpdateEvent(owner).callEvent();
    }
}

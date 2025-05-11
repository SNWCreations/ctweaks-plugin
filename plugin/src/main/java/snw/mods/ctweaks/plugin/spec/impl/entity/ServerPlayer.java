package snw.mods.ctweaks.plugin.spec.impl.entity;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import snw.lib.protocol.packet.Packet;
import snw.mods.ctweaks.ModConstants;
import snw.mods.ctweaks.entity.Player;
import snw.mods.ctweaks.plugin.CTweaksMain;
import snw.mods.ctweaks.protocol.handler.ClientboundPacketHandler;

import java.util.*;
import java.util.function.Supplier;

import static snw.mods.ctweaks.plugin.util.Logging.debug;

public final class ServerPlayer implements Player {
    @ApiStatus.Internal
    public static final Map<UUID, ServerPlayer> CACHE = new HashMap<>();
    private final UUID uuid;
    @Getter
    private final ServerScreen screen;
    @Getter
    public boolean modReady;

    public static ServerPlayer of(org.bukkit.entity.Player handle) {
        return CACHE.computeIfAbsent(handle.getUniqueId(), ServerPlayer::new);
    }

    private ServerPlayer(UUID uuid) {
        this.uuid = uuid;
        this.screen = new ServerScreen(this);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public boolean isOnline() {
        final org.bukkit.entity.Player handle = getHandle();
        return handle != null && handle.isOnline();
    }

    public void ensureOnline() {
        Preconditions.checkState(isOnline(), "Player is not online");
    }

    public void ensureCanOperate() {
        ensureOnline();
        Preconditions.checkState(modReady, "Player client is not ready");
    }

    public org.bukkit.entity.Player getHandle() {
        return Bukkit.getPlayer(uuid);
    }

    public void sendPacket(Supplier<Packet<ClientboundPacketHandler>> packetSupplier) {
        final org.bukkit.entity.Player handle = getHandle();
        if (handle != null) {
            if (handle.getListeningPluginChannels().contains(ModConstants.CHANNEL)) {
                final Packet<ClientboundPacketHandler> packet = packetSupplier.get();
                debug(() -> "Sending packet " + packet + " to player " + handle.getName());
                final byte[] rawPacket = packet.serialize();
                handle.sendPluginMessage(CTweaksMain.getInstance(), ModConstants.CHANNEL, rawPacket);
            } else {
                debug(() -> "Ignoring packet to " + handle.getName() + " as the player does not have CTweaks mod installed");
            }
        } else {
            debug(() -> "Ignoring packet to " + getUUID() + " as the player was not online");
        }
    }

    public void registerAfterUpdateCallback(String nonce, Runnable callback) {
        CTweaksMain.getInstance().getProtocolServer().getOrNewInPacketHandler(this).registerAfterUpdateCallback(nonce, callback);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerPlayer that)) return false;

        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public String toString() {
        return "ServerPlayer{" +
                "uuid=" + uuid +
                ", online=" +
                (isOnline() ? "true, name=" + getHandle().getName() : "false") +
                '}';
    }
}

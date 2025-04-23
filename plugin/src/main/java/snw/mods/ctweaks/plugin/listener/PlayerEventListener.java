package snw.mods.ctweaks.plugin.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import snw.mods.ctweaks.ModConstants;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.packet.s2c.ClientboundHelloPacket;

import static snw.lib.protocol.util.PacketHelper.newNonce;
import static snw.mods.ctweaks.plugin.util.Logging.debug;

public final class PlayerEventListener implements Listener {
    @EventHandler
    public void onPlayerChannelRegister(PlayerRegisterChannelEvent event) {
        if (ModConstants.CHANNEL.equals(event.getChannel())) {
            final Player handle = event.getPlayer();
            debug(() -> "Player " + handle.getName() + " has registered mod channel, sending hello packet");
            final ServerPlayer wrapped = ServerPlayer.of(handle);
            wrapped.sendPacket(() -> new ClientboundHelloPacket(ModConstants.PROTOCOL_VERSION, newNonce()));
            wrapped.onJoined();
        }
    }
}

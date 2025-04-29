package snw.mods.ctweaks.plugin.net;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import snw.lib.protocol.packet.Packet;
import snw.lib.protocol.serial.DeserializedPacket;
import snw.lib.protocol.serial.std.StandardPacketDeserializer;
import snw.mods.ctweaks.ModConstants;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;
import snw.mods.ctweaks.protocol.PacketTypes;
import snw.mods.ctweaks.protocol.handler.ServerboundPacketHandler;

import java.util.HashMap;
import java.util.Map;

import static snw.mods.ctweaks.plugin.util.Logging.*;

public class ProtocolServer implements PluginMessageListener {
    private final StandardPacketDeserializer<ServerboundPacketHandler> packetDeserializer;
    private final Map<ServerPlayer, ServerboundPacketHandlerImpl> packetHandlers = new HashMap<>();

    public ProtocolServer() {
        this.packetDeserializer = new StandardPacketDeserializer<>(PacketTypes.CLIENTSIDE);
    }

    public ServerboundPacketHandlerImpl getOrNewInPacketHandler(ServerPlayer wrapped) {
        return packetHandlers.computeIfAbsent(wrapped, ServerboundPacketHandlerImpl::new);
    }

    public void close() {
        for (ServerboundPacketHandlerImpl inHandler : packetHandlers.values()) {
            inHandler.close();
        }
        packetHandlers.clear();
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (channel.equals(ModConstants.CHANNEL)) {
            final ServerPlayer wrapped = ServerPlayer.of(player);
            final ByteArrayDataInput raw = ByteStreams.newDataInput(message);
            final DeserializedPacket<ServerboundPacketHandler> deserialized = packetDeserializer.deserialize(raw);
            final Packet<ServerboundPacketHandler> packet = deserialized.packet();
            if (packet != null) {
                final ServerboundPacketHandlerImpl packetHandler = getOrNewInPacketHandler(wrapped);
                debug(() -> "Handling packet: " + packet + " from player: " + wrapped);
                try {
                    packet.handle(packetHandler);
                } catch (Throwable t) {
                    error(() -> "Failed to handle packet " + packet + " from player " + player, t);
                }
            }
        }
    }
}

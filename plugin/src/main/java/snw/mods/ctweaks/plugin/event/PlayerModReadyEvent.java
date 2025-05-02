package snw.mods.ctweaks.plugin.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;

/**
 * Called when server received {@link snw.mods.ctweaks.protocol.packet.c2s.ServerboundReadyPacket} from
 *  player's client, meaning that the player has CTweaks mod installed on the client, which is compatible with
 *  the server.
 *
 * @author SNWCreations
 */
public class PlayerModReadyEvent extends Event {
    @Getter
    private static final HandlerList handlerList = new HandlerList();

    @Getter
    private final ServerPlayer modPlayer;

    public PlayerModReadyEvent(ServerPlayer modPlayer) {
        this.modPlayer = modPlayer;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}

package snw.mods.ctweaks.plugin.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import snw.mods.ctweaks.entity.Player;
import snw.mods.ctweaks.plugin.spec.impl.entity.ServerPlayer;

/**
 * Called when a player's client window properties updated. <br>
 * Listeners should read the updated properties through {@link snw.mods.ctweaks.entity.Screen}
 *  by calling {@link Player#getScreen()} on the result of {@link #getModPlayer()}.
 *
 * @author SNWCreations
 */
public class PlayerWindowPropertiesUpdateEvent extends Event {
    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final ServerPlayer who;

    public PlayerWindowPropertiesUpdateEvent(ServerPlayer who) {
        this.who = who;
    }

    public org.bukkit.entity.Player getPlayer() {
        return who.getHandle();
    }

    public ServerPlayer getModPlayer() {
        return who;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}

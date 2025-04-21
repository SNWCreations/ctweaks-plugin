package snw.mods.ctweaks.plugin;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import snw.mods.ctweaks.ModConstants;
import snw.mods.ctweaks.plugin.listener.PlayerEventListener;
import snw.mods.ctweaks.plugin.protocol.ProtocolServer;

public final class CTweaksMain extends JavaPlugin {
    @Getter
    private static CTweaksMain instance;
    @Getter
    private ProtocolServer protocolServer;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);

        protocolServer = new ProtocolServer();
        getServer().getMessenger().registerIncomingPluginChannel(this, ModConstants.CHANNEL, protocolServer);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        if (protocolServer != null) {
            protocolServer.close();
            protocolServer = null;
        }

        instance = null;
    }
}

package snw.mods.ctweaks.plugin;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import snw.mods.ctweaks.ModConstants;
import snw.mods.ctweaks.plugin.listener.PlayerEventListener;
import snw.mods.ctweaks.plugin.net.ProtocolServer;
import snw.mods.ctweaks.plugin.util.Debugging;

import java.io.IOException;
import java.util.jar.JarFile;

public final class CTweaksMain extends JavaPlugin {
    @Getter
    private static CTweaksMain instance;
    @Getter
    private ProtocolServer protocolServer;

    @Override
    public void onLoad() {
        instance = this;

        try (JarFile jarFile = new JarFile(getFile())) {
            Debugging.enabled = Boolean.parseBoolean(jarFile.getManifest().getMainAttributes().getValue("Dev-Build"));
        } catch (IOException e) {
            getSLF4JLogger().error("Failed to read build information", e);
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);

        protocolServer = new ProtocolServer();
        getServer().getMessenger().registerIncomingPluginChannel(this, ModConstants.CHANNEL, protocolServer);
        getServer().getMessenger().registerOutgoingPluginChannel(this, ModConstants.CHANNEL);
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

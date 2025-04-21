package snw.mods.ctweaks.plugin.util;

import snw.mods.ctweaks.plugin.CTweaksMain;

import java.util.function.Supplier;

public final class Logging {
    private Logging() {
    }

    public static void info(final Supplier<String> message) {
        if (CTweaksMain.getInstance().getSLF4JLogger().isInfoEnabled()) {
            CTweaksMain.getInstance().getSLF4JLogger().info(message.get());
        }
    }

    public static void warn(final Supplier<String> message) {
        if (CTweaksMain.getInstance().getSLF4JLogger().isWarnEnabled()) {
            CTweaksMain.getInstance().getSLF4JLogger().warn(message.get());
        }
    }

    public static void error(final Supplier<String> message) {
        if (CTweaksMain.getInstance().getSLF4JLogger().isErrorEnabled()) {
            CTweaksMain.getInstance().getSLF4JLogger().error(message.get());
        }
    }

    public static void error(final Supplier<String> message, final Throwable t) {
        if (CTweaksMain.getInstance().getSLF4JLogger().isErrorEnabled()) {
            CTweaksMain.getInstance().getSLF4JLogger().error(message.get(), t);
        }
    }

    public static void debug(final Supplier<String> message) {
        if (Debugging.enabled) {
            CTweaksMain.getInstance().getSLF4JLogger().info(message.get());
        }
    }
}

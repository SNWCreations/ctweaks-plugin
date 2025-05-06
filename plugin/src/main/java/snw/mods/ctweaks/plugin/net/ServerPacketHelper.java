package snw.mods.ctweaks.plugin.net;

import org.jetbrains.annotations.Nullable;
import snw.mods.ctweaks.object.IntKeyed;

import java.util.List;

public final class ServerPacketHelper {
    private ServerPacketHelper() {
    }

    public static @Nullable List<IntKeyed.Descriptor> mapToDescriptor(@Nullable List<? extends IntKeyed> objList) {
        return objList == null ? null : objList.stream().map(IntKeyed::describe).toList();
    }
}

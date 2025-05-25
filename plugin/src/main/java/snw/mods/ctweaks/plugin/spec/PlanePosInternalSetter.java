package snw.mods.ctweaks.plugin.spec;

import org.jetbrains.annotations.ApiStatus;
import snw.mods.ctweaks.object.pos.PlanePosition;

public interface PlanePosInternalSetter {
    @ApiStatus.Internal
    void setPositionInternal(PlanePosition position);
}

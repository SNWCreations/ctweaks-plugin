package snw.mods.ctweaks.plugin.spec.impl;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import snw.mods.ctweaks.object.ObjectUpdater;

public abstract class AbstractUpdater implements ObjectUpdater {
    private boolean applied;

    protected void ensureNotApplied() {
        Preconditions.checkState(!applied, "Updates from this updater was already applied");
    }

    @Override
    @MustBeInvokedByOverriders
    public void update() throws IllegalStateException {
        ensureNotApplied();
        applied = true;
    }
}

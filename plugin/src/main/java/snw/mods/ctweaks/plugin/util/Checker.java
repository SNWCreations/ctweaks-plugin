package snw.mods.ctweaks.plugin.util;

import snw.mods.ctweaks.render.layout.Layout;
import snw.mods.ctweaks.render.layout.LayoutElement;

public final class Checker {
    private Checker() {
    }

    public static void ensureCanAddLayoutElement(Layout self, LayoutElement incoming) {
        if (incoming.describe().equals(self.describe())) {
            throw new IllegalArgumentException("Cannot let self be a children of self");
        }
        if (incoming instanceof Layout otherLayout) {
            for (LayoutElement child : otherLayout.getChildren()) {
                if (child.describe().equals(self.describe())) {
                    throw new IllegalArgumentException("The given element contained the modifying layout");
                } else {
                    ensureCanAddLayoutElement(self, child);
                }
            }
        }
    }
}

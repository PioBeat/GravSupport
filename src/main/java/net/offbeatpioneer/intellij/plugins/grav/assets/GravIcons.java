package net.offbeatpioneer.intellij.plugins.grav.assets;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Icons for various files in Grav or other purposes.
 *
 * @author Dominik Grzelak
 */
public class GravIcons {

    private static @NotNull Icon load(@NotNull String path) {
        return IconLoader.getIcon(path, GravIcons.class);
//        return IconManager.getInstance().getIcon(path, GravIcons.class);
    }

    public static final Icon GravDefaultIcon = load("/icons/gravLogo.svg");
    public static final Icon GravDefaultIconLegacy = load("/icons/gravLogoLegacy.png");
    public static final Icon ThemeBlueprints = load("/icons/gravFileIcon.svg");
    public static final Icon ThemeConfiguration = load("/icons/gravFileIcon.svg");
}

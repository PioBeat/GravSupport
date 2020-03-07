package net.offbeatpioneer.intellij.plugins.grav.files;

import com.intellij.openapi.fileTypes.LanguageFileType;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Concrete implementation of Grav's theme blueprint configuration file.
 *
 * @author Dominik Grzelak
 * @since 16.07.2017.
 */
public class ThemeBlueprintsFileType extends AbstractGravFileType {
    public static final LanguageFileType INSTANCE = new ThemeBlueprintsFileType();

    public ThemeBlueprintsFileType() {
        super();
    }

    @NotNull
    @Override
    public String getName() {
        return "Theme blueprints";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Theme Blueprints File";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return GravIcons.ThemeBlueprints;
    }

    @Override
    public boolean needsFilename() {
        return false;
    }

    @Override
    public String getCorrespondingTemplateFile() {
        return "Grav Theme Blueprints";
    }

    @Override
    public String getDefaultFilename() {
        return "blueprints";
    }
}

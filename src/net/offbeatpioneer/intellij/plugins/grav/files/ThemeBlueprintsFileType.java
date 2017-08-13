package net.offbeatpioneer.intellij.plugins.grav.files;

import com.intellij.openapi.fileTypes.LanguageFileType;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLLanguage;

import javax.swing.*;

/**
 * Created by Dome on 16.07.2017.
 */
public class ThemeBlueprintsFileType extends GravConfigurationFileType {
    public static final LanguageFileType INSTANCE = new ThemeBlueprintsFileType();

    public ThemeBlueprintsFileType() {
        super(YAMLLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "blueprints";
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

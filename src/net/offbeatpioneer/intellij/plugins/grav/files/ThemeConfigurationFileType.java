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
public class ThemeConfigurationFileType extends GravConfigurationFileType {
    public static final LanguageFileType INSTANCE = new ThemeConfigurationFileType();
    private String defaultFileName;

    public ThemeConfigurationFileType() {
        super(YAMLLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Theme configuration";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Theme Configuration File";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return GravIcons.ThemeConfiguration;
    }

    @Override
    public boolean needsFilename() {
        return false;
    }

    @Override
    public String getCorrespondingTemplateFile() {
        return "Grav Theme Configuration";
    }

    public void setDefaultFileName(String defaultFileName) {
        this.defaultFileName = defaultFileName;
    }

    @Override
    public String getDefaultFilename() {
        return this.defaultFileName;
    }
}

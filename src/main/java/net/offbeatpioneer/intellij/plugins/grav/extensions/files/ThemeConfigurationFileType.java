package net.offbeatpioneer.intellij.plugins.grav.extensions.files;

import com.intellij.openapi.fileTypes.LanguageFileType;
import net.offbeatpioneer.intellij.plugins.grav.extensions.icons.GravIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Concrete implementation of Grav's theme configuration file.
 *
 * @author Dominik Grzelak
 * @since 16.07.2017.
 */
public class ThemeConfigurationFileType extends AbstractGravFileType {
    public static final LanguageFileType INSTANCE = new ThemeConfigurationFileType();
    private String defaultFileName;

    public ThemeConfigurationFileType() {
        super();
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

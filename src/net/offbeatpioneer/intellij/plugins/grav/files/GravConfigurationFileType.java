package net.offbeatpioneer.intellij.plugins.grav.files;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for Gravs configuration files
 * Configuration files are in the yaml format.
 * <ul>
 * <li>Blueprints for theme (yaml)</li>
 * <li>THEME.yaml</li>
 * </ul>
 * Created by Dome on 16.07.2017.
 */
public abstract class GravConfigurationFileType extends LanguageFileType {

    protected GravConfigurationFileType(@NotNull Language language) {
        super(language);
    }

    public static final String DEFAULT_EXTENSION = "yaml";

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    public abstract boolean needsFilename();

    public abstract String getCorrespondingTemplateFile();

    public String getDefaultFilename() {
        return "untitled";
    }
}

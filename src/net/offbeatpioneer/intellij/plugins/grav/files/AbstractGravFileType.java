package net.offbeatpioneer.intellij.plugins.grav.files;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

/**
 * Base abstarct class for all Grav-specific files (i.e., mainly configuration files).
 * Configuration files come mostly in the YAML format:
 * <ul>
 * <li>Blueprints for theme (yaml)</li>
 * <li>THEME.yaml</li>
 * </ul>
 *
 * @author Dominik Grzelak
 * @since 16.07.2017.
 */
public abstract class AbstractGravFileType extends LanguageFileType {

    public static final String DEFAULT_EXTENSION = "yaml";

    protected AbstractGravFileType() {
        super(YAMLLanguage.INSTANCE);
    }

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

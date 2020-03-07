package net.offbeatpioneer.intellij.plugins.grav.files;

/**
 * Enum-like class that provides all available file types if necessary.
 *
 * @author Dominik Grzelak
 * @since 16.07.2017.
 */
public class GravFileTypes {

    public static AbstractGravFileType[] CONFIGURATION_FILE_TYPES = new AbstractGravFileType[]{
            new ThemeBlueprintsFileType(),
            new ThemeConfigurationFileType()
    };

    public static void setModuleName(String moduleName) {
        ((ThemeConfigurationFileType) CONFIGURATION_FILE_TYPES[1]).setDefaultFileName(moduleName);
    }
}

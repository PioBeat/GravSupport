package net.offbeatpioneer.intellij.plugins.grav.files;

/**
 * Created by Dome on 16.07.2017.
 */
public class GravFileTypes {

    public static GravConfigurationFileType[] CONFIGURATION_FILE_TYPES = new GravConfigurationFileType[]{
            new ThemeBlueprintsFileType(),
            new ThemeConfigurationFileType()
    };

    public static void setModuleName(String moduleName) {
        ((ThemeConfigurationFileType)CONFIGURATION_FILE_TYPES[1]).setDefaultFileName(moduleName);
    }
}

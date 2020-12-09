package net.offbeatpioneer.intellij.plugins.grav.extensions.toolwindow;

import org.jetbrains.annotations.NotNull;

public enum GravSystemYamlConfigFiles {

    GRAV_1_6_30(1630, "system/config/system.yaml"),
    GRAV_1_6_29(1629, "system/config/system.yaml");

    private final int version;
    private final String systemYamlPath;

    private GravSystemYamlConfigFiles(int major, String systemYamlPath) {
        this.version = major;
        this.systemYamlPath = systemYamlPath;
    }

    public boolean isAtLeast(@NotNull GravSystemYamlConfigFiles level) {
        return compareTo(level) >= 0;
    }

    public boolean isLessThan(@NotNull GravSystemYamlConfigFiles level) {
        return compareTo(level) < 0;
    }

    public int getVersion() {
        return version;
    }

    public String getSystemYamlPath() {
        return systemYamlPath;
    }
}

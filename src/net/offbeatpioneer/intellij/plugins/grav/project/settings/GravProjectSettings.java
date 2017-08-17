package net.offbeatpioneer.intellij.plugins.grav.project.settings;

public class GravProjectSettings {
    private String gravInstallationPath;
    private boolean withSrcDirectory;

    public GravProjectSettings() {
    }

    public String getGravInstallationPath() {
        return gravInstallationPath;
    }

    public void setGravInstallationPath(String gravInstallationPath) {
        this.gravInstallationPath = gravInstallationPath;
    }

    public boolean isWithSrcDirectory() {
        return withSrcDirectory;
    }

    public void setWithSrcDirectory(boolean withSrcDirectory) {
        this.withSrcDirectory = withSrcDirectory;
    }
}

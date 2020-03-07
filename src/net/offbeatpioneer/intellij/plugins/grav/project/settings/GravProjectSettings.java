package net.offbeatpioneer.intellij.plugins.grav.project.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "GravProjectSettings",
        storages = {
                @Storage("grav-plugin.xml")
        }
)
public class GravProjectSettings implements PersistentStateComponent<GravProjectSettings> {
    public String gravInstallationPath = "";
    public boolean withSrcDirectory = true;
    public boolean pluginEnabled = false;
    public boolean dismissEnableNotification = false;

    public GravProjectSettings() {
    }

    @Nullable
    public static GravProjectSettings getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GravProjectSettings.class);
    }

    @Nullable
    @Override
    public GravProjectSettings getState() {
        return this;
    }

    @Override
    public void loadState(GravProjectSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}

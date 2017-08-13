package net.offbeatpioneer.intellij.plugins.grav.storage;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Grzelak
 */
@State(
        name = "GravPersistentStateComponent",
        storages = {
                @Storage("net.offbeatpioneer.gravsupport.GravSettings.xml")
        }
)
public class GravPersistentStateComponent implements PersistentStateComponent<GravPersistentStateComponent> {

    private String defaultGravDownloadPath;

    public GravPersistentStateComponent() {
        defaultGravDownloadPath = "";
    }

    public String getDefaultGravDownloadPath() {
        return defaultGravDownloadPath;
    }

    public void setDefaultGravDownloadPath(String defaultGravDownloadPath) {
        this.defaultGravDownloadPath = defaultGravDownloadPath;
    }

    @Nullable
    @Override
    public GravPersistentStateComponent getState() {
        return this;
    }

    @Override
    public void loadState(GravPersistentStateComponent state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Nullable
    public static GravPersistentStateComponent getInstance(Project project) {
        return ServiceManager.getService(project, GravPersistentStateComponent.class);
    }

    @Nullable
    public static GravPersistentStateComponent getInstance() {
        return ServiceManager.getService(GravPersistentStateComponent.class);
    }
}

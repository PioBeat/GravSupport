package net.offbeatpioneer.intellij.plugins.grav.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravProjectSettings;
import org.jetbrains.annotations.Nullable;

/**
 * Project component
 *
 * @author Dominik Grzelak
 */
public class GravProjectComponent {
    public static boolean isEnabled(Project project) {
        if (project == null) return false;
        if(GravSdkType.operationIsAvailableFor(project, false)) {
            GravProjectSettings settings = GravProjectSettings.getInstance(project);
            if (settings == null)
                return false;
            return settings.pluginEnabled;
        }
        return false;
    }

    /**
     * Returns the current project for which the plugin is enabled from all opened projects of the IDE
     *
     * @return current opened project or null, if none exists or plugin is not enabled
     */
    @Nullable
    public static Project getEnabledProject() {
        for (Project each : ProjectManager.getInstance().getOpenProjects()) {
            if (GravProjectComponent.isEnabled(each)) {
                return each;
            }
        }
        return null;
    }

}

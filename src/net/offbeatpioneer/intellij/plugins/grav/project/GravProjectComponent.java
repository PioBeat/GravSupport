package net.offbeatpioneer.intellij.plugins.grav.project;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import net.offbeatpioneer.intellij.plugins.grav.project.settings.GravProjectSettings;
import org.jetbrains.annotations.Nullable;

/**
 * Project component
 *
 * @author Dominik Grzelak
 */
public class GravProjectComponent {
    public static boolean isEnabled(Project project) {
        if (project == null) return false;
        GravProjectSettings settings = GravProjectSettings.getInstance(project);
        if (settings == null)
            return false;
        return settings.pluginEnabled;
    }

    /**
     * Returns the current project fr which the plugin is enabled from all opened projects of the IDE
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

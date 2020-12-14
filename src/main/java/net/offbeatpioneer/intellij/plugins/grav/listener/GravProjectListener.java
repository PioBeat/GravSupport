package net.offbeatpioneer.intellij.plugins.grav.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.helper.IdeHelper;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravProjectSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Project listener that tries to show a notification to activate the plugin if it's not activated yet.
 */
public class GravProjectListener implements ProjectManagerListener {

    public GravProjectListener() {
    }

    @Override
    public void projectOpened(@NotNull Project project) {
        notifyPluginEnableDialog(project);
    }

    @Override
    public void projectClosed(@NotNull Project project) {
    }

    private void notifyPluginEnableDialog(Project project) {
        if (GravSdkType.operationIsAvailableFor(project, false)) {
            GravProjectSettings settings = GravProjectSettings.getInstance(project);
            // Enable Project dialog
            if (settings != null && !settings.pluginEnabled && !settings.dismissEnableNotification) {
                IdeHelper.notifyEnableMessage(project);
            }
        }
    }


}

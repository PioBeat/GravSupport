package net.offbeatpioneer.intellij.plugins.grav.listener;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravModuleType;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.helper.IdeHelper;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravProjectSettings;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Project listener that tries to show a notification to activate the plugin if it's not activated yet.
 */
public class GravProjectListener implements ProjectManagerListener {

    public GravProjectListener() {
    }

    @Override
    public void projectOpened(@NotNull Project project) {
        notifyPluginEnableDialog(project);
        checkIfGravProjectModuleTyp(project);
    }

    @Override
    public void projectClosed(@NotNull Project project) {
    }

    private void notifyPluginEnableDialog(Project project) {
        if (GravSdkType.isOperationBasicallyAvailableFor(project)) {
            GravProjectSettings settings = GravProjectSettings.getInstance(project);
            // Enable Project dialog
            if (settings != null && !settings.pluginEnabled && !settings.dismissEnableNotification) {
                IdeHelper.notifyEnableMessage(project);
            }
        }
    }

    private void checkIfGravProjectModuleTyp(Project project) {
        if (project.getBasePath() != null) {
            VirtualFile projectPath = LocalFileSystem.getInstance().findFileByIoFile(new File(project.getBasePath()));
            GravProjectSettings gravSettings = GravProjectSettings.getInstance(project);
            if (projectPath != null &&
                    GravSdkType.isValidGravSDK(projectPath) &&
                    (gravSettings != null && !gravSettings.dismissEnableNotification)) {
                for (Module module : ModuleManager.getInstance(project).getModules()) {
                    if (!GravModuleType.ID.equals(module.getModuleTypeName())) {
                        IdeHelper.notifyConvertMessage(project, module);
                    }
                }
            }
        }
    }


}

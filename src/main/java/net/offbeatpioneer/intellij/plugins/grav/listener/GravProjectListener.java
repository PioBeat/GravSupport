package net.offbeatpioneer.intellij.plugins.grav.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.project.impl.ProjectLifecycleListener;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.helper.IdeHelper;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravProjectSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Project listener that tries to show a notification to activate the plugin if it's not activated yet.
 */
public class GravProjectListener implements ProjectManagerListener {

    public GravProjectListener() {
    }

    @Override
    public void projectOpened(@NotNull Project project) {
        initComponent(project);
        notifyPluginEnableDialog(project);
    }

    public void initComponent(Project project) {
        GravProjectSettings settings = GravProjectSettings.getInstance(project);
        if (settings != null) {
            @SystemIndependent String basePath = project.getBasePath();
            settings.withSrcDirectory = false;
            if (basePath != null) {
                VirtualFile file = VfsUtil.findFile(Paths.get(project.getBasePath(), "src"), true);
                if (file != null) {
                    settings.withSrcDirectory = true;
                }
            }
        }
    }

    private void notifyPluginEnableDialog(Project project) {
        if (Objects.isNull(project.getBasePath())) {
            IdeHelper.notifyShowGenericErrorMessage(project);
            return;
        }
        VirtualFile projectPath = LocalFileSystem.getInstance().findFileByIoFile(new File(project.getBasePath()));
        GravProjectSettings settings = GravProjectSettings.getInstance(project);
        // Enable Project dialog
        if (settings != null && !settings.pluginEnabled && !settings.dismissEnableNotification) {
            //is a src directory present?
            if (settings.withSrcDirectory && projectPath != null && projectPath.findChild("src") != null) {
                projectPath = projectPath.findChild("src");
                if (projectPath != null && !GravSdkType.isValidGravSDK(projectPath))
                    projectPath = LocalFileSystem.getInstance().findFileByIoFile(new File(project.getBasePath()));
            }
            if (projectPath != null && GravSdkType.isValidGravSDK(projectPath)) { //grav module found
                IdeHelper.notifyEnableMessage(project);
            }
        }
    }


}

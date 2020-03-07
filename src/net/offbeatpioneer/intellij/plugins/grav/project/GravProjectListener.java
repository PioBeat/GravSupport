package net.offbeatpioneer.intellij.plugins.grav.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectLifecycleListener;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.helper.IdeHelper;
import net.offbeatpioneer.intellij.plugins.grav.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.project.settings.GravProjectSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import java.nio.file.Paths;

public class GravProjectListener implements ProjectLifecycleListener {

    public GravProjectListener() {
    }

    @Override
    public void beforeProjectLoaded(@NotNull Project project) {
        System.out.println(project);
    }

    @Override
    public void afterProjectClosed(@NotNull Project project) {
        System.out.println(project);
    }

    @Override
    public void postStartupActivitiesPassed(@NotNull Project project) {
        initComponent(project);
        notifyPluginEnableDialog(project);
    }

    @Override
    public void projectComponentsInitialized(@NotNull Project project) {
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
        GravProjectSettings settings = GravProjectSettings.getInstance(project);
        // Enable Project dialog
        VirtualFile vf = project.getBaseDir();
        if (!settings.pluginEnabled && !settings.dismissEnableNotification) {
            //is a src directory present?
            if (settings.withSrcDirectory && vf.findChild("src") != null) {
                vf = vf.findChild("src");
                if (vf != null && !GravSdkType.isValidGravSDK(vf)) vf = project.getBaseDir();
            }
//            vf = settings.withSrcDirectory && vf.findChild("src") == null ? vf : vf.findChild("src");
            if (vf != null && GravSdkType.isValidGravSDK(vf)) { //grav module found
                IdeHelper.notifyEnableMessage(project);
            }
        }
    }
}

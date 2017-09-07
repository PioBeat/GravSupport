package net.offbeatpioneer.intellij.plugins.grav.project;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.helper.IdeHelper;
import net.offbeatpioneer.intellij.plugins.grav.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.project.settings.GravProjectSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Project component gets instantiated for every GRAV project created
 *
 * @author Dominik Grzelak
 */
public class GravProjectComponent implements ProjectComponent {
    final private static Logger LOG = Logger.getInstance("Grav-Plugin");
    private GravProjectSettings settings;
    private Project project;

    public GravProjectComponent(Project project) {
        this.project = project;
    }


    @Override
    public void projectOpened() {
        notifyPluginEnableDialog();
    }

    @Override
    public void projectClosed() {
    }

    @Override
    public void initComponent() {
        settings = GravProjectSettings.getInstance(project);
        if (settings != null) {
            if (project != null && VfsUtil.findRelativeFile(project.getBaseDir(), "src") == null) {
                settings.withSrcDirectory = false;
            } else {
                settings.withSrcDirectory = true;
            }
        }
    }

    @Override
    public void disposeComponent() {

    }

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

    @NotNull
    @Override
    public String getComponentName() {
        return "GravProjectComponent";
    }

    private void notifyPluginEnableDialog() {
        // Enable Project dialog
        VirtualFile vf = project.getBaseDir();
        if (!settings.pluginEnabled && !settings.dismissEnableNotification) {
            //is a src directory present?
            if (settings.withSrcDirectory && vf.findChild("src") != null) {
                vf = vf.findChild("src");
                if (!GravSdkType.isValidGravSDK(vf)) vf = project.getBaseDir();
            }
//            vf = settings.withSrcDirectory && vf.findChild("src") == null ? vf : vf.findChild("src");
            if (vf == null) return;
            if (GravSdkType.isValidGravSDK(vf)) { //grav module found
                IdeHelper.notifyEnableMessage(project);
            }
        }
    }
}

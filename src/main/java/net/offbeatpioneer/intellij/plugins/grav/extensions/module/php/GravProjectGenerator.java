package net.offbeatpioneer.intellij.plugins.grav.extensions.module.php;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.PlatformUtils;
import com.jetbrains.php.config.library.PhpIncludePathManager;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.builder.GravProjectGeneratorUtil;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravProjectSettings;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravPersistentStateComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.offbeatpioneer.intellij.plugins.grav.extensions.module.wizard.CreateGravProjectWizardStep.LAST_USED_GRAV_HOME;

/**
 * Created by Dome on 11.08.2017.
 */
public class GravProjectGenerator extends WebProjectTemplate<GravProjectSettings> {

    private GravInstallerGeneratorPeer generatorPeer;
    private GravPersistentStateComponent storage;

    GravProjectGenerator() {
        this.storage = GravPersistentStateComponent.getInstance();
    }

    @Override
    public String getDescription() {
        return "Create a new Grav project. A local PHP interpreter is required.";
    }

    @Override
    public Icon getIcon() {
        if(PlatformUtils.isPhpStorm()) {
            return GravIcons.GravDefaultIcon;
        } else {
            return GravIcons.GravDefaultIconLegacy;
        }
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Grav";
    }

    @NotNull
    @Override
    public ProjectGeneratorPeer<GravProjectSettings> createPeer() {
        if (generatorPeer == null)
            generatorPeer = new GravInstallerGeneratorPeer();
        return generatorPeer;
    }

    //after create button was pressed
    // the parent directory with the project name is automatically created
    @Override
    public void generateProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull GravProjectSettings settings, @NotNull Module module) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(new File(settings.gravInstallationPath));
        if (vf == null || !GravSdkType.isValidGravSDK(vf)) {
            NotificationHelper.showErrorNotification(project, "Project couldn't be created because Grav Installation isn't valid");
            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder("Project couldn't be created because Grav Installation isn't valid", MessageType.ERROR, null)
                    .setFadeoutTime(3500)
                    .createBalloon()
                    .show(RelativePoint.getSouthEastOf(statusBar.getComponent()), Balloon.Position.above);
        } else {
            storage.setDefaultGravDownloadPath(settings.gravInstallationPath);
            PropertiesComponent.getInstance().setValue(LAST_USED_GRAV_HOME, new File(settings.gravInstallationPath).getAbsolutePath());
            GravProjectGeneratorUtil projectGenerator = new GravProjectGeneratorUtil();
            projectGenerator.generateProject(project, baseDir, settings, module);
            try {
                List<String> includePath = new ArrayList<>();
                includePath.add(baseDir.getPath());
                PhpIncludePathManager.getInstance(project).setIncludePath(includePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isPrimaryGenerator() {
        return PlatformUtils.isPhpStorm();
    }
}

package net.offbeatpioneer.intellij.plugins.grav.module.builder;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import net.offbeatpioneer.intellij.plugins.grav.helper.ProcessUtils;
import net.offbeatpioneer.intellij.plugins.grav.module.wizard.CopyFileVisitor;
import net.offbeatpioneer.intellij.plugins.grav.project.settings.GravProjectSettings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.intellij.ide.projectView.actions.MarkRootActionBase.findContentEntry;

public class GravProjectGeneratorUtil {
    boolean showFailedNotifications = true;

    public boolean generateProject(@NotNull Project project, @NotNull VirtualFile src, @NotNull GravProjectSettings settings, @NotNull Module module) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        ApplicationManager.getApplication().runWriteAction(new Runnable() {

            @Override
            public void run() {
                try {

                    ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
                    VirtualFile src1 = src.createChildDirectory(this, "src");
                    findContentEntry(model, src1).addSourceFolder(src1, false);

                    VirtualFile test = src.createChildDirectory(this, "test");
                    findContentEntry(model, test).addSourceFolder(test, true);

                    model.commit();
                    test.refresh(false, true);
                    src1.refresh(false, true);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        File f = new File(module.getModuleFilePath());
        String tmp = null;
        try {
            tmp = f.getCanonicalFile().getParent();
            if (new File(tmp).isDirectory() && !new File(tmp).getName().equals(module.getName())) {
                tmp = new File(tmp).getParent();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (tmp == null) return false;
        Path targetPath = new File(new File(tmp) + File.separator + "src").toPath();


        String[] commands = new String[]{"bin/gpm", "install", "devtools"};
        ProcessUtils processUtils = new ProcessUtils(commands, new File(targetPath.toUri()));

        Task.Backgroundable installDevtools = new Task.Backgroundable(project, "Installing Devtools Plugin") {
            @Override
            public void onSuccess() {
                super.onSuccess();
                if (processUtils.getErrorOutput() != null && !processUtils.getErrorOutput().isEmpty()) {
                    NotificationHelper.showBaloon(processUtils.getErrorOutput(), MessageType.ERROR, project);
                } else {
                    JBPopupFactory.getInstance()
                            .createHtmlTextBalloonBuilder("Devtools plugin installed", MessageType.INFO, null)
                            .setFadeoutTime(3500)
                            .createBalloon()
                            .show(RelativePoint.getSouthEastOf(statusBar.getComponent()), Balloon.Position.above);
                }
            }

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                processUtils.execute();
            }
        };

        Task.Backgroundable t = new Task.Backgroundable(project, "Copying Grav SDK to Module Folder") {

            @Override
            public void onSuccess() {
                super.onSuccess();
                JBPopupFactory.getInstance()
                        .createHtmlTextBalloonBuilder("Module created", MessageType.INFO, null)
                        .setFadeoutTime(3500)
                        .createBalloon()
                        .show(RelativePoint.getSouthEastOf(statusBar.getComponent()), Balloon.Position.above);
                ProgressManager.getInstance().run(installDevtools);
            }

            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    Files.walkFileTree(new File(settings.getGravInstallationPath()).toPath(), new CopyFileVisitor(targetPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        ProgressManager.getInstance().run(t);

        return true;
    }
}
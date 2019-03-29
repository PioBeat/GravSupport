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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import net.offbeatpioneer.intellij.plugins.grav.module.tasks.InstallDevtoolsPlugin;
import net.offbeatpioneer.intellij.plugins.grav.module.wizard.CopyFileVisitor;
import net.offbeatpioneer.intellij.plugins.grav.project.settings.GravProjectSettings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static com.intellij.ide.projectView.actions.MarkRootActionBase.findContentEntry;

public class GravProjectGeneratorUtil {

    public boolean generateProject(@NotNull Project project, @NotNull VirtualFile src, @NotNull GravProjectSettings settings, @NotNull Module module) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        ApplicationManager.getApplication().runWriteAction(() -> {
//                try {
            //TODO
            ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
//                    if (settings.withSrcDirectory) {
//                        VirtualFile src1 = src.createChildDirectory(this, "src");
//                        findContentEntry(model, src1).addSourceFolder(src1, false);
//
//                        VirtualFile test = src.createChildDirectory(this, "test");
//                        findContentEntry(model, test).addSourceFolder(test, true);
//
//                        model.commit();
//                        test.refresh(false, true);
//                        src1.refresh(false, true);
//                    } else {
            if (Objects.isNull(findContentEntry(model, src))) {
                return;
            }
            findContentEntry(model, src).addSourceFolder(src, false);
            model.commit();
            src.refresh(false, true);
//                    }

//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
        });


        String targetPath = module.getProject().getBasePath();
        if (Objects.isNull(targetPath)) {
            NotificationHelper.showBaloon("Project path couldn't be determined. Please try again or restart the IDE.", MessageType.ERROR, project);
            return false;
        }
        Task.Backgroundable installDevtools = new InstallDevtoolsPlugin(project, "Installing Devtools Plugin", new File(targetPath));
        Task.Backgroundable t = new Task.Backgroundable(project, "Copying Grav SDK to Module Folder") {

            @Override
            public void onSuccess() {
                super.onSuccess();
                NotificationHelper.showInfoNotification(getProject(), "Grav project created");
//                JBPopupFactory.getInstance()
//                        .createHtmlTextBalloonBuilder("Grav project created", MessageType.INFO, null)
//                        .setFadeoutTime(3500)
//                        .createBalloon()
//                        .show(RelativePoint.getSouthEastOf(statusBar.getComponent()), Balloon.Position.above);
                ProgressManager.getInstance().run(installDevtools);
            }

            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    Files.walkFileTree(new File(settings.gravInstallationPath).toPath(), new CopyFileVisitor(new File(targetPath).toPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        ProgressManager.getInstance().run(t);

        return true;
    }
}

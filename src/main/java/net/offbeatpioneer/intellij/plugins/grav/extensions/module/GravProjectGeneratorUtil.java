package net.offbeatpioneer.intellij.plugins.grav.extensions.module;

import com.intellij.ide.lightEdit.project.LightEditDumbService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.tasks.InstallDevtoolsPlugin;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.wizard.CopyFileVisitor;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravProjectSettings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.intellij.ide.projectView.actions.MarkRootActionBase.findContentEntry;

public class GravProjectGeneratorUtil {

    public GravProjectGeneratorUtil(Project project) {
//        super(project);
    }


    public boolean generateProject(@NotNull Project project, @NotNull VirtualFile src, @NotNull GravProjectSettings settings, @NotNull Module module) {
//        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        String targetPath = module.getProject().getBasePath();
        if (Objects.isNull(targetPath)) {
            NotificationHelper.showBaloon("Project path couldn't be determined. Please try again or restart the IDE.", MessageType.ERROR, project);
            return false;
        }
        Task.Backgroundable installDevtools = new InstallDevtoolsPlugin(project, "Installing Devtools Plugin", new File(targetPath));
        Task t = new Task.Modal(project, "Copying Grav 'SDK' to Module Folder", false) {

            @Override
            public void onSuccess() {
                super.onSuccess();
                NotificationHelper.showInfoNotification(getProject(), String.format("New Grav project '%s' was created.", getProject().getName()));
                ProgressManager.getInstance().run(installDevtools);
            }

            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    Files.walkFileTree(new File(settings.gravInstallationPath).toPath(), new CopyFileVisitor(new File(targetPath).toPath()));
                } catch (IOException e) {
                    NotificationHelper.showErrorNotification(project, e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        };

        ProgressManager.getInstance().run(t);

//        ApplicationManager.getApplication().runWriteAction(() -> {
//            ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
//            src.refresh(false, true);
//            ContentEntry contentEntry;
//            if (Objects.nonNull(contentEntry = findContentEntry(model, src))) {
//                VirtualFile baseFilePath = src;
//
//                List<String> excludeFolders = new ArrayList<>(Arrays.asList("cache", "bin", ".github", ".phan", "backup", "logs", "tmp", "vendor"));
//                for (String each : excludeFolders) {
//                    if (src.findChild(each) != null) {
////                        contentEntry.addExcludeFolder(Paths.get(src.getPath(), each).toString());
//                        contentEntry.addExcludeFolder(src.findChild(each));
//                    }
//                }
//                try {
//                    List<String> includePath = new ArrayList<>();
//                    includePath.add(src.getPath());
////                PhpIncludePathManager.getInstance(project).setIncludePath(includePath);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                contentEntry.addSourceFolder(src, false);
//                model.commit();
//                src.refresh(false, true);
//            }
//        });

        return true;
    }
}

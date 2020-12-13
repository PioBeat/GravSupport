package net.offbeatpioneer.intellij.plugins.grav.extensions.module;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformUtils;
import com.jetbrains.php.config.library.PhpIncludePathManager;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.tasks.InstallDevtoolsPlugin;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.wizard.CopyFileVisitor;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
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

/**
 * Utility class for creating and setting up Grav-based projects.
 * This class provides the necessary functionality such as copying the Grav SDK to the freshly created project folder.
 *
 * @author Dominik Grzelak
 */
public class GravProjectGeneratorUtil {

    public GravProjectGeneratorUtil(Project project) {
    }

    public void setupContentRoot(Module module) {
        if (!PlatformUtils.isPhpStorm()) {
            ApplicationManager.getApplication().runWriteAction(() -> {
                ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
                if (Objects.nonNull(module.getProject().getBasePath())) {
                    VirtualFile moduleFile = VfsUtil.findFile(Paths.get(module.getProject().getBasePath()), true);
                    if (Objects.nonNull(moduleFile)) {
                        moduleFile.refresh(false, true);
                        List<String> includePath = new ArrayList<>();
                        VirtualFile gravFolder = VfsUtil.findRelativeFile(moduleFile, "system", "src", "Grav");
                        if (Objects.nonNull(gravFolder)) {
                            includePath.add(gravFolder.getPath());
                        }
                        VirtualFile vendors = VfsUtil.findRelativeFile(moduleFile, "vendor");
                        List<String> excludeNames = new ArrayList<>(Arrays.asList("bin", "autoload.php"));
                        if (Objects.nonNull(vendors)) {
                            for (VirtualFile eachVendor : vendors.getChildren()) {
                                if (!excludeNames.contains(eachVendor.getName())) {
                                    includePath.add(eachVendor.getPath());
                                }
                            }
                        }
                        PhpIncludePathManager.getInstance(module.getProject()).setIncludePath(includePath);
                        //PhpProjectSharedConfiguration // php language level
                        model.commit();
//            if (Objects.nonNull(contentEntry = findContentEntry(model, src))) {
//                List<String> excludeFolders = new ArrayList<>(Arrays.asList("cache", "bin", ".github", ".phan", "backup", "logs", "tmp", "vendor"));
//                for (String each : excludeFolders) {
//                    if (src.findChild(each) != null) {
////                        contentEntry.addExcludeFolder(Paths.get(src.getPath(), each).toString());
//                        contentEntry.addExcludeFolder(src.findChild(each));
//                    }
//                }
//                contentEntry.addSourceFolder(src, false);
//                model.commit();
//                src.refresh(false, true);

                    }
                }
            });
        }
    }

    public boolean generateProject(@NotNull final Project project, @NotNull final VirtualFile src, @NotNull final GravProjectSettings settings, @NotNull final Module module) {
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
                NotificationHelper.showInfoNotification(getProject(), String.format("New project '%s' was created.", getProject().getName()));
                ProgressManager.getInstance().run(installDevtools);
                setupContentRoot(module);
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

        return true;
    }
}

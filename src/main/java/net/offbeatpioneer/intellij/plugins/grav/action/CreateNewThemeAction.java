package net.offbeatpioneer.intellij.plugins.grav.action;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteActionAware;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.WebModuleTypeBase;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import net.offbeatpioneer.intellij.plugins.grav.extensions.icons.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravModuleType;
import net.offbeatpioneer.intellij.plugins.grav.helper.FileCreateUtil;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import net.offbeatpioneer.intellij.plugins.grav.helper.ProcessUtils;
import net.offbeatpioneer.intellij.plugins.grav.listener.GravProjectComponent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @author Dominik Grzelak
 */
public class CreateNewThemeAction extends AnAction implements WriteActionAware, DumbAware {

    private final static String Text = "Grav Create Theme";

    private NewThemeData themeData;

    public CreateNewThemeAction() {
        super(Text, "Create a new theme", GravIcons.GravDefaultIcon);
    }

    @Override
    public void update(final AnActionEvent e) {
        final DataContext dataContext = e.getDataContext();
        final Presentation presentation = e.getPresentation();

        final boolean enabled = isAvailable(dataContext);

        presentation.setVisible(enabled);
        presentation.setEnabled(enabled);

        if (enabled) {
            final Module module = LangDataKeys.MODULE.getData(dataContext);
            String moduleName = module == null ? "" : module.getName();
            presentation.setText(Text + " (" + moduleName + ")");
        }
    }

    private boolean isAvailable(DataContext dataContext) {
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final Module module = LangDataKeys.MODULE.getData(dataContext);
        if (module == null) return false;
        final ModuleType moduleType = ModuleType.get(module);

        final boolean pluginEnabled = GravProjectComponent.isEnabled(project);
        final boolean isGravModule = moduleType instanceof GravModuleType || moduleType instanceof WebModuleTypeBase;

        final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        boolean check2 = project != null && view != null && view.getDirectories().length != 0;
        return pluginEnabled && isGravModule && check2;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final DataContext dataContext = e.getDataContext();
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final Module module = LangDataKeys.MODULE.getData(dataContext);
        final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view == null) {
            return;
        }

        CreateNewThemeDialogWrapper dialog = new CreateNewThemeDialogWrapper(project);
        dialog.show();

        if (dialog.getExitCode() == 0) {
            themeData = dialog.getThemeData();
            if (Objects.isNull(project) || Objects.isNull(module)) {
                NotificationHelper.showBaloon("Project or module couldn't be determined. Please try again or restart the IDE.", MessageType.ERROR, project);
                return;
            }
            createTheme(project, module);
        }
    }

    private void createTheme(@NotNull Project project, Module module) {
//        String[] commands = {"echo", "0", "|", "php", "bin/plugin", "devtools", "new-theme",
        String[] commands = {"echo", "pure-blank", "|", "php", "bin/plugin", "devtools", "new-theme",
                "--name", themeData.getName(),
                "--description", themeData.getDescription(),
                "--developer", themeData.getDeveloper(),
                "--email", themeData.getEmail(),
                "--githubid", themeData.getGitHubId()
        };

        String srcPath = module.getProject().getBasePath();
        if (Objects.isNull(srcPath)) {
            NotificationHelper.showBaloon("Project path couldn't be determined. Please try again or restart the IDE.", MessageType.ERROR, project);
            return;
        }
        ProcessUtils processUtils = new ProcessUtils(commands, new File(srcPath));
        Task.Backgroundable t = new Task.Backgroundable(project, "Create New Grav Theme in Module '" + module.getName() + "'") {
            @Override
            public boolean shouldStartInBackground() {
                return true;
            }

            @Override
            public void onSuccess() {
                super.onSuccess();
                String output = processUtils.getOutputAsString();
                if (processUtils.getErrorOutput() != null) {
                    NotificationHelper.showBaloon(processUtils.getErrorOutput(), MessageType.ERROR, project);
                } else if (!output.contains("SUCCESS")) {
                    output = "Theme couldn't be created: " + output;
                    NotificationHelper.showBaloon(output, MessageType.WARNING, project, 5000);
                } else {
                    NotificationHelper.showBaloon("Theme '" + themeData.getName() + "' was created", MessageType.INFO, project);
                    VirtualFileManager.getInstance().syncRefresh();
                }
            }

            @Override
            public void onFinished() {
                super.onFinished();
                VirtualFile virtualFile = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(Paths.get(srcPath));
                if (Objects.nonNull(virtualFile)) {
                    VirtualFile childDirectory = FileCreateUtil.getChildDirectory(virtualFile, "user/themes/" + themeData.getName().replaceAll("\\s+", "-"));
                    if (!childDirectory.equals(virtualFile)) {
                        PsiManager instance = PsiManager.getInstance(project);
                        PsiDirectory directory = instance.findDirectory(childDirectory);
                        if (Objects.nonNull(directory)) {
                            directory.navigate(true);
                        }
                    }
                }
            }

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                processUtils.execute();
            }
        };
        ProgressManager.getInstance().run(t);
    }
}

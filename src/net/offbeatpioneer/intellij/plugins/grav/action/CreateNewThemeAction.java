package net.offbeatpioneer.intellij.plugins.grav.action;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteActionAware;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import net.offbeatpioneer.intellij.plugins.grav.helper.ProcessUtils;
import net.offbeatpioneer.intellij.plugins.grav.module.GravModuleType;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Dominik Grzelak
 */
public class CreateNewThemeAction extends AnAction implements WriteActionAware {

    private final static String Text = "Grav Create Theme";

    private NewThemeData themeData;

    public CreateNewThemeAction() {
        super(Text, "Create A New Theme", GravIcons.Grav);
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
            presentation.setText(Text + " (" + module.getName() + ")");
        }
    }

    private boolean isAvailable(DataContext dataContext) {
        final Module module = LangDataKeys.MODULE.getData(dataContext);
        final ModuleType moduleType = module == null ? null : ModuleType.get(module);
        final boolean isGravModule = moduleType instanceof GravModuleType;
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        boolean check2 = project != null && view != null && view.getDirectories().length != 0;
        return isGravModule && check2;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final DataContext dataContext = e.getDataContext();

        final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view == null) {
            return;
        }
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final Module module = dataContext.getData(DataKeys.MODULE);

        CreateThemeDialog dialog = new CreateThemeDialog();
        dialog.setModal(true);
        themeData = dialog.showDialog();

        //Todo validate themedata in case in dialog went something wrong
        if(dialog.EXIT_CODE == CreateThemeDialog.OK) {
            createTheme(project, module);
        }
    }

    private void createTheme(Project project, Module module) {
        String[] commands = {"echo", "0", "|", "php", "bin/plugin", "devtools", "new-theme",
                "--name", themeData.getName(),
                "--description", themeData.getDescription(),
                "--developer", themeData.getDeveloper(),
                "--email", themeData.getEmail()};


        ModuleRootManager root = ModuleRootManager.getInstance(module);
        VirtualFile srcFile = null;
        for (VirtualFile file : root.getSourceRoots()) {
            if (file.isDirectory() && file.getName().contentEquals("src")) {
                srcFile = file;
                break;
            }
        }

        if (srcFile == null) {
            NotificationHelper.showBaloon("No source root found in module '" + module.getName() + "'",
                    MessageType.ERROR, project);
            return;
        }

        VirtualFile finalSrcFile = srcFile;
        ProcessUtils processUtils = new ProcessUtils(commands, new File(finalSrcFile.getPath()));
        Task.Backgroundable t = new Task.Backgroundable(project, "Create New Grav Theme in Module '" + module.getName() + "'") {
            @Override
            public boolean shouldStartInBackground() {
                return true;
            }

            @Override
            public void onSuccess() {
                super.onSuccess();
                if (processUtils.getErrorOutput() != null) {
                    NotificationHelper.showBaloon(processUtils.getErrorOutput(), MessageType.ERROR, project);
                } else {
                    NotificationHelper.showBaloon("Theme '" + themeData.getName() + "' was created", MessageType.INFO, project);
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

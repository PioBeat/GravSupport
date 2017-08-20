package net.offbeatpioneer.intellij.plugins.grav.module.tasks;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import net.offbeatpioneer.intellij.plugins.grav.helper.ProcessUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class InstallDevtoolsPlugin extends Task.Backgroundable {
    private String[] commands = new String[]{"php", "bin/gpm", "install", "devtools"};
    private File targetPath;
    private ProcessUtils processUtils;

    public InstallDevtoolsPlugin(@Nullable Project project, @Nls @NotNull String title, File targetPath) {
        super(project, title);
        this.targetPath = targetPath;
        processUtils = new ProcessUtils(commands, targetPath);
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(getProject());
        if (processUtils.getErrorOutput() != null && !processUtils.getErrorOutput().isEmpty()) {
            NotificationHelper.showBaloon(processUtils.getErrorOutput(), MessageType.ERROR, getProject());
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

}

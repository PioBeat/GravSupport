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

/**
 * @author Dominik Grzelak
 */
public class InstallDevtoolsPlugin extends Task.Backgroundable {
    private final String[] commands = new String[]{"php", "bin/gpm", "install", "-f", "-y", "devtools"}; //TODO could be dependent on the Grav version in the future
    private final File targetPath;
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

        String output = processUtils.getOutputAsString();
        if (processUtils.getErrorOutput() != null) {
//            NotificationHelper.showBaloon(processUtils.getErrorOutput(), MessageType.ERROR, getProject());
            NotificationHelper.showErrorNotification(getProject(), processUtils.getErrorOutput());
        } else if (!output.toLowerCase().contains("success")) {
            output = "DevTools plugin couldn't be installed: " + output;
            NotificationHelper.showErrorNotification(getProject(), output);
//            NotificationHelper.showBaloon(output, MessageType.WARNING, getProject(), 5000);
        } else {
            NotificationHelper.showInfoNotification(getProject(), "Devtools plugin installed");
//            JBPopupFactory.getInstance()
//                    .createHtmlTextBalloonBuilder("Devtools plugin installed", MessageType.INFO, null)
//                    .setFadeoutTime(3500)
//                    .createBalloon()
//                    .show(RelativePoint.getSouthEastOf(statusBar.getComponent()), Balloon.Position.above);
        }
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        processUtils.execute();
    }

}

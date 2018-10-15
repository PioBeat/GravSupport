package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;

/**
 * @author Dominik Grzelak
 * @since 22.07.2017
 */
public class NotificationHelper {

    public static void showBaloon(String msg, MessageType messageType, Project project) {
        showBaloon(msg, messageType, project, Balloon.Position.above);
    }

    public static void showBaloon(String msg, MessageType messageType, Project project, Balloon.Position position) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        showBaloon(msg, messageType, project, RelativePoint.getSouthEastOf(statusBar.getComponent()), position);
    }

    public static void showBaloon(String msg, MessageType messageType, Project project, RelativePoint relativePoint, Balloon.Position position) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(msg, messageType, null)
                .setFadeoutTime(3500)
                .createBalloon()
                .show(relativePoint, position);
    }
}

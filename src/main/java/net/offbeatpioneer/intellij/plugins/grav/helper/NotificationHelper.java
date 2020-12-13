package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Grzelak
 * @since 22.07.2017
 */
public class NotificationHelper {

    public static void showBaloon(String msg, MessageType messageType, @NotNull Project project) {
        showBaloon(msg, messageType, project, Balloon.Position.above, 3500);
    }

    public static void showBaloon(String msg, MessageType messageType, @NotNull Project project, int time) {
        showBaloon(msg, messageType, project, Balloon.Position.above, time);
    }

    public static void showBaloon(String msg, MessageType messageType, @NotNull Project project, Balloon.Position position) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        showBaloon(msg, messageType, project, RelativePoint.getSouthEastOf(statusBar.getComponent()), position, 3500);
    }

    public static void showBaloon(String msg, MessageType messageType, @NotNull Project project, Balloon.Position position, int time) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        showBaloon(msg, messageType, project, RelativePoint.getSouthEastOf(statusBar.getComponent()), position, time);
    }

    public static void showBaloon(String msg, MessageType messageType, @NotNull Project project, RelativePoint relativePoint, Balloon.Position position, int time) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(msg, messageType, null)
                .setFadeoutTime(time)
                .createBalloon()
                .show(relativePoint, position);
    }

    public static void showErrorNotification(@Nullable Project project, @NotNull String content) {
        Notifications.Bus.notify(new Notification("GRAV_BUS_NOTIFICATIONS", "Grav", content, NotificationType.ERROR, null), project);
    }

    public static void showInfoNotification(@Nullable Project project, @NotNull String content) {
        Notifications.Bus.notify(new Notification("GRAV_BUS_NOTIFICATIONS", "Grav", content, NotificationType.INFORMATION, null), project);
    }
}

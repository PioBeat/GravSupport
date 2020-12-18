package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.intellij.notification.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import net.offbeatpioneer.intellij.plugins.grav.extensions.configurables.GravProjectConfigurable;
import net.offbeatpioneer.intellij.plugins.grav.extensions.icons.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.listener.GravProjectComponent;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravProjectSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Helper class to display some specific notification messages.
 *
 * @author Dominik Grzelak
 */
public class IdeHelper {
    public final static String NOTIFICATION_GROUP_ID = "Grav Plugin";
    public final static NotificationGroup GRAV_NOTIFY_GROUP = new NotificationGroup(NOTIFICATION_GROUP_ID, NotificationDisplayType.STICKY_BALLOON, true);

    public static void notifyShowGenericErrorMessage() {
//        ReportMessages.GROUP.createNotification()
        Notifications.Bus.notify(GRAV_NOTIFY_GROUP.createNotification("Grav Plugin: Operation not possible", "Operation could not be executed.", NotificationType.WARNING, null));
    }

    public static void notifyEnableMessage(@NotNull final Project project) {
        final Notification enabledNotification = GRAV_NOTIFY_GROUP.createNotification(
                "Grav plugin",
                "Plugin enabled",
                NotificationType.INFORMATION,
                (n, e) -> {
                    n.expire();
                }
        ).setImportant(false);
        final Notification gravPluginEnableNotificationQuestion = GRAV_NOTIFY_GROUP.createNotification(
                "Grav plugin",
                "<a href=\"enable\">Enable</a> the Grav Plugin now, or open <a href=\"config\">Project Settings</a>. <br/>" +
                        "<a href=\"dismiss\">Do not</a> ask again.",
                NotificationType.INFORMATION,
                (notification, event) -> {
                    if ("config".equals(event.getDescription())) {
                        // open settings dialog and show panel
                        GravProjectConfigurable.show(project);
                        notification.expire();
                    } else if ("enable".equals(event.getDescription())) {
                        enablePluginAndConfigure(project);
//                        Notifications.Bus.notify(enabledNotification, project);
                        enabledNotification.notify(project);
                        notification.expire();
                    } else if ("dismiss".equals(event.getDescription())) {
                        // user dont want to show notification again
                        Objects.requireNonNull(GravProjectSettings.getInstance(project)).dismissEnableNotification = true;
                        notification.expire();
                    }
                }
        ).setImportant(true).setIcon(GravIcons.GravFileLogoIcon);
        gravPluginEnableNotificationQuestion.notify(project);
    }

    public static void notifyConvertMessage(@NotNull final Project project, @NotNull Module module) {
        final Notification convertedNotification = GRAV_NOTIFY_GROUP.createNotification(
                "Grav plugin",
                "Project converted and Plugin enabled",
                NotificationType.INFORMATION,
                (notification, event) -> {
                    notification.expire();
                }
        ).setImportant(false);
        final Notification notification = GRAV_NOTIFY_GROUP.createNotification(
                "Grav plugin",
                "It seems that the project can be converted to a Grav-based project: " +
                        "<a href=\"enable\">Convert now</a>, or " +
                        "<a href=\"dismiss\">Do not</a> ask again.",
                NotificationType.INFORMATION,
                (notification1, event) -> {
                    // handle html click events
                    if ("enable".equals(event.getDescription())) {
                        GravProjectComponent.convertModuleToGravModule(module);
                        enablePluginAndConfigure(project);
                        convertedNotification.notify(project);
                        notification1.expire();
                    } else if ("dismiss".equals(event.getDescription())) {
                        // user dont want to show notification again
                        Objects.requireNonNull(GravProjectSettings.getInstance(project)).dismissEnableNotification = true;
                        notification1.expire();
                    }
                }
        );
        notification.setImportant(true);
        notification.setIcon(GravIcons.GravFileLogoIcon);

        // Notifications.Bus.notify(notification, project);
        notification.notify(project);
    }

    private static void enablePluginAndConfigure(@NotNull Project project) {
        Objects.requireNonNull(GravProjectSettings.getInstance(project)).pluginEnabled = true;
        GravProjectConfigurable.enableGravToolWindow(project, true);
    }
}

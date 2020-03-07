package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import net.offbeatpioneer.intellij.plugins.grav.project.settings.GravProjectConfigurable;
import net.offbeatpioneer.intellij.plugins.grav.project.settings.GravProjectSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @author Dominik Grzelak
 */
public class IdeHelper {
    public static void notifyEnableMessage(@NotNull final Project project) {
        Notification notification = new Notification("Grav Plugin", "Grav Plugin",
                "<a href=\"enable\">Enable</a> the Grav Plugin now, or open <a href=\"config\">Project Settings</a>. <br/>" +
                        "<a href=\"dismiss\">Do not</a> ask again.", NotificationType.INFORMATION, (notification1, event) -> {
            // handle html click events
            if ("config".equals(event.getDescription())) {
                // open settings dialog and show panel
                GravProjectConfigurable.show(project);
            } else if ("enable".equals(event.getDescription())) {
                enablePluginAndConfigure(project);
                Notifications.Bus.notify(new Notification("Grav Plugin", "Grav Plugin", "Plugin enabled", NotificationType.INFORMATION), project);
            } else if ("dismiss".equals(event.getDescription())) {
                // user dont want to show notification again
                GravProjectSettings.getInstance(project).dismissEnableNotification = true;
            }
            notification1.expire();
        });

        Notifications.Bus.notify(notification, project);
    }

    private static void enablePluginAndConfigure(@NotNull Project project) {
        Objects.requireNonNull(GravProjectSettings.getInstance(project)).pluginEnabled = true;
        GravProjectConfigurable.enableGravToolWindow(project, true);
    }
}

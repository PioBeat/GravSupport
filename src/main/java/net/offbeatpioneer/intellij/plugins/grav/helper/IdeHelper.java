package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import net.offbeatpioneer.intellij.plugins.grav.extensions.configurables.GravProjectConfigurable;
import net.offbeatpioneer.intellij.plugins.grav.extensions.icons.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravModuleType;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravProjectSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @author Dominik Grzelak
 */
public class IdeHelper {

    public static void notifyShowGenericErrorMessage(@NotNull final Project project) {
        Notifications.Bus.notify(new Notification("Grav Plugin", "Grav Plugin: Operation not possible", "Operation could not be executed.",
                NotificationType.INFORMATION), project);
    }

    public static void notifyEnableMessage(@NotNull final Project project) {
        Notification notification = new Notification("Grav Plugin", "Grav plugin",
                "<a href=\"enable\">Enable</a> the Grav Plugin now, or open <a href=\"config\">Project Settings</a>. <br/>" +
                        "<a href=\"dismiss\">Do not</a> ask again.", NotificationType.INFORMATION, (notification1, event) -> {
            // handle html click events
            if ("config".equals(event.getDescription())) {
                // open settings dialog and show panel
                GravProjectConfigurable.show(project);
            } else if ("enable".equals(event.getDescription())) {
                enablePluginAndConfigure(project);
                Notifications.Bus.notify(new Notification("Grav Plugin", "Grav plugin", "Plugin enabled", NotificationType.INFORMATION), project);
            } else if ("dismiss".equals(event.getDescription())) {
                // user dont want to show notification again
                Objects.requireNonNull(GravProjectSettings.getInstance(project)).dismissEnableNotification = true;
            }
            notification1.expire();
        });

        Notifications.Bus.notify(notification, project);
    }

    public static void notifyConvertMessage(@NotNull final Project project, @NotNull Module module) {
        Notification convertedNotification = new Notification("Grav Plugin", "Grav plugin",
                "Project converted and Plugin enabled",
                NotificationType.INFORMATION, (notification1, event) -> {
            notification1.expire();
        });
        Notification notification = new Notification("Grav Plugin", "Grav plugin",
                "It seems that the project can be converted to a Grav-based project. <br/>" +
                        "<a href=\"enable\">Convert now</a>, or " +
                        "<a href=\"dismiss\">Do not</a> ask again.", NotificationType.INFORMATION, (notification1, event) -> {
            // handle html click events
            if ("enable".equals(event.getDescription())) {
                // open settings dialog and show panel
//                GravProjectConfigurable.show(project);
                module.setModuleType(GravModuleType.ID);
                enablePluginAndConfigure(project);
                Notifications.Bus.notify(convertedNotification);
            } else if ("dismiss".equals(event.getDescription())) {
                // user dont want to show notification again
                Objects.requireNonNull(GravProjectSettings.getInstance(project)).dismissEnableNotification = true;
            } else if ("dismiss".equals(event.getDescription())) {

            }
//            notification1.expire();
        });
        notification.setImportant(true);
        notification.setIcon(GravIcons.GravFileLogoIcon);
        Notifications.Bus.notify(notification, project);
    }

    private static void enablePluginAndConfigure(@NotNull Project project) {
        Objects.requireNonNull(GravProjectSettings.getInstance(project)).pluginEnabled = true;
        GravProjectConfigurable.enableGravToolWindow(project, true);
    }
}

package net.offbeatpioneer.intellij.plugins.grav.project.settings;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Dominik Grzelak
 */
public class GravProjectConfigurable implements Configurable {
    private GravProjectSettings settings;
    private JPanel mainPanel;
    private JCheckBox enabled;

    public GravProjectConfigurable(Project project) {
        settings = GravProjectSettings.getInstance(project);
    }

    /**
     * Helper method to invoke this project settings page in the settings from everywhere
     *
     * @param project current project
     */
    public static void show(@NotNull Project project) {
        ShowSettingsUtilImpl.showSettingsDialog(project, "preferences.Grav.ProjectSettings", null);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Grav";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return mainPanel;
    }

    @Override
    public void reset() {
        enabled.setSelected(settings.pluginEnabled);
    }

    @Override
    public boolean isModified() {
        return settings.pluginEnabled != enabled.isSelected();
    }

    @Override
    public void apply() throws ConfigurationException {
        settings.pluginEnabled = enabled.isSelected();
    }
}

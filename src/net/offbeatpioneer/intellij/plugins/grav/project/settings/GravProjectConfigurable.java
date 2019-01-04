package net.offbeatpioneer.intellij.plugins.grav.project.settings;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
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
    private JCheckBox withSrcDir;
    private final Project project;

    public GravProjectConfigurable(Project project) {
        this.project = project;
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
        withSrcDir.setSelected(settings.withSrcDirectory);
    }

    @Override
    public boolean isModified() {
        return settings.pluginEnabled != enabled.isSelected();
    }

    @Override
    public void apply() throws ConfigurationException {
        settings.pluginEnabled = enabled.isSelected();
        GravProjectConfigurable.enableGravToolWindow(project, settings.pluginEnabled);
    }

    public static void enableGravToolWindow(@NonNull Project project, boolean enable) {
        String[] toolWindowIds = ToolWindowManager.getInstance(project).getToolWindowIds();
//        boolean gravToolWindowExists = false;
        for (String toolWindowId : toolWindowIds) {
            if (toolWindowId.equals("Grav")) {
                ToolWindowManager.getInstance(project).getToolWindow("Grav").setAvailable(enable, () -> {
                });
//                gravToolWindowExists = true;
                break;
            }
        }
//        if(!gravToolWindowExists) {
//            ToolWindowManager.getInstance(project).registerToolWindow("Grav", true, ToolWindowAnchor.RIGHT).setAvailable(true, () -> {});
//        }
    }
}

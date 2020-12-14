package net.offbeatpioneer.intellij.plugins.grav.extensions.configurables;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.listener.GravProjectComponent;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravProjectSettings;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * Project-specific settings page for Grav.
 *
 * @author Dominik Grzelak
 */
public class GravProjectConfigurable implements Configurable, Configurable.VariableProjectAppLevel {
    private GravProjectSettings settings;
    private JPanel mainPanel;
    private JCheckBox enabled;
    private JLabel gravVersionLbl;
    private JLabel currentGravVersionLbl;
    private final Project project;
    private String versionContent;

    public GravProjectConfigurable(Project project) {
        this.project = project;
        settings = GravProjectSettings.getInstance(project);
        versionContent = GravSdkType.findGravSdkVersion(project.getBasePath());
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
        currentGravVersionLbl.setText(versionContent);
        if (!GravSdkType.isOperationBasicallyAvailableFor(project)) {
            currentGravVersionLbl.setEnabled(false);
            enabled.setEnabled(false);
            mainPanel.setEnabled(false);
        } else {
            mainPanel.setEnabled(true);
            currentGravVersionLbl.setEnabled(true);
            enabled.setEnabled(true);
        }
        return mainPanel;
    }

    @Override
    public void reset() {
        if (Objects.nonNull(settings)) {
            enabled.setSelected(settings.pluginEnabled);
        }
    }

    @Override
    public boolean isModified() {
        if (Objects.nonNull(settings)) {
            return settings.pluginEnabled != enabled.isSelected();
        }
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        if (Objects.nonNull(settings)) {
            settings.pluginEnabled = enabled.isSelected();
            if (settings.pluginEnabled) {
                GravProjectConfigurable.enableGravToolWindow(project, settings.pluginEnabled);
//                ProjectView.getInstance(project).refresh();
            }
        }
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

    @Override
    public boolean isProjectLevel() {
        return true;
    }
}

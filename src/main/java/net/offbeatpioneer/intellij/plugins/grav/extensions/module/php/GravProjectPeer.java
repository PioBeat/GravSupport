package net.offbeatpioneer.intellij.plugins.grav.extensions.module.php;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.platform.ProjectGeneratorPeer;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.wizard.GravCreateProjectForm;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravPersistentStateComponent;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravProjectSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static net.offbeatpioneer.intellij.plugins.grav.extensions.module.wizard.CreateGravProjectWizardStep.LAST_USED_GRAV_HOME;

/**
 * Project wizard setup for PhpStorm.
 *
 * @author Dominik Grzelak
 * @since 2017-08-11
 */
public class GravProjectPeer implements ProjectGeneratorPeer<GravProjectSettings> {
    private final GravCreateProjectForm myForm = new GravCreateProjectForm();
    private final GravPersistentStateComponent storage;
    private TextFieldWithBrowseButton myLocationField;

    public GravProjectPeer() {
        this.storage = GravPersistentStateComponent.getInstance();
        String path = storage != null ? storage.getDefaultGravDownloadPath() : null;
        if ((path == null || path.isEmpty())) {
            path = System.getProperty("user.home");
        } else if (PropertiesComponent.getInstance().getValue(LAST_USED_GRAV_HOME) != null) {
            path = PropertiesComponent.getInstance().getValue(LAST_USED_GRAV_HOME);
        }
        myForm.setDefaultInstallationPath(path);
//        ApplicationManager.getApplication().executeOnPooledThread(() -> {
        myForm.initLayout();
//        });
    }


    @NotNull
    @Override
    public JComponent getComponent() {
        return this.myForm.getContentPane();
    }

    @Override
    public void buildUI(@NotNull SettingsStep settingsStep) {
        settingsStep.addSettingsComponent(this.myForm.getContentPane());
    }

    @Override
    public void addSettingsListener(@NotNull SettingsListener listener) {
        this.myForm.addSettingsStateListener(listener);
    }

    /**
     * This method is called after the create button was clicked while the settings being valid
     */
    @NotNull
    @Override
    public GravProjectSettings getSettings() {
        GravProjectSettings settings = GravProjectSettings.getInstance(ProjectManager.getInstance().getDefaultProject());
        if (settings == null) {
            settings = new GravProjectSettings();
        }
        settings.gravInstallationPath = myForm.getGravFinalInstallationDirectory();
        settings.pluginEnabled = true;
        return settings;
    }

    @Nullable
    @Override
    public ValidationInfo validate() {
        return this.myForm.validate();
    }

    @Override
    public boolean isBackgroundJobRunning() {
        return true;
    }
}

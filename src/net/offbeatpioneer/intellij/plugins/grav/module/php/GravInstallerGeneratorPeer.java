package net.offbeatpioneer.intellij.plugins.grav.module.php;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.platform.WebProjectGenerator;
import net.offbeatpioneer.intellij.plugins.grav.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.module.wizard.IntroStepGUI;
import net.offbeatpioneer.intellij.plugins.grav.project.settings.GravProjectConfigurable;
import net.offbeatpioneer.intellij.plugins.grav.project.settings.GravProjectSettings;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravPersistentStateComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;

import static net.offbeatpioneer.intellij.plugins.grav.module.wizard.GravIntroWizardStep.LAST_USED_GRAV_HOME;

/**
 * Created by Dome on 11.08.2017.
 */
public class GravInstallerGeneratorPeer implements ProjectGeneratorPeer<GravProjectSettings> {
    private IntroStepGUI form;
    private GravPersistentStateComponent storage;
    private GravProjectSettings settings;

    public GravInstallerGeneratorPeer() {
        this.storage = GravPersistentStateComponent.getInstance();
        settings = new GravProjectSettings();
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        if (form == null) {
            form = new IntroStepGUI(ProjectManager.getInstance().getDefaultProject());
            String path = storage.getDefaultGravDownloadPath();
            if ((path == null || path.isEmpty()) && PropertiesComponent.getInstance().getValue(LAST_USED_GRAV_HOME) != null) {
                path = PropertiesComponent.getInstance().getValue(LAST_USED_GRAV_HOME);
            }
            form.setDefaultInstallationPath(path);
            form.getFieldPanel().getTextField().getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    validate0();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    validate0();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {

                }
            });
        }
        return form.getMainPanel();
    }

    @Override
    public void buildUI(@NotNull SettingsStep settingsStep) {
        settingsStep.addSettingsComponent(getComponent());
    }
    //after create button was pressed and everthing is valid
    @NotNull
    @Override
    public GravProjectSettings getSettings() {
        settings.gravInstallationPath = form.getGravDirectory();
        settings.pluginEnabled = true;
        return settings;
    }

    @Nullable
    @Override
    public ValidationInfo validate() {
        int code = validate0();
        switch (code) {
            case -1:
                return new ValidationInfo("Path pointing to Grav installation is empty");
            case -2:
                new ValidationInfo("Path to Grav installation does not exist");
            case -3:
                return new ValidationInfo("Grav installation isn't valid");
            default:
                return null;
        }
    }

    private int validate0() {
        if (form.getGravDirectory().isEmpty()) {
            form.showHint(true);
            return -1;//new ValidationInfo("Path pointing to Grav installation is empty");
        } else {
            String file = form.getGravDirectory();
            VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(new File(file));
            if (vf == null) {
                form.showHint(true);
                return -2; //new ValidationInfo("Path to Grav installation does not exist");
            } else {
                if (!GravSdkType.isValidGravSDK(vf)) {
                    form.showHint(true);
                    return -3; //new ValidationInfo("Grav installation isn't valid");
                }
            }
        }
        form.showHint(false);
        return 0;
    }

    @Override
    public boolean isBackgroundJobRunning() {
        return false;
    }

    @Override
    public void addSettingsStateListener(@NotNull WebProjectGenerator.SettingsStateListener listener) {

    }
}

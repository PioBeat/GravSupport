package net.offbeatpioneer.intellij.plugins.grav.extensions.module.php;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.platform.WebProjectGenerator;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.wizard.CreateGravProjectWizardGUI;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravPersistentStateComponent;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravProjectSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;

import static net.offbeatpioneer.intellij.plugins.grav.extensions.module.wizard.CreateGravProjectWizardStep.LAST_USED_GRAV_HOME;

/**
 * @author Dominik Grzelak
 * @since 11.08.2017
 */
public class GravInstallerGeneratorPeer implements ProjectGeneratorPeer<GravProjectSettings> {
    private CreateGravProjectWizardGUI form;
    private final GravPersistentStateComponent storage;
    private GravProjectSettings settings;

    public GravInstallerGeneratorPeer() {
        this.storage = GravPersistentStateComponent.getInstance();
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        if (form == null) {
            form = new CreateGravProjectWizardGUI(ProjectManager.getInstance().getDefaultProject());
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
                    validate0();
                }
            });
        }
        form.initLayout();
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
        settings = GravProjectSettings.getInstance(ProjectManager.getInstance().getDefaultProject());
        if (settings == null) {
            settings = new GravProjectSettings();
        }
        settings.gravInstallationPath = form.getGravDirectory();
        settings.withSrcDirectory = false;
        settings.pluginEnabled = true;
        return settings;
    }

    @Override
    public void addSettingsListener(@NotNull SettingsListener listener) {
    }

    @Nullable
    @Override
    public ValidationInfo validate() {
        int code = validate0();
        switch (code) {
            case -1:
                return new ValidationInfo("The path pointing to Grav installation is empty").withOKEnabled();
            case -2:
                new ValidationInfo("The path to Grav installation does not exist").withOKEnabled();
            case -3:
                return new ValidationInfo("Grav installation isn't valid").withOKEnabled();
            default:
                return null;
        }
    }

    private int validate0() {
        if (form.getGravDirectory().isEmpty()) {
            form.showHint(true);
            return -1;
        } else {
            String file = form.getGravDirectory();
            VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(new File(file));
            if (vf == null) {
                form.showHint(true);
                return -2;
            } else {
                if (!GravSdkType.isValidGravSDK(vf)) {
                    form.showHint(true);
                    return -3;
                }
            }
            String gravSdkVersion = GravSdkType.findGravSdkVersion(file);
            form.setDeterminedGravVersion(gravSdkVersion);
        }
        form.showHint(false);
        return 0;
    }

    @Override
    public boolean isBackgroundJobRunning() {
        return true;
    }

}

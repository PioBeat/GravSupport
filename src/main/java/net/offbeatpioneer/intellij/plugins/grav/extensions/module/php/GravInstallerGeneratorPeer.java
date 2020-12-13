package net.offbeatpioneer.intellij.plugins.grav.extensions.module.php;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectGeneratorPeer;
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
import java.util.Objects;

import static net.offbeatpioneer.intellij.plugins.grav.extensions.module.wizard.CreateGravProjectWizardStep.LAST_USED_GRAV_HOME;

/**
 * Project wizard for PhpStorm.
 *
 * @author Dominik Grzelak
 * @since 11.08.2017
 */
public class GravInstallerGeneratorPeer implements ProjectGeneratorPeer<GravProjectSettings> {
    private CreateGravProjectWizardGUI form;
    private final GravPersistentStateComponent storage;
    private TextFieldWithBrowseButton myLocationField;

    public GravInstallerGeneratorPeer() {
        this.storage = GravPersistentStateComponent.getInstance();
    }

    @Override
    public @NotNull JComponent getComponent(@NotNull TextFieldWithBrowseButton myLocationField, @NotNull Runnable checkValid) {
        this.myLocationField = myLocationField;
        return getComponent();
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        if (form == null) {
            form = new CreateGravProjectWizardGUI(ProjectManager.getInstance().getDefaultProject());

            form.getGravDownloadFolderFieldPanel().getTextField().getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    validate0();
                    if (Objects.nonNull(myLocationField))
                        myLocationField.getTextField().setText(myLocationField.getTextField().getText()); // to trigger re-validation of project wizard in PhpStorm
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    validate0();
                    if (Objects.nonNull(myLocationField))
                        myLocationField.getTextField().setText(myLocationField.getTextField().getText()); // to trigger re-validation of project wizard in PhpStorm
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    validate0();
                    if (Objects.nonNull(myLocationField))
                        myLocationField.getTextField().setText(myLocationField.getTextField().getText()); // to trigger re-validation of project wizard in PhpStorm
                }
            });
        }
        String path = storage.getDefaultGravDownloadPath();
        if ((path == null || path.isEmpty())) {
            path = System.getProperty("user.home");
        } else if (PropertiesComponent.getInstance().getValue(LAST_USED_GRAV_HOME) != null) {
            path = PropertiesComponent.getInstance().getValue(LAST_USED_GRAV_HOME);
        }
        form.setDefaultInstallationPath(path);
        form.initLayout();
        return form.getMainPanel();
    }

    @Override
    public void buildUI(@NotNull SettingsStep settingsStep) {
        settingsStep.addSettingsComponent(getComponent());
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
        settings.gravInstallationPath = form.getGravFinalInstallationDirectory();
        settings.pluginEnabled = true;
        return settings;
    }

    @Nullable
    @Override
    public ValidationInfo validate() {
        int code = validate0();
        switch (code) {
            case -1:
                return new ValidationInfo("The path pointing to Grav is empty for the selected option"); //.withOKEnabled();
            case -2:
                new ValidationInfo("The path pointing to Grav doesn't exist for the selected option"); //.withOKEnabled();
            case -3:
                return new ValidationInfo("The selected Grav 'SDK' seems not valid for the selected option"); //.withOKEnabled();
            default:
                return null;
        }
    }

    private int validate0() {
        String gravDir = form.getGravFinalInstallationDirectory().trim();
        if (gravDir.isEmpty()) {
            form.showHint(true);
            return -1;
        } else {
            VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(new File(gravDir));
            if (vf == null) {
                form.showHint(true);
                return -2;
            } else {
                if (!GravSdkType.isValidGravSDK(vf)) {
                    form.showHint(true);
                    return -3;
                }
            }
            String gravSdkVersion = GravSdkType.findGravSdkVersion(gravDir);
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

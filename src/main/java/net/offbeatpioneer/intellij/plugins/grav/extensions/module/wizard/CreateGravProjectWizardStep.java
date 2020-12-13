package net.offbeatpioneer.intellij.plugins.grav.extensions.module.wizard;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravModuleBuilder;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravPersistentStateComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;


public class CreateGravProjectWizardStep extends ModuleWizardStep { //implements Disposable {
    public static final String LAST_USED_GRAV_HOME = "LAST_USED_GRAV_HOME";
    private GravModuleBuilder builder;
    private CreateGravProjectWizardGUI form;
    private GravPersistentStateComponent storage;
    private final Project project;

    public CreateGravProjectWizardStep(GravModuleBuilder builder, @Nullable Project project) {
        this.builder = builder;
        this.storage = GravPersistentStateComponent.getInstance();
        this.project = project;
    }

    @Override
    public JComponent getComponent() {
        if (form == null) {
            form = new CreateGravProjectWizardGUI();
            form.getGravDownloadFolderFieldPanel().getTextField().getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    try {
                        validate();
                    } catch (ConfigurationException configurationException) {
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    try {
                        validate();
                    } catch (ConfigurationException configurationException) {
                    }

                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    try {
                        validate();
                    } catch (ConfigurationException configurationException) {
                    }
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
    public boolean validate() throws ConfigurationException {
        String finalGravInstallDir = form.getGravFinalInstallationDirectory().trim();
        if (finalGravInstallDir.isEmpty()) {
            form.showHint(true);
            throw new ConfigurationException("The path pointing to Grav is empty for the selected option");
        } else {
            VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(new File(finalGravInstallDir));
            if (vf == null) {
                form.showHint(true);
                throw new ConfigurationException("The path pointing to Grav doesn't exist for the selected option");
            } else {
                if (!GravSdkType.isValidGravSDK(vf)) {
                    form.showHint(true);
                    throw new ConfigurationException("The selected Grav 'SDK' seems not valid");
                }
            }
            String gravSdkVersion = GravSdkType.findGravSdkVersion(finalGravInstallDir);
            form.setDeterminedGravVersion(gravSdkVersion);
        }
        form.showHint(false);
        return true;
    }

    /**
     * Is called after {@link CreateGravProjectWizardStep#validate()}.
     */
    @Override
    public void updateDataModel() {
        String file = form.getGravFinalInstallationDirectory();
        builder.setGravInstallPath(LocalFileSystem.getInstance().findFileByIoFile(new File(file)));
        PropertiesComponent.getInstance().setValue(LAST_USED_GRAV_HOME, new File(file).getAbsolutePath());
    }

    @Override
    public void updateStep() {
        super.updateStep();
    }

//    @Override
//    public void dispose() {
//        if (Objects.nonNull(form) && Objects.nonNull(form.getMainPanel())) {
//            DialogWrapper.cleanupRootPane(form.getMainPanel().getRootPane());
//        }
//    }
}

package net.offbeatpioneer.intellij.plugins.grav.extensions.module.wizard;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.builder.GravModuleBuilder;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravPersistentStateComponent;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravSdkType;

import javax.swing.*;
import java.io.File;


public class CreateGravProjectWizardStep extends ModuleWizardStep implements Disposable {
    public static final String LAST_USED_GRAV_HOME = "LAST_USED_GRAV_HOME";
    private GravModuleBuilder builder;
    private CreateGravProjectWizardGUI form;
    private GravPersistentStateComponent storage;
    private final Project project;

    public CreateGravProjectWizardStep(GravModuleBuilder builder, Project project) {
        this.builder = builder;
        this.storage = GravPersistentStateComponent.getInstance();
        this.project = project;
    }

    @Override
    public JComponent getComponent() {
        if (form == null) {
            form = new CreateGravProjectWizardGUI(this.project);
            String path = storage.getDefaultGravDownloadPath();
            if ((path == null || path.isEmpty()) && PropertiesComponent.getInstance().getValue(LAST_USED_GRAV_HOME) != null) {
                path = PropertiesComponent.getInstance().getValue(LAST_USED_GRAV_HOME);
            }
            form.setDefaultInstallationPath(path);
        }
        form.initLayout();
        return form.getMainPanel();
    }

    @Override
    public boolean validate() throws ConfigurationException {
        if (form.getGravDirectory().isEmpty()) {
            form.showHint(true);
            throw new ConfigurationException("Path pointing to Grav installation is empty");
        } else {
            String file = form.getGravDirectory();
            VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(new File(file));
            if (vf == null) {
                form.showHint(true);
                throw new ConfigurationException("Path to Grav installation does not exist");
            } else {
                if (!GravSdkType.isValidGravSDK(vf)) {
                    form.showHint(true);
                    throw new ConfigurationException("Grav installation isn't valid");
                }
            }
        }
//        if (StringUtil.isEmpty(sdkPanel.getSdkName())) {
//            throw new ConfigurationException("Specify Grav SDK");
//        }
        form.showHint(false);
        return super.validate();
    }

    /**
     * Is called after {@link CreateGravProjectWizardStep#validate()}.
     */
    @Override
    public void updateDataModel() {
        String file = form.getGravDirectory();
        builder.setGravInstallPath(LocalFileSystem.getInstance().findFileByIoFile(new File(file)));
//        builder.setWithSrcDirectory(false);
        PropertiesComponent.getInstance().setValue(LAST_USED_GRAV_HOME, new File(file).getAbsolutePath());
    }

    @Override
    public void updateStep() {
        super.updateStep();
    }

    @Override
    public void dispose() {

    }
}

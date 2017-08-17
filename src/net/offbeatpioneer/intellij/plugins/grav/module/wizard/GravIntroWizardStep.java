package net.offbeatpioneer.intellij.plugins.grav.module.wizard;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.module.builder.GravModuleBuilder;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravPersistentStateComponent;
import net.offbeatpioneer.intellij.plugins.grav.module.GravSdkType;

import javax.swing.*;
import java.io.File;


public class GravIntroWizardStep extends ModuleWizardStep implements Disposable {
    public static final String LAST_USED_GRAV_HOME = "LAST_USED_GRAV_HOME";
    private GravModuleBuilder builder;
    private IntroStepGUI form;
    private GravPersistentStateComponent storage;

    public GravIntroWizardStep(GravModuleBuilder builder) {
        this.builder = builder;
        this.storage = GravPersistentStateComponent.getInstance();
    }

    @Override
    public JComponent getComponent() {
        if (form == null) {
            form = new IntroStepGUI(builder.getProject());
            String path = storage.getDefaultGravDownloadPath();
            if ((path == null || path.isEmpty()) && PropertiesComponent.getInstance().getValue(LAST_USED_GRAV_HOME) != null) {
                path = PropertiesComponent.getInstance().getValue(LAST_USED_GRAV_HOME);
            }
            form.setDefaultInstallationPath(path);
        }
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


    @Override
    public void updateDataModel() {
        String file = form.getGravDirectory();
        System.out.println(file);
        builder.setGravInstallPath(LocalFileSystem.getInstance().findFileByIoFile(new File(file)));
        PropertiesComponent.getInstance().setValue(LAST_USED_GRAV_HOME, new File(file).getAbsolutePath());
    }

    @Override
    public void dispose() {

    }
}

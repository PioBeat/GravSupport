package net.offbeatpioneer.intellij.plugins.grav.extensions.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.builder.GravModuleBuilder;

import javax.swing.*;

@Deprecated
public class GravModuleWizardStep extends ModuleWizardStep {
    private GravModuleBuilder myBuilder;

    public GravModuleWizardStep(GravModuleBuilder builder) {
        myBuilder = builder;
    }

    @Override
    public JComponent getComponent() {
        return new JLabel("Provide some setting here");
    }

    @Override
    public boolean validate() throws ConfigurationException {
//        if (StringUtil.isEmpty(sdkPanel.getSdkName())) {
//            throw new ConfigurationException("Specify Redline Smalltalk SDK");
//        }
        return super.validate();
    }

    @Override
    public void updateDataModel() {
        //todo update model according to UI
    }
}
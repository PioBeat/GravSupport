package net.offbeatpioneer.intellij.plugins.grav.project.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Dominik Grzelak
 */
public class GravProjectSettings implements Configurable {
    private GravProjectSettingsForm settingsForm;

    public GravProjectSettings() {
    }


    @Nls
    @Override
    public String getDisplayName() {
        return "Grav";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsForm = new GravProjectSettingsForm();
        return settingsForm.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }
}

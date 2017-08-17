package net.offbeatpioneer.intellij.plugins.grav.project.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;

/**
 * @author Dominik Grzelak
 */
public class GravProjectConfigurable implements Configurable {
    private GravProjectSettingsForm settingsForm;
    GravProjectSettings settings = new GravProjectSettings();

    public GravProjectConfigurable() {
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

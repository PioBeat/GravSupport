package net.offbeatpioneer.intellij.plugins.grav.application.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravPersistentStateComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Application based configuration for Grav in the Settings menu of IntelliJ
 * <p>
 * What it does:
 * <ul>
 * <li>Set default download directory for Grav downloads when creating a Grav project/module</li>
 * </ul>
 *
 * @author Dominik Grzelak
 */
public class GravApplicationConfigurable implements Configurable {

    private ApplicationConfigForm applicationConfigForm = new ApplicationConfigForm();
    private GravPersistentStateComponent storage;

    public GravApplicationConfigurable() {
        storage = GravPersistentStateComponent.getInstance();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Grav";
    }

    @Override
    public void reset() {
        applicationConfigForm.getFieldPanel().setText(storage.getDefaultGravDownloadPath());
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return applicationConfigForm.getMainPanel();
    }

    @Override
    public boolean isModified() {
        if (applicationConfigForm.getFieldPanel().getText().contentEquals(storage.getDefaultGravDownloadPath())) {
            return false;
        }
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        storage.setDefaultGravDownloadPath(applicationConfigForm.getDefaultDownloadPath());
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "Select the directory where the Grav installation exists or should be downloaded when creating a new module";
    }

    @Override
    public void disposeUIResources() {

    }
}

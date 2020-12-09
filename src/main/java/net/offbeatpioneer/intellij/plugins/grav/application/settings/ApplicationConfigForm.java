package net.offbeatpioneer.intellij.plugins.grav.application.settings;

import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.ui.FieldPanel;

import javax.swing.*;

/**
 * Created by Dome on 24.07.2017.
 */
public class ApplicationConfigForm {

    private JPanel mainPanel;
    FieldPanel fieldPanel;
    private JLabel lblDefaultDownloadDir;
    private JTextField textField;
    private BrowseFilesListener browseFilesListener;

    public ApplicationConfigForm() {
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void createUIComponents() {
        textField = new JTextField();
        browseFilesListener = new BrowseFilesListener(textField, "Select Directory for Default Grav 'SDK' Installation", "", FileChooserDescriptorFactory.createSingleFileDescriptor());
        fieldPanel = ModuleWizardStep.createFieldPanel(textField, "", browseFilesListener);
    }

    public FieldPanel getFieldPanel() {
        return fieldPanel;
    }

    public String getDefaultDownloadPath() {
        return fieldPanel.getText();
    }
}

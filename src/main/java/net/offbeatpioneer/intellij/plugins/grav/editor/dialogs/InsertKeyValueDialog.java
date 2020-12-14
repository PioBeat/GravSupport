package net.offbeatpioneer.intellij.plugins.grav.editor.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import net.offbeatpioneer.intellij.plugins.grav.editor.TranslationTableModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class InsertKeyValueDialog extends DialogWrapper {

    TranslationTableModel model;
    private String selectedLanguage;
    private JPanel contentPane;
    private JTextField keyText;
    private JTextField valueText;
    private JComboBox<String> languageCombobox;

    public InsertKeyValueDialog(@Nullable Project project, TranslationTableModel model) {
        super(project, true);
        this.model = model;
        setModal(true);
        setTitle("Add a New Language Key-Value Pair");
        init();
    }

    @Override
    protected void init() {
        super.init();
        for (String lang : model.getLanguages()) {
            getLanguageCombobox().addItem(lang);
        }
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        if (contentPane == null) return super.getPreferredFocusedComponent();
        return getKeyField();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public String getKeyText() {
        return keyText.getText();
    }

    public String getValueText() {
        return valueText.getText();
    }

    public JTextField getKeyField() {
        return keyText;
    }

    public JTextField getValueField() {
        return valueText;
    }

    public JComboBox<String> getLanguageCombobox() {
        return languageCombobox;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (getKeyText().isEmpty()) {
            return new ValidationInfo("Key is empty", getKeyField());
        }
        if (model.getAvailableKeys().contains(getKeyText())) {
            return new ValidationInfo("Key already exists", getKeyField());
        }
        return super.doValidate();
    }

    public String getSelectedLangauge() {
        return (String) getLanguageCombobox().getSelectedItem();
    }

    /**
     * Set the current selected langauge of the langauge combobox.
     * The dialog has to be created first to take this into account.
     *
     * @param selectedLanguage language to set as the active option in the combobox
     */
    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
        if (getLanguageCombobox() != null)
            getLanguageCombobox().setSelectedItem(selectedLanguage);
    }
}

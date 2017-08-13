package net.offbeatpioneer.intellij.plugins.grav.editor.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import net.offbeatpioneer.intellij.plugins.grav.editor.TranslationTableModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class InsertKeyValueDialog extends DialogWrapper {

    private InsertKeyValueDialogUI dialogUI;
    TranslationTableModel model;

    public InsertKeyValueDialog(@Nullable Project project, TranslationTableModel model) {
        super(project, true);
        this.model = model;
        setModal(true);
        setTitle("Add a New Key Value Pair");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        dialogUI = new InsertKeyValueDialogUI();
        dialogUI.pack();
        return dialogUI.getContentPane();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (dialogUI.getKeyText().isEmpty()) {
            return new ValidationInfo("Key is empty", dialogUI.getKeyField());
        }
        if (model.getAvailableKeys().contains(dialogUI.getKeyText())) {
            return new ValidationInfo("Key already exists", dialogUI.getKeyField());
        }
        return super.doValidate();
    }

    public InsertKeyValueDialogUI getDialogUI() {
        return dialogUI;
    }
}

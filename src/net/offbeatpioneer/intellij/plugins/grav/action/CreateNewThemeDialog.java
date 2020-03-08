package net.offbeatpioneer.intellij.plugins.grav.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CreateNewThemeDialog extends DialogWrapper {
    private CreateThemeDialog dialog;

    protected CreateNewThemeDialog(@Nullable Project project) {
        super(project);
        init();
        setModal(true);
        setTitle("Create New Grav Theme");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (dialog == null) {
            dialog = new CreateThemeDialog();
        }
        return dialog.getMainPanel();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return dialog.textField1;
    }

    public NewThemeData getThemeData() {
        return dialog.getThemeData();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (dialog.textField1.getText().isEmpty()) {
            return new ValidationInfo("Please specify a name for the new theme", dialog.textField1);
        } else if(dialog.textField2.getText().isEmpty() || dialog.textField3.getText().isEmpty() || dialog.textField4.getText().isEmpty() || dialog.textField5.getText().isEmpty()) {
            return new ValidationInfo("Please fill out all empty fields", dialog.textField1);
        }
        return super.doValidate();
    }
}

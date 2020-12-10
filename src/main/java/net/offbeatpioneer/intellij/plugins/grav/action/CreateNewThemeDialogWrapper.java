package net.offbeatpioneer.intellij.plugins.grav.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CreateNewThemeDialogWrapper extends DialogWrapper {
    private CreateThemeDialog dialog;

    protected CreateNewThemeDialogWrapper(@Nullable Project project) {
        super(project);
        init();
        setModal(true);
        setTitle("Create New Grav Theme");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (dialog == null) {
            dialog = new CreateThemeDialog(getDisposable());
        }
        return dialog.getMainPanel();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return dialog.themeName;
    }

    public NewThemeData getThemeData() {
        return dialog.getThemeData();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (StringUtils.isBlank(dialog.themeName.getText()) ||
                StringUtils.isBlank(dialog.description.getText()) ||
                StringUtils.isBlank(dialog.developer.getText()) ||
                StringUtils.isBlank(dialog.email.getText()) ||
                StringUtils.isBlank(dialog.githubId.getText())) {
            dialog.invalidate();
            return new ValidationInfo("Please fill out all empty fields");
        }
        return null;
    }
}

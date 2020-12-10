package net.offbeatpioneer.intellij.plugins.grav.action;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CreateThemeDialog extends JDialog implements CaretListener {
    private JPanel mainPanel;
    public JTextField themeName;
    public JTextField description;
    public JTextField developer;
    public JTextField email;
    public JTextField githubId;
    public JSeparator jpsep;
    private static final String MESSAGE = "The field '%s' must not be empty";
    private NewThemeData themeData = new NewThemeData();

    public CreateThemeDialog(@NotNull Disposable disposable) {
        setContentPane(mainPanel);
        setModal(true);

        themeName.addCaretListener(this);
        description.addCaretListener(this);
        developer.addCaretListener(this);
        email.addCaretListener(this);
        githubId.addCaretListener(this);

        ThemeDialogTextfieldValidator themeNameValidator = new ThemeDialogTextfieldValidator(disposable, themeName, "Theme Name");
        ThemeDialogTextfieldValidator descriptionValidator = new ThemeDialogTextfieldValidator(disposable, description, "Description");
        ThemeDialogTextfieldValidator developerValidator = new ThemeDialogTextfieldValidator(disposable, developer, "Developer");
        ThemeDialogTextfieldValidator emailValidator = new ThemeDialogTextfieldValidator(disposable, email, "Email");
        ThemeDialogTextfieldValidator githubIdValidator = new ThemeDialogTextfieldValidator(disposable, githubId, "GithubId");
    }

//    public static void main(String[] args) {
//        CreateThemeDialog dialog = new CreateThemeDialog();
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
//    }

    @Override
    public void caretUpdate(CaretEvent e) {
        if (e.getSource() == themeName) {
            themeData.setName(themeName.getText());
        }
        if (e.getSource() == description) {
            themeData.setDescription(description.getText());
        }
        if (e.getSource() == developer) {
            themeData.setDeveloper(developer.getText());
        }
        if (e.getSource() == email) {
            themeData.setEmail(email.getText());
        }
        if (e.getSource() == githubId) {
            themeData.setGitHubId(githubId.getText());
        }
    }

    public NewThemeData getThemeData() {
        return themeData;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public static class ThemeDialogTextfieldValidator extends ComponentValidator {
        final JTextField textField;
        final String lblId;

        public ThemeDialogTextfieldValidator(@NotNull Disposable parentDisposable, JTextField textField, String lblId) {
            super(parentDisposable);
            this.textField = textField;
            this.lblId = lblId;
            Supplier<? extends ValidationInfo> validator = (Supplier<ValidationInfo>) () -> {
                String pt = ThemeDialogTextfieldValidator.this.textField.getText();
                if (StringUtil.isEmpty(pt) || pt.trim().isEmpty()) {
                    return new ValidationInfo(String.format(MESSAGE, ThemeDialogTextfieldValidator.this.lblId), ThemeDialogTextfieldValidator.this.textField);
                }
                return null;
            };
//            andStartOnFocusLost();
            withValidator(validator);
            this.textField.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                protected void textChanged(@NotNull DocumentEvent e) {
                    ComponentValidator.getInstance(ThemeDialogTextfieldValidator.this.textField).ifPresent(ComponentValidator::revalidate);
                }
            });
            installOn(this.textField);
        }
    }
}

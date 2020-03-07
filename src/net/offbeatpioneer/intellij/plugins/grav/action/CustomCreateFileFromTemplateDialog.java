/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.offbeatpioneer.intellij.plugins.grav.action;

import com.intellij.ide.actions.ElementCreator;
import com.intellij.ide.actions.TemplateKindCombo;
import com.intellij.lang.LangBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.JBUI;
import net.offbeatpioneer.intellij.plugins.grav.files.AbstractGravFileType;
import net.offbeatpioneer.intellij.plugins.grav.files.GravFileTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * @author peter
 */
public class CustomCreateFileFromTemplateDialog extends DialogWrapper {
    private JTextField myNameField;
    private TemplateKindCombo myKindCombo;
    private JPanel myPanel;
    private JLabel myUpDownHint;
    private JLabel myKindLabel;
    private JLabel myNameLabel;

    private ElementCreator myCreator;
    private InputValidator myInputValidator;

    protected CustomCreateFileFromTemplateDialog(@NotNull Project project) {
        super(project, true);
        myPanel = new JPanel();
        myNameLabel = new JLabel("Name:");
        myNameField = new JTextField("");
        myUpDownHint = new JLabel();

        myKindLabel = new JLabel("Kind:");
        myKindCombo = new TemplateKindCombo();

        myPanel.setLayout(new GridBagLayout());
        myPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

//        c.gridwidth = 1;
        c.weightx = 0.3;
        c.gridx = 0;
        c.gridy = 0;
        myPanel.add(myNameLabel, c);

        c.weightx = 4;
        c.gridx = 1;
        c.gridy = 0;
        myPanel.add(myNameField, c);

        c.weightx = 0.3;
        c.gridx = 2;
        c.gridy = 0;
        c.insets = JBUI.insetsLeft(12);
        myPanel.add(myUpDownHint, c);

        c.insets = JBUI.insets(10, 0);
        c.gridx = 0;
        c.gridy = 1;
        myPanel.add(myKindLabel, c);

        c.gridx = 1;
        c.gridwidth = 2;
        c.gridy = 1;
        c.insets = JBUI.insets(10, 0, 10, 12);
        myPanel.add(myKindCombo, c);


        myKindLabel.setLabelFor(myKindCombo);
        myKindCombo.registerUpDownHint(myNameField);
        myUpDownHint.setIcon(PlatformIcons.UP_DOWN_ARROWS);
        setTemplateKindComponentsVisible(false);
        init();
        myKindCombo.getComboBox().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (AbstractGravFileType each : GravFileTypes.CONFIGURATION_FILE_TYPES) {
                    if (getKindCombo().getSelectedName().equalsIgnoreCase(each.getCorrespondingTemplateFile())) {
                        disableEnableFields(each.needsFilename());
                        if (!each.needsFilename()) {
                            getNameField().setText(each.getDefaultFilename());
                        } else {
                            getNameField().setText("");
                        }
                    }
                }
            }
        });
    }

    public void disableEnableFields(boolean enabled) {
        getNameField().setEnabled(enabled);
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (myInputValidator != null) {
            final String text = myNameField.getText().trim();
            final boolean canClose = myInputValidator.canClose(text);
            if (!canClose) {
                String errorText = LangBundle.message("incorrect.name");
                if (myInputValidator instanceof InputValidatorEx) {
                    String message = ((InputValidatorEx) myInputValidator).getErrorText(text);
                    if (message != null) {
                        errorText = message;
                    }
                }
                return new ValidationInfo(errorText, myNameField);
            }
        }
        return super.doValidate();
    }

    public JTextField getNameField() {
        return myNameField;
    }

    public TemplateKindCombo getKindCombo() {
        return myKindCombo;
    }

    protected JLabel getKindLabel() {
        return myKindLabel;
    }

    protected JLabel getNameLabel() {
        return myNameLabel;
    }

    private String getEnteredName() {
        final JTextField nameField = getNameField();
        final String text = nameField.getText().trim();
        nameField.setText(text);
        return text;
    }

    @Override
    protected JComponent createCenterPanel() {
        return myPanel;
    }

    @Override
    protected void doOKAction() {
        if (myCreator != null && myCreator.tryCreate(getEnteredName()).length == 0) {
            return;
        }
        super.doOKAction();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return getNameField();
    }

    public void setTemplateKindComponentsVisible(boolean flag) {
        myKindCombo.setVisible(flag);
        myKindLabel.setVisible(flag);
        myUpDownHint.setVisible(flag);
    }

    public static Builder createDialog(@NotNull final Project project) {
        final CustomCreateFileFromTemplateDialog dialog = new CustomCreateFileFromTemplateDialog(project);
        return new BuilderImpl(dialog, project);
    }

    private static class BuilderImpl implements Builder {
        private final CustomCreateFileFromTemplateDialog myDialog;
        private final Project myProject;

        public BuilderImpl(CustomCreateFileFromTemplateDialog dialog, Project project) {
            myDialog = dialog;
            myProject = project;
        }

        @Override
        public Builder setTitle(String title) {
            myDialog.setTitle(title);
            return this;
        }

        @Override
        public Builder addKind(@NotNull String name, @Nullable Icon icon, @NotNull String templateName) {
            myDialog.getKindCombo().addItem(name, icon, templateName);
//      if (myDialog.getKindCombo().getComboBox().getItemCount() > 1) {
            myDialog.setTemplateKindComponentsVisible(true);
//      }
            return this;
        }

        @Override
        public Builder setValidator(InputValidator validator) {
            myDialog.myInputValidator = validator;
            return this;
        }

        @Override
        public <T extends PsiElement> T show(@NotNull String errorTitle, @Nullable String selectedTemplateName,
                                             @NotNull final FileCreator<T> creator) {
            final Ref<T> created = Ref.create(null);
            myDialog.getKindCombo().setSelectedName(selectedTemplateName);
            myDialog.myCreator = new ElementCreator(myProject, errorTitle) {

                @Override
                protected PsiElement[] create(String newName) throws Exception {
                    final T element = creator.createFile(myDialog.getEnteredName(), myDialog.getKindCombo().getSelectedName());
                    created.set(element);
                    if (element != null) {
                        return new PsiElement[]{element};
                    }
                    return PsiElement.EMPTY_ARRAY;
                }

                @Override
                public boolean startInWriteAction() {
                    return creator.startInWriteAction();
                }

                @Override
                protected String getActionName(String newName) {
                    return creator.getActionName(newName, myDialog.getKindCombo().getSelectedName());
                }
            };

            myDialog.show();
            if (myDialog.getExitCode() == OK_EXIT_CODE) {
                return created.get();
            }
            return null;
        }

        @Nullable
        @Override
        public Map<String, String> getCustomProperties() {
            return null;
        }
    }

    public interface Builder {
        Builder setTitle(String title);

        Builder setValidator(InputValidator validator);

        Builder addKind(@NotNull String kind, @Nullable Icon icon, @NotNull String templateName);

        @Nullable
        <T extends PsiElement> T show(@NotNull String errorTitle, @Nullable String selectedItem, @NotNull FileCreator<T> creator);

        @Nullable
        Map<String, String> getCustomProperties();
    }

    public interface FileCreator<T> {

        @Nullable
        T createFile(@NotNull String name, @NotNull String templateName);

        @NotNull
        String getActionName(@NotNull String name, @NotNull String templateName);

        boolean startInWriteAction();
    }
}

package net.offbeatpioneer.intellij.plugins.grav.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ThrowableRunnable;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import net.offbeatpioneer.intellij.plugins.grav.helper.Triple;
import net.offbeatpioneer.intellij.plugins.grav.project.GravProjectComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLValue;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemSettingsToolWindowFactory implements ToolWindowFactory, PsiTreeChangeListener, DumbAware {
    private ToolWindow toolWindow;
    private JPanel mainPanel;
    private JCheckBox cbTranslationEnabled;
    private JCheckBox cbActiveLanguageBrowser;
    private JCheckBox cbIncludeDefaultLang;
    private YAMLFile systemYamlFile;
    private YAMLDocument systemDocument;
    private boolean errorOccurred = false;
    private YAMLElementGenerator generator;
    List<Triple<String[], Class, JComponent>> componentList = new ArrayList<>();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content;
        if (!errorOccurred) {
            content = contentFactory.createContent(mainPanel, "", false);
            toolWindow.getContentManager().addContent(content);
        } else {
            content = contentFactory.createContent(new JLabel("An error occurred reading the user/config/system.yaml file"), "", false);
        }
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public void init(ToolWindow window) {
        System.out.println(window);
        componentList.add(new Triple<>(new String[]{"languages", "translations"}, Boolean.class, cbTranslationEnabled));
        componentList.add(new Triple<>(new String[]{"languages", "http_accept_language"}, Boolean.class, cbActiveLanguageBrowser));
        componentList.add(new Triple<>(new String[]{"languages", "include_default_lang"}, Boolean.class, cbIncludeDefaultLang));

        try {
            refreshComponents(true);
        } catch (Exception e) {
            errorOccurred = true;
        }
    }

    private void refreshComponents(boolean addListener) {
        for (Triple<String[], Class, JComponent> each : componentList) {
            applyGravSettings(each.getFirst(), each.getSecond(), each.getThird(), addListener);
        }
    }

    private void applyGravSettings(String[] qualifiedKey, Class dataType, JComponent component, boolean addListener) {
        YAMLKeyValue value = YAMLUtil.getQualifiedKeyInDocument(systemDocument, Arrays.asList(qualifiedKey));
        boolean error = false;
        try {
            if (value == null) {
                error = true;
            } else {
                YAMLValue yamlValue = value.getValue();
                if (yamlValue == null) error = true;
                else {
                    String valueText = yamlValue.getText();
                    if (dataType == Boolean.class) {
                        if (!valueText.equalsIgnoreCase("true") && !valueText.equalsIgnoreCase("false")) {
                            error = true;
                        }
                        Boolean b = Boolean.valueOf(valueText);
                        ((JCheckBox) component).setSelected(b);
                        if (addListener)
                            ((JCheckBox) component).addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        WriteCommandAction.writeCommandAction(systemYamlFile.getProject()).run(new ThrowableRunnable<Throwable>() {
                                            @Override
                                            public void run() throws Throwable {
                                                JCheckBox cb = (JCheckBox) e.getSource();
                                                String value = cb.isSelected() ? "true" : "false";
                                                YAMLKeyValue keyValue = generator.createYamlKeyValue(qualifiedKey[qualifiedKey.length - 1], value);
                                                YAMLKeyValue originalValue = YAMLUtil.getQualifiedKeyInDocument(systemDocument, Arrays.asList(qualifiedKey));
                                                if (originalValue != null) {
                                                    originalValue.replace(keyValue);
                                                    systemYamlFile.getVirtualFile().refresh(false, false);
                                                }
                                            }
                                        });
                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                        NotificationHelper.showBaloon("Could not replace value in /user/config/system.yaml", MessageType.ERROR, systemYamlFile.getProject());
                                    }
                                }
                            });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = true;
        }

        if (error) {
            component.setEnabled(false);
        } else {
            component.setEnabled(true);
        }
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        systemYamlFile = null;
        boolean pluginEnabled = GravProjectComponent.isEnabled(project);
        generator = YAMLElementGenerator.getInstance(project);
        if (pluginEnabled) {
            VirtualFile systemFile = project.getBaseDir().findFileByRelativePath("user/config/system.yaml");
            if (systemFile != null) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(systemFile);
                if (psiFile != null && psiFile instanceof YAMLFile) {
                    systemYamlFile = (YAMLFile) psiFile;
                    if (systemYamlFile.getDocuments().size() > 0) {
                        PsiManager.getInstance(project).addPsiTreeChangeListener(this);

                        systemDocument = systemYamlFile.getDocuments().get(0);
                        System.out.println(systemYamlFile);
                    }
                }
            }
        }
        return pluginEnabled && systemYamlFile != null && systemDocument != null;
    }

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        if (event.getFile() != null && event.getFile().getName().equalsIgnoreCase("system.yaml")
                && event.getFile().getParent() != null && event.getFile().getParent().getName().equalsIgnoreCase("config")) {
            refreshComponents(false);
        }
    }


    @Override
    public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {

    }
}

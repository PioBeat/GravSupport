package net.offbeatpioneer.intellij.plugins.grav.toolwindow;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.*;
import com.intellij.psi.impl.smartPointers.SmartPointerManagerImpl;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ThrowableRunnable;
import net.offbeatpioneer.intellij.plugins.grav.helper.GravYAMLUtils;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import net.offbeatpioneer.intellij.plugins.grav.helper.ProjectChecker;
import net.offbeatpioneer.intellij.plugins.grav.helper.Triple;
import net.offbeatpioneer.intellij.plugins.grav.project.GravProjectComponent;
import net.offbeatpioneer.intellij.plugins.grav.project.settings.GravProjectSettings;
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

/**
 * @author Dominik Grzelak
 * @since 2017-08-24
 */
public class SystemSettingsToolWindowFactory implements ToolWindowFactory, PsiTreeChangeListener {
    private final static String SYSTEM_SYSTEM_CONFIG_FILE = "system/config/system.yaml";
    private final static String USER_SYSTEM_CONFIG_FILE = "user/config/system.yaml";

    private ToolWindow toolWindow;
    private JPanel mainPanel;
    private JCheckBox cbTranslationEnabled;
    private JCheckBox cbActiveLanguageBrowser;
    private JCheckBox cbIncludeDefaultLang;
    private SmartPsiElementPointer<YAMLFile> systemYamlFile;
    private SmartPsiElementPointer<YAMLDocument> systemDocument;
    private boolean errorOccurred = false;
    private YAMLElementGenerator generator;
    private List<Triple<String[], Class, JComponent>> componentList = new ArrayList<>();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content;
        if (!errorOccurred) {
            content = contentFactory.createContent(mainPanel, "", false);
            toolWindow.getContentManager().addContent(content);
        } else {
            content = contentFactory.createContent(new JLabel("An error occurred reading the system/config/system.yaml file"), "", false);
        }
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public void init(ToolWindow window) {
        Project project = ProjectChecker.getFirstOpenedProject();
        String title = "Configuration";
        if (project != null) {
            title += " for '" + project.getName() + "'";
            PsiManager.getInstance(project).addPsiTreeChangeListener(this);
        }
        window.setTitle(title);
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
        Project firstProject = ProjectChecker.getFirstOpenedProject();
        if (firstProject == null) return;
        systemYamlFile = getSystemFile(firstProject);
        if (systemYamlFile != null && systemYamlFile.getElement() != null && systemYamlFile.getElement().getDocuments().size() > 0) {
            systemDocument = SmartPointerManagerImpl.getInstance(firstProject)
                    .createSmartPsiElementPointer(systemYamlFile.getElement().getDocuments().get(0), systemYamlFile.getElement());
        }

        for (Triple<String[], Class, JComponent> each : componentList) {
            applyGravSettings(each.getFirst(), each.getSecond(), each.getThird(), firstProject, addListener);
        }
    }

    private void applyGravSettings(String[] qualifiedKey, Class dataType, JComponent component, Project project, boolean addListener) {
        boolean error = false;
        try {
            YAMLKeyValue value = YAMLUtil.getQualifiedKeyInDocument(systemDocument.getElement(), Arrays.asList(qualifiedKey));
            if (value == null) {
                value = GravYAMLUtils.getKeyValue(systemYamlFile.getElement(), Arrays.asList(qualifiedKey));
            }
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
                                        WriteCommandAction.writeCommandAction(project).run(new ThrowableRunnable<Throwable>() {
                                            @Override
                                            public void run() throws Throwable {
                                                JCheckBox cb = (JCheckBox) e.getSource();
                                                String value = cb.isSelected() ? "true" : "false";
                                                YAMLKeyValue keyValue = generator.createYamlKeyValue(qualifiedKey[qualifiedKey.length - 1], value);
                                                YAMLKeyValue originalValue = YAMLUtil.getQualifiedKeyInDocument(systemDocument.getElement(), Arrays.asList(qualifiedKey));
                                                if (originalValue != null) {
                                                    originalValue.replace(keyValue);
                                                    systemYamlFile.getVirtualFile().refresh(false, false);
                                                }
                                            }
                                        });
                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                        NotificationHelper.showBaloon("Could not replace value in /system/config/system.yaml", MessageType.ERROR, systemYamlFile.getProject());
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
            systemYamlFile = getSystemFile(project);
            if (systemYamlFile != null && systemYamlFile.getElement() != null && systemYamlFile.getElement().getDocuments().size() > 0) {
                PsiManager.getInstance(project).addPsiTreeChangeListener(this);
                systemDocument = SmartPointerManagerImpl.getInstance(project).createSmartPsiElementPointer(systemYamlFile.getElement().getDocuments().get(0), systemYamlFile.getElement());
                System.out.println(systemYamlFile);
            }
        }
        return pluginEnabled && systemYamlFile != null && systemDocument != null;
    }

    private SmartPsiElementPointer<YAMLFile> getSystemFile(Project project) {
        String prefix = "";
        try {
            prefix = GravProjectSettings.getInstance(project).withSrcDirectory ? "src/" + "" : "";
        } catch (NullPointerException ignored) {
        }

        String path = prefix + SYSTEM_SYSTEM_CONFIG_FILE;
        VirtualFile systemFile = project.getBaseDir().findFileByRelativePath(path);
        if (systemFile != null) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(systemFile);
            if (psiFile != null && psiFile instanceof YAMLFile) {
                systemYamlFile = SmartPointerManagerImpl.getInstance(project).createSmartPsiElementPointer((YAMLFile) psiFile);
                return systemYamlFile;
            }
        }
        return null;
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

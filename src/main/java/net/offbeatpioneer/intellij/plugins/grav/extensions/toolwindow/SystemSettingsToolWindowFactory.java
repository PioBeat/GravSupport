package net.offbeatpioneer.intellij.plugins.grav.extensions.toolwindow;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.*;
import com.intellij.psi.impl.smartPointers.SmartPointerManagerImpl;
import com.intellij.ui.components.labels.ActionLink;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ThrowableRunnable;
import net.offbeatpioneer.intellij.plugins.grav.helper.*;
import net.offbeatpioneer.intellij.plugins.grav.listener.GravProjectComponent;
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
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Grav tool window support.
 *
 * @author Dominik Grzelak
 * @since 2017-08-24
 */
public class SystemSettingsToolWindowFactory implements ToolWindowFactory, PsiTreeChangeListener {
//    private final static String SYSTEM_SYSTEM_CONFIG_FILE = "system/config/system.yaml";
//    private final static String USER_SYSTEM_CONFIG_FILE = "user/config/system.yaml";

    private ToolWindow toolWindow;
    private JPanel mainPanel;
    private JCheckBox cbTranslationEnabled;
    private JCheckBox cbActiveLanguageBrowser;
    private JCheckBox cbIncludeDefaultLang;
    private ActionLink navigateToFileLink;
    private SmartPsiElementPointer<YAMLFile> systemYamlFile;
    private SmartPsiElementPointer<YAMLDocument> systemDocument;
    private boolean errorOccurred = false;
    private YAMLElementGenerator generator;
    private List<Triple<String[], Class, JComponent>> componentList = new ArrayList<>();
    private @NotNull Project project;
    GravSystemYamlConfigFiles gravSystemYamlConfigFilesLevel = GravSystemYamlConfigFiles.GRAV_1_6_30;

    public SystemSettingsToolWindowFactory() {

    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
        this.project = project;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content;
        if (!errorOccurred) {
            content = contentFactory.createContent(this.mainPanel, "Grav", false);
        } else {
            content = contentFactory.createContent(new JLabel("An error occurred reading the system/config/system.yaml file"), "Grav", false);
        }
        this.toolWindow.getContentManager().addContent(content);


//        Project project = ProjectChecker.getFirstOpenedProject();
        String title = "Configuration";
        title += " for '" + project.getName() + "'";
        PsiManager.getInstance(project).addPsiTreeChangeListener(this);
        toolWindow.setTitle(title);

    }

    public void createUIComponents() {
        navigateToFileLink = new ActionLink("Navigate to File", new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent e) {
//                project.getBasePath()
//                FileCreateUtil.getChildDirectory()
                if (Objects.isNull(project) || Objects.isNull(project.getBasePath())) return;
                VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Paths.get(project.getBasePath()));
//                VirtualFile virtualFile = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(Paths.get(srcPath));
                if (Objects.nonNull(virtualFile)) {
                    VirtualFile fileByRelativePath = virtualFile.findFileByRelativePath(gravSystemYamlConfigFilesLevel.getSystemYamlPath());
                    PsiManager instance = PsiManager.getInstance(project);
                    if (Objects.nonNull(fileByRelativePath) && Objects.nonNull(instance.findFile(fileByRelativePath)))
                        instance.findFile(fileByRelativePath).navigate(true);
                }
            }
        });
    }

    @Override
    public void init(@NotNull ToolWindow window) {

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
        Project firstProject = ProjectChecker.getFirstOpenedProject() != null ? ProjectChecker.getFirstOpenedProject() : project;
        ApplicationManager.getApplication().runReadAction(() -> {
            systemYamlFile = getGravSystemYamlFile(firstProject);
            if (systemYamlFile != null && systemYamlFile.getElement() != null && systemYamlFile.getElement().getDocuments().size() > 0) {
                systemDocument = SmartPointerManagerImpl.getInstance(firstProject)
                        .createSmartPsiElementPointer(systemYamlFile.getElement().getDocuments().get(0), systemYamlFile.getElement());
            }

            for (Triple<String[], Class, JComponent> each : componentList) {
                applyGravSettings(each.getFirst(), each.getSecond(), each.getThird(), firstProject, addListener);
            }
        });
    }

    private void applyGravSettings(String[] qualifiedKey, Class dataType, JComponent component, final Project project, boolean addListener) {
        if (systemDocument == null || systemDocument.getElement() == null || systemYamlFile == null || systemYamlFile.getElement() == null)
            return;
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
                        boolean b = Boolean.parseBoolean(valueText);
                        ((JCheckBox) component).setSelected(b);
                        if (addListener) {
                            final ActionListener[] listeners = ((JCheckBox) component).getActionListeners();
                            for (int i = 0, n = listeners.length; i < n; i++) {
                                ActionListener al = listeners[i];
                                ((JCheckBox) component).removeActionListener(al);
                            }
                            ((JCheckBox) component).addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        if (project.isDisposed()) return;
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
    public boolean isApplicable(@NotNull Project project) {
        return shouldBeAvailable(project);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        systemYamlFile = null;
        boolean pluginEnabled = GravProjectComponent.isEnabled(project);
        generator = YAMLElementGenerator.getInstance(project);
        if (pluginEnabled) {
            ApplicationManager.getApplication().runReadAction(() -> {
                systemYamlFile = getGravSystemYamlFile(project);
                if (systemYamlFile != null && systemYamlFile.getElement() != null && systemYamlFile.getElement().getDocuments().size() > 0) {
                    PsiManager.getInstance(project).addPsiTreeChangeListener(SystemSettingsToolWindowFactory.this, () -> {
                    });
                    systemDocument = SmartPointerManagerImpl.getInstance(project).createSmartPsiElementPointer(systemYamlFile.getElement().getDocuments().get(0), systemYamlFile.getElement());
                }
            });
        }
        return pluginEnabled && systemYamlFile != null && systemDocument != null;
    }

    private SmartPsiElementPointer<YAMLFile> getGravSystemYamlFile(@NotNull Project project) {
        String prefix = "";
        if (Objects.isNull(project.getBasePath())) {
            IdeHelper.notifyShowGenericErrorMessage();
        }
        VirtualFile projectPath = LocalFileSystem.getInstance().findFileByIoFile(new File(project.getBasePath()));
        if (Objects.nonNull(projectPath) && projectPath.exists()) {
            String path = prefix + gravSystemYamlConfigFilesLevel.getSystemYamlPath();
            VirtualFile systemFile = projectPath.findFileByRelativePath(path);
            if (systemFile != null) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(systemFile);
                if (psiFile instanceof YAMLFile) { // also checks for != null
                    systemYamlFile = SmartPointerManagerImpl.getInstance(project).createSmartPsiElementPointer((YAMLFile) psiFile);
                    return systemYamlFile;
                }
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

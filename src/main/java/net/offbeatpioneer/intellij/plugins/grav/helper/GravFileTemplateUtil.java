package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.twig.TwigFile;
import net.offbeatpioneer.intellij.plugins.grav.extensions.files.AbstractGravFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Dominik Grzelak
 */
public class GravFileTemplateUtil {
    private final static String GRAV_TEMPLATE_PREFIX = "Grav ";

    public static List<FileTemplate> getAvailableThemeConfigurationTemplates(@NotNull Project project) {
        return getApplicableTemplates(new Condition<FileTemplate>() {
            @Override
            public boolean value(FileTemplate fileTemplate) {
                return AbstractGravFileType.DEFAULT_EXTENSION.equals(fileTemplate.getExtension());
            }
        }, project);
    }

    public static FileTemplate getThemeConfigurationTemplateByName(String name, @NotNull Project project) {
        List<FileTemplate> buf = GravFileTemplateUtil.getAvailableThemeConfigurationTemplates(project);
        for (FileTemplate each : buf) {
            if (each.getName().equalsIgnoreCase(name)) {
                return each;
            }
        }
        return null;
    }

    /**
     * Finds the value to a header variable in a Grav markdown file
     *
     * @param variable PsiElement of a valid header variable in a Grav markdown file
     * @return PsiElement of the value of the variable
     */
    public static PsiElement getValueFromVariable(PsiElement variable) {
        PsiElement sibling = variable.getNextSibling();
        int maxIter = 50, iterCount = 0;
        while (sibling != null && sibling.getText().matches(":|\\s*|(:\\s*)") && iterCount < maxIter) {
            sibling = sibling.getNextSibling();
            iterCount++;
        }
        return sibling;
    }

    public static boolean isTwigTemplateFile(PsiFile file) {
        try {
            return file instanceof TwigFile;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    /**
     * Return the current theme which is set in the system/config/system.yaml file
     *
     * @param project the project
     * @return theme name or null
     */
    public static String getDefaultTheme(Project project) {
        if (Objects.isNull(project.getBasePath())) {
            IdeHelper.notifyShowGenericErrorMessage();
            return "";
        }
        VirtualFile projectPath = LocalFileSystem.getInstance().findFileByIoFile(new File(project.getBasePath()));
        VirtualFile vfile = VfsUtil.findRelativeFile(projectPath, "system", "config", "system.yaml");
        if (vfile == null) return null;
        PsiFile psiFile = PsiManager.getInstance(project).findFile(vfile);
        if (psiFile == null) return null;

        YAMLKeyValue yamlKeyValue = GravYAMLUtils.getKeyValue((YAMLFile) psiFile, Arrays.asList(new String[]{"pages", "theme"}));
        if (yamlKeyValue == null) return null;

        return yamlKeyValue.getValueText();
    }

    public static List<FileTemplate> getApplicableTemplates(Condition<FileTemplate> filter, @NotNull Project project) {
        final List<FileTemplate> applicableTemplates = new SmartList<FileTemplate>();
        applicableTemplates.addAll(ContainerUtil.findAll(FileTemplateManager.getInstance(project).getInternalTemplates(), filter));
        applicableTemplates.addAll(ContainerUtil.findAll(FileTemplateManager.getInstance(project).getAllTemplates(), filter));
        return applicableTemplates;
    }

    public static String getTemplateShortName(String templateName) {
        if (templateName.startsWith(GRAV_TEMPLATE_PREFIX)) {
            return templateName.substring(GRAV_TEMPLATE_PREFIX.length());
        }
        return templateName;
    }
}
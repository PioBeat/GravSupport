package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiFile;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.twig.TwigFile;
import net.offbeatpioneer.intellij.plugins.grav.files.GravConfigurationFileType;

import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class GravFileTemplateUtil {
    private final static String GRAV_TEMPLATE_PREFIX = "Grav ";

    public static List<FileTemplate> getAvailableThemeConfigurationTemplates() {
        return getApplicableTemplates(new Condition<FileTemplate>() {
            @Override
            public boolean value(FileTemplate fileTemplate) {
                return GravConfigurationFileType.DEFAULT_EXTENSION.equals(fileTemplate.getExtension());
            }
        });
    }

    public static FileTemplate getThemeConfigurationTemplateByName(String name) {
        List<FileTemplate> buf = GravFileTemplateUtil.getAvailableThemeConfigurationTemplates();
        for(FileTemplate each: buf) {
            if(each.getName().equalsIgnoreCase(name)) {
                return each;
            }
        }
        return null;
    }

    public static boolean isTwigTemplateFile(PsiFile file) {
        try {
            return file instanceof TwigFile;
        } catch (NoClassDefFoundError e){
            return false;
        }
    }

    public static List<FileTemplate> getApplicableTemplates(Condition<FileTemplate> filter) {
        final List<FileTemplate> applicableTemplates = new SmartList<FileTemplate>();
        applicableTemplates.addAll(ContainerUtil.findAll(FileTemplateManager.getInstance().getInternalTemplates(), filter));
        applicableTemplates.addAll(ContainerUtil.findAll(FileTemplateManager.getInstance().getAllTemplates(), filter));
        return applicableTemplates;
    }

    public static String getTemplateShortName(String templateName) {
        if (templateName.startsWith(GRAV_TEMPLATE_PREFIX)) {
            return templateName.substring(GRAV_TEMPLATE_PREFIX.length());
        }
        return templateName;
    }
}
package net.offbeatpioneer.intellij.plugins.grav.action;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteActionAware;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.WebModuleTypeBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.extensions.files.GravFileTypes;
import net.offbeatpioneer.intellij.plugins.grav.helper.FileCreateUtil;
import net.offbeatpioneer.intellij.plugins.grav.helper.GravFileTemplateUtil;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravModuleType;
import net.offbeatpioneer.intellij.plugins.grav.listener.GravProjectComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Creates various Grav specific configuration file stubs for a theme:
 * <ul>
 * <li>Create a blueprints configuration file inside a theme folder</li>
 * <li>Create THEME.yaml config file</li>
 * </ul>
 *
 * @author Dominik Grzelak
 */
public class NewThemeConfigurationFileAction extends CustomCreateFromTemplateAction implements WriteActionAware {
    public NewThemeConfigurationFileAction() {
        super("Grav Files", null, GravIcons.GravDefaultIcon);
    }

    private String themeName;

    @Override
    protected boolean isAvailable(DataContext dataContext) {
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final Module module = LangDataKeys.MODULE.getData(dataContext);
        final ModuleType moduleType = module == null ? null : ModuleType.get(module);
        final boolean isGravModule = moduleType instanceof GravModuleType || moduleType instanceof WebModuleTypeBase;
        final boolean pluginEnabled = GravProjectComponent.isEnabled(project);
        if (!pluginEnabled) return false;
        if (dataContext.getData(PlatformDataKeys.NAVIGATABLE) instanceof PsiDirectory) {
            PsiDirectory psiDirectory = (PsiDirectory) dataContext.getData(PlatformDataKeys.NAVIGATABLE);
            String themeFolder = psiDirectory.getParent().getVirtualFile().getName();
            themeName = psiDirectory.getName();
            GravFileTypes.setModuleName(themeName);
            boolean isThemeFolder = themeFolder.equalsIgnoreCase("themes");
            return super.isAvailable(dataContext) && isGravModule && isThemeFolder;
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    protected PsiElement createFile(String name, String templateName, PsiDirectory dir) {
        try {
            return FileCreateUtil.createFile(name, dir, templateName, this);
        } catch (Exception e) {
            throw new IncorrectOperationException(e.getMessage(), new Throwable(e));
        }
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory directory, CustomCreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle(IdeBundle.message("action.create.new.class"));
        for (FileTemplate fileTemplate : GravFileTemplateUtil.getAvailableThemeConfigurationTemplates(project)) {
            final String templateName = fileTemplate.getName();
            final String shortName = GravFileTemplateUtil.getTemplateShortName(templateName);
            final Icon icon = GravIcons.GravDefaultIcon;
            builder.addKind(shortName, icon, templateName);
        }

        builder.setValidator(new InputValidatorEx() {
            @Override
            public String getErrorText(String inputString) {
//                if (inputString.length() > 0 && !StringUtil.isJavaIdentifier(inputString)) {
//                    return "ERROR MESSAGE";
//                }
                return null;
            }

            @Override
            public boolean checkInput(String inputString) {
                return true;
            }

            @Override
            public boolean canClose(String inputString) {
                return getErrorText(inputString) == null;
            }
        });
    }

    @Override
    protected String getActionName(PsiDirectory directory, String newName, String templateName) {
        return "Creating File " + newName;
    }
}

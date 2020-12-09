package net.offbeatpioneer.intellij.plugins.grav.extensions.module.php;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Dominik Grzelak
 * @since 2017-08-11
 */
public class GravProjectTemplateFactory extends ProjectTemplatesFactory {

    public GravProjectTemplateFactory() {
        System.out.println("GravProjectTemplateFactory");
    }

    @Override
    public String @NotNull [] getGroups() {
        return new String[]{"PHP", WebModuleBuilder.GROUP_NAME};
    }

    public Icon getGroupIcon(String group) {
        return GravIcons.GravDefaultIcon;
    }

    @Override
    public ProjectTemplate @NotNull [] createTemplates(@Nullable String group, WizardContext context) {
        return new ProjectTemplate[]{new GravProjectGenerator()};
    }
}

package net.offbeatpioneer.intellij.plugins.grav.module.php;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Dome on 11.08.2017.
 */
public class GravProjectTemplateFactory extends ProjectTemplatesFactory {

    public GravProjectTemplateFactory() {
        System.out.println("GravProjectTemplateFactory");
    }

    @NotNull
    @Override
    public String[] getGroups() {
        return new String[]{"PHP", WebModuleBuilder.GROUP_NAME};
    }

    public Icon getGroupIcon(String group) {
        return GravIcons.Grav;
    }

    @NotNull
    @Override
    public ProjectTemplate[] createTemplates(@Nullable String group, WizardContext context) {
        return new ProjectTemplate[]{new GravProjectGenerator()};
    }
}

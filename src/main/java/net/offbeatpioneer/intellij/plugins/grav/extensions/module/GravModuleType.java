package net.offbeatpioneer.intellij.plugins.grav.extensions.module;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.extensions.icons.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.listener.GravProjectComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;

public class GravModuleType extends ModuleType<GravModuleBuilder> {
    public static final String ID = "GRAV_TYPE";
    private static final GravModuleType INSTANCE = new GravModuleType();

    public GravModuleType() {
        super(ID);
    }

    @NotNull
    @Override
    public GravModuleBuilder createModuleBuilder() {
        return new GravModuleBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return "Grav";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Add support for Grav";
    }

    @Override
    public @NotNull Icon getIcon() {
        return GravIcons.GravDefaultIcon;
    }

    @Override
    public Icon getNodeIcon(@Deprecated boolean b) {
        return GravIcons.GravDefaultIcon;
    }

    public static GravModuleType getInstance() {
        return INSTANCE; //(GravModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

//    @NotNull
//    @Override
//    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull GravModuleBuilder moduleBuilder, @NotNull ModulesProvider modulesProvider) {
//        return super.createWizardSteps(wizardContext, moduleBuilder, modulesProvider);
//    }
}
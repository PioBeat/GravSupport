package net.offbeatpioneer.intellij.plugins.grav.module;

import com.intellij.openapi.module.ModuleType;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.module.builder.GravModuleBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GravModuleType extends ModuleType<GravModuleBuilder> {
    private static final String ID = "GRAV_TYPE";
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
    public Icon getIcon() {
        return GravIcons.Grav;
    }

    @Override
    public Icon getNodeIcon(@Deprecated boolean b) {
        return GravIcons.Node_Grav;
    }

    public static GravModuleType getInstance() {
        return INSTANCE; //(GravModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }
//    @NotNull
//    @Override
//    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull GravModuleBuilder moduleBuilder, @NotNull ModulesProvider modulesProvider) {
//        //TODO mehr schritte einfügen
//        return super.createWizardSteps(wizardContext, moduleBuilder, modulesProvider);
//    }
}
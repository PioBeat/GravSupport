package net.offbeatpioneer.intellij.plugins.grav.extensions.module;

import com.intellij.framework.FrameworkTypeEx;
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Dominik Grzelak
 */
public class GravFrameworkType extends FrameworkTypeEx {
    public static final String ID = "Grav";

    protected GravFrameworkType() {
        super(ID);
    }

    @NotNull
    @Override
    public FrameworkSupportInModuleProvider createProvider() {
        return new GravModuleProvider();
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Grav";
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return GravIcons.Grav;
    }
}
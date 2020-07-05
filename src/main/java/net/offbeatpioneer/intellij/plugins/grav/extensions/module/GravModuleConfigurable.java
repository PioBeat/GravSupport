package net.offbeatpioneer.intellij.plugins.grav.extensions.module;

import com.intellij.framework.addSupport.FrameworkSupportInModuleConfigurable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.ui.GravPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Dominik Grzelak
 */
public class GravModuleConfigurable extends FrameworkSupportInModuleConfigurable {
    GravPanel gravPanel = new GravPanel();

    @Nullable
    @Override
    public JComponent createComponent() {
        return gravPanel;
    }

    @Override
    public void addSupport(@NotNull Module module, @NotNull ModifiableRootModel rootModel, @NotNull ModifiableModelsProvider modifiableModelsProvider) {
        //do what you want here: setup a library, generate a specific file, etc
    }
}

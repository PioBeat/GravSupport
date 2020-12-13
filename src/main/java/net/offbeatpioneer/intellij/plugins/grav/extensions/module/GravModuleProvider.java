package net.offbeatpioneer.intellij.plugins.grav.extensions.module;

import com.intellij.framework.FrameworkTypeEx;
import com.intellij.framework.addSupport.FrameworkSupportInModuleConfigurable;
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Dominik Grzelak
 */
public class GravModuleProvider extends FrameworkSupportInModuleProvider {

    public GravModuleProvider() {
    }

    @NotNull
    @Override
    public FrameworkTypeEx getFrameworkType() {
        return Objects.requireNonNull(FrameworkTypeEx.EP_NAME.findExtension(GravFrameworkType.class));
    }

    @NotNull
    @Override
    public FrameworkSupportInModuleConfigurable createConfigurable(@NotNull FrameworkSupportModel model) {
        return new GravModuleConfigurable();
    }

    @Override
    public boolean isEnabledForModuleType(@NotNull ModuleType moduleType) {
        return GravModuleType.getInstance().getId().equals(moduleType.getId());
    }
}

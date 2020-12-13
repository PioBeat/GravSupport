package net.offbeatpioneer.intellij.plugins.grav.extensions.icons;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.util.PlatformUtils;
import net.offbeatpioneer.intellij.plugins.grav.extensions.icons.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravModuleType;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.listener.GravProjectComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

public class GravIconProvider extends com.intellij.ide.IconProvider {
    private static final boolean isPhpStorm = PlatformUtils.isPhpStorm();


    @Override
    public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
//        System.out.println(element);
        if (isPhpStorm) {
            if (element.isPhysical() && element instanceof PsiDirectory) {
                for (Project each : ProjectManager.getInstance().getOpenProjects()) {
                    if (!GravSdkType.operationIsAvailableFor(each)) return null;
                    if (((PsiDirectory) element).getVirtualFile().getPath().equals(each.getBasePath()) &&
                            each.getName().equals(((PsiDirectory) element).getName())) {
                        return GravIcons.GravFileLogoIcon;
                    }
                }
            }
        }
        return null;
    }
}

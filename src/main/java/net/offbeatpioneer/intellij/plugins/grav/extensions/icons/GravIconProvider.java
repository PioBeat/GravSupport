package net.offbeatpioneer.intellij.plugins.grav.extensions.icons;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import net.offbeatpioneer.intellij.plugins.grav.extensions.files.ThemeConfigurationFileType;
import net.offbeatpioneer.intellij.plugins.grav.listener.GravProjectComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.YAMLLanguage;

import javax.swing.*;

public class GravIconProvider extends com.intellij.ide.IconProvider {
//    private static final boolean isPhpStorm = PlatformUtils.isPhpStorm();


    @Override
    public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
//        if (isPhpStorm) {
        if (YAMLLanguage.INSTANCE.isKindOf(element.getLanguage())) {
            if (!GravProjectComponent.isEnabled(element.getProject()) &&
                    (element instanceof PsiFile) &&
                    ((PsiFile) element).getName().endsWith(ThemeConfigurationFileType.DEFAULT_EXTENSION)) {
                return YAMLFileType.YML.getIcon();
            }
        }
        if (element.isPhysical() && element instanceof PsiDirectory) {
            for (Project each : ProjectManager.getInstance().getOpenProjects()) {
                if (!GravProjectComponent.isEnabled(each)) return null;
                if (((PsiDirectory) element).getVirtualFile().getPath().equals(each.getBasePath()) &&
                        each.getName().equals(((PsiDirectory) element).getName())) {
                    return GravIcons.GravFileLogoIcon;
                }
            }
        }
//        }
        return null;
    }
}

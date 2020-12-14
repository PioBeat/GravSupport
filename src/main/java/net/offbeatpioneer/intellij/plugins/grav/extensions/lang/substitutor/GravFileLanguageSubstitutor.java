package net.offbeatpioneer.intellij.plugins.grav.extensions.lang.substitutor;

import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutor;
import net.offbeatpioneer.intellij.plugins.grav.extensions.files.ThemeBlueprintsFileType;
import net.offbeatpioneer.intellij.plugins.grav.extensions.files.ThemeConfigurationFileType;
import net.offbeatpioneer.intellij.plugins.grav.listener.GravProjectComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;

/**
 * File Language substitutor for non-Grav projects.
 * <p>
 * We don't want that *.yaml files are displayed as "Grav files" when the project isn't assigned the
 * {@link net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravModuleType}.
 * In that case we display the default one.
 *
 * @author Dominik Grzelak
 */
public class GravFileLanguageSubstitutor extends LanguageSubstitutor {

    @Override
    public @Nullable Language getLanguage(@NotNull VirtualFile file, @NotNull Project project) {
        if (!GravProjectComponent.isEnabled(project) && file.getName().endsWith(ThemeConfigurationFileType.DEFAULT_EXTENSION)) {
//            return null; //ThemeBlueprintsFileType.INSTANCE.getLanguage();
            return YAMLFileType.YML.getLanguage();
        }
        return null;
    }
}

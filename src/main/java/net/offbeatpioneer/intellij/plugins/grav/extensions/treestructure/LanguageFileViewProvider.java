package net.offbeatpioneer.intellij.plugins.grav.extensions.treestructure;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

public class LanguageFileViewProvider implements FileViewProviderFactory {
    @NotNull
    @Override
    public FileViewProvider createFileViewProvider(@NotNull VirtualFile file, Language language, @NotNull PsiManager manager, boolean eventSystemEnabled) {
        return new SingleRootFileViewProvider(manager, file, eventSystemEnabled) {
            @NotNull
            @Override
            public Language getBaseLanguage() {
                return YAMLLanguage.INSTANCE;
            }
        };
    }
}

package net.offbeatpioneer.intellij.plugins.grav.treestructure;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

public class LanguageFileViewProvider implements FileViewProviderFactory {
    @NotNull
    @Override
    public FileViewProvider createFileViewProvider(@NotNull VirtualFile file, Language language, @NotNull PsiManager manager, boolean eventSystemEnabled) {
        return new MultiplePsiFilesPerDocumentFileViewProvider(manager, file, eventSystemEnabled) {
            @NotNull
            @Override
            public Language getBaseLanguage() {
                return YAMLLanguage.INSTANCE;
            }

            @Override
            protected MultiplePsiFilesPerDocumentFileViewProvider cloneInner(VirtualFile fileCopy) {
                return this;
            }
        };
    }
}

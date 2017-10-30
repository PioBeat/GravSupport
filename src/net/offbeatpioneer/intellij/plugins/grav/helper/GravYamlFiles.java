package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static net.offbeatpioneer.intellij.plugins.grav.helper.GravYamlFiles.LangFileEditorType.LANGUAGE_FILE;
import static net.offbeatpioneer.intellij.plugins.grav.helper.GravYamlFiles.LangFileEditorType.LANGUAGE_FOLDER;
import static net.offbeatpioneer.intellij.plugins.grav.helper.GravYamlFiles.LangFileEditorType.NONE;


public class GravYamlFiles {

    private final VirtualFile file;

    public enum LangFileEditorType {
        LANGUAGE_FOLDER("LANGUAGE_FOLDER"), LANGUAGE_FILE("LANGUAGE_FILE"), NONE("NONE");
        String id;

        LangFileEditorType(String id) {
            this.id = id;
        }
    }

    public GravYamlFiles(VirtualFile file) {
        this.file = file;
    }

    public static LangFileEditorType getLanguageFileType(@NotNull final VirtualFile file) {
        VirtualFile parent = file.getParent();
        if (GravYamlFiles.withYamlExtension(file) && parent != null && parent.exists() && parent.getName().equalsIgnoreCase("languages")) {
            return LANGUAGE_FOLDER;
        } else if (GravYamlFiles.withYamlExtension(file) && file.getNameWithoutExtension().equalsIgnoreCase("languages")) {
            return LANGUAGE_FILE;
        }
        return NONE;
    }

    public static boolean withYamlExtension(VirtualFile file) {
        return new GravYamlFiles(file).fileHasYamlExtension();
    }

    private boolean fileHasYamlExtension() {
        String ext = file.getExtension();
        return ext != null && file.getExtension().equalsIgnoreCase("yaml");
    }
}

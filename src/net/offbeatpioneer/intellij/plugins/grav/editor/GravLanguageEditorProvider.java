package net.offbeatpioneer.intellij.plugins.grav.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.editor.strategy.FileEditorStrategy;
import net.offbeatpioneer.intellij.plugins.grav.helper.GravYamlFiles;
import net.offbeatpioneer.intellij.plugins.grav.project.GravProjectComponent;
import org.jetbrains.annotations.NotNull;

import static net.offbeatpioneer.intellij.plugins.grav.helper.GravYamlFiles.LangFileEditorType.NONE;

//TODO listen for file deletion and updates
public class GravLanguageEditorProvider implements FileEditorProvider, DumbAware {

    private final String ID = "GravLanguageEditorProvider";
    GravLangFileEditor gravLangFileEditor = null;
    GravYamlFiles.LangFileEditorType langFileEditorType = NONE;

    public GravLanguageEditorProvider() {
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        if (!GravProjectComponent.isEnabled(project)) {
            return false;
        }
        langFileEditorType = GravYamlFiles.getLanguageFileType(file);
        return gravLangFileEditor == null && langFileEditorType != NONE;
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        if (gravLangFileEditor == null) {
            FileEditorStrategy editorStrategy = FileEditorStrategy.create(this, project);
            editorStrategy.createFileMap(file);
            gravLangFileEditor = new GravLangFileEditor(this, editorStrategy, project);
        }
        return gravLangFileEditor;
    }

    @Override
    public void disposeEditor(@NotNull FileEditor editor) {
    }

    public GravYamlFiles.LangFileEditorType getLangFileEditorType() {
        return langFileEditorType;
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return ID + langFileEditorType;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        if (gravLangFileEditor == null)
            return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }
}

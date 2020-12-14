package net.offbeatpioneer.intellij.plugins.grav.editor;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.editor.strategy.FileEditorStrategy;
import net.offbeatpioneer.intellij.plugins.grav.helper.GravYamlFiles;
import net.offbeatpioneer.intellij.plugins.grav.listener.GravProjectComponent;
import org.jetbrains.annotations.NotNull;

import static net.offbeatpioneer.intellij.plugins.grav.helper.GravYamlFiles.LangFileEditorType.NONE;

//TODO listen for file deletion and updates
public class GravLanguageEditorProvider implements FileEditorProvider {

    private final String ID = "GravLanguageEditorProvider";
    GravYamlFiles.LangFileEditorType langFileEditorType = NONE;
    String languageFile = "";

    public GravLanguageEditorProvider() {
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        if (!GravProjectComponent.isEnabled(project)) {
            return false;
        }
        langFileEditorType = GravYamlFiles.getLanguageFileType(file);
        languageFile = file.getPath();
        return langFileEditorType != NONE;
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        FileEditorStrategy editorStrategy = FileEditorStrategy.create(this, project);
        editorStrategy.createFileMap(file);
        GravLangFileEditor gravLangFileEditor = new GravLangFileEditor(langFileEditorType, editorStrategy);
        gravLangFileEditor.setConnect(project.getMessageBus().connect());
        return gravLangFileEditor;
    }

    @Override
    public void disposeEditor(@NotNull FileEditor editor) {
        if (editor instanceof GravLangFileEditor) {
            Disposer.dispose(editor); //same:  ((GravLangFileEditor) editor).dispose();
        }
    }

    public GravYamlFiles.LangFileEditorType getLangFileEditorType() {
        return langFileEditorType;
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return ID + langFileEditorType + languageFile;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
//        if (gravLangFileEditor == null)
//            return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
        return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR;
    }
}

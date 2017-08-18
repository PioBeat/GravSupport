package net.offbeatpioneer.intellij.plugins.grav.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static net.offbeatpioneer.intellij.plugins.grav.editor.GravLanguageEditorProvider.LangFileEditorType.LANGUAGE_FILE;
import static net.offbeatpioneer.intellij.plugins.grav.editor.GravLanguageEditorProvider.LangFileEditorType.LANGUAGE_FOLDER;
import static net.offbeatpioneer.intellij.plugins.grav.editor.GravLanguageEditorProvider.LangFileEditorType.NONE;

//TODO file deleted and created -> update fileMap
public class GravLanguageEditorProvider implements FileEditorProvider, VirtualFileListener, DumbAware {

    private String ID = "GravLanguageEditorProvider";
    private String[] languages = new String[]{};
    private ConcurrentHashMap<String, VirtualFile> fileMap = new ConcurrentHashMap<>();
    public GravLangFileEditor gravLangFileEditor = null;
    public LangFileEditorType langFileEditorType = NONE;

    public enum LangFileEditorType {
        LANGUAGE_FOLDER, LANGUAGE_FILE, NONE
    }

    public GravLanguageEditorProvider() {
        fileMap = new ConcurrentHashMap<>();
        VirtualFileManager.getInstance().addVirtualFileListener(this);
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        langFileEditorType = isLanguageFile(file);
        if (gravLangFileEditor == null && langFileEditorType != NONE) {
            return true;
        }
        return false;
    }

    public static LangFileEditorType isLanguageFile(VirtualFile file) {
        VirtualFile parent = file.getParent();
        if (file.getExtension().equalsIgnoreCase("yaml") && parent.exists() && parent.getName().equalsIgnoreCase("languages")) {
            return LANGUAGE_FOLDER;
        } else if (file.getExtension().equalsIgnoreCase("yaml") && file.getNameWithoutExtension().equalsIgnoreCase("languages")) {
            return LANGUAGE_FILE;
        }
        return NONE;
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        fileMap.clear();
        switch (langFileEditorType) {
            case LANGUAGE_FOLDER:
                VirtualFile parent = file.getParent();
                VirtualFile[] childs = parent.getChildren();
                languages = new String[childs.length];
                int cnt = 0;
                for (VirtualFile each : childs) {
                    languages[cnt] = each.getNameWithoutExtension();
                    fileMap.put(languages[cnt], each);
                    cnt++;
                }
                FileEditor[] fileEditors = FileEditorManager.getInstance(project).getAllEditors();
                for (FileEditor each : fileEditors) {
                    if (Objects.equals(each.getName(), GravLangFileEditor.NAME)) {
                        return each;
                    }
                }
                break;
            case LANGUAGE_FILE:
                System.out.println(LANGUAGE_FILE);
                //TODO detect languages inside file
                YAMLFile yamlFile = (YAMLFile) PsiManager.getInstance(project).findFile(file);
                Collection<YAMLKeyValue> topLevelKeys = YAMLUtil.getTopLevelKeys(yamlFile);
                for (YAMLKeyValue each : topLevelKeys) {
                    fileMap.put(each.getKeyText(), file);
                }
                break;
        }

        if (gravLangFileEditor == null) {
            gravLangFileEditor = new GravLangFileEditor(this, project, fileMap);
        }
        return gravLangFileEditor;
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

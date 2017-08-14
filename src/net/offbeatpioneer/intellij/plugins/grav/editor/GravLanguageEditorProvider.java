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
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

//TODO file deleted and created -> update fileMap
public class GravLanguageEditorProvider implements FileEditorProvider, VirtualFileListener, DumbAware {

    private String ID = "GravLanguageEditorProvider";
    private String[] languages = new String[]{};
    private ConcurrentHashMap<String, VirtualFile> fileMap = new ConcurrentHashMap<>();
    public GravLangFileEditor gravLangFileEditor = null;

    public GravLanguageEditorProvider() {
        VirtualFileManager.getInstance().addVirtualFileListener(this);
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
//        VirtualFile parent = file.getParent();
//        if (gravLangFileEditor == null && file.getExtension().equalsIgnoreCase("yaml") && parent.exists() && parent.getName().equalsIgnoreCase("languages")) {
//            return true;
//        }
//        return false;
//
        if (gravLangFileEditor == null && isLanguageFile(file)) {
            return true;
        }
        return false;
    }

    public static boolean isLanguageFile(VirtualFile file) {
        VirtualFile parent = file.getParent();
        if (file.getExtension().equalsIgnoreCase("yaml") && parent.exists() && parent.getName().equalsIgnoreCase("languages")) {
            return true;
        }
        return false;
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
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
        for(FileEditor each: fileEditors) {
            if(Objects.equals(each.getName(), GravLangFileEditor.NAME)) {
                return each;
            }
        }
        if (gravLangFileEditor == null) {
            gravLangFileEditor = new GravLangFileEditor(this, project, fileMap);
        }
        return gravLangFileEditor;
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return ID;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        if (gravLangFileEditor == null)
            return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }
}

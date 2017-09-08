package net.offbeatpioneer.intellij.plugins.grav.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import net.offbeatpioneer.intellij.plugins.grav.editor.strategy.FileEditorStrategy;
import net.offbeatpioneer.intellij.plugins.grav.helper.GravYamlFiles;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.impl.YAMLFileImpl;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ConcurrentHashMap;

public class GravLangFileEditor implements Disposable, FileEditor, TableModelListener, VirtualFileListener {
    final static String NAME = "Language Text";
    private LanguageFileEditorGUI editor;

    private Project project;
    private GravLanguageEditorProvider provider;
    private TranslationTableModel model;
    FileEditorStrategy editorStrategy;

    GravLangFileEditor(GravLanguageEditorProvider provider, FileEditorStrategy editorStrategy, Project project) {
        this.provider = provider;
        this.project = project;
        this.editorStrategy = editorStrategy.withFileEditor(this);
        VirtualFileManager.getInstance().addVirtualFileListener(this);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        if (editor == null) {
            model = createTableModel();
            model.addTableModelListener(this);
            editor = new LanguageFileEditorGUI(this, model);
            editor.initTabs();
        }
        return editor.getMainPanel();
    }

    private TranslationTableModel createTableModel() {
        return editorStrategy.createTableModel();
    }

    public LanguageFileEditorGUI getGUI() {
        return editor;
    }

    GravYamlFiles.LangFileEditorType getLanguageFileEditorType() {
        return provider.langFileEditorType;
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
        if (event.getFile().getParent().isDirectory() && event.getFile().getParent().getNameWithoutExtension().compareTo("languages") == 0) {
            if (GravYamlFiles.getLanguageFileType(event.getFile()) != GravYamlFiles.LangFileEditorType.NONE) {
                for (VirtualFile each : getFileMap().values()) {
                    if (each.getName().compareTo(event.getFile().getName()) == 0) {
                        //TODO make changes to model according to file changes
                    }
                }
            }
        }
    }

    ConcurrentHashMap<String, VirtualFile> getFileMap() {
        return editorStrategy.getFileMap();
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {
        for (VirtualFile each : getFileMap().values()) {
            if (each.getName().compareTo(event.getFile().getName()) == 0) {
                getFileMap().remove(each.getNameWithoutExtension());
                model.removeLanguage(each.getNameWithoutExtension());
                editor.initTabs();
            }
        }
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event) {
        //process only for files inside the language directory
        if (event.getFile().getParent().isDirectory() && event.getFile().getParent().getNameWithoutExtension().compareTo("languages") == 0) {
            if (GravYamlFiles.getLanguageFileType(event.getFile()) == GravYamlFiles.LangFileEditorType.LANGUAGE_FOLDER) {
                boolean present = false;
                for (VirtualFile each : getFileMap().values()) {
                    if (each.getName().compareTo(event.getFile().getName()) == 0) {
                        present = true;
                        break;
                    }
                }
                if (!present) {
                    getFileMap().put(event.getFile().getNameWithoutExtension(), event.getFile());
                    model.addLanguage(event.getFile().getNameWithoutExtension());
                    editor.initTabs();
                }
            } else {
                NotificationHelper.showBaloon("Not a valid language resource name", MessageType.WARNING, project);
            }
        }
    }

    /**
     * TableModel listener
     *
     * @param e
     */
    @Override
    public void tableChanged(TableModelEvent e) {
//        TODO Do something with the data...
        System.out.println(e.getType());
        if (e.getType() == TableModelEvent.INSERT) {
            //TODO update all files
//            TranslationTableModel model = (TranslationTableModel) editor.getTable1().getModel();
            String lang = editor.getCurrentLang();
            VirtualFile file = getFileMap().get(lang);
            if (file.exists()) {
                YAMLFileImpl yamlFile = (YAMLFileImpl) PsiManager.getInstance(project).findFile(file);
                if (yamlFile != null) {
                    System.out.println(yamlFile);
                }
            }
        }
    }

    public Project getProject() {
        return project;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public boolean isModified() {
        System.out.println("modified?");
        return false;
    }

    //ToDO
    @Override
    public boolean isValid() {
        return true;//languages != null && languages.length != 0;
    }

    @Override
    public void selectNotify() {

    }

    @Override
    public void deselectNotify() {

    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {
        this.provider.gravLangFileEditor = null;
    }

    @Nullable
    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }


}

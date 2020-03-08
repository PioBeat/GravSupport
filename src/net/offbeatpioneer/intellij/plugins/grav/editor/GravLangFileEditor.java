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
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiManager;
import com.intellij.util.messages.MessageBus;
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GravLangFileEditor implements Disposable, FileEditor, TableModelListener, BulkFileListener {
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
        init(project.getMessageBus());
    }

    public void init(MessageBus messageBus) {
        messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, this);
    }

    @Override
    public void before(@NotNull List<? extends VFileEvent> events) {

    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent each : events) {
            if (each instanceof VFileContentChangeEvent) {
                if (each.getFile().getParent().isDirectory() && each.getFile().getParent().getNameWithoutExtension().compareTo("languages") == 0) {
                    if (GravYamlFiles.getLanguageFileType(each.getFile()) == GravYamlFiles.LangFileEditorType.LANGUAGE_FOLDER) {
                        boolean present = false;
                        for (VirtualFile eachVF : getFileMap().values()) {
                            if (eachVF.getName().compareTo(each.getFile().getName()) == 0) {
                                present = true;
                                break;
                            }
                        }
                        if (!present) {
                            getFileMap().put(each.getFile().getNameWithoutExtension(), each.getFile());
                            model.addLanguage(each.getFile().getNameWithoutExtension());
                            editor.initTabs();
                        }
                    } else {
                        NotificationHelper.showBaloon("Not a valid language resource name", MessageType.WARNING, project);
                    }
                }
            }

            if (each instanceof VFileDeleteEvent) {
                for (VirtualFile eachVF : getFileMap().values()) {
                    if (eachVF.getName().compareTo(each.getFile().getName()) == 0) {
                        getFileMap().remove(eachVF.getNameWithoutExtension());
                        model.removeLanguage(eachVF.getNameWithoutExtension());
                        editor.initTabs();
                    }
                }
            }

            if (each instanceof VFileCreateEvent) {
                //process only for files inside the language directory
                if (each.getFile().getParent().isDirectory() && each.getFile().getParent().getNameWithoutExtension().compareTo("languages") == 0) {
                    if (GravYamlFiles.getLanguageFileType(each.getFile()) == GravYamlFiles.LangFileEditorType.LANGUAGE_FOLDER) {
                        boolean present = false;
                        for (VirtualFile eachVF : getFileMap().values()) {
                            if (eachVF.getName().compareTo(each.getFile().getName()) == 0) {
                                present = true;
                                break;
                            }
                        }
                        if (!present) {
                            getFileMap().put(each.getFile().getNameWithoutExtension(), each.getFile());
                            model.addLanguage(each.getFile().getNameWithoutExtension());
                            editor.initTabs();
                        }
                    } else {
                        NotificationHelper.showBaloon("Not a valid language resource name", MessageType.WARNING, project);
                    }
                }
            }
        }
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

    public ConcurrentHashMap<String, VirtualFile> getFileMap() {
        return editorStrategy.getFileMap();
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

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
import net.offbeatpioneer.intellij.plugins.grav.editor.strategy.LanguageFileStrategy;
import net.offbeatpioneer.intellij.plugins.grav.editor.strategy.LanguageFolderStrategy;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;
import org.jetbrains.yaml.psi.impl.YAMLFileImpl;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.offbeatpioneer.intellij.plugins.grav.editor.GravLanguageEditorProvider.LangFileEditorType.LANGUAGE_FOLDER;
import static net.offbeatpioneer.intellij.plugins.grav.editor.GravLanguageEditorProvider.LangFileEditorType.NONE;

//TODO
//You should be performing modifications through a Document, not through a
//VirtualFile.
//        See http://confluence.jetbrains.net/display/IDEADEV/IntelliJIDEAArchitectural+Overview
//        for more information.
public class GravLangFileEditor implements Disposable, FileEditor, TableModelListener, ChangeListener, VirtualFileListener {
    public final static String NAME = "Language Text";
    private LanguageFileEditorGUI editor;
    private String[] languages;
    //lang <-> lang-file
    private ConcurrentHashMap<String, VirtualFile> fileMap = new ConcurrentHashMap<>();
    private Project project;
    private GravLanguageEditorProvider provider;
    private TranslationTableModel model;
    FileEditorStrategy editorStrategy;

    public GravLangFileEditor(GravLanguageEditorProvider provider, Project project, ConcurrentHashMap<String, VirtualFile> fileMap) {
        this.provider = provider;
        this.project = project;
        this.fileMap = fileMap;
        this.languages = Collections.list(fileMap.keys()).toArray(new String[fileMap.size()]);
        VirtualFileManager.getInstance().addVirtualFileListener(this);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        if (editor == null) {
            model = createTableModel();
            model.addTableModelListener(this);
            editor = new LanguageFileEditorGUI(this, languages, model);
            editor.initTabs(project, fileMap);
            setDefaultLanguageForEditor();
        }
        return editor.getMainPanel();
    }

    public void setDefaultLanguageForEditor() {
        if (fileMap.size() != 0) {
            String lang = fileMap.keys().hasMoreElements() ? fileMap.keys().nextElement() : "";
            editor.setCurrentLang(lang);
        }
    }

    public TranslationTableModel createTableModel() {

        switch (provider.langFileEditorType) {
            case LANGUAGE_FOLDER:
                editorStrategy = new LanguageFolderStrategy(languages, project);
                break;
            case LANGUAGE_FILE:
                editorStrategy = new LanguageFileStrategy(languages, project);
                break;
        }
        return editorStrategy.createTableModel(fileMap);
    }

    public GravLanguageEditorProvider.LangFileEditorType getLanguageFileEditorType() {
        return provider.langFileEditorType;
    }

    /**
     * Tab change listener
     *
     * @param e
     */
    @Override
    public void stateChanged(ChangeEvent e) {
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
        if (event.getFile().getParent().isDirectory() && event.getFile().getParent().getNameWithoutExtension().compareTo("languages") == 0) {
            if (GravLanguageEditorProvider.isLanguageFile(event.getFile()) != NONE) {
                for (VirtualFile each : fileMap.values()) {
                    if (each.getName().compareTo(event.getFile().getName()) == 0) {
                        //TODO make changes to model according to file changes
                    }
                }
            }
        }
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {
        for (VirtualFile each : fileMap.values()) {
            if (each.getName().compareTo(event.getFile().getName()) == 0) {
                fileMap.remove(each.getNameWithoutExtension());
                model.removeLanguage(each.getNameWithoutExtension());
                editor.initTabs(project, fileMap);
                setDefaultLanguageForEditor();
            }
        }
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event) {
        //process only for files inside the language directory
        if (event.getFile().getParent().isDirectory() && event.getFile().getParent().getNameWithoutExtension().compareTo("languages") == 0) {
            if (GravLanguageEditorProvider.isLanguageFile(event.getFile()) == LANGUAGE_FOLDER) {
                boolean present = false;
                for (VirtualFile each : fileMap.values()) {
                    if (each.getName().compareTo(event.getFile().getName()) == 0) {
                        present = true;
                        break;
                    }
                }
                if (!present) {
                    fileMap.put(event.getFile().getNameWithoutExtension(), event.getFile());
                    model.addLanguage(event.getFile().getNameWithoutExtension());
                    editor.initTabs(project, fileMap);
                    setDefaultLanguageForEditor();
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
            VirtualFile file = fileMap.get(lang);
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

    @Override
    public boolean isValid() {
        return languages != null && languages.length != 0;
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

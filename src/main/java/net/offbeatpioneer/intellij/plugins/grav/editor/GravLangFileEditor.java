package net.offbeatpioneer.intellij.plugins.grav.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.messages.MessageBusConnection;
import net.offbeatpioneer.intellij.plugins.grav.editor.strategy.FileEditorStrategy;
import net.offbeatpioneer.intellij.plugins.grav.helper.GravYamlFiles;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GravLangFileEditor implements FileEditor, TableModelListener, BulkFileListener {
    final static String NAME = "Grav Language Editor";
    private GravLanguageEditorForm myEditorUI;

    private TranslationTableModel model;
    FileEditorStrategy editorStrategy;
    MessageBusConnection connect;

    public GravLangFileEditor(GravYamlFiles.LangFileEditorType langFileEditorType, FileEditorStrategy editorStrategy) {
        this.editorStrategy = editorStrategy.withFileEditor(this);
        model = createTableModel();
        model.addTableModelListener(this);
        myEditorUI = new GravLanguageEditorForm(editorStrategy, langFileEditorType, model);
        myEditorUI.initTabs();
        myEditorUI.setTab(0);
    }

    public MessageBusConnection getConnect() {
        return connect;
    }

    public void setConnect(MessageBusConnection connect) {
        if (this.connect == null) {
            this.connect = connect;
            connect.subscribe(VirtualFileManager.VFS_CHANGES, this);
        }
    }

    @Override
    public void before(@NotNull List<? extends VFileEvent> events) {
        if (!isValid()) return;
    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        if (!isValid()) return;
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
                            myEditorUI.initTabs();
                        }
                    } else {
//                        NotificationHelper.showBaloon("Not a valid language resource name", MessageType.WARNING, project);
                        NotificationHelper.showErrorNotification(null, "Not a valid language resource name");
                    }
                }
            }

            if (each instanceof VFileDeleteEvent) {
                for (VirtualFile eachVF : getFileMap().values()) {
                    if (eachVF.getName().compareTo(each.getFile().getName()) == 0) {
                        getFileMap().remove(eachVF.getNameWithoutExtension());
                        model.removeLanguage(eachVF.getNameWithoutExtension());
                        myEditorUI.initTabs();
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
                            myEditorUI.initTabs();
                        }
                    } else {
//                        NotificationHelper.showBaloon("Not a valid language resource name", MessageType.WARNING, project);
                        NotificationHelper.showErrorNotification(null, "Not a valid language resource name");
                    }
                }
            }
        }
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return myEditorUI.getContentPane();
    }

    private TranslationTableModel createTableModel() {
        return editorStrategy.createTableModel();
    }

    public GravLanguageEditorForm getForm() {
        return myEditorUI;
    }

//    GravYamlFiles.LangFileEditorType getLanguageFileEditorType() {
//        return provider.langFileEditorType;
//    }

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
//        System.out.println(e.getType());
        if (e.getType() == TableModelEvent.INSERT) {
            //TODO update all files
//            TranslationTableModel model = (TranslationTableModel) editor.getTable1().getModel();
            String lang = myEditorUI.getCurrentLang();
            VirtualFile file = getFileMap().get(lang);
            if (file.exists()) {
//                YAMLFileImpl yamlFile = (YAMLFileImpl) PsiManager.getInstance(project).findFile(file);
//                if (yamlFile != null) {
//                    System.out.println(yamlFile);
//                }
            }
        }
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
//        System.out.println("modified?");
        return false;
    }

    //ToDO
    @Override
    public boolean isValid() {
        return myEditorUI != null;//languages != null && languages.length != 0;
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
        if (connect != null) {
            connect.disconnect();
        }
        if (model != null) {
            model.removeTableModelListener(this);
            model = null;
        }
        if (editorStrategy != null) {
            editorStrategy.clearStrategy();
            editorStrategy = null;
        }
        myEditorUI = null;
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

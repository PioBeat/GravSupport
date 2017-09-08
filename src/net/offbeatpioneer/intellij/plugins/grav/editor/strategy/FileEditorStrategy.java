package net.offbeatpioneer.intellij.plugins.grav.editor.strategy;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.editor.GravLangFileEditor;
import net.offbeatpioneer.intellij.plugins.grav.editor.GravLanguageEditorProvider;
import net.offbeatpioneer.intellij.plugins.grav.editor.LanguageFileEditorGUI;
import net.offbeatpioneer.intellij.plugins.grav.editor.TranslationTableModel;
import net.offbeatpioneer.intellij.plugins.grav.editor.dialogs.InsertKeyValueDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FileEditorStrategy implements ActionListener {

    YAMLUtil yamlUtil = new YAMLUtil();

    protected String[] languages;
    protected Project project;
    protected GravLangFileEditor fileEditor;
    public ConcurrentHashMap<String, Editor> editorMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, VirtualFile> fileMap = new ConcurrentHashMap<>();
    String currentLang = "";
    InsertKeyValueDialog dialog;
    TranslationTableModel model;

    public FileEditorStrategy(Project project) {
        this.project = project;
    }

    @NotNull
    public static FileEditorStrategy create(GravLanguageEditorProvider provider, Project project) {
        switch (provider.getLangFileEditorType()) {
            case LANGUAGE_FOLDER:
                return new LanguageFolderStrategy(project);
            case LANGUAGE_FILE:
                return new LanguageFileStrategy(project);
            default:
                return new FileEditorStrategy(project) {
                    @Override
                    public void initTab(LanguageFileEditorGUI gui) {

                    }

                    @Override
                    public void createFileMap(@NotNull VirtualFile file) {
                    }

                    @Override
                    public TranslationTableModel createTableModel() {
                        return null;
                    }

                    @Override
                    protected void updateDocumentHook(Document document, Project project, String lang, String key, String value, TranslationTableModel model) {

                    }
                };
        }
    }

    public abstract void initTab(LanguageFileEditorGUI gui);

    public abstract void createFileMap(@NotNull VirtualFile file);

    protected void detactLanguages() {
        this.languages = Collections.list(getFileMap().keys()).toArray(new String[getFileMap().size()]);
    }

    public abstract TranslationTableModel createTableModel();

    public FileEditorStrategy withFileEditor(GravLangFileEditor editor) {
        this.fileEditor = editor;
        return this;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model = (TranslationTableModel) fileEditor.getGUI().getTable1().getModel();
        int ixTab = fileEditor.getGUI().getSelectedTab();
        dialog = new InsertKeyValueDialog(fileEditor.getProject(), model);
        if (ixTab >= 1) {
            dialog.setSelectedLanguage(model.getLanguages().get(ixTab - 1));
        } else {
            dialog.setSelectedLanguage(model.getLanguages().get(0));
        }
    }

    void getCompoundKeys0(YAMLKeyValue keyValue, String compKey, List<String> keysList, ConcurrentHashMap<String, Collection<YAMLKeyValue>> dataMap, String lang) {

        if (!(keyValue.getValue() instanceof YAMLMapping)) {
            keysList.add(compKey);
            dataMap.get(lang).add(keyValue);
        } else {
            Collection<YAMLKeyValue> collection = ((YAMLBlockMappingImpl) keyValue.getValue()).getKeyValues();
            for (YAMLKeyValue each : collection) {
                getCompoundKeys0(each, compKey + "." + each.getKeyText(), keysList, dataMap, lang);
            }
        }
    }

    public ConcurrentHashMap<String, VirtualFile> getFileMap() {
        return fileMap;
    }

    protected abstract void updateDocumentHook(Document document, Project project, String lang, String key, String value, TranslationTableModel model);
}

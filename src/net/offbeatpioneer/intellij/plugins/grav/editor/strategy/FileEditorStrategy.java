package net.offbeatpioneer.intellij.plugins.grav.editor.strategy;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.editor.GravLangFileEditor;
import net.offbeatpioneer.intellij.plugins.grav.editor.TranslationTableModel;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FileEditorStrategy implements ActionListener {
    protected String[] languages;
    protected Project project;
    protected JTable table;
    protected GravLangFileEditor editor;
    protected ConcurrentHashMap<String, Editor> editorMap;
    protected String currentLang;

    public FileEditorStrategy(String[] languages, Project project) {
        this.languages = languages;
        this.project = project;
    }

    public abstract TranslationTableModel createTableModel(ConcurrentHashMap<String, VirtualFile> fileMap);

    public void setUIElements(JTable table, GravLangFileEditor editor, ConcurrentHashMap<String, Editor> editorMap, String currentLang) {
        this.table = table;
        this.editor = editor;
        this.editorMap = editorMap;
        this.currentLang = currentLang;
    }

    public void getCompoundKeys0(YAMLKeyValue keyValue, String compKey, List<String> keysList, ConcurrentHashMap<String, Collection<YAMLKeyValue>> dataMap, String lang) {

        if (!(keyValue.getValue() instanceof YAMLMapping)) {
//            String k = getCompKey(keyValue, compKey);
            keysList.add(compKey);
            dataMap.get(lang).add(keyValue);
        } else {
            Collection<YAMLKeyValue> collection = ((YAMLBlockMappingImpl) keyValue.getValue()).getKeyValues();
            for (YAMLKeyValue each : collection) {
                getCompoundKeys0(each, compKey + "." + each.getKeyText(), keysList, dataMap, lang);
            }
        }
    }

//    @Deprecated
//    public void getCompoundKeys(Collection<YAMLKeyValue> childs, String compKey, List<String> keysList) {
//        for (YAMLKeyValue each : childs) {
//            if (each.getYAMLElements() != null && each.getYAMLElements().size() >= 1) {
//                Collection<YAMLKeyValue> collection = ((YAMLBlockMappingImpl) each.getValue()).getKeyValues();
//                getCompoundKeys(collection, getCompKey(each, compKey), keysList);
//            } else {
//                String k = getCompKey(each, compKey);
//                keysList.add(k);
//            }
//        }
//    }

    public String getCompKey(YAMLKeyValue keyValue, String compKey) {
        return compKey + "." + keyValue.getKeyText();
    }

}

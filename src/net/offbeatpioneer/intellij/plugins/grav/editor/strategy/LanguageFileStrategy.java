package net.offbeatpioneer.intellij.plugins.grav.editor.strategy;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import net.offbeatpioneer.intellij.plugins.grav.editor.TranslationTableModel;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.impl.YAMLFileImpl;

import java.awt.event.ActionEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageFileStrategy extends FileEditorStrategy {


    public LanguageFileStrategy(String[] languages, Project project) {
        super(languages, project);
    }

    @Override
    public TranslationTableModel createTableModel(ConcurrentHashMap<String, VirtualFile> fileMap) {
        Collection<String> availableKeys = new LinkedHashSet<String>();//preserve order, no dups
        VirtualFile virtualFile = fileMap.elements().nextElement();
        YAMLFileImpl yamlFile = (YAMLFileImpl) PsiManager.getInstance(project).findFile(virtualFile);
        ConcurrentHashMap<String, Collection<YAMLKeyValue>> dataMap = new ConcurrentHashMap<>();
        Collection<YAMLKeyValue> topLevelKeys = YAMLUtil.getTopLevelKeys(yamlFile);
        for (String eachLanguage : languages) {
            dataMap.put(eachLanguage, new HashSet<>());
            Collection<YAMLKeyValue> topLevelValuesLang = findChildValues(topLevelKeys, eachLanguage);
            for (YAMLKeyValue keyValue : topLevelValuesLang) {
                if (keyValue.getValue() instanceof YAMLCompoundValue) {
                    List<String> keysBuffer = new ArrayList<>();
                    getCompoundKeys0(keyValue, keyValue.getKeyText(), keysBuffer, dataMap, eachLanguage);
                    availableKeys.addAll(keysBuffer);
                } else {
                    availableKeys.add(keyValue.getKeyText());
                    dataMap.get(eachLanguage).add(keyValue);
                }
            }
        }
        return new TranslationTableModel(languages, availableKeys, dataMap).setPrefixKey(true);
    }

    public Collection<YAMLKeyValue> findChildValues(Collection<YAMLKeyValue> topLevelKeys, String lang) {
        for (YAMLKeyValue each : topLevelKeys) {
            if (each.getKeyText().equalsIgnoreCase(lang)) {
                YAMLValue topLevelValue = each.getValue();
                return topLevelValue instanceof YAMLMapping ? ((YAMLMapping) topLevelValue).getKeyValues() : Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

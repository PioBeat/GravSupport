package net.offbeatpioneer.intellij.plugins.grav.editor.strategy;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiManager;
import com.intellij.util.IncorrectOperationException;
import net.offbeatpioneer.intellij.plugins.grav.editor.TranslationTableModel;
import net.offbeatpioneer.intellij.plugins.grav.editor.dialogs.InsertKeyValueDialog;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.*;
import org.jetbrains.yaml.psi.impl.YAMLFileImpl;

import java.awt.event.ActionEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.intellij.openapi.ui.DialogWrapper.CANCEL_EXIT_CODE;

public class LanguageFileStrategy extends FileEditorStrategy {

    public LanguageFileStrategy(String[] languages, Project project) {
        super(languages, project);
    }

    @Override
    public TranslationTableModel createTableModel(ConcurrentHashMap<String, VirtualFile> fileMap) {
        Collection<String> availableKeys = new LinkedHashSet<String>();//preserve order, no dups
        ConcurrentHashMap<String, Collection<YAMLKeyValue>> dataMap = new ConcurrentHashMap<>();
        VirtualFile virtualFile = fileMap.elements().nextElement();
        YAMLFileImpl yamlFile = (YAMLFileImpl) PsiManager.getInstance(project).findFile(virtualFile);
        if (yamlFile != null) {
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
        super.actionPerformed(e);
        dialog.show();
        int exitCode = dialog.getExitCode();
        if (exitCode != CANCEL_EXIT_CODE) {
            String key = dialog.getKeyText();
            String value = dialog.getValueText();
            currentLang = dialog.getSelectedLangauge();
            if (currentLang != null && !currentLang.isEmpty()) {
                Editor ieditor = editorMap.get(currentLang);
                Document document = ieditor.getDocument();
                WriteCommandAction.runWriteCommandAction(editor.getProject(), new Runnable() {
                    @Override
                    public void run() {
                        updateDocument(document, ieditor.getProject(), currentLang, key, value, model);
                    }
                });
            } else {
                NotificationHelper.showBaloon("No language file available", MessageType.WARNING, editor.getProject());
            }
        }
    }

    private void updateDocument(Document document, Project project, String lang, String key, String value, TranslationTableModel model) {
        if (!document.isWritable()) {
            return;
        }
        YAMLFile yamlFile = (YAMLFile) PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (yamlFile != null) {
            try {
                for (String eachLang : languages) {
                    String value0;
                    String key0;
                    if (eachLang.equalsIgnoreCase(currentLang)) {
                        key0 = currentLang + "." + key;
                        value0 = value;
                    } else {
                        key0 = eachLang + "." + key;
                        value0 = "";
                    }
                    YAMLKeyValue yamlKeyValue = yamlUtil.createI18nRecord(yamlFile, key0, value0);
                    model.addElement(eachLang, yamlKeyValue, key);
                }
                PsiDocumentManager.getInstance(project).commitDocument(document);

            } catch (IncorrectOperationException e) {

            }
        }
    }
}

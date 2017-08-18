package net.offbeatpioneer.intellij.plugins.grav.editor.strategy;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.LineSeparator;
import net.offbeatpioneer.intellij.plugins.grav.editor.TranslationTableModel;
import net.offbeatpioneer.intellij.plugins.grav.editor.dialogs.InsertKeyValueDialog;
import net.offbeatpioneer.intellij.plugins.grav.helper.GravYAMLUtils;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.impl.YAMLFileImpl;

import java.awt.event.ActionEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.intellij.openapi.ui.DialogWrapper.CANCEL_EXIT_CODE;

public class LanguageFolderStrategy extends FileEditorStrategy {

    public LanguageFolderStrategy(String[] languages, Project project) {
        super(languages, project);
    }

    @Override
    public TranslationTableModel createTableModel(ConcurrentHashMap<String, VirtualFile> fileMap) {
        ConcurrentHashMap<String, Collection<YAMLKeyValue>> dataMap = new ConcurrentHashMap<>();
        Collection<String> availableKeys = new LinkedHashSet<>();//preserve order, no dups
        Collection<VirtualFile> removeFiles = new ArrayList<>();
        Set<Map.Entry<String, VirtualFile>> set = fileMap.entrySet();
        for (Map.Entry<String, VirtualFile> each : set) {
            //this should never be accessed if VirtualFileListener works correctly
            if (each.getValue() == null || !each.getValue().exists()) {
                fileMap.remove(each.getKey());
                continue;
            }
            YAMLFileImpl yamlFile = (YAMLFileImpl) PsiManager.getInstance(project).findFile(each.getValue());
            if (yamlFile != null) {
                String lang = each.getValue().getNameWithoutExtension();
                dataMap.put(lang, new HashSet<>());
                for (YAMLKeyValue keyValue : YAMLUtil.getTopLevelKeys(yamlFile)) {

                    if (keyValue.getValue() instanceof YAMLCompoundValue) {
                        List<String> keysBuffer = new ArrayList<>();
                        getCompoundKeys0(keyValue, keyValue.getKeyText(), keysBuffer, dataMap, lang);
                        availableKeys.addAll(keysBuffer);
                    } else {
                        availableKeys.add(keyValue.getKeyText());
                        dataMap.get(lang).add(keyValue);
                    }
                }
            }
        }
        return new TranslationTableModel(languages, availableKeys, dataMap);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TranslationTableModel model = (TranslationTableModel) table.getModel();
        InsertKeyValueDialog dialog = new InsertKeyValueDialog(editor.getProject(), model);
        dialog.show();
        int exitCode = dialog.getExitCode();
        if (exitCode != CANCEL_EXIT_CODE) {
            String key = dialog.getDialogUI().getKeyText();
            String value = dialog.getDialogUI().getValueText();
            currentLang = dialog.getSelectedLangauge();
            if (currentLang != null && !currentLang.isEmpty()) {
                Editor ieditor = editorMap.get(currentLang);
                Document document = ieditor.getDocument();

                WriteCommandAction.runWriteCommandAction(editor.getProject(), new Runnable() {
                    @Override
                    public void run() {
                        updateDocument(document, ieditor.getProject(), currentLang, key, value, model);
                        for (String eachLang : model.getLanguages()) {
                            if (!eachLang.equalsIgnoreCase(currentLang)) {
                                Editor ieditor = editorMap.get(eachLang);
                                Document document = ieditor.getDocument();
                                updateDocument(document, ieditor.getProject(), eachLang, key, "", model);
                            }
                        }
                        model.fireChange();
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
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile != null) {
            YAMLKeyValue yamlKeyValue = yamlUtil.createI18nRecord((YAMLFile) psiFile, key, value);
            model.addElement(lang, yamlKeyValue, key);
            PsiDocumentManager.getInstance(project).commitDocument(document);
        }
    }
}

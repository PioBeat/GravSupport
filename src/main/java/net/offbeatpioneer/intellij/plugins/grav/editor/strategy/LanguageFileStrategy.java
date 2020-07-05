package net.offbeatpioneer.intellij.plugins.grav.editor.strategy;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThrowableRunnable;
import net.offbeatpioneer.intellij.plugins.grav.editor.LanguageFileEditorGUI;
import net.offbeatpioneer.intellij.plugins.grav.editor.TranslationTableModel;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.*;
import org.jetbrains.yaml.psi.impl.YAMLFileImpl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.intellij.openapi.ui.DialogWrapper.CANCEL_EXIT_CODE;

public class LanguageFileStrategy extends FileEditorStrategy {

    LanguageFileStrategy(Project project) {
        super(project);
    }

    @Override
    public void initTab(LanguageFileEditorGUI gui) {
        VirtualFile file = fileMap.elements().nextElement();
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null) {
            Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
            if (document != null) {
                for (String eachLang : languages) {
                    Editor editorTextField = gui.createEditor(document, project, psiFile.getFileType());
                    editorMap.put(eachLang, editorTextField);
                    gui.addTab(eachLang, editorTextField.getComponent());
                }
            }
        }
    }

    @Override
    public void createFileMap(@NotNull VirtualFile file) {
        fileMap.clear();
        YAMLFile yamlFile = (YAMLFile) PsiManager.getInstance(project).findFile(file);
        if (yamlFile != null) {
            Collection<YAMLKeyValue> topLevelKeys = YAMLUtil.getTopLevelKeys(yamlFile);
            for (YAMLKeyValue each : topLevelKeys) {
                fileMap.put(each.getKeyText(), file);
            }
        }
    }

    @Override
    public TranslationTableModel createTableModel() {
        detactLanguages();
        Collection<String> availableKeys = new LinkedHashSet<>();//preserve order, no dups
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
        return new TranslationTableModel(availableKeys, dataMap).setPrefixKey(true);
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
        if (e.getSource() instanceof JComponent) {
            if (((JComponent) e.getSource()).getName().equals(LanguageFileEditorGUI.UI_BTN_INSERT_KEY)) {
                if (dialog == null) return;
                dialog.show();
                int exitCode = dialog.getExitCode();
                if (exitCode != CANCEL_EXIT_CODE) {
                    String key = dialog.getKeyText();
                    String value = dialog.getValueText();
                    currentLang = dialog.getSelectedLangauge();
                    if (currentLang != null && !currentLang.isEmpty()) {
                        Editor ieditor = editorMap.get(currentLang);
                        Document document = ieditor.getDocument();
                        TranslationTableModel model = (TranslationTableModel) fileEditor.getGUI().getTable1().getModel();
                        WriteCommandAction.runWriteCommandAction(fileEditor.getProject(), () -> updateDocumentHook(document, ieditor.getProject(), currentLang, key, value, model));
                    } else {
                        NotificationHelper.showBaloon("No language file available", MessageType.WARNING, fileEditor.getProject());
                    }
                }
            }
        }
    }

    @Override
    public void removeKeyComplete(List<String> qualifiedKey, String key, TranslationTableModel model) {
        VirtualFile file = fileEditor.getFileMap().elements().nextElement();
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null || ((YAMLFile) psiFile).getDocuments() == null) return;
        YAMLDocument doc = ((YAMLFile) psiFile).getDocuments().get(0);

        try {
            WriteCommandAction.writeCommandAction(project).withName(ACTION_NAME).run((ThrowableRunnable<Throwable>) () -> {

                for (String eachLang : model.getLanguages()) {
                    List<String> key0 = new ArrayList<>(qualifiedKey);
                    key0.add(0, eachLang);
                    YAMLKeyValue value = YAMLUtil.getQualifiedKeyInDocument(doc, key0);
                    if (value != null) {
                        value.delete();
                        model.removeElement(eachLang, value, key);
                    }
                }
                file.refresh(false, false);
            });
        } catch (Throwable throwable) {
            LOG.error(throwable);
        }
    }

    @Override
    protected void updateDocumentHook(Document document, Project project, String lang, String key, String value, TranslationTableModel model) {
        if (!document.isWritable()) {
            return;
        }
        YAMLFile yamlFile = (YAMLFile) PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (yamlFile != null) {
            try {
                for (String eachLang : languages) {
                    String value0;
                    String key0;
                    if (eachLang.equalsIgnoreCase(lang)) {
                        key0 = lang + "." + key;
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

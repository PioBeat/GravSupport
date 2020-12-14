package net.offbeatpioneer.intellij.plugins.grav.editor.strategy;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThrowableRunnable;
import net.offbeatpioneer.intellij.plugins.grav.editor.GravLanguageEditorForm;
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

    LanguageFileStrategy(PsiManager psiManager, PsiDocumentManager psiDocumentManager) {
        super(psiManager, psiDocumentManager);
    }

    @Override
    public void initTab(GravLanguageEditorForm gui) {
        if(fileMap != null && fileMap.size() > 0) {
            VirtualFile file = fileMap.elements().nextElement();
            PsiFile psiFile = getPsiManager().findFile(file);
            if (psiFile != null) {
                Document document = getPsiDocumentManager().getDocument(psiFile);
                if (document != null) {
                    for (String eachLang : languages) {
                        Editor editorTextField = gui.createEditor(document, psiFile.getProject(), psiFile.getFileType()); //ieditor.getProject()
                        editorMap.put(eachLang, editorTextField);
                        gui.addTab(eachLang, editorTextField.getComponent());
                    }
                }
            }
        }
    }

    @Override
    public void createFileMap(@NotNull VirtualFile file) {
        fileMap.clear();
        YAMLFile yamlFile = (YAMLFile) getPsiManager().findFile(file);
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
        YAMLFileImpl yamlFile = (YAMLFileImpl) getPsiManager().findFile(virtualFile);
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
    public synchronized void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (e.getSource() instanceof JComponent) {
            if (((JComponent) e.getSource()).getName().equals(GravLanguageEditorForm.UI_BTN_INSERT_KEY)) {
                if (dialog == null) return;
                dialog.show();
                int exitCode = dialog.getExitCode();
                if (exitCode != CANCEL_EXIT_CODE) {
                    String key = dialog.getKeyText();
                    String value = dialog.getValueText();
                    currentLang = dialog.getSelectedLangauge();
                    if (currentLang != null && !currentLang.isEmpty()) {
                        Editor ieditor = editorMap.get(currentLang);
                        Objects.requireNonNull(ieditor);
                        Document document = ieditor.getDocument();
                        TranslationTableModel model = (TranslationTableModel) fileEditor.getForm().getTable1().getModel();
                        WriteCommandAction.runWriteCommandAction(ieditor.getProject(), () -> {
                            updateDocumentHook(document, currentLang, key, value, model);
                        });
                    } else {
//                        NotificationHelper.showBaloon("No language file available", MessageType.WARNING, fileEditor.getProject());
                        NotificationHelper.showErrorNotification(null, "No language file available");
                    }
                }
            }
        }
    }

    @Override
    public synchronized void removeKeyComplete(List<String> qualifiedKey, String key, TranslationTableModel model) {
        VirtualFile file = fileEditor.getFileMap().elements().nextElement();
        PsiFile psiFile = getPsiManager().findFile(file);
        if (psiFile == null || ((YAMLFile) psiFile).getDocuments() == null) return;
        YAMLDocument doc = ((YAMLFile) psiFile).getDocuments().get(0);

        try {
            WriteCommandAction.writeCommandAction(psiFile.getProject()).withName(ACTION_NAME).run((ThrowableRunnable<Throwable>) () -> {
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
    protected synchronized void updateDocumentHook(Document document, String lang, String key, String value, TranslationTableModel model) {
        if (!document.isWritable()) {
            return;
        }
        YAMLFile yamlFile = (YAMLFile) getPsiDocumentManager().getPsiFile(document);
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
                getPsiDocumentManager().commitDocument(document);

            } catch (IncorrectOperationException e) {

            }
        }
    }
}

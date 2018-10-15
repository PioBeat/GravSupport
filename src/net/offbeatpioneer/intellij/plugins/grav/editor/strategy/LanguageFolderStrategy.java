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
import com.intellij.util.ThrowableRunnable;
import net.offbeatpioneer.intellij.plugins.grav.editor.LanguageFileEditorGUI;
import net.offbeatpioneer.intellij.plugins.grav.editor.TranslationTableModel;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.impl.YAMLFileImpl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.intellij.openapi.ui.DialogWrapper.CANCEL_EXIT_CODE;

public class LanguageFolderStrategy extends FileEditorStrategy {

    public LanguageFolderStrategy(Project project) {
        super(project);
    }

    @Override
    public void initTab(LanguageFileEditorGUI gui) {
        for (VirtualFile file : fileMap.values()) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile != null) {
                Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                if (document != null) {
                    Editor editorTextField = gui.createEditor(document, project, psiFile.getFileType());
                    editorMap.put(psiFile.getVirtualFile().getNameWithoutExtension(), editorTextField);
                    gui.addTab(psiFile.getVirtualFile().getNameWithoutExtension(), editorTextField.getComponent());
                }
            }
        }
    }

    @Override
    public void createFileMap(@NotNull VirtualFile file) {
        fileMap.clear();
        VirtualFile parent = file.getParent();
        VirtualFile[] childs = parent.getChildren();
        languages = new String[childs.length];
        int cnt = 0;
        for (VirtualFile each : childs) {
            languages[cnt] = each.getNameWithoutExtension();
            fileMap.put(languages[cnt], each);
            cnt++;
        }
    }

    @Override
    public TranslationTableModel createTableModel() {
        detactLanguages();
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
        return new TranslationTableModel(availableKeys, dataMap);
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
                        WriteCommandAction.runWriteCommandAction(fileEditor.getProject(), () -> {
                            updateDocumentHook(document, ieditor.getProject(), currentLang, key, value, model);
                            for (String eachLang : model.getLanguages()) {
                                if (!eachLang.equalsIgnoreCase(currentLang)) {
                                    Editor ieditor1 = editorMap.get(eachLang);
                                    Document document1 = ieditor1.getDocument();
                                    updateDocumentHook(document1, ieditor1.getProject(), eachLang, key, "", model);
                                }
                            }
                            model.fireChange();
                        });
                    } else {
                        NotificationHelper.showBaloon("No language file available", MessageType.WARNING, fileEditor.getProject());
                    }
                }
            }
        }
    }

    @Override
    public void removeKeyComplete(List<String> qualifiedKey, String key, TranslationTableModel model) {
        try {
            WriteCommandAction.writeCommandAction(project).withName(ACTION_NAME).run((ThrowableRunnable<Throwable>) () -> {
                for (String eachLang : model.getLanguages()) {
                    VirtualFile file1 = fileEditor.getFileMap().get(eachLang);
                    PsiFile psiFile1 = PsiManager.getInstance(project).findFile(file1);
                    if (psiFile1 == null || ((YAMLFile) psiFile1).getDocuments() == null) return;
                    YAMLDocument doc1 = ((YAMLFile) psiFile1).getDocuments().get(0);

                    YAMLKeyValue value = YAMLUtil.getQualifiedKeyInDocument(doc1, qualifiedKey);
                    if (value != null) {
                        value.delete();
                        model.removeElement(eachLang, value, key);
                    }
                    file1.refresh(false, false);
                }
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
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile != null) {
            YAMLKeyValue yamlKeyValue = yamlUtil.createI18nRecord((YAMLFile) psiFile, key, value);
            model.addElement(lang, yamlKeyValue, key);
            PsiDocumentManager.getInstance(project).commitDocument(document);
        }
    }
}

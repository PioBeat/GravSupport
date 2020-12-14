package net.offbeatpioneer.intellij.plugins.grav.editor.strategy;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiManager;
import com.intellij.ui.awt.RelativePoint;
import net.offbeatpioneer.intellij.plugins.grav.editor.GravLangFileEditor;
import net.offbeatpioneer.intellij.plugins.grav.editor.GravLanguageEditorProvider;
import net.offbeatpioneer.intellij.plugins.grav.editor.GravLanguageEditorForm;
import net.offbeatpioneer.intellij.plugins.grav.editor.TranslationTableModel;
import net.offbeatpioneer.intellij.plugins.grav.editor.dialogs.InsertKeyValueDialog;
import net.offbeatpioneer.intellij.plugins.grav.helper.GravYAMLUtils;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides different strategies for handling translations:
 * <ul>
 * <li>File-based</li>
 * <li>Directory-based</li>
 * </ul>
 * <p>
 * Grav supports these two mechanisms for translation management.
 * The concrete implementations implement the specific behaviour to handle a language file
 * containing all translations or folders containing separate language files.
 * <p>
 * The strategies can be created by the factory method of this class.
 *
 * @author Dominik Grzelak
 * @see LanguageFileStrategy
 * @see LanguageFolderStrategy
 */
public abstract class FileEditorStrategy implements ActionListener {
    public static final String ACTION_NAME = "GravRemoveKey";
    protected Logger LOG = Logger.getInstance(this.getClass());
    YAMLUtil yamlUtil = new YAMLUtil();
    PsiManager psiManager;
    PsiDocumentManager psiDocumentManager;

    protected String[] languages;
    protected GravLangFileEditor fileEditor;
    public ConcurrentHashMap<String, Editor> editorMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, VirtualFile> fileMap = new ConcurrentHashMap<>();
    String currentLang = "";
    InsertKeyValueDialog dialog;

    public FileEditorStrategy(PsiManager psiManager, PsiDocumentManager psiDocumentManager) {
        this.psiManager = psiManager;
        this.psiDocumentManager = psiDocumentManager;
    }

    /**
     * For dispose process.
     * Should not be called directly!
     */
    public void clearStrategy() {
        for (Editor eachEditor : editorMap.values()) {
            EditorFactory.getInstance().releaseEditor(eachEditor); // should already run in EDT (event dispatcher thread)
//            SwingUtilities.invokeLater(() -> EditorFactory.getInstance().releaseEditor(eachEditor));
        }
        fileMap.clear();
        fileMap = null;
        psiManager = null;
        psiDocumentManager = null;
    }

    public PsiManager getPsiManager() {
        return psiManager;
    }

    public PsiDocumentManager getPsiDocumentManager() {
        return psiDocumentManager;
    }

    /**
     * Factory method to create the concrete implementation.
     *
     * @param provider
     * @return
     */
    @NotNull
    public static FileEditorStrategy create(GravLanguageEditorProvider provider, Project project) {
        switch (provider.getLangFileEditorType()) {
            case LANGUAGE_FOLDER:
                return new LanguageFolderStrategy(PsiManager.getInstance(project), PsiDocumentManager.getInstance(project));
            case LANGUAGE_FILE:
                return new LanguageFileStrategy(PsiManager.getInstance(project), PsiDocumentManager.getInstance(project));
            default:
                return new FileEditorStrategy(null, null) {
                    @Override
                    public void initTab(GravLanguageEditorForm gui) {

                    }

                    @Override
                    public void createFileMap(@NotNull VirtualFile file) {
                    }

                    @Override
                    public TranslationTableModel createTableModel() {
                        return null;
                    }

                    @Override
                    public void removeKeyComplete(List<String> qualifiedKey, String key, TranslationTableModel model) {

                    }

                    @Override
                    protected void updateDocumentHook(Document document, String lang, String key, String value, TranslationTableModel model) {

                    }
                };
        }
    }

    public abstract void initTab(GravLanguageEditorForm gui);

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
    public synchronized void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComponent) {
            if (((JComponent) e.getSource()).getName().equals(GravLanguageEditorForm.UI_BTN_INSERT_KEY)) {
                TranslationTableModel model = (TranslationTableModel) fileEditor.getForm().getTable1().getModel();
                int ixTab = fileEditor.getForm().getSelectedTab();
                dialog = new InsertKeyValueDialog(null, model);
                if (ixTab >= 1) {
                    dialog.setSelectedLanguage(model.getLanguages().get(ixTab - 1));
                } else {
                    dialog.setSelectedLanguage(model.getLanguages().get(0));
                }
                return;
            }

            if (((JComponent) e.getSource()).getName().equals(GravLanguageEditorForm.UI_BTN_REMOVE_KEY)) {
                TranslationTableModel model = (TranslationTableModel) fileEditor.getForm().getTable1().getModel();
                int rowAtPoint = fileEditor.getForm().getTable1().getSelectedRow();
                if (rowAtPoint == -1 || rowAtPoint >= model.getKeys(false).size()) {
//                    IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);
                    NotificationHelper.showBaloon("Please select a language key first", MessageType.WARNING,
                            RelativePoint.getCenterOf(fileEditor.getForm().getDeleteLanguageKeyBtn()), Balloon.Position.below, 3500);
//                    NotificationHelper.showErrorNotification(null, "Please select a key first");
                    return;
                }
                if (rowAtPoint < model.getKeys(false).size()) {
                    String key = model.getKeys(true).get(rowAtPoint);
                    List<String> qualifiedKey = GravYAMLUtils.splitKeyAsList(key);
                    WriteCommandAction.runWriteCommandAction(getPsiManager().getProject(), () -> {
                        removeKeyComplete(qualifiedKey, key, model);
                    });
//                    ApplicationManager.getApplication().invokeLater(() -> {
//                        removeKeyComplete(qualifiedKey, key, model);
//                    }, ModalityState.current());
                }
                return;
            }
        }
    }

    public abstract void removeKeyComplete(List<String> qualifiedKey, String key, TranslationTableModel model);

    protected abstract void updateDocumentHook(Document document, String lang, String key, String value, TranslationTableModel model);

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


}

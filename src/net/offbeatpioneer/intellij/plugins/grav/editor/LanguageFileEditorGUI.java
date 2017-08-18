package net.offbeatpioneer.intellij.plugins.grav.editor;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.LineSeparator;
import net.offbeatpioneer.intellij.plugins.grav.editor.dialogs.InsertKeyValueDialog;
import net.offbeatpioneer.intellij.plugins.grav.helper.GravYAMLUtils;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import org.apache.commons.logging.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.impl.YAMLFileImpl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ConcurrentHashMap;

import static com.intellij.openapi.ui.DialogWrapper.CANCEL_EXIT_CODE;

public class LanguageFileEditorGUI {
    private TranslationTableModel model;
    private String[] languages;
    private String currentLang = "de"; //TODO detect current language by selected tab
    private GravLangFileEditor editor;
    private ConcurrentHashMap<String, Editor> editorMap;
    private ConcurrentHashMap<String, VirtualFile> fileMap;
    private JPanel mainPanel;
    private JTable table1;
    private JScrollPane scrollPane1;
    private JButton button1;
    private JTabbedPane tabbedPane;
    private JLabel langInfo;

    public LanguageFileEditorGUI(GravLangFileEditor editor, String[] languages, TranslationTableModel model) {
        this.languages = languages;
        this.model = model;
        this.editor = editor;
        this.editorMap = new ConcurrentHashMap<>();
        this.fileMap = new ConcurrentHashMap<>();
    }

    public String getCurrentLang() {
        if (currentLang == null) {
            return "";
        }
        return currentLang;
    }

    public void setCurrentLang(String currentLang) {
        this.currentLang = currentLang;
        if (langInfo != null) {
            langInfo.setText("Default language: " + getCurrentLang());
        }
    }

    private void createUIComponents() {
        langInfo = new JLabel("Default language: " + getCurrentLang());
        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(editor);
        table1 = new JBTable(model);
        table1.setFillsViewportHeight(true);
        scrollPane1 = new JBScrollPane(table1);
        setCellRenderer();
        button1 = new JButton();

        button1.addActionListener(editor.editorStrategy);
//        button1.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                TranslationTableModel model = (TranslationTableModel) table1.getModel();
//                InsertKeyValueDialog dialog = new InsertKeyValueDialog(editor.getProject(), model);
//                dialog.show();
//                int exitCode = dialog.getExitCode();
//                if (exitCode != CANCEL_EXIT_CODE) {
//                    String key = dialog.getDialogUI().getKeyText();
//                    String value = dialog.getDialogUI().getValueText();
//                    currentLang = dialog.getSelectedLangauge();
//                    if (currentLang != null && !currentLang.isEmpty()) {
//                        Editor ieditor = editorMap.get(currentLang);
//                        Document document = ieditor.getDocument();
//
//                        WriteCommandAction.runWriteCommandAction(editor.getProject(), new Runnable() {
//                            @Override
//                            public void run() {
//                                updateDocument(document, ieditor.getProject(), currentLang, key, value);
//                                for (String eachLang : model.getLanguages()) {
//                                    if (!eachLang.equalsIgnoreCase(currentLang)) {
//                                        Editor ieditor = editorMap.get(eachLang);
//                                        Document document = ieditor.getDocument();
//                                        updateDocument(document, ieditor.getProject(), eachLang, key, "");
//                                    }
//                                }
//                                model.fireChange();
//                            }
//                        });
//                    } else {
//                        NotificationHelper.showBaloon("No language file available", MessageType.WARNING, editor.getProject());
//                    }
//                }
//            }
//        });
    }

//    private void updateDocument(Document document, Project project, String lang, String key, String value) {
//        if (!document.isWritable()) {
//            return;
//        }
//        int l = document.getTextLength();
//        document.insertString(l, LineSeparator.LF.getSeparatorString() + key + ": " + value);
//        PsiDocumentManager.getInstance(project).commitDocument(document);
//        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
//        YAMLKeyValue keyValue = YAMLUtil.getQualifiedKeyInFile((YAMLFile) psiFile, GravYAMLUtils.splitKey(key));
//        model.addElement(lang, keyValue);
//    }

    private void setCellRenderer() {
        for (int i = 1; i < table1.getColumnCount(); i++) {
            table1.getColumnModel().getColumn(i).setCellRenderer(new ValueEnteredTableCellRenderer());
        }
    }

    public void initTabs(Project project, ConcurrentHashMap<String, VirtualFile> fileMap) {
        tabbedPane.removeAll();
        this.fileMap = fileMap;
        tabbedPane.addTab("Overview", scrollPane1);
        for (VirtualFile file : fileMap.values()) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile != null) {
                Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                if (document != null) {
                    Editor editorTextField = createEditor(document, project, psiFile.getFileType());
                    editorMap.put(psiFile.getVirtualFile().getNameWithoutExtension(), editorTextField);
                    tabbedPane.addTab(psiFile.getVirtualFile().getNameWithoutExtension(), editorTextField.getComponent());
                }
            }
        }
        editor.editorStrategy.setUIElements(table1, editor, editorMap, currentLang);
        setCellRenderer();
    }

    public Editor createEditor(Document document, Project project, @Nullable FileType fileType) {
        EditorImpl editor = (EditorImpl) EditorFactory.getInstance().createEditor(document, project);
        if (fileType != null) {
            editor.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project, fileType));
        }
        return editor;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTable getTable1() {
        return table1;
    }
}


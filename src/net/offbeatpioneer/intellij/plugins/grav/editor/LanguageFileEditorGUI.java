package net.offbeatpioneer.intellij.plugins.grav.editor;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.impl.EditorImpl;
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
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import org.apache.commons.logging.Log;
import org.jetbrains.annotations.Nullable;

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
    ConcurrentHashMap<String, VirtualFile> fileMap;
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
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TranslationTableModel model = (TranslationTableModel) table1.getModel();
                InsertKeyValueDialog dialog = new InsertKeyValueDialog(editor.getProject(), model);
                dialog.show();
                int exitCode = dialog.getExitCode();
                if (exitCode != CANCEL_EXIT_CODE) {
                    String key = dialog.getDialogUI().getKeyText();
                    String value = dialog.getDialogUI().getValueText();
                    if (currentLang != null && !currentLang.isEmpty()) {
                        model.addElement(currentLang, key, value);
                        Editor ieditor = editorMap.get(currentLang);
                        Document document = ieditor.getDocument();
                        if(!document.isWritable()) {
                            return;
                        }
                        int l = document.getTextLength();
                        WriteCommandAction.runWriteCommandAction(editor.getProject(), new Runnable() {
                            @Override
                            public void run() {
                                document.insertString(l, LineSeparator.LF.getSeparatorString() + key + ": " + value);
                            }
                        });
                    } else {
                        NotificationHelper.showBaloon("No language file available", MessageType.WARNING, editor.getProject());
                    }
                }
            }
        });

    }

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


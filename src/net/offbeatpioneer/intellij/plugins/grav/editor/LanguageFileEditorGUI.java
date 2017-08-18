package net.offbeatpioneer.intellij.plugins.grav.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.concurrent.ConcurrentHashMap;

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
        table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setCellRenderer();
        button1 = new JButton();
        button1.addActionListener(editor.editorStrategy);
    }

    private void setCellRenderer() {
        for (int i = 1; i < table1.getColumnCount(); i++) {
            table1.getColumnModel().getColumn(i).setCellRenderer(new ValueEnteredTableCellRenderer());
        }
    }

    public void initTabs(Project project, ConcurrentHashMap<String, VirtualFile> fileMap) {
        tabbedPane.removeAll();
        if (project.isDisposed()) {
            this.fileMap.clear();
            return;
        }
        this.fileMap = fileMap;
        tabbedPane.addTab("Overview", scrollPane1);
        switch (editor.getLanguageFileEditorType()) {
            case LANGUAGE_FOLDER:
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
                break;
            case LANGUAGE_FILE:
                VirtualFile file = fileMap.elements().nextElement();
                PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                if (psiFile != null) {
                    Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                    if (document != null) {
                        for (String eachLang : languages) {
                            Editor editorTextField = createEditor(document, project, psiFile.getFileType());
                            editorMap.put(eachLang, editorTextField);
                            tabbedPane.addTab(eachLang, editorTextField.getComponent());
                        }
                    }
                }
                break;
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


package net.offbeatpioneer.intellij.plugins.grav.editor;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
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
import com.intellij.util.ThrowableRunnable;
import net.offbeatpioneer.intellij.plugins.grav.project.GravProjectComponent;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageFileEditorGUI {
    Logger LOG = Logger.getInstance(this.getClass());
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
        table1.setComponentPopupMenu(createPopupMenu());
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
    int colAtPoint = -1;
    int rowAtPoint = -1;
    private JPopupMenu createPopupMenu() {

        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final Project project = GravProjectComponent.getEnabledProject();
                        if (colAtPoint != 0 & project != null) return;
                        String key = model.getKeys(true).get(rowAtPoint);
                        PsiFile psiFile = null;
                        String[] splitted = key.split("\\.");
                        ArrayList<String> qualifiedKey = new ArrayList<String>(Arrays.asList(splitted));
                        switch (editor.getLanguageFileEditorType()) {
                            case LANGUAGE_FILE:
                                VirtualFile file = fileMap.elements().nextElement();
                                psiFile = PsiManager.getInstance(project).findFile(file);
                                YAMLDocument doc = ((YAMLFile) psiFile).getDocuments().get(0);

                                ApplicationManager.getApplication().invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            WriteCommandAction.writeCommandAction(project).withName("GravRemoveKey").run(new ThrowableRunnable<Throwable>() {

                                                @Override
                                                public void run() throws Throwable {

                                                    for (String eachLang : model.getLanguages()) {
                                                        ArrayList<String> key0 = new ArrayList<String>(qualifiedKey);
                                                        key0.add(0, eachLang);
                                                        YAMLKeyValue value = YAMLUtil.getQualifiedKeyInDocument(doc, key0);
                                                        if (value != null) {
                                                            value.delete();
                                                            model.removeElement(eachLang, value, key);
                                                        }
                                                    }
                                                    file.refresh(false, false);
                                                }
                                            });
                                        } catch (Throwable throwable) {
                                            LOG.error(throwable);
                                        }
                                    }
                                }, ModalityState.current());

                                break;
                            case LANGUAGE_FOLDER:
                                ApplicationManager.getApplication().invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            WriteCommandAction.writeCommandAction(project).withName("GravRemoveKey").run(new ThrowableRunnable<Throwable>() {
                                                @Override
                                                public void run() throws Throwable {
                                                    for (String eachLang : model.getLanguages()) {
                                                        VirtualFile file = fileMap.get(eachLang);
                                                        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                                                        YAMLDocument doc = ((YAMLFile) psiFile).getDocuments().get(0);

                                                        YAMLKeyValue value = YAMLUtil.getQualifiedKeyInDocument(doc, qualifiedKey);
                                                        if (value != null) {
                                                            value.delete();
                                                            model.removeElement(eachLang, value, key);
                                                        }
                                                        file.refresh(false, false);
                                                    }
                                                }
                                            });
                                        } catch (Throwable throwable) {
                                            LOG.error(throwable);
                                        }
                                    }
                                });
                                break;
                        }

                        colAtPoint = -1;
                        rowAtPoint = -1;
                    }
                });
            }
        });
        popupMenu.add(deleteItem);
        popupMenu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        colAtPoint = table1.columnAtPoint(SwingUtilities.convertPoint(popupMenu, new Point(0, 0), table1));
                        rowAtPoint = table1.rowAtPoint(SwingUtilities.convertPoint(popupMenu, new Point(0, 0), table1));
                    }
                });
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        return popupMenu;
    }

    private Editor createEditor(Document document, Project project, @Nullable FileType fileType) {
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


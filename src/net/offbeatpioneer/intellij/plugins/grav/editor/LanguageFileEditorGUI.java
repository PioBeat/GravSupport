package net.offbeatpioneer.intellij.plugins.grav.editor;

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
import net.offbeatpioneer.intellij.plugins.grav.helper.GravYAMLUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUI of the translation editor
 * <p>
 * The two types of language files {@link GravLanguageEditorProvider.LangFileEditorType#LANGUAGE_FILE} and
 * {@link GravLanguageEditorProvider.LangFileEditorType#LANGUAGE_FOLDER} are taken into consideration here. That means
 * the handling of removing or adding keys is slightly different.
 *
 * @author Dominik Grzelak
 */
public class LanguageFileEditorGUI {
    private Logger LOG = Logger.getInstance(this.getClass());
    private static final String ACTION_NAME = "GravRemoveKey";

    private GravLangFileEditor editor;
    private TranslationTableModel model;
    private String[] languages;
    private String currentLang = "de"; //TODO detect current language by selected tab
    private int colAtPoint = -1;
    private int rowAtPoint = -1;
    private ConcurrentHashMap<String, Editor> editorMap;
    private ConcurrentHashMap<String, VirtualFile> fileMap;

    private JPanel mainPanel;
    private JTable table1;
    private JScrollPane scrollPane1;
    private JButton btnAddNewKey;
    private JTabbedPane tabbedPane;

    public LanguageFileEditorGUI(GravLangFileEditor editor, TranslationTableModel model) {
        this.model = model;
        this.languages = model.getLanguages();
        this.editor = editor;
        this.editorMap = new ConcurrentHashMap<>();
        this.fileMap = new ConcurrentHashMap<>();

        table1.setModel(model);
        tabbedPane.addChangeListener(editor);
        btnAddNewKey.addActionListener(editor.editorStrategy);
    }

    String getCurrentLang() {
        if (currentLang == null) {
            return "";
        }
        return currentLang;
    }

    void setCurrentLang(String currentLang) {
        this.currentLang = currentLang;
    }

    private void createUIComponents() {
        tabbedPane = new JTabbedPane();
        table1 = new JBTable();
        table1.setFillsViewportHeight(true);
        table1.setComponentPopupMenu(createPopupMenu());
        scrollPane1 = new JBScrollPane(table1);
        table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setCellRenderer();
        btnAddNewKey = new JButton();
    }

    private void setCellRenderer() {
        for (int i = 1; i < table1.getColumnCount(); i++) {
            table1.getColumnModel().getColumn(i).setCellRenderer(new ValueEnteredTableCellRenderer());
        }
    }

    void initTabs(Project project, ConcurrentHashMap<String, VirtualFile> fileMap) {
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


    private JPopupMenu createPopupMenu() {

        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            final Project project = GravProjectComponent.getEnabledProject();
            if (colAtPoint != 0 && project != null) return;
            String key = model.getKeys(true).get(rowAtPoint);
            List<String> qualifiedKey = GravYAMLUtils.splitKeyAsList(key);
            switch (editor.getLanguageFileEditorType()) {
                case LANGUAGE_FILE:
                    VirtualFile file = fileMap.elements().nextElement();
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(file); // project can't be null here, so ignore warning
                    if (psiFile == null || ((YAMLFile) psiFile).getDocuments() == null) return;
                    YAMLDocument doc = ((YAMLFile) psiFile).getDocuments().get(0);

                    ApplicationManager.getApplication().invokeLater(() -> {
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
                    }, ModalityState.current());

                    break;
                case LANGUAGE_FOLDER:
                    ApplicationManager.getApplication().invokeLater(() -> {
                        try {
                            WriteCommandAction.writeCommandAction(project).withName(ACTION_NAME).run((ThrowableRunnable<Throwable>) () -> {
                                for (String eachLang : model.getLanguages()) {
                                    VirtualFile file1 = fileMap.get(eachLang);
                                    PsiFile psiFile1 = PsiManager.getInstance(project).findFile(file1); // project can't be null here, so ignore warning
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
                    });
                    break;
            }

            colAtPoint = -1;
            rowAtPoint = -1;
        }));
        popupMenu.add(deleteItem);
        popupMenu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                SwingUtilities.invokeLater(() -> {
                    colAtPoint = table1.columnAtPoint(SwingUtilities.convertPoint(popupMenu, new Point(0, 0), table1));
                    rowAtPoint = table1.rowAtPoint(SwingUtilities.convertPoint(popupMenu, new Point(0, 0), table1));

                    if (colAtPoint != 0)
                        deleteItem.setEnabled(false);
                    else {
                        deleteItem.setEnabled(true);
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

    @SuppressWarnings("unused")
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    @SuppressWarnings("unused")
    public JTable getTable1() {
        return table1;
    }
}


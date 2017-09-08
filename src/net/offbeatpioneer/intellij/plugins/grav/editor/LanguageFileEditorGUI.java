package net.offbeatpioneer.intellij.plugins.grav.editor;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ThrowableRunnable;
import net.offbeatpioneer.intellij.plugins.grav.helper.GravYAMLUtils;
import net.offbeatpioneer.intellij.plugins.grav.helper.GravYamlFiles;
import net.offbeatpioneer.intellij.plugins.grav.project.GravProjectComponent;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI of the translation editor
 * <p>
 * The two types of language files {@link GravYamlFiles.LangFileEditorType#LANGUAGE_FILE} and
 * {@link GravYamlFiles.LangFileEditorType#LANGUAGE_FOLDER} are taken into consideration here. That means
 * the handling of removing or adding keys is slightly different.
 *
 * @author Dominik Grzelak
 */
public class LanguageFileEditorGUI implements ChangeListener {
    private Logger LOG = Logger.getInstance(this.getClass());
    private static final String ACTION_NAME = "GravRemoveKey";

    private GravLangFileEditor fileEditor;
    private TranslationTableModel model;
    private String currentLang = "de";
    private int selectedTab = -1;

    private BasicPopupListener basicPopupListener;
    private JPanel mainPanel;
    private JTable table1;
    private JScrollPane scrollPane1;
    private JButton btnAddNewKey;
    public JTabbedPane tabbedPane;
    private JPanel topPanel;
    private JPanel centerPanel;

    LanguageFileEditorGUI(GravLangFileEditor fileEditor, TranslationTableModel model) {
        this.model = model;
        this.fileEditor = fileEditor;

        this.table1.setModel(model);
        this.tabbedPane.addChangeListener(this);
        this.btnAddNewKey.addActionListener(fileEditor.editorStrategy);
        this.btnAddNewKey.setIcon(AllIcons.General.Add);
        this.setDefaultLanguageForEditor();
    }

    private void setDefaultLanguageForEditor() {
        if (fileEditor.getFileMap().size() != 0) {
            String lang = fileEditor.getFileMap().keys().hasMoreElements() ? fileEditor.getFileMap().keys().nextElement() : "";
            setCurrentLang(lang);
        }
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

    void initTabs() {
        Project project = fileEditor.getProject();
        tabbedPane.removeAll();
        if (project.isDisposed()) {
            fileEditor.editorStrategy.getFileMap().clear();
            return;
        }

        addTab("Overview", scrollPane1);
        fileEditor.editorStrategy.initTab(this);

        setDefaultLanguageForEditor();

        setCellRenderer();
    }

    public void addTab(String name, JComponent component) {
        tabbedPane.addTab(name, component);
    }

    private JPopupMenu createPopupMenu() {

        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete Key");
        deleteItem.setIcon(AllIcons.General.Remove);
        deleteItem.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            final Project project = GravProjectComponent.getEnabledProject();
            if (project == null) return;
            String key = model.getKeys(true).get(basicPopupListener.rowAtPoint);
            List<String> qualifiedKey = GravYAMLUtils.splitKeyAsList(key);
            switch (fileEditor.getLanguageFileEditorType()) {
                case LANGUAGE_FILE:
                    VirtualFile file = fileEditor.getFileMap().elements().nextElement();
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
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
                    });
                    break;
            }

            basicPopupListener.colAtPoint = -1;
            basicPopupListener.rowAtPoint = -1;
        }));
        popupMenu.add(deleteItem);

        JMenuItem jumpToKey = new JMenuItem("Jump To Key");
        jumpToKey.setIcon(AllIcons.General.Locate);
        popupMenu.add(jumpToKey);
        jumpToKey.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            final Project project = GravProjectComponent.getEnabledProject();
            if (project == null) return;
            String key = model.getKeys(true).get(basicPopupListener.rowAtPoint);
            List<String> qualifiedKey = GravYAMLUtils.splitKeyAsList(key);
            String lang = model.getLanguages().get(basicPopupListener.colAtPoint - 1);
            VirtualFile file;
            switch (fileEditor.getLanguageFileEditorType()) {
                case LANGUAGE_FILE:
                    file = fileEditor.getFileMap().elements().nextElement();
                    qualifiedKey.add(0, lang);
                    break;
                case LANGUAGE_FOLDER:
                    file = fileEditor.getFileMap().get(lang);
                    break;
                default:
                    file = fileEditor.getFileMap().get(lang);
                    break;
            }
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile == null || ((YAMLFile) psiFile).getDocuments() == null) return;
            YAMLDocument doc = ((YAMLFile) psiFile).getDocuments().get(0);

            tabbedPane.setSelectedIndex(basicPopupListener.colAtPoint);

            YAMLKeyValue value = YAMLUtil.getQualifiedKeyInDocument(doc, qualifiedKey);
            if (value == null || value.getOriginalElement() == null) return;
            PsiElement psiElement = value.getOriginalElement();
            EventQueue.invokeLater(() -> {
                fileEditor.editorStrategy.editorMap.get(lang).getContentComponent().grabFocus();
                fileEditor.editorStrategy.editorMap.get(lang).getContentComponent().requestFocusInWindow();
            });

            ScrollingModel scrollingModel = fileEditor.editorStrategy.editorMap.get(lang).getScrollingModel();
            CaretModel caretModel = fileEditor.editorStrategy.editorMap.get(lang).getCaretModel();
            caretModel.moveToOffset(psiElement.getTextOffset() + psiElement.getTextLength(), false);
            scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE);
            caretModel.getCurrentCaret().setSelection(caretModel.getOffset(), caretModel.getOffset());

        }));

        basicPopupListener = new BasicPopupListener(table1, popupMenu, deleteItem, jumpToKey);
        popupMenu.addPopupMenuListener(basicPopupListener);
        return popupMenu;
    }

    public Editor createEditor(Document document, Project project, @Nullable FileType fileType) {
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


    /**
     * Tab change listener
     *
     * @param e
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        selectedTab = -1;
        if (e.getSource() instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
            selectedTab = tabbedPane.getSelectedIndex();
        }
    }

    /**
     * Returns the currently selected index of the tabbedpane of the language file editor.
     *
     * @return index of the tab, otherwise -1 if there is no currently selected tab
     */
    public int getSelectedTab() {
        return selectedTab;
    }
}


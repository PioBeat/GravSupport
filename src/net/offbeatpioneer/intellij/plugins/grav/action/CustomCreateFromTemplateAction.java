package net.offbeatpioneer.intellij.plugins.grav.action;


import com.intellij.CommonBundle;
import com.intellij.ide.IdeView;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteActionAware;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import net.offbeatpioneer.intellij.plugins.grav.project.GravProjectComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 *
 * Copy of CreateFromTemplateAction to use another CreateFileDialog (from Eugene.Kudelevsky)
 *
 * @author Dominik Grzelak
 */
public abstract class CustomCreateFromTemplateAction<T extends PsiElement> extends AnAction implements WriteActionAware {
    protected static final Logger LOG = Logger.getInstance("CustomCreateFromTemplateAction");

    public CustomCreateFromTemplateAction() {
    }

    public CustomCreateFromTemplateAction(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    @Override
    public final void actionPerformed(final AnActionEvent e) {
        final DataContext dataContext = e.getDataContext();

        final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view == null) {
            return;
        }

        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final Module module = dataContext.getData(DataKeys.MODULE);

        final PsiDirectory dir = view.getOrChooseDirectory();
        if (dir == null || project == null) return;

        final CustomCreateFileFromTemplateDialog.Builder builder = CustomCreateFileFromTemplateDialog.createDialog(project);
        buildDialog(project, dir, builder);

        final Ref<String> selectedTemplateName = Ref.create(null);
        final T createdElement =
                builder.show(getErrorTitle(), getDefaultTemplateName(dir), new CustomCreateFileFromTemplateDialog.FileCreator<T>() {

                    @Override
                    public T createFile(@NotNull String name, @NotNull String templateName) {
                        selectedTemplateName.set(templateName);
                        return CustomCreateFromTemplateAction.this.createFile(name, templateName, dir);
                    }

                    @Override
                    public boolean startInWriteAction() {
                        return CustomCreateFromTemplateAction.this.startInWriteAction();
                    }

                    @Override
                    @NotNull
                    public String getActionName(@NotNull String name, @NotNull String templateName) {
                        return CustomCreateFromTemplateAction.this.getActionName(dir, name, templateName);
                    }
                });
        if (createdElement != null) {
            view.selectElement(createdElement);
            postProcess(createdElement, selectedTemplateName.get(), builder.getCustomProperties());
        }
    }

    protected void postProcess(T createdElement, String templateName, Map<String,String> customProperties) {
    }

    @Nullable
    protected abstract T createFile(String name, String templateName, PsiDirectory dir);

    protected abstract void buildDialog(Project project, PsiDirectory directory, CustomCreateFileFromTemplateDialog.Builder builder);

    @Nullable
    protected String getDefaultTemplateName(@NotNull PsiDirectory dir) {
        String property = getDefaultTemplateProperty();
        return property == null ? null : PropertiesComponent.getInstance(dir.getProject()).getValue(property);
    }

    @Nullable
    protected String getDefaultTemplateProperty() {
        return null;
    }

    @Override
    public void update(final AnActionEvent e) {
        final DataContext dataContext = e.getDataContext();
        final Presentation presentation = e.getPresentation();

        final boolean enabled = isAvailable(dataContext);

        presentation.setVisible(enabled);
        presentation.setEnabled(enabled);
    }

    protected boolean isAvailable(DataContext dataContext) {
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        final boolean pluginEnabled = GravProjectComponent.isEnabled(project);
        return pluginEnabled && view != null && view.getDirectories().length != 0;
    }

    protected abstract String getActionName(PsiDirectory directory, String newName, String templateName);

    protected String getErrorTitle() {
        return CommonBundle.getErrorTitle();
    }

    //todo append $END variable to templates?
    public static void moveCaretAfterNameIdentifier(PsiNameIdentifierOwner createdElement) {
        final Project project = createdElement.getProject();
        final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            final VirtualFile virtualFile = createdElement.getContainingFile().getVirtualFile();
            if (virtualFile != null) {
                if (FileDocumentManager.getInstance().getDocument(virtualFile) == editor.getDocument()) {
                    final PsiElement nameIdentifier = createdElement.getNameIdentifier();
                    if (nameIdentifier != null) {
                        editor.getCaretModel().moveToOffset(nameIdentifier.getTextRange().getEndOffset());
                    }
                }
            }
        }
    }
}

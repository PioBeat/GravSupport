package net.offbeatpioneer.intellij.plugins.grav.editor;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import icons.TwigIcons;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.helper.FileCreateUtil;
import net.offbeatpioneer.intellij.plugins.grav.helper.GravFileTemplateUtil;
import net.offbeatpioneer.intellij.plugins.grav.listener.GravProjectComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Switch from the markdown file to the related template file.
 * <p>
 * From the documentation:
 * "the template from the theme that is used to render a page is based on the filename of the .md file."
 * This behavior can be overridden by setting the <strong>template</strong> variable in the header of a markdown file.
 * Both approaches are used to find all template files in all theme directories.
 *
 * @author Dominik Grzelak
 */
public class SwitchToTemplateLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        Project project = element.getProject();
        if (!GravProjectComponent.isEnabled(project)) return;
        if (!element.getText().contentEquals("---")) return;
        PsiElement[] horizontalLines = PsiTreeUtil.collectElements(element.getContainingFile(), new PsiElementFilter() {
            @Override
            public boolean isAccepted(@NotNull PsiElement element) {
                return element.getText().contentEquals("---");
            }
        });

        PsiElement[] templateVariables = PsiTreeUtil.collectElements(element.getContainingFile(), new TemplateVariableFinder());
        PsiElement templateVariable = null;
        if (templateVariables.length > 0) {
            templateVariable = templateVariables[0];
        }
        int i;
        for (i = 0; i < horizontalLines.length; i++) {
            if (horizontalLines[i] instanceof LeafPsiElement) {
                if (PsiManager.getInstance(project).areElementsEquivalent(element, horizontalLines[i]) && i == 0) {
                    break;
                }
            }
        }

        if (i != 0) return;
        PsiFile mdFile = element.getContainingFile();
        String filename = mdFile.getVirtualFile().getNameWithoutExtension();
        int ix = filename.indexOf(".");
        if (ix != -1) {
            filename = filename.substring(0, ix);
        }
//        String defaultTheme = GravFileTemplateUtil.getDefaultTheme(project);
//        defaultTheme = defaultTheme == null ? "antimatter" : defaultTheme;

        List<VirtualFile> files = FileCreateUtil.findFileByRelativePath(project, filename + ".html.twig");
        if (templateVariable != null) {
            PsiElement sibling = GravFileTemplateUtil.getValueFromVariable(templateVariable);
            if (sibling != null) {
                String templateName = sibling.getText();
                List<VirtualFile> files2 = FileCreateUtil.findFileByRelativePath(project, templateName + ".html.twig");
                files.addAll(files2);
            }
        }

        if (files.size() != 0) {
            Collection<PsiElement> psiElements = new ArrayList<>();
            for (VirtualFile each : files) {
                PsiFile templatePsiFile = PsiManager.getInstance(project).findFile(each);
                if (templatePsiFile != null) {
                    psiElements.add(templatePsiFile);
                }
            }

            NavigationGutterIconBuilder<PsiElement> builder =
                    NavigationGutterIconBuilder.create(getTwigIcon()).
                            setTargets(psiElements).
                            setTooltipText("Navigate to related template file");
            result.add(builder.createLineMarkerInfo(element));
        }
    }

    private class TemplateVariableFinder implements PsiElementFilter {
        int isWithinHeader = 0;

        @Override
        public boolean isAccepted(@NotNull PsiElement element) {
            if (isWithinHeader >= 2) return false;
            if (element.getText().contentEquals("---")) {
                isWithinHeader++;
            }
            if (element instanceof LeafPsiElement && element.getText().contains("template")) {
                return true;
            }
            return false;
        }

    }

    public Icon getTwigIcon() {
        try {
            return TwigIcons.TwigFileIcon;
        } catch (Exception e) {
            return GravIcons.Node_Grav;
        }
    }
}

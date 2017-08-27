package net.offbeatpioneer.intellij.plugins.grav.editor;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.helper.FileCreateUtil;
import net.offbeatpioneer.intellij.plugins.grav.project.GravProjectComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SwitchToTemplateLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
        Project project = element.getProject();
        if (!GravProjectComponent.isEnabled(project)) return;
        if (!element.getText().contentEquals("---")) return;
        PsiElement[] horizontalLines = PsiTreeUtil.collectElements(element.getContainingFile(), new PsiElementFilter() {
            @Override
            public boolean isAccepted(PsiElement element) {
                return element.getText().contentEquals("---");
            }
        });
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

//        String defaultTheme = GravFileTemplateUtil.getDefaultTheme(project);
//        defaultTheme = defaultTheme == null ? "antimatter" : defaultTheme;

        List<VirtualFile> files = FileCreateUtil.findFileByRelativePath(project, filename + ".html.twig");
        if (files.size() != 0) {
            Collection<PsiElement> psiElements = new ArrayList<>();
            for (VirtualFile each : files) {
                PsiFile templatePsiFile = PsiManager.getInstance(project).findFile(each);
                if (templatePsiFile != null) {
                    PsiElement element1 = templatePsiFile.getFirstChild();
                    if (element1 == null) continue;
                    psiElements.add(element1);
                }
            }

            NavigationGutterIconBuilder<PsiElement> builder =
                    NavigationGutterIconBuilder.create(GravIcons.Node_Grav).
                            setTargets(psiElements).
                            setTooltipText("Navigate to related template file");
            result.add(builder.createLineMarkerInfo(element));
        }
    }
}

package net.offbeatpioneer.intellij.plugins.grav.extensions.treestructure;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

public class LanguageNodeFile extends PsiFileNode {


    public LanguageNodeFile(Project project, PsiFile value, ViewSettings viewSettings) {
        super(project, value, viewSettings);
    }

    @Override
    protected void updateImpl(PresentationData data) {
        super.updateImpl(data);
//        data.setPresentableText("languages-file-1");
    }
}
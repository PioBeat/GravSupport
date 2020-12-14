package net.offbeatpioneer.intellij.plugins.grav.extensions.treestructure;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.impl.nodes.NestingTreeNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class LanguageNestingNode extends NestingTreeNode {

    public LanguageNestingNode(@NotNull PsiFileNode originalNode, @NotNull Collection<PsiFileNode> childNodes) {
        super(originalNode, childNodes);
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    public void update(PresentationData presentation) {
        super.update(presentation);
        if (getValue() == null) {
            setValue(null);
        } else {
            presentation.setPresentableText("localization");
            presentation.setIcon(AllIcons.FileTypes.Properties);
        }
    }
}

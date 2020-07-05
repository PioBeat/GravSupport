package net.offbeatpioneer.intellij.plugins.grav.treestructure;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class LanguageTreeStructureProvider implements TreeStructureProvider, DumbAware {
//    @NotNull
//    @Override
//    public Collection<AbstractTreeNode> modify(@NotNull AbstractTreeNode parent, @NotNull Collection<AbstractTreeNode> children, ViewSettings settings) {
//        ArrayList<AbstractTreeNode> nodes = new ArrayList<AbstractTreeNode>();
//
//        if (isLanguageFolder(parent)) {
//            if (children.size() > 0 && parent.getValue() instanceof PsiDirectory) {
//                Collection<PsiFileNode> childrenPsi2 = new ArrayList<>();
//                for (AbstractTreeNode eachChild : children) {
//                    if (!(eachChild.getValue() instanceof PsiFile)) continue;
//                    PsiFile psiFile = (PsiFile) eachChild.getValue();
//                    LanguageNodeFile nodeFile = new LanguageNodeFile(parent.getProject(), psiFile, settings);
//                    childrenPsi2.add(nodeFile);
//                }
//                LanguageNestingNode languageFormNode = new LanguageNestingNode(childrenPsi2.iterator().next(), childrenPsi2);
//                nodes.add(languageFormNode);
//            }
//        } else {
//            nodes.addAll(children);
//        }
//        return nodes;
//    }

    private boolean isLanguageFolder(AbstractTreeNode parent) {
        if (parent != null) {
            return parent.getName() != null && parent.getName().compareTo("languages") == 0;
        }
        return false;
    }

    @Override
    public @NotNull Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent, @NotNull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
        ArrayList<AbstractTreeNode<?>> nodes = new ArrayList<AbstractTreeNode<?>>();
        if (isLanguageFolder(parent)) {
            if (children.size() > 0 && parent.getValue() instanceof PsiDirectory) {
                Collection<PsiFileNode> childrenPsi2 = new ArrayList<>();
                for (AbstractTreeNode<?> eachChild : children) {
                    if (!(eachChild.getValue() instanceof PsiFile)) continue;
                    PsiFile psiFile = (PsiFile) eachChild.getValue();
                    LanguageNodeFile nodeFile = new LanguageNodeFile(parent.getProject(), psiFile, settings);
                    childrenPsi2.add(nodeFile);
                }
                LanguageNestingNode languageFormNode = new LanguageNestingNode(childrenPsi2.iterator().next(), childrenPsi2);
                nodes.add(languageFormNode);
            }
        } else {
            nodes.addAll(children);
        }
        return nodes;
    }

    @Override
    public @Nullable Object getData(@NotNull Collection<AbstractTreeNode<?>> selected, @NotNull String dataId) {
        return null;
    }
}

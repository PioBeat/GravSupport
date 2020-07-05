package net.offbeatpioneer.intellij.plugins.grav.extensions.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.impl.source.xml.XmlAttributeImpl;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.twig.elements.TwigElementFactory;
import com.jetbrains.twig.elements.TwigElementTypes;
import net.offbeatpioneer.intellij.plugins.grav.helper.GravFileTemplateUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ConvertTwigResource extends PsiElementBaseIntentionAction implements IntentionAction {

    @NotNull
    @Override
    public String getText() {
        return "Convert to theme resource link";
    }


    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        String text = "{{ url(\"theme://" + element.getText() + "\") }}";
        PsiElement psiElement = TwigElementFactory.createPsiElement(project, text, TwigElementTypes.PRINT_BLOCK);
        if (psiElement != null)
            element.replace(psiElement);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!element.isWritable()) return false;
        boolean isTwigFile = GravFileTemplateUtil.isTwigTemplateFile(element.getContainingFile()) || element.getContainingFile() instanceof HtmlFileImpl;
        boolean isXmlAttribute = false;
        if (!isTwigFile) return false;
        if (element.getParent() instanceof XmlAttributeValueImpl) {
            XmlAttributeValueImpl parent0 = ((XmlAttributeValueImpl) element.getParent());
            boolean hasTwigElement = PsiTreeUtil.findChildOfType(parent0, OuterLanguageElement.class) != null;
            if (!hasTwigElement && parent0.getParent() instanceof XmlAttributeImpl) {
                XmlAttributeImpl parent1 = (XmlAttributeImpl) parent0.getParent();
                if (parent1.getName().equalsIgnoreCase("href") || parent1.getName().equalsIgnoreCase("src"))
                    isXmlAttribute = true;
            }
        }
        return isXmlAttribute;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }
}

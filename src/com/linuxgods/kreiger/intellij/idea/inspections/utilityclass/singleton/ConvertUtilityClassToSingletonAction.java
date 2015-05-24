package com.linuxgods.kreiger.intellij.idea.inspections.utilityclass.singleton;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ig.psiutils.UtilityClassUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConvertUtilityClassToSingletonAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = getPsiClass(e);
        if (null != psiClass) {
            new ConvertUtilityClassToSingletonFix().applyFix(psiClass.getProject(), psiClass);
        }
    }

    @Nullable
    private PsiClass getPsiClass(AnActionEvent e) {
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (null != element) {
            return PsiTreeUtil.getParentOfType(element, PsiClass.class, false);
        }
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        List<PsiClass> classChildren = PsiTreeUtil.getChildrenOfTypeAsList(psiFile, PsiClass.class);
        if (!classChildren.isEmpty()) {
            return classChildren.get(0);
        }
        return null;
    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = getPsiClass(e);
        if (null != psiClass) {
            e.getPresentation().setVisible(true);
            e.getPresentation().setEnabled(UtilityClassUtil.isUtilityClass(psiClass));
        } else {
            e.getPresentation().setVisible(false);
        }
    }
}

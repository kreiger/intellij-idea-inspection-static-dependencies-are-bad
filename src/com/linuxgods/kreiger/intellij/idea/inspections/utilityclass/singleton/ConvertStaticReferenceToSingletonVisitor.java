package com.linuxgods.kreiger.intellij.idea.inspections.utilityclass.singleton;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.util.RefactoringChangeUtil;

class ConvertStaticReferenceToSingletonVisitor extends JavaElementVisitor {
    private final PsiElementFactory psiElementFactory;
    private PsiClass psiClass;
    private final PsiManager psiManager;

    public ConvertStaticReferenceToSingletonVisitor(PsiElementFactory psiElementFactory, PsiClass psiClass, PsiManager psiManager) {
        this.psiElementFactory = psiElementFactory;
        this.psiClass = psiClass;
        this.psiManager = psiManager;
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression referenceExpression) {
        boolean inClassOrInner = PsiTreeUtil.isAncestor(psiClass, referenceExpression, true) && !hasStaticParentClass(referenceExpression);
        PsiExpression qualifierExpression;
        if (inClassOrInner) {
            qualifierExpression = RefactoringChangeUtil.createThisExpression(psiManager, psiClass);
        } else {
            qualifierExpression = referenceExpression.getQualifierExpression();
            if (qualifierExpression == null) {
                qualifierExpression = psiElementFactory.createReferenceExpression(psiClass);
            }
            qualifierExpression = psiElementFactory.createExpressionFromText(qualifierExpression.getText() + ".getInstance()", referenceExpression);
        }
        referenceExpression.setQualifierExpression(qualifierExpression);
    }

    private boolean hasStaticParentClass(PsiReferenceExpression referenceExpression) {
        PsiElement element = referenceExpression;
        while ( null != (element = PsiTreeUtil.getParentOfType(element, PsiClass.class))) {
            PsiClass parentClass = (PsiClass) element;
            if (parentClass.equals(psiClass)) {
                break;
            }
            if (parentClass.hasModifierProperty(PsiModifier.STATIC)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visitImportStaticReferenceElement(PsiImportStaticReferenceElement reference) {
        reference.getParent().delete();
    }
}

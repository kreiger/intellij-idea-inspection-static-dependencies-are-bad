package com.linuxgods.kreiger.intellij.idea.inspection.statics.dependency.injection;

import com.intellij.psi.*;

import static com.intellij.psi.PsiModifier.STATIC;
import static com.intellij.psi.PsiType.VOID;

class StaticExpressionVisitor extends JavaElementVisitor {
    private boolean result;

    @Override
    public void visitClassObjectAccessExpression(PsiClassObjectAccessExpression expression) {
        result = true;
    }

    @Override
    public void visitField(PsiField field) {
        if (field.getModifierList().hasModifierProperty(STATIC)) {
            result = true;
        }
    }

    @Override
    public void visitMethod(PsiMethod method) {
        if (!method.getModifierList().hasModifierProperty(STATIC)) {
            return;
        }
        if (VOID.equals(method.getReturnType())) {
            return;
        }
        result = true;
    }

    public static boolean isStatic(PsiExpression expression) {
        if (expression instanceof PsiClassObjectAccessExpression) {
            return true;
        }
        if (!(expression instanceof PsiReferenceExpression)) {
            return false;
        }
        PsiReferenceExpression referenceExpression = (PsiReferenceExpression)expression;
        PsiElement referent = referenceExpression.resolve();
        if (null == referent) {
            return false;
        }
        StaticExpressionVisitor psiElementVisitor = new StaticExpressionVisitor();
        referent.accept(psiElementVisitor);
        return psiElementVisitor.result;
    }
}

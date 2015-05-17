package com.linuxgods.kreiger.intellij.idea.inspections.utilityclass.singleton;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.util.RefactoringChangeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

class ConvertUtilityClassToSingletonFix implements LocalQuickFix {
    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Convert utility class to singleton.";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Convert utility class to singleton.";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        PsiIdentifier nameIdentifier = (PsiIdentifier) problemDescriptor.getPsiElement();
        PsiElement parent = nameIdentifier.getParent();
        if (!(parent instanceof PsiClass)) {
            return;
        }
        final PsiClass psiClass = (PsiClass) parent;
        final PsiManager psiManager = PsiManager.getInstance(project);
        final PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(project);
        PsiClassType psiClassType = PsiType.getTypeByName(psiClass.getQualifiedName(), project, GlobalSearchScope.projectScope(project));
        for (PsiMember psiMember : members(psiClass.getFields(), psiClass.getMethods())) {
            if (psiMember instanceof PsiField && psiMember.hasModifierProperty(PsiModifier.FINAL)) {
                continue;
            }
            for (PsiReference memberReference : ReferencesSearch.search(psiMember).findAll()) {
                memberReference.getElement().accept(new StaticReferenceToSingletonVisitor(psiElementFactory, psiClass, psiManager));
            }
            psiMember.getModifierList().setModifierProperty(PsiModifier.STATIC, false);
        }
        PsiField instanceField = psiElementFactory.createField("instance", psiClassType);
        instanceField.getModifierList().setModifierProperty(PsiModifier.STATIC, true);
        instanceField.setInitializer(psiElementFactory.createExpressionFromText("new "+psiClass.getName()+"()", instanceField));
        psiClass.add(instanceField);

        PsiMethod getInstanceMethod = psiElementFactory.createMethod("getInstance", psiClassType);
        getInstanceMethod.getModifierList().setModifierProperty(PsiModifier.STATIC, true);
        getInstanceMethod.getBody().add(psiElementFactory.createStatementFromText("return instance;", getInstanceMethod.getBody()));
        psiClass.add(getInstanceMethod);
    }

    @NotNull
    private static PsiMember[] members(PsiMember[]... memberses) {
        List<PsiMember> result = new ArrayList<>();
        for (PsiMember[] members : memberses) {
            result.addAll(asList(members));
        }
        return result.toArray(new PsiMember[result.size()]);
    }

    private static class StaticReferenceToSingletonVisitor extends JavaElementVisitor {
        private final PsiElementFactory psiElementFactory;
        private PsiClass psiClass;
        private final PsiManager psiManager;

        public StaticReferenceToSingletonVisitor(PsiElementFactory psiElementFactory, PsiClass psiClass, PsiManager psiManager) {
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
}

package com.linuxgods.kreiger.intellij.idea.inspections.utilityclass.singleton;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
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

        fixReferences(psiClass, psiManager, psiElementFactory);

        PsiClassType psiClassType = PsiType.getTypeByName(psiClass.getQualifiedName(), project, GlobalSearchScope.projectScope(project));
        psiClass.add(createInstanceField(psiClass, psiElementFactory, psiClassType));
        psiClass.add(createGetInstanceMethod(psiElementFactory, psiClassType));
    }

    private void fixReferences(PsiClass psiClass, PsiManager psiManager, PsiElementFactory psiElementFactory) {
        for (PsiMember psiMember : members(psiClass.getFields(), psiClass.getMethods())) {
            if (psiMember instanceof PsiField && psiMember.hasModifierProperty(PsiModifier.FINAL)) {
                continue;
            }
            for (PsiReference memberReference : ReferencesSearch.search(psiMember).findAll()) {
                memberReference.getElement().accept(new ConvertStaticReferenceToSingletonVisitor(psiElementFactory, psiClass, psiManager));
            }
            psiMember.getModifierList().setModifierProperty(PsiModifier.STATIC, false);
        }
    }

    @NotNull
    private PsiMethod createGetInstanceMethod(PsiElementFactory psiElementFactory, PsiClassType psiClassType) {
        PsiMethod getInstanceMethod = psiElementFactory.createMethod("getInstance", psiClassType);
        getInstanceMethod.getModifierList().setModifierProperty(PsiModifier.STATIC, true);
        getInstanceMethod.getBody().add(psiElementFactory.createStatementFromText("return instance;", getInstanceMethod.getBody()));
        return getInstanceMethod;
    }

    @NotNull
    private PsiField createInstanceField(PsiClass psiClass, PsiElementFactory psiElementFactory, PsiClassType psiClassType) {
        PsiField instanceField = psiElementFactory.createField("instance", psiClassType);
        instanceField.getModifierList().setModifierProperty(PsiModifier.STATIC, true);
        instanceField.setInitializer(psiElementFactory.createExpressionFromText("new "+psiClass.getName()+"()", instanceField));
        return instanceField;
    }

    @NotNull
    private static PsiMember[] members(PsiMember[]... memberses) {
        List<PsiMember> result = new ArrayList<>();
        for (PsiMember[] members : memberses) {
            result.addAll(asList(members));
        }
        return result.toArray(new PsiMember[result.size()]);
    }

}

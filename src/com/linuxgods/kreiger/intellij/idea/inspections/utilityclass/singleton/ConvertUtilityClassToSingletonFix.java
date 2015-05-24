package com.linuxgods.kreiger.intellij.idea.inspections.utilityclass.singleton;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
    public void applyFix(@NotNull final Project project, @NotNull ProblemDescriptor problemDescriptor) {
        final PsiClass psiClass = getPsiClass(problemDescriptor);
        if (psiClass == null) {
            return;
        }

        final Map<PsiMember, Collection<PsiReference>> references = findReferences(members(psiClass.getFields(), psiClass.getMethods(), psiClass.getInitializers()));
        new ConvertUtilityClassToSingletonCommandAction(project, psiClass, references).execute();
    }

    @Nullable
    private PsiClass getPsiClass(@NotNull ProblemDescriptor problemDescriptor) {
        PsiIdentifier nameIdentifier = (PsiIdentifier) problemDescriptor.getPsiElement();
        PsiElement parent = nameIdentifier.getParent();
        if (!(parent instanceof PsiClass)) {
            return null;
        }
        return (PsiClass) parent;
    }

    @NotNull
    private PsiFile[] getContainingFiles(PsiClass psiClass, Map<PsiMember, Collection<PsiReference>> references) {
        Set<PsiFile> files = new HashSet<>();
        for (Collection<PsiReference> memberReferences : references.values()) {
            for (PsiReference memberReference : memberReferences) {
                files.add(memberReference.getElement().getContainingFile());
            }
        }
        files.add(psiClass.getContainingFile());
        return files.toArray(new PsiFile[files.size()]);
    }

    private void fixReferences(PsiClass psiClass, PsiManager psiManager, PsiElementFactory psiElementFactory, Map<PsiMember, Collection<PsiReference>> references) {
        for (Map.Entry<PsiMember, Collection<PsiReference>> memberReferences : references.entrySet()) {
            for (PsiReference memberReference : memberReferences.getValue()) {
                memberReference.getElement().accept(new ConvertStaticReferenceToSingletonVisitor(psiElementFactory, psiClass, psiManager));
            }
            memberReferences.getKey().getModifierList().setModifierProperty(PsiModifier.STATIC, false);
        }
    }

    @NotNull
    private Map<PsiMember, Collection<PsiReference>> findReferences(PsiMember[] members) {
        Map<PsiMember, Collection<PsiReference>> references = new HashMap<>();
        for (PsiMember psiMember : members) {
            if (psiMember instanceof PsiField && psiMember.hasModifierProperty(PsiModifier.FINAL)) {
                continue;
            }
            references.put(psiMember, ReferencesSearch.search(psiMember).findAll());
        }
        return references;
    }

    @NotNull
    private PsiMethod createGetInstanceMethod(PsiElementFactory psiElementFactory, PsiClassType psiClassType) {
        PsiMethod getInstanceMethod = psiElementFactory.createMethod("getInstance", psiClassType);
        getInstanceMethod.getModifierList().setModifierProperty(PsiModifier.STATIC, true);
        getInstanceMethod.getBody().add(psiElementFactory.createStatementFromText("return instance;", getInstanceMethod.getBody()));
        return getInstanceMethod;
    }

    @NotNull
    private static PsiMember[] members(PsiMember[]... memberses) {
        List<PsiMember> result = new ArrayList<>();
        for (PsiMember[] members : memberses) {
            result.addAll(asList(members));
        }
        return result.toArray(new PsiMember[result.size()]);
    }

    private class ConvertUtilityClassToSingletonCommandAction extends WriteCommandAction {
        private final PsiClass psiClass;
        private final Map<PsiMember, Collection<PsiReference>> references;

        public ConvertUtilityClassToSingletonCommandAction(Project project, PsiClass psiClass, Map<PsiMember, Collection<PsiReference>> references) {
            super(project, ConvertUtilityClassToSingletonFix.this.getContainingFiles(psiClass, references));
            this.psiClass = psiClass;
            this.references = references;
        }

        @Override
        protected void run(@NotNull Result result) throws Throwable {
            final PsiManager psiManager = PsiManager.getInstance(getProject());
            final PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(getProject());

            fixReferences(psiClass, psiManager, psiElementFactory, references);

            PsiClassType psiClassType = PsiType.getTypeByName(psiClass.getQualifiedName(), getProject(), GlobalSearchScope.projectScope(getProject()));
            psiClass.add(createGetInstanceMethod(psiElementFactory, psiClassType));
        }
    }
}

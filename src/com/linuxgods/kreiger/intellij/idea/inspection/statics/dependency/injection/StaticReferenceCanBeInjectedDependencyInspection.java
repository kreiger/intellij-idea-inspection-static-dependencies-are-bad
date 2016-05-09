package com.linuxgods.kreiger.intellij.idea.inspection.statics.dependency.injection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

import static com.linuxgods.kreiger.intellij.idea.inspection.statics.dependency.injection.StaticExpressionVisitor.isStatic;

public class StaticReferenceCanBeInjectedDependencyInspection extends LocalInspectionTool {
    private final static Logger LOGGER = Logger.getInstance(StaticReferenceCanBeInjectedDependencyInspection.class);
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (isStatic(expression.getMethodExpression())
                        && Stream.of(expression.getArgumentList().getExpressions()).allMatch(StaticExpressionVisitor::isStatic)
                        ) {
                    holder.registerProblem(expression, null, "static "+expression.getMethodExpression().getReferenceName());
                }
            }
        };
    }

}

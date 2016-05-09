package com.linuxgods.kreiger.intellij.idea.inspection.statics.singleton;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ex.BaseLocalInspectionTool;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.siyeh.ig.psiutils.UtilityClassUtil;
import org.jetbrains.annotations.Nullable;

public class StaticsClassCanBeSingletonInspection extends BaseLocalInspectionTool {

    @Nullable
    @Override
    public ProblemDescriptor[] checkClass(final PsiClass psiClass, InspectionManager manager, boolean isOnTheFly) {
        if (UtilityClassUtil.isUtilityClass(psiClass)) {
            PsiIdentifier nameIdentifier = psiClass.getNameIdentifier();
            if (null != nameIdentifier) {
                return new ProblemDescriptor[] {manager.createProblemDescriptor(nameIdentifier, (TextRange)null, "Utility class can be singleton.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly, new ConvertStaticsClassToSingletonFix())};
            }
        }
        return ProblemDescriptor.EMPTY_ARRAY;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }
}

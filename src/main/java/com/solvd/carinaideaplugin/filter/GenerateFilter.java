package com.solvd.carinaideaplugin.filter;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.Contract;

public interface GenerateFilter {
    ExtensionPointName<GenerateFilter> EP_NAME = ExtensionPointName.create("");

    /**
     * @param psiClass class to check
     * @return return false if toString should not be generated for the class
     */
    @Contract(pure = true)
    boolean canGenerateToString(PsiClass psiClass);
}

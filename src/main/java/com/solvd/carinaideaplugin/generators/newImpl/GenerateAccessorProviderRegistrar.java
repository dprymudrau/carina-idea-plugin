package com.solvd.carinaideaplugin.generators.newImpl;

import com.intellij.codeInsight.generation.EncapsulatableClassMember;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiClass;
import com.intellij.util.NotNullFunction;
import com.intellij.util.containers.ContainerUtil;

import java.util.Collection;
import java.util.List;

public final class GenerateAccessorProviderRegistrar {
    public static final ExtensionPointName<NotNullFunction<PsiClass, Collection<EncapsulatableClassMember>>> EP_NAME = ExtensionPointName.create("com.intellij.generateAccessorProvider");

    public GenerateAccessorProviderRegistrar() {
    }

    static synchronized List<EncapsulatableClassMember> getEncapsulatableClassMembers(PsiClass psiClass) {
        return ContainerUtil.concat(EP_NAME.getExtensionList(), (s) -> {
            return (Collection)s.fun(psiClass);
        });
    }
}
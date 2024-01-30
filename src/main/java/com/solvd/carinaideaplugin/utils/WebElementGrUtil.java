package com.solvd.carinaideaplugin.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class WebElementGrUtil {

    public static PsiField @NotNull [] getAvailableFields(PsiClass clazz) {
        return Arrays.stream(clazz.getFields()).filter(field -> {
            String type = Objects.requireNonNull(PsiUtil.resolveClassInType(field.getType())).getQualifiedName();
            return type != null && type.contains("ExtendedWebElement");
        }).toList().toArray(PsiField.EMPTY_ARRAY);
    }
}

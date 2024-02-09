package com.solvd.carinaideaplugin.utils;

import com.intellij.psi.*;
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

    public static String capitaliseFirstLetter(String fieldName) {
        String firstLetter = fieldName.substring(0, 1);
        String theRest = fieldName.substring(1);
        return firstLetter.toUpperCase() + theRest;
    }

}

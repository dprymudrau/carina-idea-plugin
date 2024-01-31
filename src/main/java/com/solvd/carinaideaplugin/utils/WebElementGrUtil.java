package com.solvd.carinaideaplugin.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
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

    public static PsiMethod generateIsPresentPrototype(@NotNull PsiField psiField) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiField.getProject());
        Project project = psiField.getProject();
        String name = psiField.getName();
        String methodName = "is" + capitalise(name) + "Present";
        PsiMethod method = factory.createMethod(methodName, PsiTypes.booleanType());
        PsiUtil.setModifierProperty(method, PsiModifier.PUBLIC, true);
        PsiCodeBlock body = factory.createCodeBlockFromText("{\nreturn " + name + ".isElementPresent(1);\n}", null);
        Objects.requireNonNull(method.getBody()).replace(body);
        method = (PsiMethod) CodeStyleManager.getInstance(project).reformat(method);
        return method;
    }

    private static String capitalise(String fieldName) {
        String firstLetter = fieldName.substring(0, 1);
        String theRest = fieldName.substring(1);
        return firstLetter.toUpperCase() + theRest;
    }

}

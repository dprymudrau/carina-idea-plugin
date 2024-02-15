package com.solvd.carinaideaplugin.generators.gettext;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiTypes;
import com.solvd.carinaideaplugin.generators.GenerateElementActionsHandlerBase;
import com.solvd.carinaideaplugin.templates.GenerationTemplate;

public class GenerateGetTextHandler extends GenerateElementActionsHandlerBase implements GenerateGetTextActionHandler {

    private final static GenerationTemplate template = GenerationTemplate.builder()
            .name("getText()")
            .message("Select Fields to Generate getText()")
            .methodName("get%sText")
            .returnType(null)
            .returnTypeClass(String.class)
//            .returnType((PsiPrimitiveType) JavaPsiFacade.getElementFactory(get).createTypeFromText("String", null))
            .methodBody("{\nreturn %s.getText();\n}")
            .build();

    public GenerateGetTextHandler() {
        super(template.getMessage(), template);

    }
}
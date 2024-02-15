package com.solvd.carinaideaplugin.generators.click;

import com.intellij.psi.PsiTypes;
import com.solvd.carinaideaplugin.generators.GenerateElementActionsHandlerBase;
import com.solvd.carinaideaplugin.templates.GenerationTemplate;

public class GenerateClickHandler extends GenerateElementActionsHandlerBase implements GenerateClickActionHandler {

    private final static GenerationTemplate template = GenerationTemplate.builder()
            .name("click()")
            .message("Select Fields to Generate click()")
            .methodName("click%s")
            .returnType(PsiTypes.voidType())
            .methodBody("{\n%s.click();\n}")
            .build();

    public GenerateClickHandler() {
        super(template.getMessage(), template);
    }
}
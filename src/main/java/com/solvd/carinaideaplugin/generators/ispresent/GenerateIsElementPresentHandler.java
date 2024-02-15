package com.solvd.carinaideaplugin.generators.ispresent;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiTypes;
import com.solvd.carinaideaplugin.generators.GenerateElementActionsHandlerBase;
import com.solvd.carinaideaplugin.templates.GenerationTemplate;

public class GenerateIsElementPresentHandler extends GenerateElementActionsHandlerBase implements GenerateIsPresentActionHandler {
    private static final Logger LOG = Logger.getInstance(GenerateIsPresentActionHandler.class);

    private final static GenerationTemplate template = GenerationTemplate.builder()
            .name("isElementPresent()")
            .message("Select Fields to Generate isElementPresent()")
            .methodName("is%sPresent")
            .returnType(PsiTypes.booleanType())
            .methodBody("{\nreturn %s.isElementPresent(1);\n}")
            .build();

    public GenerateIsElementPresentHandler() {
        super(template.getMessage(), template);
    }

}
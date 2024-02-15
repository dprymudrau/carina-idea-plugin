package com.solvd.carinaideaplugin.generators.ispresent;

import com.intellij.codeInsight.generation.*;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.*;
import com.solvd.carinaideaplugin.generators.GenerateElementActionsHandlerBase;
import com.solvd.carinaideaplugin.templates.GenerationTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.exception.GenerateCodeException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
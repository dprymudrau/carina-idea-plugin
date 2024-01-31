package com.solvd.carinaideaplugin.generators.newImpl;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.project.DumbAware;

public class GenerateGetterAction extends BaseGenerateAction implements DumbAware {
    public GenerateGetterAction() {
        super(new GenerateGetterHandler());
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
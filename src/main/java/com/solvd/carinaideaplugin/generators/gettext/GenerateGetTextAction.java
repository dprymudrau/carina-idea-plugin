package com.solvd.carinaideaplugin.generators.gettext;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.project.DumbAware;

public class GenerateGetTextAction extends BaseGenerateAction implements DumbAware {
    public GenerateGetTextAction() {
        super(new GenerateGetTextHandler());
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
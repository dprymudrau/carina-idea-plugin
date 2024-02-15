package com.solvd.carinaideaplugin.generators.click;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.project.DumbAware;

public class GenerateClickAction extends BaseGenerateAction implements DumbAware {
    public GenerateClickAction() {
        super(new GenerateClickHandler());
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
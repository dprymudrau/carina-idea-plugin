package com.solvd.carinaideaplugin.generators.ispresent;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.project.DumbAware;

public class GenerateIsElementPresentAction extends BaseGenerateAction implements DumbAware {
    public GenerateIsElementPresentAction() {
        super(new GenerateIsElementPresentHandler());
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
package com.solvd.carinaideaplugin.generators.ispresent;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.project.DumbAware;

public class GenerateIsPresentPopUpDialogAction extends BaseGenerateAction implements DumbAware {

    @Override
    public boolean isDumbAware() {
        return false;
    }
    public GenerateIsPresentPopUpDialogAction() {
        super(new GenerateIsPresentActionHandlerImpl());
    }
}

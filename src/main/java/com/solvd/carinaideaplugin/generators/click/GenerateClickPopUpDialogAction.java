package com.solvd.carinaideaplugin.generators.click;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.project.DumbAware;

public class GenerateClickPopUpDialogAction extends BaseGenerateAction implements DumbAware {

    @Override
    public boolean isDumbAware() {
        return false;
    }
    public GenerateClickPopUpDialogAction() {
        super(new GenerateClickActionHandlerImpl());
    }
}

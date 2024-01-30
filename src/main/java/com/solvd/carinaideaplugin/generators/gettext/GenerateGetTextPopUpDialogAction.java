package com.solvd.carinaideaplugin.generators.gettext;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.project.DumbAware;

public class GenerateGetTextPopUpDialogAction extends BaseGenerateAction implements DumbAware {

    @Override
    public boolean isDumbAware() {
        return false;
    }
    public GenerateGetTextPopUpDialogAction() {
        super(new GenerateGetTextActionHandlerImpl());
    }
}

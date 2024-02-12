package com.solvd.carinaideaplugin.generators;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

import static com.intellij.ui.popup.PopupComponent.LOG;

public class GenerationUtil {

    public static PsiElementClassMember[] filterExistingMethod(PsiField[] filteredFields, PsiMethod[] filteredMethods) {
        PsiElementClassMember[] members = new PsiElementClassMember[filteredFields.length + filteredMethods.length];
        int i = 0;
        for (PsiField filteredField : filteredFields) {
            LOG.debug("Searching existed method for " + filteredField.getName());
            boolean isExists = false;
            for (PsiMethod filteredMethod : filteredMethods) {
                if(filteredMethod.getName().toLowerCase().contains(filteredField.getName())){
                    isExists = true;
                    break;
                }
            }
            if(!isExists){
                members[i] =  new PsiFieldMember(filteredField);
                i++;
            }
        }
        PsiElementClassMember[] result = new PsiElementClassMember[i];
        System.arraycopy(members, 0, result, 0, i);
        return result;
    }
}

package com.solvd.carinaideaplugin.generators.newImpl;

import com.intellij.codeInsight.generation.*;
import com.intellij.java.JavaBundle;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class GenerateGetterHandler extends GenerateGetterSetterHandlerBase {
    public GenerateGetterHandler() {
        super("Select Fields to Generate isElementPresent()");
    }

    @Override
    protected String getHelpId() {
        return "Generate_Getter_Dialog";
    }

    @Override
    protected ClassMember[] chooseOriginalMembers(PsiClass aClass, Project project) {
        if (aClass.isInterface()) {
            return ClassMember.EMPTY_ARRAY; // TODO
        }
        return super.chooseOriginalMembers(aClass, project);
    }

    @Nullable
    @Override
    protected JComponent getHeaderPanel(final Project project) {
        return getHeaderPanel(project, GetterTemplatesManager.getInstance(), JavaBundle.message("generate.equals.hashcode.template"));
    }

    @Override
    protected GenerationInfo[] generateMemberPrototypes(PsiClass aClass, ClassMember original) throws IncorrectOperationException {
        if (original instanceof PropertyClassMember propertyClassMember) {
            final GenerationInfo[] getters = propertyClassMember.generateGetters(aClass);
            if (getters != null) {
                return getters;
            }
        } else if (original instanceof EncapsulatableClassMember encapsulatableClassMember) {
            final GenerationInfo getter = encapsulatableClassMember.generateGetter();
            if (getter != null) {
                return new GenerationInfo[]{getter};
            }
        }
        return GenerationInfo.EMPTY_ARRAY;
    }

    @Override
    protected String getNothingFoundMessage() {
        return JavaBundle.message("generate.getter.error.no.fields");
    }

    @Override
    protected String getNothingAcceptedMessage() {
        return JavaBundle.message("generate.getter.error.no.fields.without.getters");
    }
}
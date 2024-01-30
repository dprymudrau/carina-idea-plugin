package com.solvd.carinaideaplugin.generators.ispresent;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.solvd.carinaideaplugin.panels.MemberChooserHeaderPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.GenerateToStringContext;
import org.jetbrains.java.generate.GenerateToStringUtils;
import org.jetbrains.java.generate.GenerationUtil;
import org.jetbrains.java.generate.config.Config;

import java.util.Arrays;

import static com.solvd.carinaideaplugin.utils.WebElementGrUtil.getAvailableFields;

public class GenerateIsPresentActionHandlerImpl implements GenerateIsPresentActionHandler, CodeInsightActionHandler {
    private static final Logger LOG = Logger.getInstance(GenerateIsPresentActionHandlerImpl.class);

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
        PsiClass psiClass = getSubjectClass(editor, psiFile);
        if(psiClass == null) return;
        execAction(project, psiClass, editor);
    }

    private static void execAction(@NotNull final Project project, @NotNull final PsiClass clazz, final Editor editor){
        LOG.debug("+++ doExecuteAction - START +++");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Current project " + project.getName());
        }

        final PsiElementClassMember<?>[] dialogMembers = buildMembersToShow(clazz);

        final MemberChooserHeaderPanel header = new MemberChooserHeaderPanel(clazz);
        LOG.debug("Displaying member chooser dialog");

        final MemberChooser<PsiElementClassMember<?>> chooser = new MemberChooser<>(dialogMembers, true, true, project, false, header);
        //noinspection DialogTitleCapitalization
        chooser.setTitle("generate isElementPresent()");

        chooser.setCopyJavadocVisible(false);
        chooser.selectElements(getPreselection(clazz, dialogMembers));
        header.setChooser(chooser);

        LOG.debug("Displaying member chooser dialog");

        if (ApplicationManager.getApplication().isUnitTestMode()) {
            chooser.close(DialogWrapper.OK_EXIT_CODE);
        }
        else {
            chooser.show();
        }

    }

    private static PsiElementClassMember<?>[] getPreselection(@NotNull PsiClass clazz, PsiElementClassMember<?>[] dialogMembers) {
        return Arrays.stream(dialogMembers)
                .filter(member -> member.getElement().getContainingClass() == clazz)
                .toArray(PsiElementClassMember[]::new);
    }




    @Nullable
    private static PsiClass getSubjectClass(Editor editor, final PsiFile file) {
        if (file == null) return null;

        int offset = editor.getCaretModel().getOffset();
        PsiElement context = file.findElementAt(offset);

        if (context == null) return null;

        PsiClass clazz = PsiTreeUtil.getParentOfType(context, PsiClass.class, false);
        if (clazz == null) {
            return null;
        }

        //exclude interfaces, non-java classes etc
//        for (GenerateFilter filter : GenerateFilter.EP_NAME.getExtensionList()) {
//            if (!filter.canGenerateToString(clazz)) return null;
//        }
        return clazz;
    }


    public static PsiElementClassMember[] buildMembersToShow(PsiClass clazz) {
        Config config = GenerateToStringContext.getConfig();
        PsiField[] filteredFields = getAvailableFields(clazz);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of fields after filtering: " + filteredFields.length);
        }

        PsiMethod[] filteredMethods;
        if (config.enableMethods) {
            filteredMethods = GenerateToStringUtils.filterAvailableMethods(clazz, config.getFilterPattern());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Number of methods after filtering: " + filteredMethods.length);
            }
        } else {
            filteredMethods = PsiMethod.EMPTY_ARRAY;
        }

        return GenerationUtil.combineToClassMemberList(filteredFields, filteredMethods);
    }
}

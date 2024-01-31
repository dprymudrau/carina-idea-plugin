package com.solvd.carinaideaplugin.generators.newImpl;

import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.GetterTemplatesManager;
import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.util.MemberChooser;
import com.intellij.java.JavaBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.solvd.carinaideaplugin.generators.ispresent.GenerateIsPresentActionHandlerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.GenerateToStringContext;
import org.jetbrains.java.generate.GenerateToStringUtils;
import org.jetbrains.java.generate.GenerationUtil;
import org.jetbrains.java.generate.config.Config;
import org.jetbrains.java.generate.exception.GenerateCodeException;

import javax.swing.*;
import java.util.Arrays;

import static com.solvd.carinaideaplugin.utils.WebElementGrUtil.getAvailableFields;

public class GenerateIsElementPresentHandler extends GenerateElementActionsHandlerBase {
    private static final Logger LOG = Logger.getInstance(GenerateIsPresentActionHandlerImpl.class);
    public GenerateIsElementPresentHandler() {
        super("Select Fields to Generate isElementPresent()");
    }


    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
        PsiClass psiClass = getSubjectClass(editor, psiFile);
        if(psiClass == null) return;
        execAction(project, psiClass, editor);
    }

    private  void execAction(@NotNull final Project project, @NotNull final PsiClass clazz, final Editor editor){
        LOG.debug("+++ doExecuteAction - START +++");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Current project " + project.getName());
        }

        final PsiElementClassMember<?>[] dialogMembers = buildMembersToShow(clazz);

        LOG.debug("Displaying member chooser dialog");

        MemberChooser<ClassMember> chooser = new MemberChooser<>(dialogMembers, true, true, project, false, null);
        //noinspection DialogTitleCapitalization
        chooser.setTitle("generate isElementPresent()");

        chooser.setCopyJavadocVisible(false);
//        chooser.selectElements(getPreselection(clazz, dialogMembers));

        LOG.debug("Displaying member chooser dialog");

        if (ApplicationManager.getApplication().isUnitTestMode()) {
            chooser.close(DialogWrapper.OK_EXIT_CODE);
        }
        else {
            chooser.show();
        }

        try {
            CommandProcessor.getInstance().executeCommand(project, () -> {
                final int offset = editor.getCaretModel().getOffset();
                try {
                    doGenerate(project, editor, clazz, null);
                }
                catch (GenerateCodeException e) {
                    final String message = e.getMessage();
                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (!editor.isDisposed()) {
                            editor.getCaretModel().moveToOffset(offset);
                            HintManager.getInstance().showErrorHint(editor, message);
                        }
                    }, project.getDisposed());
                }
            }, null, null);
        }
        finally {
            cleanup();
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

    @Override
    protected String getHelpId() {
        return "Generate_IsPresent_Dialog";
    }

    @Nullable
    @Override
    protected JComponent getHeaderPanel(final Project project) {
        return getHeaderPanel(project, GetterTemplatesManager.getInstance(), JavaBundle.message("generate.equals.hashcode.template"));
    }

    @Override
    protected String getNothingFoundMessage() {
        return "No fields have been found to generate isElementPresent() for";
    }

    @Override
    protected String getNothingAcceptedMessage() {
        return "No fields without isElementPresent() were found";
    }
}
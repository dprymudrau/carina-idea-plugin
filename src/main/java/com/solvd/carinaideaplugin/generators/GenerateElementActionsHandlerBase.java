package com.solvd.carinaideaplugin.generators;

import com.intellij.codeInsight.generation.*;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.ide.util.MemberChooser;
import com.intellij.java.JavaBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.DumbModeAccessType;
import com.intellij.util.ui.UIUtil;
import com.solvd.carinaideaplugin.templates.GenerationTemplate;
import com.solvd.carinaideaplugin.utils.WebElementGrUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.GenerateToStringContext;
import org.jetbrains.java.generate.GenerateToStringUtils;
import org.jetbrains.java.generate.config.Config;
import org.jetbrains.java.generate.exception.GenerateCodeException;
import org.jetbrains.java.generate.template.TemplateResource;
import org.jetbrains.java.generate.template.TemplatesManager;
import org.jetbrains.java.generate.view.TemplatesPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.solvd.carinaideaplugin.utils.WebElementGrUtil.getAvailableFields;

public abstract class GenerateElementActionsHandlerBase extends GenerateMembersHandlerBase {
    private static final Logger LOG = Logger.getInstance(com.intellij.codeInsight.generation.GenerateGetterSetterHandlerBase.class);
    private GenerationTemplate template;
    public GenerateElementActionsHandlerBase(@NlsContexts.DialogTitle String chooserTitle) {
        super(chooserTitle);
    }

    public GenerateElementActionsHandlerBase(@NlsContexts.DialogTitle String chooserTitle, GenerationTemplate template) {
        this(chooserTitle);
        this.template = template;
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
        PsiClass psiClass = getSubjectClass(editor, psiFile);
        if(psiClass == null) return;
        execAction(project, psiClass, editor);
    }

    protected void execAction(@NotNull final Project project, @NotNull final PsiClass clazz, final Editor editor){
        LOG.debug("+++ doExecuteAction - START +++");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Current project " + project.getName());
        }

        final PsiElementClassMember<?>[] dialogMembers = buildMembersToShow(clazz, template);

        LOG.debug("Displaying member chooser dialog");

        MemberChooser<PsiElementClassMember<?>> chooser = new MemberChooser<>(dialogMembers, true, true, project, false, null);
        //noinspection DialogTitleCapitalization
        chooser.setTitle(template.getMessage());

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
                    writeMethods(
                            project,
                            editor,
                            clazz,
                            chooser.getSelectedElements(),
                            this::generatePrototype);
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

    @Override
    protected boolean hasMembers(@NotNull PsiClass aClass) {
//        return !GenerateAccessorProviderRegistrar.getEncapsulatableClassMembers(aClass).isEmpty();
        return true;
    }

    @Override
    protected String getHelpId() {
        return "Getter_and_Setter_Templates_Dialog";
    }

    @Override
    protected ClassMember[] chooseOriginalMembers(PsiClass aClass, Project project, Editor editor) {
        final ClassMember[] allMembers = getAllOriginalMembers(aClass);
        if (allMembers == null) {
            HintManager.getInstance().showErrorHint(editor, getNothingFoundMessage());
            return null;
        }
        if (allMembers.length == 0) {
            HintManager.getInstance().showErrorHint(editor, getNothingAcceptedMessage());
            return null;
        }
        return chooseMembers(allMembers, false, false, project, editor);
    }

    protected static JComponent getHeaderPanel(final Project project, final TemplatesManager templatesManager, final @Nls String templatesTitle) {
        final JPanel panel = new JPanel(new BorderLayout());
        final JLabel templateChooserLabel = new JLabel(templatesTitle);
        panel.add(templateChooserLabel, BorderLayout.WEST);
        final ComboBox<TemplateResource> comboBox = new ComboBox<>();
        templateChooserLabel.setLabelFor(comboBox);
        comboBox.setRenderer(SimpleListCellRenderer.create("", TemplateResource::getName));
        final ComponentWithBrowseButton<ComboBox<?>> comboBoxWithBrowseButton =
                new ComponentWithBrowseButton<>(comboBox, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final TemplatesPanel ui = new TemplatesPanel(project, templatesManager) {
                            @Override
                            protected boolean onMultipleFields() {
                                return false;
                            }

                            @Nls
                            @Override
                            public String getDisplayName() {
                                return StringUtil.capitalizeWords(UIUtil.removeMnemonic(StringUtil.trimEnd(templatesTitle, ":")), true);
                            }
                        };
                        ui.setHint(JavaBundle.message("generate.getter.setter.header.visibility.hint."));
                        ui.selectNodeInTree(templatesManager.getDefaultTemplate());
                        if (ShowSettingsUtil.getInstance().editConfigurable(panel, ui)) {
                            setComboboxModel(templatesManager, comboBox);
                        }
                    }
                });

        setComboboxModel(templatesManager, comboBox);
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(@NotNull final ActionEvent M) {
                templatesManager.setDefaultTemplate((TemplateResource)comboBox.getSelectedItem());
            }
        });

        panel.add(comboBoxWithBrowseButton, BorderLayout.CENTER);
        return panel;
    }

    @Nullable
    @Override
    protected JComponent getHeaderPanel(final Project project) {
        return getHeaderPanel(project, GetterTemplatesManager.getInstance(), JavaBundle.message("generate.equals.hashcode.template"));
    }

    private static void setComboboxModel(TemplatesManager templatesManager, ComboBox<TemplateResource> comboBox) {
        final Collection<TemplateResource> templates = templatesManager.getAllTemplates();
        comboBox.setModel(new DefaultComboBoxModel<>(templates.toArray(new TemplateResource[0])));
        comboBox.setSelectedItem(templatesManager.getDefaultTemplate());
    }

    @Override
    protected String getNothingFoundMessage() {
        return String.format("No fields have been found to generate %s for", template.getName());
    }

    protected String getNothingAcceptedMessage() {
        return String.format("No fields without %s were found", template.getName());
    }

//    public boolean canBeAppliedTo(PsiClass targetClass) {
//        final ClassMember[] allMembers = getAllOriginalMembers(targetClass);
//        return allMembers != null && allMembers.length != 0;
//    }

    @Override
    protected ClassMember @Nullable [] getAllOriginalMembers(final PsiClass aClass) {
        final java.util.List<EncapsulatableClassMember> list = GenerateAccessorProviderRegistrar.getEncapsulatableClassMembers(aClass);
        if (list.isEmpty()) {
            return null;
        }
        final List<EncapsulatableClassMember> members = ContainerUtil.findAll(list, member -> {
            try {
                return DumbModeAccessType.RELIABLE_DATA_ONLY.ignoreDumbMode(() -> generateMemberPrototypes(aClass, member).length > 0);
            }
            catch (GenerateCodeException e) {
                return true;
            }
            catch (IncorrectOperationException e) {
                LOG.error(e);
                return false;
            }
        });
        return members.toArray(ClassMember.EMPTY_ARRAY);
    }

    protected static void writeMethods(final Project project, final @NotNull Editor editor, final PsiClass clazz, List<PsiElementClassMember<?>> members, Function<List<PsiElementClassMember<?>>, List<GenerationInfo>> generationFunction) {
        int offset = editor.getCaretModel().getOffset();
        ArrayList<TemplateGenerationInfo> templates = WriteAction.compute(
                        () -> DumbModeAccessType.RELIABLE_DATA_ONLY.ignoreDumbMode(
                                () -> GenerateMembersUtil.insertMembersAtOffset(clazz, offset, generationFunction.apply(members))))
                .stream()
                .filter(member -> member instanceof TemplateGenerationInfo)
                .map(member -> (TemplateGenerationInfo) member).collect(Collectors.toCollection(ArrayList::new));
        if (!templates.isEmpty()) {
            runTemplates(project, editor, templates, 0);
        }
    }


    protected static void runTemplates(final Project myProject, final @NotNull Editor editor, final @NotNull List<? extends TemplateGenerationInfo> templates, final int index) {
        TemplateGenerationInfo info = templates.get(index);
        final Template template = info.getTemplate();

        PsiElement element = Objects.requireNonNull(info.getPsiMember());
        final TextRange range = element.getTextRange();
        WriteAction.run(() -> editor.getDocument().deleteString(range.getStartOffset(), range.getEndOffset()));
        int offset = range.getStartOffset();
        editor.getCaretModel().moveToOffset(offset);
        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
        TemplateManager.getInstance(myProject).startTemplate(editor, template, new TemplateEditingAdapter() {
            @Override
            public void templateFinished(@NotNull Template template, boolean brokenOff) {
                if (index + 1 < templates.size()) {
                    ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(myProject, () ->
                            runTemplates(myProject, editor, templates, index + 1)
                    ));
                }
            }
        });
    }

    protected static PsiMethod generatePrototypeByTemplate(@NotNull PsiField psiField, GenerationTemplate template) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiField.getProject());
        Project project = psiField.getProject();
        String name = psiField.getName();
        String methodName = String.format(template.getMethodName(), WebElementGrUtil.capitaliseFirstLetter(name));
        PsiMethod method = factory.createMethod(methodName, template.getReturnType());
        PsiUtil.setModifierProperty(method, PsiModifier.PUBLIC, true);
        PsiCodeBlock body = factory.createCodeBlockFromText(String.format(template.getMethodBody(), name ), null);
        Objects.requireNonNull(method.getBody()).replace(body);
        method = (PsiMethod) CodeStyleManager.getInstance(project).reformat(method);
        return method;
    }

    protected static @NotNull List<PsiMethod> generate(List<PsiElementClassMember<?>> members, GenerationTemplate template) {
        List<PsiMethod> methods = new ArrayList<>();
        for (PsiElementClassMember<?> member : members) {
            PsiElement field = member.getPsiElement();
            if (field instanceof PsiField) {
                final PsiMethod isPresent = generatePrototypeByTemplate((PsiField) field, template);
                methods.add(isPresent);
            }
        }
        return methods;
    }

    public static PsiElementClassMember[] buildMembersToShow(PsiClass clazz, GenerationTemplate template) {
        Config config = GenerateToStringContext.getConfig();
        PsiField[] filteredFields = getAvailableFields(clazz);
        PsiMethod[] filteredMethods = GenerateToStringUtils.filterAvailableMethods(clazz, config.getFilterPattern());
        return GenerationUtil.filterExistingMethod(filteredFields, filteredMethods, template);
    }

    @Nullable
    protected static PsiClass getSubjectClass(Editor editor, final PsiFile file) {
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

    private  @NotNull List<GenerationInfo> generatePrototype(List<PsiElementClassMember<?>> members) {
        List<PsiMethod> prototypes = generate(members, template);
        final List<GenerationInfo> methods = new ArrayList<>();
        methods.addAll(prototypes.stream().map(prototype -> new PsiGenerationInfo(prototype)).collect(Collectors.toList()));
        return methods;
    }

}

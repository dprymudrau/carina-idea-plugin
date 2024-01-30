package com.solvd.carinaideaplugin;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.java.JavaBundle;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.options.TabbedConfigurable;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.GenerateToStringConfigurable;
import org.jetbrains.java.generate.template.TemplateResource;
import org.jetbrains.java.generate.template.TemplatesManager;
import org.jetbrains.java.generate.template.toString.ToStringTemplatesManager;
import org.jetbrains.java.generate.view.TemplatesPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.jetbrains.java.generate.GenerateToStringActionHandlerImpl.updateDialog;

public class MemberChooserHeaderPanel extends JPanel {

    private MemberChooser<PsiElementClassMember<?>> chooser;
    private final JComboBox<TemplateResource> comboBox;
    private Set<TemplateResource> inaccessibleTemplates = null;

    public void setChooser(MemberChooser<PsiElementClassMember<?>> chooser) {
        this.chooser = chooser;
    }

    public MemberChooserHeaderPanel(final PsiClass clazz) {
        super(new GridBagLayout());

        TemplatesManager templatesManager = ToStringTemplatesManager.getInstance();
        final Collection<TemplateResource> templates = templatesManager.getAllTemplates();

        final JButton settingsButton = new JButton(JavaBundle.message("button.text.settings"));
        settingsButton.setMnemonic(KeyEvent.VK_S);

        comboBox = new ComboBox<>(templates.toArray(new TemplateResource[0]));
        comboBox.addActionListener(e -> updateErrorMessage());
        Project project = clazz.getProject();
        final JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
        final GlobalSearchScope resolveScope = clazz.getResolveScope();
        DumbService dumbService = DumbService.getInstance(project);
        ReadAction.nonBlocking(() -> {
                    final Set<TemplateResource> invalid = new HashSet<>();
                    for (TemplateResource template : templates) {
                        String className = template.getClassName();
                        if (className != null &&
                                dumbService.computeWithAlternativeResolveEnabled(() -> instance.findClass(className, resolveScope)) == null) {
                            invalid.add(template);
                        }
                    }
                    return invalid;
                })
                .finishOnUiThread(ModalityState.any(), invalid -> {
                    inaccessibleTemplates = invalid;
                    updateErrorMessage();
                    comboBox.repaint();
                })
                .submit(AppExecutorUtil.getAppExecutorService());
        comboBox.setRenderer(SimpleListCellRenderer.create((label, value, index) -> {
            label.setText(value.getName());
            if (inaccessibleTemplates != null && inaccessibleTemplates.contains(value)) {
                label.setForeground(JBColor.RED);
            }
        }));
//        settingsButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                final TemplatesPanel ui = new TemplatesPanel(project);
//                Configurable composite = new TabbedConfigurable() {
//                    @Override
//                    @NotNull
//                    protected List<Configurable> createConfigurables() {
//                        List<Configurable> res = new ArrayList<>();
//                        res.add(new GenerateToStringConfigurable(project));
//                        res.add(ui);
//                        return res;
//                    }
//
//                    @Override
//                    public String getDisplayName() {
//                        return JavaBundle.message("generate.tostring.tab.title");
//                    }
//
//                    @Override
//                    public String getHelpTopic() {
//                        return "editing.altInsert.tostring.settings";
//                    }
//
//                    @Override
//                    public void apply() throws ConfigurationException {
//                        super.apply();
//                        updateDialog(clazz, chooser);
//
//                        comboBox.removeAllItems();
//                        for (TemplateResource resource : templatesManager.getAllTemplates()) {
//                            comboBox.addItem(resource);
//                        }
//                        comboBox.setSelectedItem(templatesManager.getDefaultTemplate());
//                    }
//                };
//
//                ShowSettingsUtil.getInstance().editConfigurable(MemberChooserHeaderPanel.this, composite, () -> ui.selectItem(templatesManager.getDefaultTemplate()));
//                composite.disposeUIResources();
//            }
//        });

        comboBox.setSelectedItem(templatesManager.getDefaultTemplate());

        final JLabel templatesLabel = new JLabel(JavaBundle.message("generate.tostring.template.label"));
        templatesLabel.setLabelFor(comboBox);

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.BASELINE;
        constraints.gridx = 0;
        add(templatesLabel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(comboBox, constraints);
        constraints.gridx = 2;
        constraints.weightx = 0.0;
        add(settingsButton, constraints);
    }

    public TemplateResource getSelectedTemplate() {
        return (TemplateResource) comboBox.getSelectedItem();
    }

    private void updateErrorMessage() {
        if (chooser == null || chooser.isDisposed()) return;
        TemplateResource template = (TemplateResource)comboBox.getSelectedItem();
        if (template != null && inaccessibleTemplates != null && inaccessibleTemplates.contains(template)) {
            ComponentValidator validator = ComponentValidator.getInstance(comboBox)
                    .orElseGet(() -> new ComponentValidator(chooser.getDisposable()).installOn(comboBox));
            String className = template.getClassName();
            String message = className != null
                    ? JavaBundle.message("dialog.message.class.not.found", className)
                    : JavaBundle.message("dialog.message.template.not.applicable");
            validator.updateInfo(new ValidationInfo(message, comboBox));
        }
        else {
            ComponentValidator.getInstance(comboBox).ifPresent(v -> v.updateInfo(null));
        }
    }
}

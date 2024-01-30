package com.solvd.carinaideaplugin.panels;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.java.JavaBundle;
import com.intellij.psi.PsiClass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MemberChooserHeaderPanel extends JPanel {

    private MemberChooser<PsiElementClassMember<?>> chooser;

    public void setChooser(MemberChooser<PsiElementClassMember<?>> chooser) {
        this.chooser = chooser;
    }

    public MemberChooserHeaderPanel(final PsiClass clazz) {
        super(new GridBagLayout());

        final JButton settingsButton = new JButton(JavaBundle.message("button.text.settings"));
        settingsButton.setMnemonic(KeyEvent.VK_S);
    }
}

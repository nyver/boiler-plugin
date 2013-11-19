package com.nyver.idea.plugin.boiler.view;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.nyver.idea.plugin.boiler.action.UploadClassAction;
import com.nyver.idea.plugin.boiler.util.ActionUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * BoilerToolWindowFactory
 *
 * @author Yuri Novitsky
 */
public class BoilerToolWindowFactory implements ToolWindowFactory
{
    private static final String TOOLBAR_GROUP_BOILER = "BoilerToolbalGroup";

    private JPanel boilerToolWindowContent;
    private JComboBox urlComboBox;
    private JTextArea boilerTextArea;
    private ToolWindow boilerToolWindow;

    private SimpleToolWindowPanel simpleToolWindowPanel;

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow)
    {
        boilerToolWindow = toolWindow;

        simpleToolWindowPanel = new SimpleToolWindowPanel(false);
        simpleToolWindowPanel.add(boilerToolWindowContent);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(simpleToolWindowPanel, "", false);
        boilerToolWindow.getContentManager().addContent(content);

        DefaultActionGroup actionGroup = new DefaultActionGroup(TOOLBAR_GROUP_BOILER, false);
        actionGroup.add(new UploadClassAction(this));

        JComponent actionToolbar = ActionManager.getInstance().createActionToolbar(TOOLBAR_GROUP_BOILER, actionGroup, true).getComponent();

        simpleToolWindowPanel.setToolbar(actionToolbar);
    }

    public JTextArea getTextArea()
    {
        return boilerTextArea;
    }

    public String getBoilerUrl()
    {
        return urlComboBox.getSelectedItem().toString();
    }
}

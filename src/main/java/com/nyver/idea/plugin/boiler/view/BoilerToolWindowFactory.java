package com.nyver.idea.plugin.boiler.view;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.nyver.idea.plugin.boiler.BoilerPluginSettings;
import com.nyver.idea.plugin.boiler.action.UploadClassAction;

import javax.swing.*;
import java.util.LinkedList;

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

    private Project project;

    private BoilerPluginSettings settings;

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow)
    {
        this.project = project;
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

        setupSettings();
    }

    public void setupSettings()
    {
        settings = ServiceManager.getService(project, BoilerPluginSettings.class);
        LinkedList<String> urls = settings.getUrls();

        if (!urls.isEmpty()) {
            urlComboBox.removeAllItems();
            for(String url: urls) {
                urlComboBox.addItem(url);
            }
        }
    }

    public JTextArea getTextArea()
    {
        return boilerTextArea;
    }

    public String getBoilerUrl()
    {
        return (null != urlComboBox.getSelectedItem() ? urlComboBox.getSelectedItem().toString() : "");
    }

    public BoilerPluginSettings getSettings()
    {
        return settings;
    }
}

package com.nyver.idea.plugin.boiler.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;

/**
 * Action util class
 * @author Yuri Novitsky
 */
public class ActionUtil
{
    public static Project getProject(AnActionEvent event)
    {
        DataContext dataContext = event.getDataContext();
        return PlatformDataKeys.PROJECT.getData(dataContext);
    }
}

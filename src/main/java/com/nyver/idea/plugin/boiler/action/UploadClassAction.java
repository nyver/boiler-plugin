package com.nyver.idea.plugin.boiler.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.nyver.idea.plugin.boiler.util.ActionUtil;

/**
 * Upload to boiler action
 *
 * @author Yuri Novitsky
 */
public class UploadClassAction extends AnAction
{
    public static final String WINDOW_ID_BOILER = "Boiler";

    private Project project;

    /**
     * Get current java file
     *
     * @param event
     * @return
     */
    private PsiJavaFile getCurrentJavaFile(AnActionEvent event)
    {
        PsiFile file = DataKeys.PSI_FILE.getData(event.getDataContext());
        return file instanceof PsiJavaFile ? (PsiJavaFile) file : null;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent)
    {
        project = ActionUtil.getProject(anActionEvent);

        PsiJavaFile file = getCurrentJavaFile(anActionEvent);

        if (null != file) {

            PsiClass[] classes = file.getClasses();

            if (classes.length >= 1) {

                for(PsiClass cls: classes) {

                    StringBuilder className = new StringBuilder();
                    className.append(file.getPackageName());
                    if (className.length() > 0) {
                        className.append(".");
                    }

                    className.append(cls.getNameIdentifier().getText());

                    UploadClassRunnable process = new UploadClassRunnable(className.toString(), file.getText());
                    ProgressManager.getInstance().runProcess(process, process);
                }
            } else {
                notifyMessage("Java classes are not found", MessageType.ERROR);
            }
        } else {
            notifyMessage("Current file is not a java file", MessageType.ERROR);
        }
    }

    public Project getProject()
    {
        return project;
    }

    public void notifyMessage(String message, MessageType type)
    {
        ToolWindowManager.getInstance(getProject()).notifyByBalloon(
                WINDOW_ID_BOILER,
                type,
                message
        );
    }

    private class UploadClassRunnable extends ProgressIndicatorBase implements Runnable
    {
        private String className;
        private String text;

        private UploadClassRunnable(String className, String text)
        {
            this.className = className;
            this.text = text;
        }

        @Override
        public void run()
        {
            notifyMessage(String.format("Class \"%s\" has uploaded successfully", className), MessageType.INFO);
        }
    }
}

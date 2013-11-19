package com.nyver.idea.plugin.boiler.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.StatusBarEx;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.nyver.idea.plugin.boiler.Boiler;
import com.nyver.idea.plugin.boiler.BoilerException;
import com.nyver.idea.plugin.boiler.util.ActionUtil;
import com.nyver.idea.plugin.boiler.view.BoilerToolWindowFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Upload to boiler action
 *
 * @author Yuri Novitsky
 */
public class UploadClassAction extends AnAction
{
    public static final String WINDOW_ID_BOILER = "Boiler";
    public static final String ICON_PATH = "/images/boiler.png";

    private Project project;

    private BoilerToolWindowFactory toolWindowFactory;

    public UploadClassAction(BoilerToolWindowFactory toolWindowFactory)
    {
        super(IconLoader.findIcon(ICON_PATH));
        this.toolWindowFactory = toolWindowFactory;
    }

    public UploadClassAction()
    {
        super();
    }

    public UploadClassAction(Icon icon)
    {
        super(icon);
    }

    public UploadClassAction(@Nullable String text)
    {
        super(text);
    }

    public UploadClassAction(@Nullable String text, @Nullable String description, @Nullable Icon icon)
    {
        super(text, description, icon);
    }

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

        final PsiJavaFile file = getCurrentJavaFile(anActionEvent);

        if (null != file) {

            PsiClass[] classes = file.getClasses();

            if (classes.length >= 1) {

                StringBuilder builder = new StringBuilder();
                builder.append(file.getPackageName());
                if (builder.length() > 0) {
                    builder.append(".");
                }

                builder.append(classes[0].getNameIdentifier().getText());

                final String className = builder.toString();

                Task.Backgroundable task = new Task.Backgroundable(null, "Uploading class to boiler", true)
                {

                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator)
                    {
                        Runnable process = new UploadClassRunnable(className, file.getText());
                        process.run();
                    }
                };

                ProgressManager.getInstance().run(task);

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

    public void notifyMessage(final String message, final MessageType type)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ToolWindowManager.getInstance(getProject()).notifyByBalloon(
                        WINDOW_ID_BOILER,
                        type,
                        message
                );
            }
        });
    }

    private class UploadClassRunnable implements Runnable
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
            String url = toolWindowFactory.getBoilerUrl();

            if (!url.isEmpty()) {

                toolWindowFactory.getTextArea().setText("");

                toolWindowFactory.getSettings().saveUrl(url);
                toolWindowFactory.setupSettings();

                toolWindowFactory.getTextArea().append(String.format("Class \"%s\" is uploading to \"%s\"...\n", className, url));

                Boiler boiler = new Boiler();

                try {

                    boiler.upload(url, className, text);

                    String message = String.format("Class \"%s\" has been uploaded successfully\n", className);
                    toolWindowFactory.getTextArea().append(message);
                    notifyMessage(message, MessageType.INFO);
                } catch (BoilerException e) {
                    notifyMessage(e.getMessage(), MessageType.ERROR);
                    toolWindowFactory.getTextArea().append(e.getMessage() + "\n");
                    String response = boiler.getResponse();
                    if (!response.isEmpty()) {
                        toolWindowFactory.getTextArea().append(response);
                    }
                }

            } else {
                notifyMessage("Boiler url is empty", MessageType.ERROR);
            }
        }

    }
}

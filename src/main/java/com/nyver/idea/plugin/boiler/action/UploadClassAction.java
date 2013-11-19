package com.nyver.idea.plugin.boiler.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
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

                ApplicationManager.getApplication().runReadAction(
                        new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                UploadClassRunnable process = new UploadClassRunnable(className.toString(), file.getText());
                                ProgressManager.getInstance().runProcess(process, process);
                            }
                        }
                );
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

        @Override
        public void start() {
            super.start();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (ApplicationManager.getApplication().isDisposed()) return;
                    final WindowManager windowManager = WindowManager.getInstance();
                    if (windowManager == null) return;

                    Project[] projects= ProjectManager.getInstance().getOpenProjects();
                    if (projects.length==0){
                        projects=new Project[]{null};
                    }

                    for (Project project : projects) {
                        final StatusBarEx statusBar = (StatusBarEx) windowManager.getStatusBar(project);
                        if (statusBar == null) continue;

                        statusBar.startRefreshIndication("Uploading to boiler...");
                    }
                }
            });
        }

        @Override
        public void stop() {
            super.stop();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (ApplicationManager.getApplication().isDisposed()) return;
                    final WindowManager windowManager = WindowManager.getInstance();
                    if (windowManager == null) return;

                    Project[] projects= ProjectManager.getInstance().getOpenProjects();
                    if (projects.length==0){
                        projects=new Project[]{null};
                    }

                    for (Project project : projects) {
                        final StatusBarEx statusBar = (StatusBarEx) windowManager.getStatusBar(project);
                        if (statusBar == null) continue;

                        statusBar.stopRefreshIndication();
                    }
                }
            });
        }

    }
}

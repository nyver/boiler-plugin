package com.nyver.idea.plugin.boiler.view;

import com.intellij.openapi.ui.SimpleToolWindowPanel;

/**
 * Boiler panel class
 *
 * @author Yuri Novitsky
 */
public class BoilerPanel extends SimpleToolWindowPanel
{
    public BoilerPanel(boolean vertical)
    {
        super(vertical);
    }

    public BoilerPanel(boolean vertical, boolean borderless)
    {
        super(vertical, borderless);
    }
}

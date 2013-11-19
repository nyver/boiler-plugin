package com.nyver.idea.plugin.boiler;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

/**
 * @author Yuri Novitsky
 */
@State(
        name = "Boiler.Plugin.Settings",
        storages = {
                @Storage(id = "BoilerPluginSettings", file = "$PROJECT_FILE$"),
                @Storage(file = "$PROJECT_CONFIG_DIR$/boilerPluginSettings.xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class BoilerPluginSettings implements PersistentStateComponent<BoilerPluginSettings.State>
{
    private static final int NUMBER_OF_URLS = 5;

    private State myState = new State();

    @Nullable
    @Override
    public State getState()
    {
        return myState;
    }

    @Override
    public void loadState(State state)
    {
        XmlSerializerUtil.copyBean(state, myState);
    }

    public LinkedList<String> getUrls()
    {
        return myState.urls;
    }

    public void saveUrl(String url)
    {
        LinkedList<String> urls = getUrls();
        if (urls.isEmpty() || !urls.getFirst().equals(url)) {
            urls.addFirst(url);
        }

        if (urls.size() > NUMBER_OF_URLS) {
            urls.removeLast();
        }
    }


    public static class State {

        public LinkedList<String> urls = new LinkedList<String>();

    }
}

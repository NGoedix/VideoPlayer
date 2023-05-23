package com.github.NGoedix.watchvideo.util.vlc;

import uk.co.caprica.vlcj.binding.LibC;
import uk.co.caprica.vlcj.binding.RuntimeUtil;

/** Default implementation of a native discovery strategy that searches directories on the Windows operating system. */
public class WindowsNativeDiscoveryStrategyFixed extends DirectoryProviderDiscoveryStrategyFixed {

    private static final String[] FILENAME_PATTERNS = new String[] { "libvlc\\.dll", "libvlccore\\.dll" };

    private static final String[] PLUGIN_PATH_FORMATS = new String[] { "%s\\plugins", "%s\\vlc\\plugins" };

    public WindowsNativeDiscoveryStrategyFixed() {
        super(FILENAME_PATTERNS, PLUGIN_PATH_FORMATS);
    }

    @Override
    public boolean supported() {
        return RuntimeUtil.isWindows();
    }

    @Override
    protected boolean setPluginPath(String pluginPath) {
        return LibC.INSTANCE._putenv(String.format("%s=%s", PLUGIN_ENV_NAME, pluginPath)) == 0;
    }
}
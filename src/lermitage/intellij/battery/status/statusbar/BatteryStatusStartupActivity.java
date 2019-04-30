package lermitage.intellij.battery.status.statusbar;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

public class BatteryStatusStartupActivity implements StartupActivity {
    
    private Logger LOG = Logger.getInstance(getClass().getName());
    
    @Override
    public void runActivity(@NotNull Project project) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        BatteryStatusWidget batteryStatusWidget = new BatteryStatusWidget(project);
        if (statusBar == null) {
            LOG.error("Battery Status activity can't find status bar for project " + project.getName() + " -> Battery Status widget won't be loaded");
        } else {
            statusBar.addWidget(batteryStatusWidget, "after " + (SystemInfo.isMac ? "Encoding" : "InsertOverwrite"), project);
        }
    }
}

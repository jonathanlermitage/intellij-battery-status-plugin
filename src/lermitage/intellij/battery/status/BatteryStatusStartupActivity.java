package lermitage.intellij.battery.status;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

public class BatteryStatusStartupActivity implements StartupActivity {
    
    @Override
    public void runActivity(@NotNull Project project) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        BatteryStatusWidget batteryStatusWidget = new BatteryStatusWidget(project);
        if (statusBar != null) {
            statusBar.addWidget(batteryStatusWidget);
        }
    }
}

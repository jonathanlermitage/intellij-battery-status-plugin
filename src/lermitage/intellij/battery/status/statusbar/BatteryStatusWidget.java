package lermitage.intellij.battery.status.statusbar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import lermitage.intellij.battery.status.cfg.SettingsService;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("WeakerAccess")
public class BatteryStatusWidget implements StatusBarWidget {
    
    private Logger LOG = Logger.getInstance(getClass().getName());
    private Project project;
    private boolean forceExit = false;
    
    public static final String ID = BatteryStatusWidget.class.getName();
    
    @Contract(pure = true)
    public BatteryStatusWidget(Project project) {
        this.project = project;
    }
    
    @NotNull
    @Override
    public String ID() {
        return ID;
    }
    
    @Nullable
    @Override
    public WidgetPresentation getPresentation(@NotNull PlatformType type) {
        return new BatteryStatusPresentation(project);
    }
    
    @Override
    public void install(@NotNull StatusBar statusBar) {
        statusBar.install(WindowManager.getInstance().getStatusBar(project).getFrame());
        ApplicationManager.getApplication().executeOnPooledThread(() -> continuousBatteryStatusWidgetUpdate(statusBar));
    }
    
    private void continuousBatteryStatusWidgetUpdate(StatusBar statusBar) {
        try {
            SettingsService settingsService = ServiceManager.getService(SettingsService.class);
            LOG.info("Battery Status widget will refresh battery status every " + settingsService.getBatteryRefreshIntervalInMs() + " ms");
            while (!forceExit) {
                statusBar.updateWidget(ID);
                Thread.sleep(settingsService.getBatteryRefreshIntervalInMs());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void dispose() {
        forceExit = true;
    }
}

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
    private boolean forceExit = false;
    private StatusBar statusBar;

    public static final String ID = BatteryStatusWidget.class.getName();

    @Contract(pure = true)
    public BatteryStatusWidget(Project project) {
        this.statusBar = WindowManager.getInstance().getStatusBar(project);
    }

    @NotNull
    @Override
    public String ID() {
        return ID;
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation() {
        return new BatteryStatusPresentation(statusBar);
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
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

package lermitage.intellij.battery.status.statusbar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import lermitage.intellij.battery.status.IJUtils;
import lermitage.intellij.battery.status.cfg.SettingsService;
import lermitage.intellij.battery.status.core.Globals;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("WeakerAccess")
public class BatteryStatusWidget implements StatusBarWidget {

    private final Logger LOG = Logger.getInstance(getClass().getName());
    private final StatusBar statusBar;
    private Timer timer;

    @Contract(pure = true)
    public BatteryStatusWidget(Project project) {
        this.statusBar = WindowManager.getInstance().getStatusBar(project);
    }

    @NotNull
    @Override
    public String ID() {
        return Globals.WIDGET_ID;
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation() {
        return new BatteryStatusPresentation(statusBar);
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> start(statusBar));
    }

    public void reload() {
        dispose();
        start(statusBar);
    }

    private void start(StatusBar statusBar) {
        try {
            SettingsService settingsService = IJUtils.getSettingsService();
            LOG.info("Battery Status widget will refresh battery status every " + settingsService.getBatteryRefreshIntervalInMs() + " ms");
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    statusBar.updateWidget(Globals.WIDGET_ID);
                }
            }, 0, settingsService.getBatteryRefreshIntervalInMs());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }
}

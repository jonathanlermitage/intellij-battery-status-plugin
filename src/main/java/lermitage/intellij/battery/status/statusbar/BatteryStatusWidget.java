package lermitage.intellij.battery.status.statusbar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import lermitage.intellij.battery.status.IJUtils;
import lermitage.intellij.battery.status.cfg.SettingsService;
import lermitage.intellij.battery.status.core.Globals;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class BatteryStatusWidget implements StatusBarWidget {

    private final Logger LOG = Logger.getInstance(getClass().getName());
    private final StatusBar statusBar;
    private ScheduledFuture<?> timer;

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
        return new BatteryStatusPresentation();
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
            timer = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
                statusBar.updateWidget(Globals.WIDGET_ID);
            }, 0, settingsService.getBatteryRefreshIntervalInMs(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOG.warn(e);
        }
    }

    @Override
    public void dispose() {
        if (timer != null) {
            timer.cancel(true);
            timer = null;
        }
    }
}

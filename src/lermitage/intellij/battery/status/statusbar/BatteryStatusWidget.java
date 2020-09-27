package lermitage.intellij.battery.status.statusbar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import lermitage.intellij.battery.status.cfg.SettingsService;
import lermitage.intellij.battery.status.core.Globals;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("WeakerAccess")
public class BatteryStatusWidget implements StatusBarWidget {

    private final Logger LOG = Logger.getInstance(getClass().getName());
    private final StatusBar statusBar;
    private final Project project;
    private boolean forceExit = false;
    private Thread updateThread = null;

    @Contract(pure = true)
    public BatteryStatusWidget(Project project) {
        this.statusBar = WindowManager.getInstance().getStatusBar(project);
        this.project = project;
    }

    @NotNull
    @Override
    public String ID() {
        return Globals.PLUGIN_ID;
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation() {
        return new BatteryStatusPresentation(statusBar, project, this);
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> continuousBatteryStatusWidgetUpdate(statusBar));
    }

    private void continuousBatteryStatusWidgetUpdate(StatusBar statusBar) {
        try {
            updateThread = Thread.currentThread();
            LOG.info("Registered updateThread " + updateThread.getId());
            SettingsService settingsService = ServiceManager.getService(SettingsService.class);
            LOG.info("Battery Status widget will refresh battery status every " + settingsService.getBatteryRefreshIntervalInMs() + " ms");
            while (!forceExit) {
                statusBar.updateWidget(Globals.PLUGIN_ID);
                Thread.sleep(settingsService.getBatteryRefreshIntervalInMs());
            }
        } catch (InterruptedException e) {
            LOG.warn("App disposed, forced update thread interuption.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        forceExit = true;
        if (updateThread != null && !updateThread.isInterrupted()) {
            LOG.info("Interrupting updateThread " + updateThread.getId());
            updateThread.interrupt();
        }
    }
}

package lermitage.intellij.battery.status;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BatteryStatusWidget implements StatusBarWidget {
    
    private Project project;
    private boolean forceExit = false;
    
    @Contract(pure = true)
    BatteryStatusWidget(Project project) {
        this.project = project;
    }
    
    @NotNull
    @Override
    public String ID() {
        return "lermitage.intellij.battery.status.BatteryStatusWidget";
    }
    
    @Nullable
    @Override
    public WidgetPresentation getPresentation(@NotNull PlatformType type) {
        return new BatteryStatusPresentation();
    }
    
    @Override
    public void install(@NotNull StatusBar statusBar) {
        statusBar.install(WindowManager.getInstance().getStatusBar(project).getFrame());
        ApplicationManager.getApplication().executeOnPooledThread(() -> continuousBatteryStatusWidgetUpdate(statusBar));
    }
    
    private void continuousBatteryStatusWidgetUpdate(StatusBar statusBar) {
        try {
            while (!forceExit) {
                statusBar.updateWidget(ID());
                Thread.sleep(20_000); // TODO make it configurable
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

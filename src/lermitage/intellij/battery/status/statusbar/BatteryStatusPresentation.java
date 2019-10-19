package lermitage.intellij.battery.status.statusbar;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.Consumer;
import lermitage.intellij.battery.status.cfg.SettingsService;
import lermitage.intellij.battery.status.core.BatteryUtils;
import lermitage.intellij.battery.status.core.OS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("WeakerAccess")
public class BatteryStatusPresentation implements StatusBarWidget.TextPresentation {
    
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private String lastBatteryStatus = getText();
    private StatusBar statusBar;
    private SettingsService settingsService;
    
    public BatteryStatusPresentation(Project project) {
        this.statusBar = WindowManager.getInstance().getStatusBar(project);
    }
    
    @NotNull
    @Override
    public String getText() {
        if (settingsService == null) {
            settingsService = ServiceManager.getService(SettingsService.class);
        }
        switch (OS.detectOS()) {
            case WIN:
                lastBatteryStatus = BatteryUtils.readWindowsBatteryStatus(settingsService.getWindowsBatteryFields());
                break;
            case MACOS:
                lastBatteryStatus = BatteryUtils.readMacOSBatteryStatus(settingsService.getMacosBatteryCommand(),
                        settingsService.getMacosPreferScriptShowBattPercent());
                break;
            default:
                lastBatteryStatus = BatteryUtils.readLinuxBatteryStatus(settingsService.getLinuxBatteryCommand());
        }
        return lastBatteryStatus;
    }
    
    @NotNull
    //@Override IMPORTANT getMaxPossibleText() is deprecated: comment Override annotation to make it compatible with future IDE builds
    public String getMaxPossibleText() {
        return "";
    }
    
    @Override
    public float getAlignment() {
        return Component.CENTER_ALIGNMENT;
    }
    
    @Nullable
    @Override
    public String getTooltipText() {
        return lastBatteryStatus + " -- Last update: " + timeFormat.format(BatteryUtils.getLastCallTime());
    }
    
    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return mouseEvent -> statusBar.updateWidget(BatteryStatusWidget.ID);
    }
}

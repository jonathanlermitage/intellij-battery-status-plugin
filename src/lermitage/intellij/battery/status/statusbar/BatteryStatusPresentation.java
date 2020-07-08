package lermitage.intellij.battery.status.statusbar;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import lermitage.intellij.battery.status.cfg.SettingsService;
import lermitage.intellij.battery.status.core.BatteryLabel;
import lermitage.intellij.battery.status.core.BatteryUtils;
import lermitage.intellij.battery.status.core.Globals;
import lermitage.intellij.battery.status.core.OS;
import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;

class BatteryStatusPresentation implements StatusBarWidget.TextPresentation {

    public BatteryStatusPresentation(StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    private SettingsService settingsService;
    private StatusBar statusBar;
    private String lastBatteryStatus = getText();

    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    @NotNull
    @Override
    public String getText() {
        if (settingsService == null) {
            settingsService = ServiceManager.getService(SettingsService.class);
        }
        BatteryLabel batteryLabel = settingsService.getBatteryLabel();
        switch (OS.detectOS()) {
            case WIN:
                lastBatteryStatus = BatteryUtils.readWindowsBatteryStatus(settingsService.getWindowsBatteryFields(), batteryLabel);
                break;
            case MACOS:
                lastBatteryStatus = BatteryUtils.readMacOSBatteryStatus(settingsService.getMacosBatteryCommand(),
                        settingsService.getMacosPreferScriptShowBattPercent(), batteryLabel);
                break;
            default:
                lastBatteryStatus = BatteryUtils.readLinuxBatteryStatus(settingsService.getLinuxBatteryCommand(), batteryLabel);
        }
        return lastBatteryStatus;
    }

    @Override
    public float getAlignment() {
        return Component.CENTER_ALIGNMENT;
    }

    @Override
    public String getTooltipText() {
        return lastBatteryStatus + " -- Last update: " + timeFormat.format(BatteryUtils.getLastCallTime());
    }

    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return mouseEvent -> statusBar.updateWidget(Globals.PLUGIN_ID);
    }
}

package lermitage.intellij.battery.status.statusbar;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import lermitage.intellij.battery.status.cfg.SettingsService;
import lermitage.intellij.battery.status.core.BatteryLabel;
import lermitage.intellij.battery.status.core.BatteryUtils;
import lermitage.intellij.battery.status.core.Globals;
import lermitage.intellij.battery.status.core.OS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BatteryStatusPresentation implements StatusBarWidget.MultipleTextValuesPresentation, StatusBarWidget.Multiframe {

    public BatteryStatusPresentation(StatusBar statusBar, Project project, Disposable widget) {
        this.statusBar = statusBar;
        this.project = project;
        this.widget = widget;
    }

    private final StatusBar statusBar;
    private final Project project;
    private final Disposable widget;
    private SettingsService settingsService;
    private String lastBatteryStatus = getSelectedValue();

    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final Pattern batteryPercentagePattern = Pattern.compile(".*[^0-9]+([0-9]+)%.*");

    @Override
    public String getTooltipText() {
        return lastBatteryStatus + " -- Last update: " + timeFormat.format(BatteryUtils.getLastCallTime());
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        // FIXME getClickConsumer() is never called since migration to MultipleTextValuesPresentation + Multiframe
        return mouseEvent -> statusBar.updateWidget(Globals.PLUGIN_ID);
    }

    @Override
    public @Nullable("null means the widget is unable to show the popup") ListPopup getPopupStep() {
        return null;
    }

    @Override
    public @Nullable String getSelectedValue() {
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
    public @Nullable Icon getIcon() {
        String batteryStatus = getSelectedValue();
        if (batteryStatus != null) {
            Matcher matcher = batteryPercentagePattern.matcher(" " + batteryStatus);
            if (matcher.find()) {
                String batteryPercentage = matcher.group(1);
                int batteryPercentageInt = Integer.parseInt(batteryPercentage);
                String battStatusUppercase = batteryStatus.toUpperCase();
                String name = battStatusUppercase.contains("OFFLINE") || battStatusUppercase.contains("DISCHARGING") ? "battery" : "online";
                int charge;
                if (batteryPercentageInt == 100) {
                    charge = 100;
                } else if (batteryPercentageInt >= 75) {
                    charge = 75;
                } else if (batteryPercentageInt >= 50) {
                    charge = 50;
                } else if (batteryPercentageInt >= 25) {
                    charge = 25;
                } else {
                    charge = 0;
                }
                return IconLoader.getIcon("/icons/" + name + charge + ".svg");
            }
            return IconLoader.getIcon("/icons/batterynone.svg");
        }
        return null;
    }

    @Override
    public StatusBarWidget copy() {
        return new BatteryStatusWidget(project);
    }

    @Override
    public @NotNull String ID() {
        return Globals.PLUGIN_ID;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
    }

    @Override
    public void dispose() {
        Disposer.dispose(widget);
    }
}

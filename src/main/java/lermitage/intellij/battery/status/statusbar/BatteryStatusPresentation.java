package lermitage.intellij.battery.status.statusbar;

import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import lermitage.intellij.battery.status.IJUtils;
import lermitage.intellij.battery.status.cfg.SettingsService;
import lermitage.intellij.battery.status.core.BatteryReader;
import lermitage.intellij.battery.status.core.BatteryUtils;
import lermitage.intellij.battery.status.core.UIUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

class BatteryStatusPresentation implements StatusBarWidget.MultipleTextValuesPresentation {

    private Integer lastBatteryLevel = 100;

    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String getTooltipText() {
        return "<html>" + "<b>Last toolwindow update: " + timeFormat.format(BatteryUtils.getLastCallTime()) + "</b><br>" +
            "Detailed information at " + timeFormat.format(LocalTime.now()) + ":<br>" +
            "- " + BatteryReader.getBatteryHTMLDetailedInfo().replaceAll(",", "<br>- ") + "</html>";
    }

    // removed @Override as MultipleTextValuesPresentation.getClickConsumer is scheduled for removal in a future release
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    @Override
    public @Nullable String getSelectedValue() {
        String lastBatteryStatus = BatteryReader.getBatteryStatus();
        SettingsService settingsService = IJUtils.getSettingsService();

        // enable/disable Power Saver mode if plugin is configured to configure this mode based on power level
        if (settingsService.getConfigurePowerSaverBasedOnPowerLevel()) {

            if (lastBatteryLevel != null) {
                Optional<Integer> batteryChargeLevel = UIUtils.getBatteryChargeLevel(lastBatteryStatus);
                if (batteryChargeLevel.isPresent()) {
                    int batteryAlertLevel = settingsService.getLowPowerValue();
                    if (!lastBatteryLevel.equals(batteryChargeLevel.get())) {
                        IJUtils.enablePowerSaver(batteryChargeLevel.get() < batteryAlertLevel);
                    }
                    lastBatteryLevel = batteryAlertLevel;
                }
            }
        }

        return lastBatteryStatus;
    }

    @Override
    public @Nullable Icon getIcon() {
        return UIUtils.getIconByBatteryStatusText(getSelectedValue(), IJUtils.getSettingsService().getIconsSet());
    }
}

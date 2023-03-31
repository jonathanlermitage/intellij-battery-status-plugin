package lermitage.intellij.battery.status.statusbar;

import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import lermitage.intellij.battery.status.IJUtils;
import lermitage.intellij.battery.status.cfg.SettingsService;
import lermitage.intellij.battery.status.core.BatteryReader;
import lermitage.intellij.battery.status.core.BatteryUtils;
import lermitage.intellij.battery.status.core.UIUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

class BatteryStatusPresentation implements StatusBarWidget.MultipleTextValuesPresentation {

    private SettingsService settingsService;
    private String lastBatteryStatus = getSelectedValue();

    private Integer lastBatteryLevel = 100;

    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String getTooltipText() {
        return lastBatteryStatus + " -- Last update: " + timeFormat.format(BatteryUtils.getLastCallTime());
    }

    // removed @Override as MultipleTextValuesPresentation.getClickConsumer is scheduled for removal in a future release
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    // removed @Override as MultipleTextValuesPresentation.getPopupStep is scheduled for removal in a future release
    public @Nullable("null means the widget is unable to show the popup") ListPopup getPopupStep() {
        return null;
    }

    @Override
    public @Nullable String getSelectedValue() {
        if (settingsService == null) {
            settingsService = IJUtils.getSettingsService();
        }
        lastBatteryStatus = BatteryReader.getBatteryStatus(settingsService);

        // enable/disable Power Saver mode if plugin is configured to configure this mode based on power level
        if (settingsService.isConfigurePowerSaverBasedOnPowerLevel()) {

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
        return UIUtils.getIconByBatteryStatusText(getSelectedValue(), settingsService.getIconsSet());
    }
}

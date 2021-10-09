package lermitage.intellij.battery.status.statusbar;

import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import lermitage.intellij.battery.status.IJUtils;
import lermitage.intellij.battery.status.cfg.SettingsService;
import lermitage.intellij.battery.status.core.BatteryReader;
import lermitage.intellij.battery.status.core.BatteryUtils;
import lermitage.intellij.battery.status.core.Globals;
import lermitage.intellij.battery.status.core.UIUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;

class BatteryStatusPresentation implements StatusBarWidget.MultipleTextValuesPresentation {

    public BatteryStatusPresentation(StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    private final StatusBar statusBar;
    private SettingsService settingsService;
    private String lastBatteryStatus = getSelectedValue();

    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

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
            settingsService = IJUtils.getSettingsService();
        }
        lastBatteryStatus = BatteryReader.getBatteryStatus(settingsService);
        return lastBatteryStatus;
    }

    @Override
    public @Nullable Icon getIcon() {
        return UIUtils.getIconByBatteryStatusText(getSelectedValue(), settingsService.getIconsSet());
    }
}

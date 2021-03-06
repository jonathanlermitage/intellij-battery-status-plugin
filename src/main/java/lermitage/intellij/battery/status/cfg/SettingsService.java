package lermitage.intellij.battery.status.cfg;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lermitage.intellij.battery.status.core.BatteryUtils;
import lermitage.intellij.battery.status.core.Kernel32;
import org.jetbrains.annotations.NotNull;

// see http://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html
@SuppressWarnings({"WeakerAccess", "unused"})
@State(
        name = "BatteryStatusSettings",
        storages = @Storage("lermitage-battery-status.xml")
)
public class SettingsService implements PersistentStateComponent<SettingsService> {
    
    private final Logger LOG = Logger.getInstance(getClass().getName());
    
    public static final int DEFAULT_REFRESH_INTERVAL = 90_000;
    public static final int MINIMAL_REFRESH_INTERVAL = 250;
    public static final String DEFAULT_WINDOWS_BATTERY_FIELDS = Kernel32.FIELD_BATTERYLIFEPERCENT + "," + Kernel32.FIELD_ACLINESTATUS + "," + Kernel32.FIELD_BATTERYLIFETIME;
    public static final String DEFAULT_LINUX_COMMAND = BatteryUtils.LINUX_COMMAND;
    public static final String DEFAULT_MACOS_COMMAND = BatteryUtils.MACOS_COMMAND;
    public static final String DEFAULT_MACOS_COMMAND_BATTERY_PERCENT = BatteryUtils.MACOS_ALTERNATIVE_COMMAND;
    public static final Boolean DEFAULT_MACOS_COMMAND_BATTERY_PERCENT_ENABLED = false;
    public static final Integer DEFAULT_ICONS_SET = 0;

    // the implementation of PersistentStateComponent works by serializing public fields, so keep it public
    public Integer batteryRefreshIntervalInMs;
    public String windowsBatteryFields;
    public String linuxBatteryCommand;
    public String macosBatteryCommand;
    public Boolean macosPreferScriptShowBattPercent;
    public Integer iconsSet;
    
    public Integer getBatteryRefreshIntervalInMs() {
        if (batteryRefreshIntervalInMs == null) {
            setBatteryRefreshIntervalInMs(DEFAULT_REFRESH_INTERVAL);
        } else if (batteryRefreshIntervalInMs < MINIMAL_REFRESH_INTERVAL) {
            LOG.warn("Battery Status refresh interval is too low (" + batteryRefreshIntervalInMs
                    + " ms, min value is 250 ms), it will be updated automatically to " + DEFAULT_REFRESH_INTERVAL + " ms");
            setBatteryRefreshIntervalInMs(DEFAULT_REFRESH_INTERVAL);
        }
        return batteryRefreshIntervalInMs;
    }

    public Integer getIconsSet() {
        if (iconsSet == null) {
            setIconsSet(DEFAULT_ICONS_SET);
        }
        return iconsSet;
    }

    public void setIconsSet(Integer iconsSet) {
        LOG.info("Battery Status settings updated: will use '" + iconsSet + "' icons set");
        this.iconsSet = iconsSet;
    }

    public void setBatteryRefreshIntervalInMs(Integer batteryRefreshIntervalInMs) {
        LOG.info("Battery Status settings updated: will refresh battery status every " + batteryRefreshIntervalInMs + " ms");
        this.batteryRefreshIntervalInMs = batteryRefreshIntervalInMs;
    }
    
    public String getWindowsBatteryFields() {
        if (windowsBatteryFields == null) {
            setWindowsBatteryFields(DEFAULT_WINDOWS_BATTERY_FIELDS);
        }
        return windowsBatteryFields;
    }
    
    public void setWindowsBatteryFields(String windowsBatteryFields) {
        LOG.info("Battery Status settings updated: will retrieve Windows battery status fields '" + windowsBatteryFields + "'");
        this.windowsBatteryFields = windowsBatteryFields;
    }
    
    public String getLinuxBatteryCommand() {
        if (linuxBatteryCommand == null) {
            setLinuxBatteryCommand(DEFAULT_LINUX_COMMAND);
        }
        return linuxBatteryCommand;
    }
    
    public void setLinuxBatteryCommand(String linuxBatteryCommand) {
        LOG.info("Battery Status settings updated: will retrieve Linux battery status via '" + linuxBatteryCommand + "' command");
        this.linuxBatteryCommand = linuxBatteryCommand;
    }
    
    public String getMacosBatteryCommand() {
        if (macosBatteryCommand == null) {
            setMacosBatteryCommand(DEFAULT_MACOS_COMMAND);
        }
        return macosBatteryCommand;
    }
    
    public void setMacosBatteryCommand(String macosBatteryCommand) {
        LOG.info("Battery Status settings updated: will retrieve MacOS battery status via '" + macosBatteryCommand + "' command");
        this.macosBatteryCommand = macosBatteryCommand;
    }

    public Boolean getMacosPreferScriptShowBattPercent() {
        if (macosPreferScriptShowBattPercent == null) {
            setMacosPreferScriptShowBattPercent(false);
        }
        return macosPreferScriptShowBattPercent;
    }

    public void setMacosPreferScriptShowBattPercent(Boolean macosPreferScriptShowBattPercent) {
        LOG.info("Battery Status settings updated: will retrieve MacOS battery status (percent only) via bundled script");
        this.macosPreferScriptShowBattPercent = macosPreferScriptShowBattPercent;
    }

    @Override
    public SettingsService getState() {
        return this;
    }
    
    @Override
    public void loadState(@NotNull SettingsService state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}

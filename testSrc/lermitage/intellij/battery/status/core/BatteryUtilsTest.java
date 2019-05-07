package lermitage.intellij.battery.status.core;

import lermitage.intellij.battery.status.cfg.SettingsService;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BatteryUtilsTest {
    
    @Test
    public void read_battery_status() {
        String battery;
        switch (OS.detectOS()) {
            case WIN:
                battery = BatteryUtils.readWindowsBatteryStatus(SettingsService.DEFAULT_WINDOWS_BATTERY_FIELDS);
                break;
            case MACOS:
                battery = BatteryUtils.readMacOSBatteryStatus(SettingsService.DEFAULT_MACOS_COMMAND);
                break;
            default:
                battery = BatteryUtils.readLinuxBatteryStatus(SettingsService.DEFAULT_LINUX_COMMAND);
        }
        System.out.println(battery);
        assertTrue(battery.contains("%") && !battery.toLowerCase().contains("error"));
    }
}

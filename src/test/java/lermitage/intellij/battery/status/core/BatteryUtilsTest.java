package lermitage.intellij.battery.status.core;

import lermitage.intellij.battery.status.cfg.SettingsService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BatteryUtilsTest {

    @Test
    public void read_battery_status() {
        String battery;
        OS os = OS.detectOS();
        battery = switch (os) {
            case WIN -> BatteryUtils.readWindowsBatteryStatus(SettingsService.DEFAULT_WINDOWS_BATTERY_FIELDS);
            case MACOS -> BatteryUtils.readMacOSBatteryStatus(SettingsService.DEFAULT_MACOS_COMMAND);
            default -> BatteryUtils.readLinuxBatteryStatus(SettingsService.DEFAULT_LINUX_COMMAND);
        };
        System.out.println("battery: " + battery + ", OS: " + os);
        assertFalse(battery.toLowerCase().contains("error"));
        // CI runner has no battery, so ACPI returns an error message
        assertTrue(battery.contains("%") ||
            battery.equalsIgnoreCase("Battery: unknown") ||
            battery.equalsIgnoreCase("Battery: cannot invoke 'acpi -b'"));
    }
}

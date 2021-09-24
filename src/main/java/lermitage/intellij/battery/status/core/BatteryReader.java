// SPDX-License-Identifier: MIT

package lermitage.intellij.battery.status.core;

import lermitage.intellij.battery.status.cfg.SettingsService;

import java.time.LocalTime;

public class BatteryReader {

    private static LocalTime lastCall;
    private static String lastBatteryStatus;
    private final static OS detectedOS = OS.detectOS();

    /**
     * Get battery status. Returned message is cached for 10s.
     * @param settingsService settings service.
     * @return battery status.
     */
    public static synchronized String getBatteryStatus(SettingsService settingsService) {
        if (lastBatteryStatus != null && lastCall != null && lastCall.plusSeconds(10).isAfter(LocalTime.now())) {
            return lastBatteryStatus;
        }
        switch (detectedOS) {
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
        lastCall = LocalTime.now();
        return lastBatteryStatus;
    }
}

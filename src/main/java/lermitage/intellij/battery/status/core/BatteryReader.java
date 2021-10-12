// SPDX-License-Identifier: MIT

package lermitage.intellij.battery.status.core;

import lermitage.intellij.battery.status.cfg.SettingsService;

import java.time.LocalDateTime;

import static lermitage.intellij.battery.status.cfg.SettingsService.MINIMAL_REFRESH_INTERVAL;

public class BatteryReader {

    private static LocalDateTime lastCall;
    private static String lastBatteryStatus;
    private final static OS detectedOS = OS.detectOS();
    private static final int CACHE_DURATION_SEC = MINIMAL_REFRESH_INTERVAL / 1000;

    /**
     * Get battery status. Returned message is cached for 10s.
     * @param settingsService settings service.
     * @return battery status.
     */
    public static synchronized String getBatteryStatus(SettingsService settingsService) {
        if (lastBatteryStatus != null && lastCall != null && lastCall.plusSeconds(CACHE_DURATION_SEC).isAfter(LocalDateTime.now())) {
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
        lastCall = LocalDateTime.now();
        return lastBatteryStatus;
    }
}

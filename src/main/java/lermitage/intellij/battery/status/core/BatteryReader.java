// SPDX-License-Identifier: MIT

package lermitage.intellij.battery.status.core;

import lermitage.intellij.battery.status.IJUtils;
import lermitage.intellij.battery.status.cfg.SettingsService;

import java.time.LocalDateTime;

import static lermitage.intellij.battery.status.cfg.SettingsService.MINIMAL_REFRESH_INTERVAL;

public class BatteryReader {

    private final static OS detectedOS = OS.detectOS();

    private static LocalDateTime lastCall;
    private static String lastBatteryStatus;
    private static final int CACHE_DURATION_SEC = MINIMAL_REFRESH_INTERVAL / 1000;

    /**
     * Get battery status. Returned message is cached for 10s.
     *
     * @return battery status.
     */
    public static synchronized String getBatteryStatus() {
        if (lastBatteryStatus != null && lastCall != null && lastCall.plusSeconds(CACHE_DURATION_SEC).isAfter(LocalDateTime.now())) {
            return lastBatteryStatus;
        }
        lastCall = LocalDateTime.now();

        SettingsService settingsService = IJUtils.getSettingsService();
        if (settingsService.getUseOshi()) {
            lastBatteryStatus = BatteryUtils.readBatteryStatusWithOshi();
        } else {
            switch (detectedOS) {
                case WIN ->
                    lastBatteryStatus = BatteryUtils.readWindowsBatteryStatus(settingsService.getWindowsBatteryFields());
                case LINUX ->
                    lastBatteryStatus = BatteryUtils.readLinuxBatteryStatus(settingsService.getLinuxBatteryCommand());
                case MACOS ->
                    lastBatteryStatus = BatteryUtils.readMacOSBatteryStatus(settingsService.getMacosBatteryCommand(),
                        settingsService.getMacosPreferScriptShowBattPercent());
            }
        }

        return lastBatteryStatus;
    }

    /**
     * Get battery detailed info. Returned message is cached for 10s.
     *
     * @return battery detailed info.
     */
    public static synchronized String getBatteryHTMLDetailedInfo() {
        return BatteryUtils.readBatteryLongDescWithOshi();
    }
}

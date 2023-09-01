package lermitage.intellij.battery.status.core;

import lermitage.intellij.battery.status.IJUtils;
import lermitage.intellij.battery.status.cfg.SettingsService;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class BatteryUtils {

    private static LocalTime lastCall = LocalTime.now();
    private static File macOSTmpScriptFile;

    public static final String LINUX_COMMAND = "acpi -b";
    public static final String MACOS_COMMAND = "pmset -g batt";
    public static final String MACOS_ALTERNATIVE_COMMAND = "pmset -g batt | grep -Eo \"\\d+%\"|echo \"Battery:$(cat -)\"";

    @Contract(pure = true)
    public static LocalTime getLastCallTime() {
        return lastCall;
    }

    public static void updateLastCallTime() {
        lastCall = LocalTime.now();
    }

    private static List<String> execCommandThenReadLines(String command) throws IOException {
        Process chkBat = Runtime.getRuntime().exec(command);
        try (BufferedReader chkBuf = new BufferedReader(new InputStreamReader(chkBat.getInputStream()))) {
            return chkBuf.lines().filter(Objects::nonNull).collect(Collectors.toList());
        }
    }

    @NotNull
    @Contract(pure = true)
    public static String readWindowsBatteryStatus(String batteryFields) {
        updateLastCallTime();
        Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
        int status = Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);
        if (status == -2) {
            return "Battery: cannot invoke Kernel32";
        } else if (status == -1) {
            return "Battery: error";
        }

        List<String> batteryields = new ArrayList<>();
        boolean fieldFound = false;
        for (String field : batteryFields.split(",")) {
            field = field.trim();
            if (field.equalsIgnoreCase(Kernel32.FIELD_ACLINESTATUS)) {
                batteryields.add(batteryStatus.getACLineStatusString());
                fieldFound = true;
            }
            if (field.equalsIgnoreCase(Kernel32.FIELD_BATTERYFLAG)) {
                batteryields.add(batteryStatus.getBatteryFlagString());
                fieldFound = true;
            }
            if (field.equalsIgnoreCase(Kernel32.FIELD_BATTERYLIFEPERCENT)) {
                batteryields.add(batteryStatus.getBatteryLifePercent());
                fieldFound = true;
            }
            if (field.equalsIgnoreCase(Kernel32.FIELD_BATTERYLIFETIME)) {
                batteryields.add(batteryStatus.getBatteryLifeTime());
                fieldFound = true;
            }
            if (field.equalsIgnoreCase(Kernel32.FIELD_BATTERYFULLLIFETIME)) {
                batteryields.add(batteryStatus.getBatteryFullLifeTime());
                fieldFound = true;
            }
        }
        if (!fieldFound) {
            return "Battery: no valid field, please edit settings";
        }

        return batteryields.stream()
            .filter(s -> s != null && !s.isEmpty() && !s.equalsIgnoreCase("unknown"))
            .map(String::trim)
            .collect(Collectors.joining(", "));
    }

    @NotNull
    @Contract(pure = true)
    public static String readLinuxBatteryStatus(String command) {
        updateLastCallTime();
        return extractUnixBatteryInformation(command);
    }

    @NotNull
    @Contract(pure = true)
    public static String readMacOSBatteryStatus(String command, boolean runInTmpScript) {
        if (runInTmpScript) {
            return readMacOSBatteryStatusViaTmpScript(command);
        }
        return readMacOSBatteryStatus(command);
    }

    @NotNull
    @Contract(pure = true)
    public static String readMacOSBatteryStatus(String command) {
        updateLastCallTime();
        // see http://osxdaily.com/2015/12/10/get-mac-battery-life-info-command-line-os-x/
        try {
            List<String> chkLines = execCommandThenReadLines(command);
            if (!chkLines.isEmpty()) {
                String status = chkLines.stream()
                    .map(s -> s
                        .replace("Now drawing from 'Battery Power'", "Offline")
                        .replace("Now drawing from 'AC Power'", "Online")
                        .replace("-InternalBattery-", "Battery ")
                        .replace("present: true", "")
                        .replace("(no estimate)", "")
                        .replace("0:00 remaining", "")
                        .replaceAll("\\(id=[0-9]+\\)", "")
                        .replaceAll("\\s+", " ")
                    )
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(", "));
                if (!status.contains("Battery 1")) {
                    status = status.replace("Battery 0", "Battery");
                }
                if (status.endsWith(";")) {
                    status = status.substring(0, status.length() - 1);
                }
                return status
                    .replace(", (no estimate)", "")
                    .replaceAll("[Bb]attery:?", "")
                    .replaceAll(";", ",")
                    .replaceAll("\\s+", " ");
            }
        } catch (Exception e) {
            return "Battery: cannot invoke '" + command + "'";
        }
        return "Battery: unknown";
    }

    @NotNull
    @Contract(pure = true)
    public static String readMacOSBatteryStatusViaTmpScript(String command) {
        updateLastCallTime();
        try {
            if (macOSTmpScriptFile == null || !macOSTmpScriptFile.exists()) {
                macOSTmpScriptFile = File.createTempFile("ij-battery-status-v1_5-macos", ".sh");
                macOSTmpScriptFile.deleteOnExit();
            }
            if (!macOSTmpScriptFile.setExecutable(true)) {
                return "Battery: can't set '" + macOSTmpScriptFile.getAbsolutePath() + "' executable";
            }
            Files.writeString(macOSTmpScriptFile.toPath(), "#!/bin/sh\n\n" + command);
        } catch (IOException e) {
            return "Battery: can't create temporary script file";
        }
        return extractUnixBatteryInformation(command);
    }

    @NotNull
    private static String extractUnixBatteryInformation(String command) {
        // output looks like "Battery 0: Discharging, 98%, 05:26:03 remaining" or "Battery 0: Full, 100%", and
        // may be multi-line if many batteries are detected (at least on Linux).
        try {
            List<String> chkLines = execCommandThenReadLines(command);
            if (!chkLines.isEmpty()) {
                String status = chkLines.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(", "));
                status = status.replaceAll("[Bb]attery [0-9]?:?", "");

                // observed in a VirtualBox Ubuntu VM only: no (dis)charging rate. The battery info string is a
                // bit long, trying to shorten it.
                // Also, I don't trust the "zero" discharging rate. At least, this information is not accurate on
                // my machine: Windows host and battery discharge + Ubuntu VM that shows a zero rate discharge.
                // Some power adapters may charge at a lower rate (and the OS will warn you about that), but a
                // zero rate is very uncommon, that's why I am ignoring it.
                if (status.toLowerCase().contains(", charging at zero rate - will never fully charge.")) {
                    status = status.toLowerCase()
                        .replace(", charging at zero rate - will never fully charge.", "")
                        .replace("charging,", "Online,");
                }
                if (status.toLowerCase().contains(", discharging at zero rate - will never fully discharge.")) {
                    status = status.toLowerCase()
                        .replace(", discharging at zero rate - will never fully discharge.", "")
                        .replace("discharging,", "Offline,");
                }

                return status;
            }
        } catch (Exception e) {
            return "Battery: cannot invoke '" + command + "'";
        }
        return "Battery: unknown";
    }


    @NotNull
    @Contract(pure = true)
    public static String readBatteryStatusWithOshi() {
        updateLastCallTime();

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hardware = si.getHardware();
        List<PowerSource> powerSources = hardware.getPowerSources();

        if (!powerSources.isEmpty()) {
            PowerSource powerSource = powerSources.get(0);

            SettingsService settingsService = IJUtils.getSettingsService();
            String requestedFields = settingsService.getOshiBatteryFields();

            if (requestedFields.contains(OshiFeatureName.CHARGE_LEVEL.getLabel())) {
                float currentCapacity = (float) powerSource.getCurrentCapacity();
                float maxCapacity = (float) powerSource.getMaxCapacity();
                String remainingCapacityPercent = Math.round((currentCapacity * 100f) / maxCapacity) + "%";
                requestedFields = requestedFields.replace(OshiFeatureName.CHARGE_LEVEL.getLabel(), remainingCapacityPercent);
            }

            if (requestedFields.contains(OshiFeatureName.AC.getLabel())) {
                String powerSourceState = powerSource.isPowerOnLine() ? "Online" : "Offline";
                requestedFields = requestedFields.replace(OshiFeatureName.AC.getLabel(), powerSourceState);
            }

            boolean containsDischargeInstantTimeLong = requestedFields.contains(OshiFeatureName.DISCHARGE_INSTANT_TIME_LONG.getLabel());
            if (requestedFields.contains(OshiFeatureName.DISCHARGE_INSTANT_TIME_SHORT.getLabel()) || containsDischargeInstantTimeLong) {
                String remainingTimeDischarging = "";
                if (powerSource.isDischarging()) {
                    long timeRemaining = (long) powerSource.getTimeRemainingInstant();
                    if (timeRemaining > 0) {
                        remainingTimeDischarging = formatSecondsToHumanDuration(timeRemaining);
                        if (containsDischargeInstantTimeLong) {
                            remainingTimeDischarging += " remaining";
                        }
                    }
                }
                if (containsDischargeInstantTimeLong) {
                    requestedFields = requestedFields.replace(OshiFeatureName.DISCHARGE_INSTANT_TIME_LONG.getLabel(), remainingTimeDischarging);
                } else {
                    requestedFields = requestedFields.replace(OshiFeatureName.DISCHARGE_INSTANT_TIME_SHORT.getLabel(), remainingTimeDischarging);
                }
            }

            boolean containsDischargeEstimatedTimeLong = requestedFields.contains(OshiFeatureName.DISCHARGE_ESTIMATED_TIME_LONG.getLabel());
            if (requestedFields.contains(OshiFeatureName.DISCHARGE_ESTIMATED_TIME_SHORT.getLabel()) || containsDischargeEstimatedTimeLong) {
                String remainingTimeDischarging = "";
                if (powerSource.isDischarging()) {
                    long timeRemaining = (long) powerSource.getTimeRemainingEstimated();
                    if (timeRemaining > 0) {
                        remainingTimeDischarging = formatSecondsToHumanDuration(timeRemaining);
                        if (containsDischargeEstimatedTimeLong) {
                            remainingTimeDischarging += " remaining";
                        }
                    }
                }
                if (containsDischargeEstimatedTimeLong) {
                    requestedFields = requestedFields.replace(OshiFeatureName.DISCHARGE_ESTIMATED_TIME_LONG.getLabel(), remainingTimeDischarging);
                } else {
                    requestedFields = requestedFields.replace(OshiFeatureName.DISCHARGE_ESTIMATED_TIME_SHORT.getLabel(), remainingTimeDischarging);
                }
            }

            boolean containsChargeInstantTimeLong = requestedFields.contains(OshiFeatureName.CHARGE_INSTANT_TIME_LONG.getLabel());
            if (requestedFields.contains(OshiFeatureName.CHARGE_INSTANT_TIME_SHORT.getLabel()) || containsChargeInstantTimeLong) {
                String remainingTimeCharging = "";
                if (powerSource.isCharging()) {
                    long timeRemaining = (long) powerSource.getTimeRemainingInstant();
                    if (timeRemaining > 0) {
                        remainingTimeCharging = formatSecondsToHumanDuration(timeRemaining);
                        if (containsChargeInstantTimeLong) {
                            remainingTimeCharging += " to full charge";
                        }
                    }
                }
                if (containsChargeInstantTimeLong) {
                    requestedFields = requestedFields.replace(OshiFeatureName.CHARGE_INSTANT_TIME_LONG.getLabel(), remainingTimeCharging);
                } else {
                    requestedFields = requestedFields.replace(OshiFeatureName.CHARGE_INSTANT_TIME_SHORT.getLabel(), remainingTimeCharging);
                }
            }

            boolean containsChargeEstimatedTimeLong = requestedFields.contains(OshiFeatureName.CHARGE_ESTIMATED_TIME_LONG.getLabel());
            if (requestedFields.contains(OshiFeatureName.CHARGE_ESTIMATED_TIME_SHORT.getLabel()) || containsChargeEstimatedTimeLong) {
                String remainingTimeCharging = "";
                if (powerSource.isCharging()) {
                    long timeRemaining = (long) powerSource.getTimeRemainingEstimated();
                    if (timeRemaining > 0) {
                        remainingTimeCharging = formatSecondsToHumanDuration(timeRemaining);
                        if (containsChargeEstimatedTimeLong) {
                            remainingTimeCharging += " to full charge";
                        }
                    }
                }
                if (containsChargeEstimatedTimeLong) {
                    requestedFields = requestedFields.replace(OshiFeatureName.CHARGE_ESTIMATED_TIME_LONG.getLabel(), remainingTimeCharging);
                } else {
                    requestedFields = requestedFields.replace(OshiFeatureName.CHARGE_ESTIMATED_TIME_SHORT.getLabel(), remainingTimeCharging);
                }
            }

            String res = requestedFields.trim();
            if (res.endsWith(",")) {
                res = res.substring(0, res.length() - 1).trim();
            }
            return res;
        }

        return "unknown";
    }

    @NotNull
    @Contract(pure = true)
    public static String readBatteryLongDescWithOshi() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hardware = si.getHardware();
        List<PowerSource> powerSources = hardware.getPowerSources();

        if (!powerSources.isEmpty()) {
            return powerSources.get(0).toString();
        }

        return "unknown";
    }

    @NotNull
    private static String formatSecondsToHumanDuration(long totalSeconds) {
        long durationMillis = TimeUnit.SECONDS.toMillis(totalSeconds);
        String res = DurationFormatUtils.formatDuration(durationMillis, "HH:mm").replace(":", "hr ") + "m";
        if (res.startsWith("00")) {
            res = res.substring(1);
        }
        if (res.startsWith("0hr")) {
            res = res.substring("0hr".length()).trim();
        }
        return res;
    }
}

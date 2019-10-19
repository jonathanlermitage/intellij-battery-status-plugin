package lermitage.intellij.battery.status.core;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
        List<String> chkLines = new ArrayList<>();
        Process chkBat = Runtime.getRuntime().exec(command);
        try (BufferedReader chkBuf = new BufferedReader(new InputStreamReader(chkBat.getInputStream()))) {
            String line;
            do {
                line = chkBuf.readLine();
                if (line != null) {
                    chkLines.add(line);
                }
            } while (line != null);
        }
        return chkLines;
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
        
        return "Battery: " + batteryields.stream()
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
                        .filter(s -> s != null && !s.isEmpty())
                        .map(s -> s
                                .replace("Now drawing from 'Battery Power'", "Offline")
                                .replace("Now drawing from 'AC Power'", "Online")
                                .replace("-InternalBattery-", "Battery ")
                        )
                        .map(String::trim)
                        .collect(Collectors.joining("; "));
                if (!status.contains("Battery 1")) {
                    status = status.replace("Battery 0", "Battery");
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
    public static String readMacOSBatteryStatusViaTmpScript(String command) {
        updateLastCallTime();
        try {
            if (macOSTmpScriptFile == null || !macOSTmpScriptFile.exists()) {
                macOSTmpScriptFile = File.createTempFile("ij-battery-status-v1_5-macos", ".sh");
            }
            macOSTmpScriptFile.deleteOnExit();
            if (!macOSTmpScriptFile.setExecutable(true)) {
                return "Battery: can't set '" + macOSTmpScriptFile.getAbsolutePath() + "' executable";
            }
            Files.write(macOSTmpScriptFile.toPath(), ("#!/bin/sh\n\n" + command).getBytes(StandardCharsets.UTF_8));
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
                        .filter(s -> s != null && !s.isEmpty())
                        .map(String::trim)
                        .collect(Collectors.joining("; "));
                if (!status.contains("Battery 1")) {
                    status = status.replace("Battery 0", "Battery");
                }
                return status;
            }
        } catch (Exception e) {
            return "Battery: cannot invoke '" + command + "'";
        }
        return "Battery: unknown";
    }
}

package lermitage.intellij.battery.status.core;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BatteryUtils { // TODO transform to Service
    
    private static LocalTime lastCall = LocalTime.now();
    
    @Contract(pure = true)
    public static LocalTime getLastCallTime() {
        return lastCall;
    }
    
    @NotNull
    @Contract(pure = true)
    private static String readLinuxBatteryStatus() {
        try {
            List<String> chkLines = new ArrayList<>();
            // output looks like "Battery 0: Discharging, 98%, 05:26:03 remaining" or "Battery 0: Full, 100%", and
            // may be multi-line if many batteries are detected.
            Process chkBat = Runtime.getRuntime().exec("acpi -b");
            try (BufferedReader chkBuf = new BufferedReader(new InputStreamReader(chkBat.getInputStream()))) {
                String line;
                do {
                    line = chkBuf.readLine();
                    if (line != null) {
                        chkLines.add(line);
                    }
                } while (line != null);
            }
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
            if (e.getMessage().toUpperCase().contains("CANNOT RUN PROGRAM \"ACPI\"")) {
                return "Cannot invoke 'acpi -b'";
            }
            return "Error";
        }
        return "Unknown";
    }
    
    @NotNull
    @Contract(pure = true)
    private static String readWindowsBatteryStatus() {
        Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
        int status = Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);
        if (status == -2) {
            return "Cannot invoke Kernel32";
        } else if (status == -1) {
            return "Error";
        }
        return "Battery: " + Stream.of(
                batteryStatus.getBatteryLifePercent(),
                batteryStatus.getACLineStatusString(),
                batteryStatus.getBatteryLifeTime()
        ).filter(s -> s != null && !s.isEmpty() && !s.equalsIgnoreCase("unknown"))
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }
    
    @NotNull
    @Contract(pure = true)
    private static String readMacOSBatteryStatus() {
        try {
            List<String> chkLines = new ArrayList<>();
            // see http://osxdaily.com/2015/12/10/get-mac-battery-life-info-command-line-os-x/
            Process chkBat = Runtime.getRuntime().exec("pmset -g batt");
            try (BufferedReader chkBuf = new BufferedReader(new InputStreamReader(chkBat.getInputStream()))) {
                String line;
                do {
                    line = chkBuf.readLine();
                    if (line != null) {
                        chkLines.add(line);
                    }
                } while (line != null);
            }
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
            if (e.getMessage().toUpperCase().contains("PMSET")) {
                return "Cannot invoke 'pmset -g batt'";
            }
            return "Error";
        }
        return "Unknown";
    }
    
    @NotNull
    public static String readBatteryStatus() {
        lastCall = LocalTime.now();
        String batteryStatus;
        String os = System.getProperty("os.name").toUpperCase();
        if (os.contains("WIN")) {
            batteryStatus = readWindowsBatteryStatus();
        } else if (os.contains("NIX") || os.contains("NUX") || os.contains("AIX")) {
            batteryStatus = readLinuxBatteryStatus();
        } else if (os.contains("MAC")) {
            batteryStatus = readMacOSBatteryStatus();
        } else {
            batteryStatus = readLinuxBatteryStatus();
        }
        return batteryStatus;
    }
}

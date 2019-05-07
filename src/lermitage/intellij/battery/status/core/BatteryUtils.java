package lermitage.intellij.battery.status.core;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BatteryUtils {
    
    private static LocalTime lastCall = LocalTime.now();
    
    @Contract(pure = true)
    public static LocalTime getLastCallTime() {
        return lastCall;
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
        try {
            List<String> chkLines = execCommandThenReadLines(command);
            // output looks like "Battery 0: Discharging, 98%, 05:26:03 remaining" or "Battery 0: Full, 100%", and
            // may be multi-line if many batteries are detected.
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
    
    @NotNull
    @Contract(pure = true)
    public static String readMacOSBatteryStatus(String command) {
        try {
            List<String> chkLines = execCommandThenReadLines(command);
            // see http://osxdaily.com/2015/12/10/get-mac-battery-life-info-command-line-os-x/
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
}

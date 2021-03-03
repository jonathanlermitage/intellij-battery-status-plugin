package lermitage.intellij.battery.status.core;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UIUtils {

    private static final Pattern BATTERY_PERCENTAGE_PATTERN = Pattern.compile(".*[^0-9]+([0-9]+)%.*");

    public static @Nullable Icon getIconByBatteryStatusText(@Nullable String batteryStatusTxt, Integer iconsSet) {
        if (batteryStatusTxt != null) {
            Matcher matcher = BATTERY_PERCENTAGE_PATTERN.matcher(" " + batteryStatusTxt);
            if (matcher.find()) {
                String batteryPercentage = matcher.group(1);
                int batteryPercentageInt = Integer.parseInt(batteryPercentage);
                String battStatusUppercase = batteryStatusTxt.toUpperCase();
                String name = battStatusUppercase.contains("OFFLINE") || battStatusUppercase.contains("DISCHARGING") ? "battery" : "online";
                int charge;
                if (batteryPercentageInt >= 95) {
                    charge = 100;
                } else if (batteryPercentageInt >= 75) {
                    charge = 75;
                } else if (batteryPercentageInt >= 50) {
                    charge = 50;
                } else if (batteryPercentageInt >= 25) {
                    charge = 25;
                } else {
                    charge = 0;
                }
                return IconLoader.getIcon("/icons/set" + iconsSet + "/" + name + charge + ".svg", UIUtils.class);
            }
            return IconLoader.getIcon("/icons/set" + iconsSet + "/batterynone.svg", UIUtils.class);
        }
        return null;
    }
}

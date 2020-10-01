package lermitage.intellij.battery.status.core;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * See <a href="https://stackoverflow.com/questions/3434719/how-to-get-the-remaining-battery-life-in-a-windows-system">original code</a> and
 * <a href="https://docs.microsoft.com/en-us/windows/desktop/api/winbase/ns-winbase-_system_power_status">Microsoft documentation</a>.
 */
@SuppressWarnings({"WeakerAccess", "UnnecessaryInterfaceModifier"})
public interface Kernel32 extends StdCallLibrary {
    
    public static final String FIELD_ACLINESTATUS = "AC"; // ACLineStatus
    public static final String FIELD_BATTERYFLAG = "Flag"; // BatteryFlag
    public static final String FIELD_BATTERYLIFEPERCENT = "LifePercent"; // BatteryLifePercent
    public static final String FIELD_BATTERYLIFETIME = "LifeTime"; // BatteryLifeTime
    public static final String FIELD_BATTERYFULLLIFETIME = "FullLifeTime"; // BatteryFullLifeTime

    public static Kernel32 INSTANCE = getKernel32();
    
    public static Kernel32 getKernel32() {
        try {
            return Native.load("Kernel32", Kernel32.class);
        } catch (UnsatisfiedLinkError ule) {
            return result -> -2;
        } catch (Throwable e) {
            return result -> -1;
        }
    }
    
    /**
     * http://msdn2.microsoft.com/en-us/library/aa373232.aspx
     */
    @SuppressWarnings("unused")
    public class SYSTEM_POWER_STATUS extends Structure {
        
        private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        public byte ACLineStatus;
        public byte BatteryFlag;
        public byte BatteryLifePercent;
        public byte Reserved1;
        public int BatteryLifeTime;
        public int BatteryFullLifeTime;
        
        @Override
        protected List<String> getFieldOrder() {
            ArrayList<String> fields = new ArrayList<>();
            fields.add("ACLineStatus");
            fields.add("BatteryFlag");
            fields.add("BatteryLifePercent");
            fields.add("Reserved1");
            fields.add("BatteryLifeTime");
            fields.add("BatteryFullLifeTime");
            return fields;
        }
        
        /** The AC power status. */
        public String getACLineStatusString() {
            switch (ACLineStatus) {
                case (0):
                    return "Offline";
                case (1):
                    return "Online";
                default:
                    return "Unknown";
            }
        }
        
        /** The battery charge status. */
        public String getBatteryFlagString() {
            switch (BatteryFlag) {
                case (1):
                    return "High, more than 66 percent";
                case (2):
                    return "Low, less than 33 percent";
                case (4):
                    return "Critical, less than five percent";
                case (8):
                    return "Charging";
                case ((byte) 128):
                    return "No system battery";
                default:
                    return "Unknown";
            }
        }
        
        /** The percentage of full battery charge remaining. */
        public String getBatteryLifePercent() {
            return (BatteryLifePercent == (byte) 255) ? "Unknown" : BatteryLifePercent + "%";
        }
        
        /** The number of seconds of battery life remaining. */
        public String getBatteryLifeTime() {
            if (BatteryLifeTime == -1) {
                return "Unknown";
            } else {
                if (BatteryLifeTime > 86400) { // fix java.time.DateTimeException: Invalid value for SecondOfDay (valid values 0 - 86399): 106922
                    int nbDays = BatteryLifeTime / 86400;
                    int batteryLifeTimeMinusDays = BatteryLifeTime - (nbDays * 86400);
                    if (batteryLifeTimeMinusDays > 0) {
                        return nbDays + "d " + timeFormat.format(LocalTime.ofSecondOfDay(batteryLifeTimeMinusDays)) + " remaining";
                    }
                    return nbDays + "d remaining";
                }
                return timeFormat.format(LocalTime.ofSecondOfDay(BatteryLifeTime)) + " remaining";
            }
        }
        
        /** The number of seconds of battery life when at full charge. */
        public String getBatteryFullLifeTime() {
            return (BatteryFullLifeTime == -1) ? "Unknown" : BatteryFullLifeTime + " seconds";
        }
        
        @Override
        public String toString() {
            return ("ACLineStatus: " + getACLineStatusString() + "\n") +
                    "Battery Flag: " + getBatteryFlagString() + "\n" +
                    "Battery Life: " + getBatteryLifePercent() + "\n" +
                    "Battery Left: " + getBatteryLifeTime() + "\n" +
                    "Battery Full: " + getBatteryFullLifeTime() + "\n";
        }
    }
    
    @SuppressWarnings("UnusedReturnValue")
    int GetSystemPowerStatus(SYSTEM_POWER_STATUS result);
}

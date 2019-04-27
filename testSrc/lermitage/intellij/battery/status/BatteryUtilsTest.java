package lermitage.intellij.battery.status;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BatteryUtilsTest {
    
    @Test
    public void read_battery_status() {
        String battery = BatteryUtils.readBatteryStatus();
        assertTrue(battery.contains("Battery") && battery.contains("%"));
    }
}

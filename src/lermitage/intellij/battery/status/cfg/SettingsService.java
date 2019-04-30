package lermitage.intellij.battery.status.cfg;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

// see http://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html
@State(
        name = "BatteryStatusSettings",
        storages = @Storage("lermitage-battery-status.xml")
)
public class SettingsService implements PersistentStateComponent<SettingsService> {
    
    private Logger LOG = Logger.getInstance(getClass().getName());
    
    private static final int DEFAULT_REFRESH_INTERVAL = 20_000;
    
    @SuppressWarnings("WeakerAccess") // the implementation of PersistentStateComponent works by serializing public fields, so keep it public
    public Integer batteryRefreshIntervalInMs;
    
    public Integer getBatteryRefreshIntervalInMs() {
        if (batteryRefreshIntervalInMs == null) {
            setBatteryRefreshIntervalInMs(DEFAULT_REFRESH_INTERVAL);
        } else if (batteryRefreshIntervalInMs < 250) {
            LOG.warn("Battery Status refresh interval is too low (" + batteryRefreshIntervalInMs
                    + " ms, min value is 250 ms), it will be updated automatically to " + DEFAULT_REFRESH_INTERVAL + " ms");
            setBatteryRefreshIntervalInMs(DEFAULT_REFRESH_INTERVAL);
        }
        return batteryRefreshIntervalInMs;
    }
    
    public void setBatteryRefreshIntervalInMs(Integer batteryRefreshIntervalInMs) {
        LOG.info("Battery Status settings updated: will refresh battery status every " + batteryRefreshIntervalInMs + " ms");
        this.batteryRefreshIntervalInMs = batteryRefreshIntervalInMs;
    }
    
    @Override
    public SettingsService getState() {
        return this;
    }
    
    @Override
    public void loadState(@NotNull SettingsService state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}

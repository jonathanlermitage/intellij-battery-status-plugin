package lermitage.intellij.battery.status;

import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;

public class BatteryStatusPresentation implements StatusBarWidget.TextPresentation {
    
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private String lastBatteryStatus = getText();
    
    @NotNull
    @Override
    public String getText() {
        lastBatteryStatus = BatteryUtils.readBatteryStatus();
        return lastBatteryStatus;
    }
    
    @NotNull
    @Override
    public String getMaxPossibleText() {
        return getText();
    }
    
    @Override
    public float getAlignment() {
        return 0;
    }
    
    @Nullable
    @Override
    public String getTooltipText() {
        return lastBatteryStatus + " -- Last update: " + timeFormat.format(BatteryUtils.getLastCallTime());
    }
    
    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return mouseEvent -> {
        };
    }
}

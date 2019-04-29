package lermitage.intellij.battery.status;

import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
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
    //@Override -> this method is deprecated: comment annotation to make it compatible with future IDE builds
    public String getMaxPossibleText() {
        return "";
    }
    
    @Override
    public float getAlignment() {
        return Component.CENTER_ALIGNMENT;
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

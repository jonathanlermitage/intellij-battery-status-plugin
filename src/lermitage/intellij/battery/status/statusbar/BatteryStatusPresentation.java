package lermitage.intellij.battery.status.statusbar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.Consumer;
import lermitage.intellij.battery.status.core.BatteryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("WeakerAccess")
public class BatteryStatusPresentation implements StatusBarWidget.TextPresentation {
    
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private String lastBatteryStatus = getText();
    
    private StatusBar statusBar;
    
    public BatteryStatusPresentation(Project project) {
        this.statusBar = WindowManager.getInstance().getStatusBar(project);
    }
    
    @NotNull
    @Override
    public String getText() {
        lastBatteryStatus = BatteryUtils.readBatteryStatus();
        return lastBatteryStatus;
    }
    
    @NotNull
    //@Override IMPORTANT getMaxPossibleText() is deprecated: comment Override annotation to make it compatible with future IDE builds
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
        return mouseEvent -> statusBar.updateWidget(BatteryStatusWidget.ID);
    }
}

package lermitage.intellij.battery.status.statusbar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import lermitage.intellij.battery.status.core.Globals;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class BatteryStatusWidgetFactory implements StatusBarWidgetFactory {

    @NotNull
    @Override
    public String getId() {
        return Globals.PLUGIN_ID;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Battery Status";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @NotNull
    @Override
    public StatusBarWidget createWidget(@NotNull Project project) {
        return new BatteryStatusWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}

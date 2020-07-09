package lermitage.intellij.battery.status.core;

import lermitage.intellij.battery.status.cfg.SettingsService;

public enum BatteryLabel {
    BATTERY_GLYPH("\uD83D\uDD0B", 0,"Battery glyph: \uD83D\uDD0B"),
    FLASH_GLYPH("\u26a1", 1, "Flash glyph: \u26a1"),
    BATTERY_TEXT("Battery", 2, "\"Battery\"");

    private final String label;
    private final int index;
    private final String desc;

    BatteryLabel(String label, int index, String desc) {
        this.label = label;
        this.index = index;
        this.desc = desc;
    }

    public String getLabel() {
        return label;
    }

    public int getIndex() {
        return index;
    }

    public String getDesc() {
        return desc;
    }

    public static BatteryLabel fromIndex(int index) {
        if (BATTERY_GLYPH.getIndex() == index) {
            return BATTERY_GLYPH;
        }
        if (FLASH_GLYPH.getIndex() == index) {
            return FLASH_GLYPH;
        }
        if (BATTERY_TEXT.getIndex() == index) {
            return BATTERY_TEXT;
        }
        return SettingsService.DEFAULT_BATTERY_LABEL;
    }

    public static BatteryLabel getDefault() {
        if (OS.detectOS() == OS.WIN) {
            return BATTERY_GLYPH;
        }
        return FLASH_GLYPH;
    }
}

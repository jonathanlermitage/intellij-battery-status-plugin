package lermitage.intellij.battery.status.core;

public enum OshiFeatureName {
    CAPACITY_PERCENT("$CAPACITY_PERCENT"),
    AC("$AC"),
    DISCHARGE_TIME("$DISCHARGE_TIME"),
    DISCHARGE_TIME_LONG("$DISCHARGE_TIME_LONG"),
    CHARGE_TIME("$CHARGE_TIME"),
    CHARGE_TIME_LONG("$CHARGE_TIME_LONG");

    private final String label;

    OshiFeatureName(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

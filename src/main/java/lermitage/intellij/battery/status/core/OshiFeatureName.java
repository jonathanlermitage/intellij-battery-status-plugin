package lermitage.intellij.battery.status.core;

public enum OshiFeatureName {
    CHARGE_LEVEL("$CHARGE_LVL$"),
    AC("$AC$"),
    DISCHARGE_ESTIMATED_TIME_SHORT("$DISCHARGE_EST_TIME_SHORT$"),
    DISCHARGE_ESTIMATED_TIME_LONG("$DISCHARGE_EST_TIME_LONG$"),
    DISCHARGE_INSTANT_TIME_SHORT("$DISCHARGE_INST_TIME_SHORT$"),
    DISCHARGE_INSTANT_TIME_LONG("$DISCHARGE_INST_TIME_LONG$"),
    CHARGE_ESTIMATED_TIME_SHORT("$CHARGE_EST_TIME_SHORT$"),
    CHARGE_ESTIMATED_TIME_LONG("$CHARGE_EST_TIME_LONG$"),
    CHARGE_INSTANT_TIME_SHORT("$CHARGE_INST_TIME_SHORT$"),
    CHARGE_INSTANT_TIME_LONG("$CHARGE_INST_TIME_LONG$");

    private final String label;

    OshiFeatureName(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

package lermitage.intellij.battery.status.cfg.gui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.IconLoader;
import lermitage.intellij.battery.status.IJUtils;
import lermitage.intellij.battery.status.cfg.SettingsService;
import lermitage.intellij.battery.status.core.Kernel32;
import lermitage.intellij.battery.status.core.OshiFeatureName;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;

import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_CONFIGURE_POWER_SAVER_BASED_ON_POWER_LEVEL;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_LINUX_COMMAND;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_LOW_POWER_VALUE;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_MACOS_COMMAND;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_MACOS_COMMAND_BATTERY_PERCENT_ENABLED;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_OSHI_BATTERY_FIELDS;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_REFRESH_INTERVAL;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_USE_OSHI;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_WINDOWS_BATTERY_FIELDS;
import static lermitage.intellij.battery.status.cfg.SettingsService.MINIMAL_REFRESH_INTERVAL;

public class SettingsForm implements Configurable {

    private JLabel refreshRateLabel;
    private JPanel mainPane;
    private JTextField refreshRateField;
    private JButton resetDefaultsBtn;
    private JTextField windowsFieldsField;
    private JTextField linuxCommandField;
    private JTextField macosCommandField;
    private JLabel windowsFieldsLabel;
    private JLabel linuxCommandLabel;
    private JLabel macosCommandLabel;
    private JTextField windowsFieldsFieldSample;
    private JCheckBox macosPreferScriptShowBattPercent;
    private JComboBox<Icon> iconsSetSelector;
    private JLabel iconsSetSelectorLabel;
    private JCheckBox drivePowerModeLabelCheckBox;
    private JSpinner lowBatteryLevelSpinner;
    private JLabel lowBatteryLevelLabel;
    private JCheckBox useOshiCheckBox;
    private JPanel oshiPane;
    private JPanel regularPane;
    private JTextPane oshiFieldsLabel;
    private JTextField oshiFieldsTextField;

    private boolean modified = false;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Battery Status";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        useOshiCheckBox.setText("Use bundled Oshi to read battery information");
        resetDefaultsBtn.addActionListener(e -> {
            refreshRateField.setText(Integer.toString(DEFAULT_REFRESH_INTERVAL));
            windowsFieldsField.setText(DEFAULT_WINDOWS_BATTERY_FIELDS);
            linuxCommandField.setText(DEFAULT_LINUX_COMMAND);
            macosCommandField.setText(DEFAULT_MACOS_COMMAND);
            macosPreferScriptShowBattPercent.setSelected(DEFAULT_MACOS_COMMAND_BATTERY_PERCENT_ENABLED);
            iconsSetSelector.setSelectedIndex(0);
            drivePowerModeLabelCheckBox.setSelected(DEFAULT_CONFIGURE_POWER_SAVER_BASED_ON_POWER_LEVEL);
            lowBatteryLevelSpinner.setValue(DEFAULT_LOW_POWER_VALUE);
            useOshiCheckBox.setSelected(DEFAULT_USE_OSHI);
            oshiFieldsTextField.setText(DEFAULT_OSHI_BATTERY_FIELDS);
            showHidePanes();
            modified = true;
        });
        useOshiCheckBox.setToolTipText("<html>Use bundled Oshi library in order to read battery status.<br>" +
            "With Oshi, plugin is more configurable and should work everywhere, even on macOS.<br>" +
            "Please note that when hovering the battery status, detailed status tooltip is always provided by Oshi.</html>");
        refreshRateLabel.setText("Refresh Battery Status every (ms):");
        resetDefaultsBtn.setText("Reset to defaults");
        windowsFieldsLabel.setText("<html><b>Windows only:</b> battery status fields to display (comma separated values):</html>");
        windowsFieldsFieldSample.setText("Possible values are " + Kernel32.FIELD_ACLINESTATUS
            + "," + Kernel32.FIELD_BATTERYFLAG
            + "," + Kernel32.FIELD_BATTERYLIFEPERCENT
            + "," + Kernel32.FIELD_BATTERYLIFETIME
            + "," + Kernel32.FIELD_BATTERYFULLLIFETIME);
        windowsFieldsFieldSample.setBorder(BorderFactory.createEmptyBorder());
        linuxCommandLabel.setText("<html><b>Linux only:</b> command to retrieve battery status:</html>");
        macosCommandLabel.setText("<html><b>MacOS only:</b> command to retrieve battery status:</html>");
        refreshRateField.setToolTipText("Choose a value between " + MINIMAL_REFRESH_INTERVAL + " and " + Integer.MAX_VALUE + ".");
        macosPreferScriptShowBattPercent.setText("<html>Instead, try to show battery percentage only via a bundled script:<br><i>" +
            SettingsService.DEFAULT_MACOS_COMMAND_BATTERY_PERCENT + "</i><br>" +
            "stored in system's temporary directory.");
        iconsSetSelectorLabel.setText("Battery icons set:");
        for (int i = 0; i < 5; i++) {
            iconsSetSelector.addItem(IconLoader.getIcon("/icons/batterystatus/setsSelector/iconsSet" + i + ".png", SettingsForm.class));
        }
        drivePowerModeLabelCheckBox.setText("Watch battery level to enable/disable IDE's Power Save");
        lowBatteryLevelLabel.setText("          Enable Power Save when battery level is lower than %:");
        oshiFieldsLabel.setText("Battery fields to display. Example values:\n" +
            "- " + OshiFeatureName.CAPACITY_PERCENT.getLabel() + ":\n   charge level from 0% to 100%\n" +
            "- " + OshiFeatureName.AC.getLabel() + ":\n   Online or Offline\n" +
            "- " + OshiFeatureName.DISCHARGE_ESTIMATED_TIME.getLabel() + ":\n   estimated (as reported by OS) battery time remaining as 'XXhr YYm'. Visible when discharging\n" +
            "- " + OshiFeatureName.DISCHARGE_ESTIMATED_TIME_LONG.getLabel() + ":\n   estimated (as reported by OS) battery time remaining as 'XXhr YYm remaining'. Visible when discharging\n" +
            "- " + OshiFeatureName.DISCHARGE_INSTANT_TIME.getLabel() + ":\n   instant battery time remaining as 'XXhr YYm'. Visible when discharging\n" +
            "- " + OshiFeatureName.DISCHARGE_INSTANT_TIME_LONG.getLabel() + ":\n   instant battery time remaining as 'XXhr YYm remaining'. Visible when discharging\n" +
            "- " + OshiFeatureName.CHARGE_ESTIMATED_TIME.getLabel() + ":\n   estimated (as reported by OS) time to full charge as 'XXhr YYm'. Visible when charging\n" +
            "- " + OshiFeatureName.CHARGE_ESTIMATED_TIME_LONG.getLabel() + ":\n   estimated (as reported by OS) time to full charge as 'XXhr YYm to full charge'. Visible when charging\n" +
            "- " + OshiFeatureName.CHARGE_INSTANT_TIME.getLabel() + ":\n   instant time to full charge as 'XXhr YYm'. Visible when charging\n" +
            "- " + OshiFeatureName.CHARGE_INSTANT_TIME_LONG.getLabel() + ":\n   instant time to full charge as 'XXhr YYm to full charge'. Visible when charging\n" +
            "Fields are replaced by actual values. Other characters are preserved.");

        drivePowerModeLabelCheckBox.addActionListener(e -> {
            lowBatteryLevelLabel.setVisible(drivePowerModeLabelCheckBox.isSelected());
            lowBatteryLevelSpinner.setVisible(drivePowerModeLabelCheckBox.isSelected());
            modified = true;
        });

        loadConfig();

        DocumentListener docListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                modified = true;
            }

            public void removeUpdate(DocumentEvent e) {
                modified = true;
            }

            public void insertUpdate(DocumentEvent e) {
                modified = true;
            }
        };
        ActionListener actionListener = e -> modified = true;
        ChangeListener changeListener = e -> modified = true;
        useOshiCheckBox.addActionListener(e -> {
            showHidePanes();
            modified = true;
        });
        refreshRateField.getDocument().addDocumentListener(docListener);
        windowsFieldsField.getDocument().addDocumentListener(docListener);
        linuxCommandField.getDocument().addDocumentListener(docListener);
        macosCommandField.getDocument().addDocumentListener(docListener);
        macosPreferScriptShowBattPercent.addActionListener(actionListener);
        iconsSetSelector.addActionListener(actionListener);
        lowBatteryLevelSpinner.addChangeListener(changeListener);
        oshiFieldsTextField.getDocument().addDocumentListener(docListener);

        return mainPane;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void apply() {
        SettingsService settingsService = IJUtils.getSettingsService();
        try {
            int refreshRate = Integer.parseInt(refreshRateField.getText());
            if (refreshRate < MINIMAL_REFRESH_INTERVAL) {
                JOptionPane.showMessageDialog(refreshRateField,
                    "Please type an integer value greater or equal to " + MINIMAL_REFRESH_INTERVAL + ".", "Bad input", JOptionPane.ERROR_MESSAGE);
            } else {
                settingsService.setBatteryRefreshIntervalInMs(refreshRate);
                modified = false;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(refreshRateField,
                "Please type an integer value.", "Bad input", JOptionPane.ERROR_MESSAGE);
        }

        settingsService.setWindowsBatteryFields(windowsFieldsField.getText());
        settingsService.setLinuxBatteryCommand(linuxCommandField.getText());
        settingsService.setMacosBatteryCommand(macosCommandField.getText());
        settingsService.setMacosPreferScriptShowBattPercent(macosPreferScriptShowBattPercent.isSelected());
        settingsService.setIconsSet(iconsSetSelector.getSelectedIndex());
        settingsService.setConfigurePowerSaverBasedOnPowerLevel(drivePowerModeLabelCheckBox.isSelected());
        settingsService.setLowPowerValue((int) lowBatteryLevelSpinner.getValue());
        settingsService.setUseOshi(useOshiCheckBox.isSelected());
        settingsService.setOshiBatteryFields(oshiFieldsTextField.getText());
        IJUtils.refreshOpenedProjects();
    }

    @Override
    public void reset() {
        SettingsService settingsService = IJUtils.getSettingsService();
        settingsService.setBatteryRefreshIntervalInMs(settingsService.getBatteryRefreshIntervalInMs());
        settingsService.setWindowsBatteryFields(settingsService.getWindowsBatteryFields());
        settingsService.setLinuxBatteryCommand(settingsService.getLinuxBatteryCommand());
        settingsService.setMacosBatteryCommand(settingsService.getMacosBatteryCommand());
        settingsService.setMacosPreferScriptShowBattPercent(settingsService.getMacosPreferScriptShowBattPercent());
        settingsService.setIconsSet(settingsService.getIconsSet());
        settingsService.setConfigurePowerSaverBasedOnPowerLevel(settingsService.getConfigurePowerSaverBasedOnPowerLevel());
        settingsService.setLowPowerValue(settingsService.getLowPowerValue());
        settingsService.setUseOshi(settingsService.getUseOshi());
        settingsService.setOshiBatteryFields(settingsService.getOshiBatteryFields());
        loadConfig();
        modified = false;
    }

    private void loadConfig() {
        SettingsService settingsService = IJUtils.getSettingsService();
        refreshRateField.setText(Integer.toString(settingsService.getBatteryRefreshIntervalInMs()));
        windowsFieldsField.setText(settingsService.getWindowsBatteryFields());
        linuxCommandField.setText(settingsService.getLinuxBatteryCommand());
        macosCommandField.setText(settingsService.getMacosBatteryCommand());
        macosPreferScriptShowBattPercent.setSelected(settingsService.getMacosPreferScriptShowBattPercent());
        iconsSetSelector.setSelectedIndex(settingsService.getIconsSet());
        drivePowerModeLabelCheckBox.setSelected(settingsService.getConfigurePowerSaverBasedOnPowerLevel());
        lowBatteryLevelSpinner.setValue(settingsService.getLowPowerValue());
        lowBatteryLevelLabel.setVisible(settingsService.getConfigurePowerSaverBasedOnPowerLevel());
        lowBatteryLevelSpinner.setVisible(settingsService.getConfigurePowerSaverBasedOnPowerLevel());
        useOshiCheckBox.setSelected(settingsService.getUseOshi());
        oshiFieldsTextField.setText(settingsService.getOshiBatteryFields());
        showHidePanes();
    }

    private void showHidePanes() {
        boolean oshiSelected = useOshiCheckBox.isSelected();
        regularPane.setVisible(!oshiSelected);
        oshiPane.setVisible(oshiSelected);
    }
}

package lermitage.intellij.battery.status.cfg.gui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.IconLoader;
import lermitage.intellij.battery.status.IJUtils;
import lermitage.intellij.battery.status.cfg.SettingsService;
import lermitage.intellij.battery.status.core.BatteryUtils;
import lermitage.intellij.battery.status.core.Kernel32;
import lermitage.intellij.battery.status.core.OS;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_LINUX_COMMAND;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_MACOS_COMMAND;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_MACOS_COMMAND_BATTERY_PERCENT_ENABLED;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_REFRESH_INTERVAL;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_WINDOWS_BATTERY_FIELDS;
import static lermitage.intellij.battery.status.cfg.SettingsService.MINIMAL_REFRESH_INTERVAL;

public class SettingsForm implements Configurable {

    private final SettingsService settingsService;
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
    private JLabel battPreviewLabel;
    private JComboBox<Icon> iconsSetSelector;
    private JLabel iconsSetSelectorLabel;

    private boolean modified = false;
    private static final String PREVIEW_TITLE = "Preview status text (hit Apply button to see changes): ";

    public SettingsForm() {
        this.settingsService = ServiceManager.getService(SettingsService.class);
        iconsSetSelector.addItem(IconLoader.getIcon("/icons/setsSelector/iconsSet0.png", SettingsForm.class));
        iconsSetSelector.addItem(IconLoader.getIcon("/icons/setsSelector/iconsSet1.png", SettingsForm.class));
        iconsSetSelector.addItem(IconLoader.getIcon("/icons/setsSelector/iconsSet2.png", SettingsForm.class));
        resetDefaultsBtn.addActionListener(e -> {
            refreshRateField.setText(Integer.toString(DEFAULT_REFRESH_INTERVAL));
            windowsFieldsField.setText(DEFAULT_WINDOWS_BATTERY_FIELDS);
            linuxCommandField.setText(DEFAULT_LINUX_COMMAND);
            macosCommandField.setText(DEFAULT_MACOS_COMMAND);
            macosPreferScriptShowBattPercent.setSelected(DEFAULT_MACOS_COMMAND_BATTERY_PERCENT_ENABLED);
            iconsSetSelector.setSelectedIndex(0);
            modified = true;
        });
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Battery Status";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
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
        refreshRateField.setToolTipText("Choose a value between " + MINIMAL_REFRESH_INTERVAL + " and " + Integer.MAX_VALUE + ".\nChange takes effect at next refresh.");
        macosPreferScriptShowBattPercent.setText("<html>Instead, try to show battery percentage only via a bundled script:<br><i>" +
                SettingsService.DEFAULT_MACOS_COMMAND_BATTERY_PERCENT + "</i><br>" +
                "stored in system's temporary directory.");
        battPreviewLabel.setText(PREVIEW_TITLE);
        iconsSetSelectorLabel.setText("Battery icons set:");

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
        ComponentListener componentListener = new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                modified = true;
            }

            public void componentMoved(ComponentEvent e) {
                modified = true;
            }

            public void componentShown(ComponentEvent e) {
                modified = true;
            }

            public void componentHidden(ComponentEvent e) {
                modified = true;
            }
        };
        refreshRateField.getDocument().addDocumentListener(docListener);
        windowsFieldsField.getDocument().addDocumentListener(docListener);
        linuxCommandField.getDocument().addDocumentListener(docListener);
        macosCommandField.getDocument().addDocumentListener(docListener);
        macosPreferScriptShowBattPercent.addComponentListener(componentListener);
        iconsSetSelector.addComponentListener(componentListener);
        previewBatt();

        return mainPane;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void apply() {
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
        previewBatt();
        IJUtils.refreshOpenedProjects();
    }

    @Override
    public void reset() {
        settingsService.setBatteryRefreshIntervalInMs(settingsService.getBatteryRefreshIntervalInMs());
        settingsService.setWindowsBatteryFields(settingsService.getWindowsBatteryFields());
        settingsService.setLinuxBatteryCommand(settingsService.getLinuxBatteryCommand());
        settingsService.setMacosBatteryCommand(settingsService.getMacosBatteryCommand());
        settingsService.setMacosPreferScriptShowBattPercent(settingsService.getMacosPreferScriptShowBattPercent());
        settingsService.setIconsSet(settingsService.getIconsSet());
        loadConfig();
        modified = false;
        previewBatt();
    }

    private void loadConfig() {
        refreshRateField.setText(Integer.toString(settingsService.getBatteryRefreshIntervalInMs()));
        windowsFieldsField.setText(settingsService.getWindowsBatteryFields());
        linuxCommandField.setText(settingsService.getLinuxBatteryCommand());
        macosCommandField.setText(settingsService.getMacosBatteryCommand());
        macosPreferScriptShowBattPercent.setSelected(settingsService.getMacosPreferScriptShowBattPercent());
        iconsSetSelector.setSelectedIndex(settingsService.getIconsSet());
    }

    private void previewBatt() {
        String batteryStatus;
        switch (OS.detectOS()) {
            case WIN:
                batteryStatus = BatteryUtils.readWindowsBatteryStatus(settingsService.getWindowsBatteryFields());
                break;
            case MACOS:
                batteryStatus = BatteryUtils.readMacOSBatteryStatus(settingsService.getMacosBatteryCommand(),
                        settingsService.getMacosPreferScriptShowBattPercent());
                break;
            default:
                batteryStatus = BatteryUtils.readLinuxBatteryStatus(settingsService.getLinuxBatteryCommand());
        }
        battPreviewLabel.setText("<html>" + PREVIEW_TITLE + "<b>" + batteryStatus + "</b>&nbsp;</html>");
    }
}

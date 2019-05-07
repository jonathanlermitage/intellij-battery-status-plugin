package lermitage.intellij.battery.status.cfg.gui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import lermitage.intellij.battery.status.cfg.SettingsService;
import lermitage.intellij.battery.status.core.Kernel32;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_LINUX_COMMAND;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_MACOS_COMMAND;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_REFRESH_INTERVAL;
import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_WINDOWS_BATTERY_FIELDS;
import static lermitage.intellij.battery.status.cfg.SettingsService.MINIMAL_REFRESH_INTERVAL;

public class SettingsForm implements Configurable {
    
    private SettingsService settingsService;
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
    
    private boolean modified = false;
    
    public SettingsForm() {
        this.settingsService = ServiceManager.getService(SettingsService.class);
        resetDefaultsBtn.addActionListener(e -> {
            refreshRateField.setText(Integer.toString(DEFAULT_REFRESH_INTERVAL));
            windowsFieldsField.setText(DEFAULT_WINDOWS_BATTERY_FIELDS);
            linuxCommandField.setText(DEFAULT_LINUX_COMMAND);
            macosCommandField.setText(DEFAULT_MACOS_COMMAND);
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
        refreshRateField.getDocument().addDocumentListener(docListener);
        windowsFieldsField.getDocument().addDocumentListener(docListener);
        linuxCommandField.getDocument().addDocumentListener(docListener);
        macosCommandField.getDocument().addDocumentListener(docListener);
        
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
    }
    
    @Override
    public void reset() {
        settingsService.setBatteryRefreshIntervalInMs(settingsService.getBatteryRefreshIntervalInMs());
        settingsService.setWindowsBatteryFields(settingsService.getWindowsBatteryFields());
        settingsService.setLinuxBatteryCommand(settingsService.getLinuxBatteryCommand());
        settingsService.setMacosBatteryCommand(settingsService.getMacosBatteryCommand());
        loadConfig();
        modified = false;
    }
    
    private void loadConfig() {
        refreshRateField.setText(Integer.toString(settingsService.getBatteryRefreshIntervalInMs()));
        windowsFieldsField.setText(settingsService.getWindowsBatteryFields());
        linuxCommandField.setText(settingsService.getLinuxBatteryCommand());
        macosCommandField.setText(settingsService.getMacosBatteryCommand());
    }
}

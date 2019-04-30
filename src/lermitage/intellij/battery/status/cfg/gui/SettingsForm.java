package lermitage.intellij.battery.status.cfg.gui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import lermitage.intellij.battery.status.cfg.SettingsService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static lermitage.intellij.battery.status.cfg.SettingsService.DEFAULT_REFRESH_INTERVAL;
import static lermitage.intellij.battery.status.cfg.SettingsService.MINIMAL_REFRESH_INTERVAL;

public class SettingsForm implements Configurable {
    
    private SettingsService settingsService;
    private JLabel refreshRateLabel;
    private JPanel mainPane;
    private JTextField refreshRateField;
    private JButton refreshRateDefaultBtn;
    
    private boolean modified = false;
    
    public SettingsForm() {
        this.settingsService = ServiceManager.getService(SettingsService.class);
        refreshRateDefaultBtn.addActionListener(e -> {
            refreshRateField.setText(Integer.toString(DEFAULT_REFRESH_INTERVAL));
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
        
        refreshRateDefaultBtn.setText("Reset to default");
        refreshRateDefaultBtn.setToolTipText("Reset to " + DEFAULT_REFRESH_INTERVAL + ".");
        
        refreshRateField.setText(Integer.toString(settingsService.getBatteryRefreshIntervalInMs()));
        refreshRateField.setToolTipText("Choose a value between " + MINIMAL_REFRESH_INTERVAL + " and " + Integer.MAX_VALUE + ".\nChange takes effect at next refresh.");
        refreshRateField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                modified = true;
            }
            
            public void removeUpdate(DocumentEvent e) {
                modified = true;
            }
            
            public void insertUpdate(DocumentEvent e) {
                modified = true;
            }
        });
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
    }
    
    @Override
    public void reset() {
        settingsService.setBatteryRefreshIntervalInMs(settingsService.getBatteryRefreshIntervalInMs());
        refreshRateField.setText(Integer.toString(settingsService.getBatteryRefreshIntervalInMs()));
        modified = false;
    }
}

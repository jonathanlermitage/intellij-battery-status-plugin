package lermitage.intellij.battery.status.core;

import org.jetbrains.annotations.NotNull;

public enum OS {
    WIN,
    LINUX,
    MACOS;
    
    private static OS detectedOS;
    
    @NotNull
    public static OS detectOS() {
        if (detectedOS == null) {
            String os = System.getProperty("os.name").toUpperCase();
            if (os.contains("WIN")) {
                detectedOS = WIN;
            } else if (os.contains("NIX") || os.contains("NUX") || os.contains("AIX")) {
                detectedOS = LINUX;
            } else if (os.contains("MAC")) {
                detectedOS = MACOS;
            } else {
                detectedOS = LINUX;
            }
        }
        return detectedOS;
    }
}

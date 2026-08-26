package com.dafi.desktop.infrastructure;

import javafx.application.Application;

/**
 * Entry point that decouples JVM startup from {@link DafiApplication}.
 * Launching an Application subclass directly from the classpath fails on
 * recent JDKs (the JavaFX runtime must be resolved as modules); delegating
 * through this plain launcher keeps the application runnable from a flat
 * classpath and simplifies native packaging.
 */
public class DafiLauncher {

    /**
     * Boots the JavaFX application.
     *
     * @param args command-line arguments forwarded to DafiApplication
     */
    public static void main(String[] args) {
        Application.launch(DafiApplication.class, args);
    }
}

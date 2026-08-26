package com.dafi.desktop.adapters.inbound.fx;

import javafx.application.Platform;
import javafx.scene.control.TextField;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Reusable text-field debounce: delays firing the callback until the user
 * stops typing for the specified interval, avoiding expensive searches on
 * every keystroke.
 */
public final class SearchDebounceUtils {

    private static final int DEFAULT_DELAY_MS = 300;

    private SearchDebounceUtils() {
    }

    /**
     * Attaches a debounced listener to the given text field.
     * Each keystroke resets the timer; the callback fires only after the
     * delay elapses without further input.
     *
     * @param field the text field to watch
     * @param onDebounced callback to run on the FX Application Thread
     */
    public static void attach(TextField field, Runnable onDebounced) {
        Timer[] timerHolder = new Timer[1];
        field.textProperty().addListener((obs, old, value) -> {
            if (timerHolder[0] != null) {
                timerHolder[0].cancel();
            }
            timerHolder[0] = new Timer(true);
            timerHolder[0].schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(onDebounced);
                }
            }, DEFAULT_DELAY_MS);
        });
    }
}

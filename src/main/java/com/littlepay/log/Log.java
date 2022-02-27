package com.littlepay.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
    private static final Logger log = Logger.getLogger(Log.class.getName());

    public static void info(String message, Object[] params) {
        log.log(Level.INFO, message, params);
    }
    public static void warning(String message, Object[] params, Exception e) {
        log.log(Level.WARNING, message, params);
        if (e != null)
            e.printStackTrace();
    }

    public static void fine(String message, Object[] params) {
        log.log(Level.FINE, message, params);
    }
}

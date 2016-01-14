package org.wildfly.byteman.log;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;
/**
 * A log helper class. Creates log records via {@link LogManager}.
 * @author Radek Koubsky
 * */
public class LogHelper extends Helper{

    /**
     * Ctor.
     * @param rule a rule
     * */
    protected LogHelper(final Rule rule) {
        super(rule);
    }

    /**
     * Creates a log record by given logger name, log level and message.
     * @param loggerName full package qualified logger name
     * @param level a log level
     * @param message a log message
     * */
    public void log(final String loggerName, final String level, final String message){
        final Logger logger = LogManager.getLogManager().getLogger(loggerName);
        if(logger != null){
            logger.log(Level.parse(level), "[ThreadId-" + Thread.currentThread().getId() + "] " + message);
        }
    }

    /**
     * Creates a log record by given logger name, log level, message and exception associated with the log record.
     * @param loggerName full package qualified logger name
     * @param level a log level
     * @param message a log message
     * @param thrown exception to be logged
     * */
    public void log(final String loggerName, final String level, final String message, final Throwable thrown){
        final Logger logger = LogManager.getLogManager().getLogger(loggerName);
        if(logger != null){
            logger.log(Level.parse(level), "[ThreadId-" + Thread.currentThread().getId() + "] " + message, thrown);
        }
    }
}

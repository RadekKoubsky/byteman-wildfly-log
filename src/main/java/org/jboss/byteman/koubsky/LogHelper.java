package org.jboss.byteman.koubsky;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;

public class LogHelper extends Helper{

    protected LogHelper(final Rule rule) {
        super(rule);
    }

    public void log(final String loggerName, final String level, final String message){
        final Logger logger = LogManager.getLogManager().getLogger(loggerName);
        if(logger != null){
            logger.log(Level.parse(level), message);
        }
    }

    public void log(final String loggerName, final String level, final String message, final Throwable thrown){
        final Logger logger = LogManager.getLogManager().getLogger(loggerName);
        if(logger != null){
            logger.log(Level.parse(level), message, thrown);
        }
    }
}

package org.jboss.byteman.koubsky.servlets;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;

public class ServletHandlerLog extends Helper{

    protected ServletHandlerLog(final Rule rule) {
        super(rule);
        // TODO Auto-generated constructor stub
    }

    public void logCookie(final String clazz, final HttpServerExchange exchange){
        final Logger logger = LogManager.getLogManager().getLogger(clazz);
        if(logger != null){
            final Level level = Level.parse("DEBUG");
            logger.log(level, "Request cookies:");
            final Map<String, Cookie> cookies = exchange.getRequestCookies();
            final Iterator<String> it = cookies.keySet().iterator();
            while(it.hasNext()) {
                final Cookie cookie = cookies.get(it.next());
                logger.log(level, cookie.getName() + " = " + cookie.getValue());
            }
        }
    }
}

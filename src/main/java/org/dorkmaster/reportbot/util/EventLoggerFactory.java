package org.dorkmaster.reportbot.util;

public interface EventLoggerFactory {

    EventLogger getLogger();
    CircularBuffer<Object> getLog();

    static enum Levels {
        TRACE, DEBUG, INFO, WARN, FATAL
    }
}

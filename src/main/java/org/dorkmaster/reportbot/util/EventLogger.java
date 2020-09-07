package org.dorkmaster.reportbot.util;

public interface EventLogger {

    void trace(String message);

    void debug(String message);

    void info(String message);

    void warn(String message);

    void fatal(String message);

    void message(String guild, String author, String content);

    void close();

}

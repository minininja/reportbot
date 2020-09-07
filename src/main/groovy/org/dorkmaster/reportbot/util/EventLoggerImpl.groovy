package org.dorkmaster.reportbot.util

import com.fasterxml.jackson.databind.ObjectMapper

import java.text.SimpleDateFormat

class EventLoggerImpl implements EventLogger {
    CircularBuffer buffer
    EventLoggerFactory.Levels level
    List log = []
    Map event = [
            ts : new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()),
            log: log
    ]

    EventLoggerImpl(EventLoggerFactory.Levels level, CircularBuffer buffer) {
        this.level = level
        this.buffer = buffer
    }

    @Override
    void trace(String message) {
        log(EventLoggerFactory.Levels.TRACE, message)
    }

    @Override
    void debug(String message) {
        log(EventLoggerFactory.Levels.DEBUG, message)
    }

    @Override
    void info(String message) {
        log(EventLoggerFactory.Levels.INFO, message)
    }

    @Override
    void warn(String message) {
        log(EventLoggerFactory.Levels.WARN, message)
    }

    @Override
    void fatal(String message) {
        log(EventLoggerFactory.Levels.FATAL, message)
    }

    void log(EventLoggerFactory.Levels l, String message) {
        if (l.ordinal() >= level.ordinal()) {
            log << [level: l.toString(), data: message]
        }
    }

    @Override
    void message(String guild, String author, String content) {
        event.message = [
                event: 'event',
                data : [
                        guild  : guild,
                        author : author,
                        message: content
                ]
        ]
    }

    @Override
    void close() {
        System.out.println(new ObjectMapper().writeValueAsString(event));
        buffer.push(event)
    }
}

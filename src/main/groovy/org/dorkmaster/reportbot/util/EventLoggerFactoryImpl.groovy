package org.dorkmaster.reportbot.util

import org.dorkmaster.reportbot.config.Config

class EventLoggerFactoryImpl implements EventLoggerFactory {
    Config config
    CircularBuffer<Object> buffer

    EventLoggerFactoryImpl(Config config) {
        this.config = config
        this.buffer = new CircularBuffer<>(config.find("logger.size").asInt(200))
    }

    @Override
    EventLogger getLogger() {
        Levels level = Levels.valueOf(config.find("logger.level").asString(Levels.TRACE.toString()))
        return new EventLoggerImpl(level, buffer)
    }

    @Override
    CircularBuffer<Object> getLog() {
        return buffer
    }
}

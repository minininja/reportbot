package org.dorkmaster.reportbot

import groovyx.net.http.HTTPBuilder
import org.dorkmaster.reportbot.config.Config
import org.dorkmaster.reportbot.util.EventLogger

class SubmitReportImpl implements SubmitReport {

    Config config
    EventLogger logger

    SubmitReportImpl(Config config, EventLogger logger) {
        this.config = config
        this.logger = logger
    }

    @Override
    boolean submit(def server,  Map<String, String> detail) {
        boolean ok = false

        Config.Value url = config.get("servers.${server}.url")
        if (!url.isNull()) {
            new HTTPBuilder(url.asString()).post (query: detail) { resp, reader ->
                logger.info("responseStatus: ${resp.statusLine}")
                def t = resp.statusLine
                if (200 == resp?.statusLine?.statusCode) {
                    ok = true
                }
            }
        }

        return ok
    }

}
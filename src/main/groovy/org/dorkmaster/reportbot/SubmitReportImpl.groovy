package org.dorkmaster.reportbot

import groovyx.net.http.HTTPBuilder
import org.dorkmaster.reportbot.config.Config

class SubmitReportImpl implements SubmitReport {

    Config config

    SubmitReportImpl(Config config) {
        this.config = config
    }

    @Override
    boolean submit(def server,  Map<String, String> detail) {
        boolean ok = false

        Config.Value url = config.get("servers.${server}.url")
        if (!url.isNull()) {
            new HTTPBuilder(url.asString()).post (query: detail) { resp, reader ->
                println "responseStatus: ${resp.statusLine}"
                def t = resp.statusLine
                if (200 == resp?.statusLine?.statusCode) {
                    ok = true
                }
            }
        }

        return ok
    }

}
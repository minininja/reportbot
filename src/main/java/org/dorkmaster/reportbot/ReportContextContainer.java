package org.dorkmaster.reportbot;

public interface ReportContextContainer {
    boolean has(String user);
    ReportContext create(String guildId, String user);
    ReportContext get(String user);
    boolean submit(ReportContext context);
    boolean abort(ReportContext context);
}

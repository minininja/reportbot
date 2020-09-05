package org.dorkmaster.reportbot;

import java.util.Map;

public interface SubmitReport {
    boolean submit(Object server, Map<String,String> detail);
}

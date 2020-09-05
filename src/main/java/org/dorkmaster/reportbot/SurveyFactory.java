package org.dorkmaster.reportbot;

import java.util.Map;

public interface SurveyFactory {

    Map<String,String> mapToSurvey(Object server, Map<String,String> detail, String author);

}

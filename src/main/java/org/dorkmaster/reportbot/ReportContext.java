package org.dorkmaster.reportbot;

import java.util.Map;

public interface ReportContext {

    boolean abort();
    boolean isAbortState();
    boolean isAborting(String message);
    boolean hasNextQuestion();
    String nextQuestion();
    boolean answer(String message);
    boolean submit();
    String getUser();
    String getGuild();
    Map<String,String> getQuestionAnswers();

}

package org.dorkmaster.reportbot.service;

import org.dorkmaster.reportbot.ReportContext;
import org.dorkmaster.reportbot.ReportContextContainer;
import org.dorkmaster.reportbot.config.Config;

import java.util.HashMap;
import java.util.Map;

public class ReportContextImpl implements ReportContext {
    private String guildId;
    private Config config;
    private ReportContextContainer rcc;
    private boolean aborting = false;
    private int currentQuestion = 0;
    private Map<String,String> questionsAnswers = new HashMap<>();
    private String user;

    public ReportContextImpl(String guildId, String user, Config config, ReportContextContainer rcc) {
        this.guildId = guildId;
        this.config = config;
        this.rcc = rcc;
        this.user = user;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getGuild() {
        return guildId;
    }

    @Override
    public Map<String, String> getQuestionAnswers() {
        return questionsAnswers;
    }


    protected Config.Value prop(String path) {
        String p = "servers." + guildId + ".automaton." + path;
        return config.get(p);
    }

    protected String compressString(String tmp) {
        return tmp.replaceAll("\\s", "");
    }

    protected String currentQuestion() {
        Config.Value value = prop("questions");

        if (value.isNull()) {
            return null;
        }

        return (String) value.asList().get(currentQuestion);
    }

    @Override
    public boolean abort() {
        return rcc.abort(this);
    }

    @Override
    public boolean isAbortState() {
        return aborting;
    }

    @Override
    public boolean isAborting(String message) {
        return config
                .get("servers.automaton." + guildId + ".abortString")
                .asString("abortreport")
                .equalsIgnoreCase(compressString(message));
    }

    @Override
    public boolean hasNextQuestion() {
        Config.Value value = prop("questions");
        return value.isNull() ? false : value.asList().size() > currentQuestion;
    }

    @Override
    public String nextQuestion() {
        return currentQuestion();
    }

    @Override
    public boolean answer(String answer) {
        Config.Value value = prop("questions");
        if (value.isNull()) {
            return false;
        }
        if (currentQuestion >= value.asList().size()) {
            return false;
        }
        String question = (String) value.asList().get(currentQuestion);
        questionsAnswers.put( question, answer);
        currentQuestion++;
        return true;
    }

    @Override
    public boolean submit() {
        return rcc.submit(this);
    }
}

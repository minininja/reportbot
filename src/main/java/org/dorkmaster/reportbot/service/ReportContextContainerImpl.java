package org.dorkmaster.reportbot.service;

import org.dorkmaster.reportbot.ReportContext;
import org.dorkmaster.reportbot.ReportContextContainer;
import org.dorkmaster.reportbot.SubmitReport;
import org.dorkmaster.reportbot.SurveyFactory;
import org.dorkmaster.reportbot.config.Config;

import java.util.HashMap;
import java.util.Map;

public class ReportContextContainerImpl implements ReportContextContainer {

    private Config config;
    private SurveyFactory surveyFactory;
    private SubmitReport submitReport;
    private Map<String, ReportContext> contexts = new HashMap<>();

    public ReportContextContainerImpl(Config config, SurveyFactory surveyFactory, SubmitReport submitReport) {
        this.config = config;
        this.surveyFactory = surveyFactory;
        this.submitReport = submitReport;
    }


    @Override
    public boolean has(String user) {
        return contexts.containsKey(user);
    }

    @Override
    public ReportContext create(String guildId, String user) {
        if (has(user)) {
            return contexts.remove(user);
        }

        ReportContext rc = new ReportContextImpl(guildId, user, config, this);
        contexts.put(user, rc);
        return rc;
    }

    @Override
    public ReportContext get(String user) {
        return null;
    }

    @Override
    public boolean submit(ReportContext context) {
        if (!has(context.getUser())) {
            return false;
        }

        Map<String,String> translated = surveyFactory.mapToSurvey(
                context.getGuild(),
                context.getQuestionAnswers(),
                context.getUser()
        );

        boolean result = submitReport.submit(context.getGuild(), translated);

        contexts.remove(context.getUser());

        return result;
    }

    @Override
    public boolean abort(ReportContext context) {
        return false;
    }

}

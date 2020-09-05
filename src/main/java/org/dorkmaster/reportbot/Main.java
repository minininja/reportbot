package org.dorkmaster.reportbot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.gateway.MessageCreate;
import org.dorkmaster.reportbot.config.Config;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
$report
test=test
 */
public class Main {

    static class Tuple {
        String first;
        String second;

        public Tuple(String first, String second) {
            this.first = first;
            this.second = second;
        }

        public String getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }
    }

    private static String param(String name) {
        return param(name, null);
    }

    private static String param(String name, String def) {
        String tmp = System.getenv(name);
        if (null == tmp) {
            return def;
        }
        return tmp;
    }

    public static void main(String[] args) {
        final String token = param("token");
        final String activation = param("activation");
        final String surveyUrl = param("surveyUrl", "https://docs.google.com/forms/u/0/d/e/1FAIpQLSeOYk6BICHRkdscVlLS8DglTPRR3N33UHMg6Cf12V-MtHsKrQ/formResponse");
        final Config config = new Config(param("config"));
        final SurveyFactory sf = new SurveyFactoryImpl(config);
        final SubmitReport sr = new SubmitReportImpl(config);

        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            MessageCreateEvent evt = (MessageCreateEvent) event;

            Message message = event.getMessage();
            String content = message.getContent();

            // ignore pms
            if (content.startsWith(activation) && evt.getGuildId().isPresent()) {
                Optional<User> ou = message.getAuthor();

                String id = "servers." + evt.getGuildId().get().asString();
                Config.Value url = config.get(id + ".url");
                Config.Value delete = config.get(id + ".deleteOnSubmit");

                Map<String, String> formData = Arrays.stream(content.split("\\n"))
                        .map(a-> a.split("[=:]"))
                        .filter(a-> a.length == 2)
                        .map(a-> new Tuple(a[0], a[1]))
                        .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));

                Map<String,String> surveyData = sf.mapToSurvey(
                        evt.getGuildId().get().asString(), formData, ou.isPresent() ? ou.get().getUsername() : "");

                sr.submit(evt.getGuildId().get().asString(), surveyData);

                MessageChannel channel = message.getChannel().block();
                if (true) {
                    if (delete.asBoolean(false)) {
                        message.delete().block();
                    }
                    channel.createMessage("Thank you for your report").block();
                } else {
                    channel.createMessage("Something bad happened, please report again later").block();
                }
            }


//            MessageChannel channel = message.getChannel().block();
//            Optional<User> ou = message.getAuthor();
//
//            if (content.startsWith(activation)) {
//
//                Map<String, String> formData = Arrays.stream(content.split("\\n"))
//                        .map(a-> a.split("[=:]"))
//                        .filter(a-> a.length == 2)
//                        .map(a-> new Tuple(a[0], a[1]))
//                        .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
//                if (ou.isPresent()) {
//                    formData.put("REPORTER", ou.get().getUsername());
//                }
//                if (sr.submit(formData)) {
//                    channel.createMessage("Thank you for your report").block();
//                } else {
//                    channel.createMessage("Something bad happened, please report again later").block();
//                }
//            }
        });

        gateway.onDisconnect().block();

    }
}

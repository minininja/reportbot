package org.dorkmaster.reportbot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import org.dorkmaster.reportbot.config.Config;
import org.dorkmaster.reportbot.http.MiniServer;
import org.dorkmaster.reportbot.util.EventLogger;
import org.dorkmaster.reportbot.util.EventLoggerFactory;
import org.dorkmaster.reportbot.util.EventLoggerFactoryImpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
$report
test=test
 */
public class Main {

    private class Tuple {
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

    private String name(Optional<Member> member) {
        if (member.isPresent()) {
            if (member.get().getNickname().isPresent()) {
                return member.get().getNickname().get();
            }
            else {
                return member.get().getDisplayName();
            }
        }
        return "Nobody";
    }

    private String token;
    private String activation;
    private Config config;
    private EventLoggerFactory lf;

    public Main(String token, String activation, Config config) {
        this.token = token;
        this.activation = activation;
        this.config = config;

        // setup loggers
        lf = new EventLoggerFactoryImpl(config);
    }

    public void startMiniServer() {
        EventLogger logger= lf.getLogger();
        try {
            try {
                new MiniServer(config, lf).start();
            } catch (IOException e) {
                logger.warn("Couldn't start mini server");
                logger.warn(e.getMessage());
            }
        } finally {
            logger.close();
        }
    }

    public void consumeMessages() {
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            EventLogger logger = lf.getLogger();
            try {
                SurveyFactory sf = new SurveyFactoryImpl(config);
                SubmitReport sr = new SubmitReportImpl(config, logger);

                MessageCreateEvent evt = (MessageCreateEvent) event;

                String guild = evt.getGuildId().isPresent() ? evt.getGuildId().get().asString() : "direct";
                String message = evt.getMessage().getContent();
                String author = name(evt.getMember());
                logger.message(guild, author, message);

                logger.debug("Message from " + author);
                // ignore pms
                if (message.startsWith(activation) && evt.getGuildId().isPresent()) {
                    logger.debug("Activated on " + message);

                    String id = "servers." + evt.getGuildId().get().asString();
                    Config.Value url = config.get(id + ".url");
                    Config.Value delete = config.get(id + ".deleteOnSubmit");

                    Map<String, String> formData = Arrays.stream(message.split("\\n"))
                            .map(a -> a.split("[=:]"))
                            .filter(a -> a.length == 2)
                            .map(a -> new Tuple(a[0], a[1]))
                            .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));

                    Map<String, String> surveyData = sf.mapToSurvey(evt.getGuildId().get().asString(), formData, author);

                    MessageChannel channel = evt.getMessage().getChannel().block();
                    if (sr.submit(evt.getGuildId().get().asString(), surveyData)) {
                        logger.debug("Message from " + author + " processed successfully");
                        if (delete.asBoolean(false)) {
                            evt.getMessage().delete().block();
                        }
                        channel.createMessage("Thank you for your report").block();
                    } else {
                        logger.debug("Could not post content from " + author + " to survey");
                        channel.createMessage("Something bad happened, please report again later").block();
                    }
                }
            } finally {
                logger.close();
            }
        });

        gateway.onDisconnect().block();
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
        final Config config = new Config(param("config"));

        Main main = new Main(token, activation, config);
        main.startMiniServer();
        // this blocks
        main.consumeMessages();
    }
}

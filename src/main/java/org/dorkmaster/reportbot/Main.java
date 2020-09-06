package org.dorkmaster.reportbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import org.dorkmaster.reportbot.config.Config;
import org.dorkmaster.reportbot.util.CircularBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
$report
test=test
 */
public class Main {

    static class Debug {
        String server;
        String author;
        String content;
        String ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());

        public Debug(String server, String author, String content) {
            this.server = server;
            this.author = author;
            this.content = content;
        }

        public String getServer() {
            return server;
        }

        public String getAuthor() {
            return author;
        }

        public String getContent() {
            return content;
        }

        public String getTs() {
            return ts;
        }
    }

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

    private static String name(Optional<Member> member) {
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

    public static void main(String[] args) {
        final String token = param("token");
        final String activation = param("activation");
        final Config config = new Config(param("config"));

        final SurveyFactory sf = new SurveyFactoryImpl(config);
        final SubmitReport sr = new SubmitReportImpl(config);

        final CircularBuffer<Debug> buffer = new CircularBuffer<>(config.get("webservice.buffer.size").asInt(200));
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();

        try {
            miniWeb(config, buffer);
        } catch (IOException e) {
            System.out.println("Couldn't create web service");
            e.printStackTrace();
        }

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            MessageCreateEvent evt = (MessageCreateEvent) event;

            String guild = evt.getGuildId().isPresent() ? evt.getGuildId().get().asString() : "direct";
            String message = evt.getMessage().getContent();
            String author = name(evt.getMember());

            buffer.push(new Debug(guild, author, message));

            // ignore pms
            if (message.startsWith(activation) && evt.getGuildId().isPresent()) {
                String id = "servers." + evt.getGuildId().get().asString();
                Config.Value url = config.get(id + ".url");
                Config.Value delete = config.get(id + ".deleteOnSubmit");

                Map<String, String> formData = Arrays.stream(message.split("\\n"))
                        .map(a-> a.split("[=:]"))
                        .filter(a-> a.length == 2)
                        .map(a-> new Tuple(a[0], a[1]))
                        .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));

                Map<String,String> surveyData = sf.mapToSurvey(evt.getGuildId().get().asString(), formData, author);

                MessageChannel channel = evt.getMessage().getChannel().block();
                if (sr.submit(evt.getGuildId().get().asString(), surveyData)) {
                    if (delete.asBoolean(false)) {
                        evt.getMessage().delete().block();
                    }
                    channel.createMessage("Thank you for your report").block();
                } else {
                    channel.createMessage("Something bad happened, please report again later").block();
                }
            }
        });

        gateway.onDisconnect().block();
    }

    private static ObjectMapper om = new ObjectMapper();

    public static void miniWeb(Config config, CircularBuffer buffer) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(config.get("webservice.port").asInt(8080)), 0);
        server.createContext("/messages", httpExchange -> {
            byte response[] = om.writeValueAsString(buffer.asList()).getBytes();
            httpExchange.getResponseHeaders().add("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = httpExchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();
    }
}

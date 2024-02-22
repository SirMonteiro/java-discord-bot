package top.gabrielsouza;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import top.gabrielsouza.listeners.MessagesEvent;

import javax.security.auth.login.LoginException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class botMain {

    private static JDABuilder builder;
    private static JDA jda;

    private static JDABuilder initBot(String TOKEN) throws LoginException, InterruptedException {
        builder = JDABuilder.createDefault(TOKEN)
        .setStatus(OnlineStatus.ONLINE)
        .setActivity(Activity.watching("U!"))
        .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES)
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .setChunkingFilter(ChunkingFilter.ALL)
        .enableCache(CacheFlag.ONLINE_STATUS);

        builder.addEventListeners(new MessagesEvent());

        jda = builder.build();
        return builder;
    }

    public static JDABuilder getJDABuilder() {
        return builder;
    }

    public static JDA getJDA() {
        return jda;
    }

    public static void main(String[] args) {
        Dotenv config = Dotenv.configure().load();

        SpringApplication.run(botMain.class, args);

        try {
            JDABuilder builder = initBot(config.get("TOKEN"));
        } catch (LoginException|InterruptedException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

    }
}
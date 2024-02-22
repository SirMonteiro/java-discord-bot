package top.gabrielsouza.listeners;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.ArrayList;
import java.util.List;

public class MessagesEvent extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        String message = event.getMessage().getContentRaw();
        if (message.contains("ping")) {
            long currentTime = System.currentTimeMillis();
            event.getChannel().sendMessage("Pinging...").queue(response -> {
                long ping = System.currentTimeMillis() - currentTime;
                response.editMessageFormat("pong: %d ms, gateway: %d ms", ping, event.getJDA().getGatewayPing()).queue();
            });
        }
//        System.out.println(event.getMessage().getContentDisplay());

    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("welcome")) {
            // Run the 'ping' command
            String userTag = event.getUser().getAsTag();
            event.reply("Welcome to the server, **" + userTag + "**!").queue();
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("welcome", "Get welcomed by the bot"));
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}

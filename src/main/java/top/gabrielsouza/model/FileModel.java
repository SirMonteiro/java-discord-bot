package top.gabrielsouza.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public class FileModel {
    @Id
    private String id;
    private List<String> messageIds = new ArrayList<>();
    private String filename;
    private String guildId;
    private String channelId;

    // Getters and Setters declaration

    public String getId() {
        return id;
    }
    public List<String> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<String> messagesIds) {
        this.messageIds = messagesIds;
    }

    public void addToMessageIds(String messageId) {
        this.messageIds.add(messageId);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}

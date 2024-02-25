package top.gabrielsouza.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class GuildModel {
    private String id;
    private String guildName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGuildName() {
        return guildName;
    }

    public void setGuildName(String guildName) {
        this.guildName = guildName;
    }

}

package top.gabrielsouza.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import top.gabrielsouza.model.GuildModel;

public interface GuildRepository extends MongoRepository<GuildModel, String> {
}

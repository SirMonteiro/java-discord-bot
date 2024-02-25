package top.gabrielsouza.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import top.gabrielsouza.model.FileModel;

import java.util.List;

public interface FileRepository extends MongoRepository<FileModel, String> {
    FileModel findByChannelIdAndFilename(String channelId, String guildId);
    List<FileModel> findAllByChannelId(String channelId);
}

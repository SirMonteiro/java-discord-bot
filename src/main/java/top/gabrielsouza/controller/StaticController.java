package top.gabrielsouza.controller;

import jakarta.servlet.http.HttpServletResponse;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import top.gabrielsouza.botMain;
import top.gabrielsouza.model.FileModel;
import top.gabrielsouza.model.GuildModel;
import top.gabrielsouza.repository.FileRepository;
import top.gabrielsouza.repository.GuildRepository;
import top.gabrielsouza.utils.fileSplitter;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.springframework.util.StreamUtils.BUFFER_SIZE;

@Controller
public class StaticController {

    @Autowired
    private GuildRepository guildRepository;
    @Autowired
    private FileRepository fileRepository;
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/success")
    public String success() {
        return "success";
    }

    @GetMapping("/create/{guildId}")
    public String create(@PathVariable String guildId) {
        Guild guild = botMain.getJDA().getGuildById(guildId);
        if (guild == null) return "index";
        GuildModel guildModel = new GuildModel();
        guildModel.setId(guildId);
        guildModel.setGuildName(guild.getName());
        guildRepository.save(guildModel);
        return "success";
    }

    @GetMapping("/files/{channelId}")
    public String files(@PathVariable String channelId, Model model) {
        List<String> filenames = new ArrayList<>();
        List<FileModel> filemodel = fileRepository.findAllByChannelId(channelId);
        for (int i = 0; i < filemodel.size(); i++) {
            filenames.add(filemodel.get(i).getFilename());
        }
        model.addAttribute("files", filenames);
        return "files";
    }

    @GetMapping("/video/{channelId}/{filename}")
    public String video(@PathVariable String channelId, @PathVariable String filename, Model model) {
        return "video";
    }

    @PostMapping("/")
    public String fileUpload(@RequestParam("file")MultipartFile file, RedirectAttributes redirectAttributes, @RequestParam("channelId")String channelId) {

        String UPLOADED_FOLDER = "C://Users//gabriel//IdeaProjects//discordBot//src//main//resources//test//";

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file!");
            return "redirect:index";
        }

        try {
            // Get the file and save it somewhere
//            byte[] bytes = file.getBytes();
//            Path dir = Paths.get(UPLOADED_FOLDER);
//            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            // Create parent dir if not exists
//            if(!Files.exists(dir)) {
//                Files.createDirectories(dir);
//            }
//            Files.write(path, bytes);

            List<byte[]> chunks = fileSplitter.getFileChunks(file.getBytes());

// guardar, o nome original, array com as ids das mensagens

            try {
                for (int i = 0; i < chunks.size(); i++) {
                    FileUpload upload = FileUpload.fromData(chunks.get(i), i + file.getOriginalFilename());
                    botMain.getJDA().getTextChannelById(channelId).sendFiles(upload).queue((message -> {
                        FileModel filemodel = fileRepository.findByChannelIdAndFilename(channelId, file.getOriginalFilename());
                        String messageId = message.getId();
                        if (filemodel != null) {
                            filemodel.addToMessageIds(messageId);
                            fileRepository.save(filemodel);
                        } else {
                            filemodel = new FileModel();
                            List<String> messagesIds = new ArrayList<>();
                            filemodel.setChannelId(channelId);
                            filemodel.setGuildId(botMain.getJDA().getTextChannelById(channelId).getGuild().getId());
                            filemodel.setFilename(file.getOriginalFilename());
                            messagesIds.add(messageId);
                            filemodel.setMessageIds(messagesIds);
                            fileRepository.save(filemodel);
                        }
//                        botMain.getJDA().getTextChannelById(channelId).sendMessage(Long.toString(messageID)).queue();
                    }));

                }
//                botMain.getJDA().getTextChannelById(channelId).sendMessage("sending file...").addFiles(upload).queue();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("message", "Server throw IOException");
            e.printStackTrace();
        }

        return "redirect:success";
    }

    // https://stackoverflow.com/a/60822304
    @GetMapping("/get/{channelId}/{originalFilename}")
    public StreamingResponseBody downloadFile(HttpServletResponse response, @PathVariable String channelId, @PathVariable String originalFilename) throws ExecutionException, InterruptedException {
        List<String> messageIds = new ArrayList<>();
        FileModel filemodel = fileRepository.findByChannelIdAndFilename(channelId, originalFilename);
        messageIds = filemodel.getMessageIds();


        List<InputStream> stream = new ArrayList<>();
        String contentType = "";
        for (int i = 0; i < messageIds.size(); i++) {
            String messageId = messageIds.get(i);
            CompletableFuture<Message> futureMessage = new CompletableFuture<>();
            botMain.getJDA().getTextChannelById(channelId).retrieveMessageById(messageId).queue(futureMessage::complete);
            Message.Attachment fileAttachment = futureMessage.get().getAttachments().get(0);
            if (i == 0) contentType = fileAttachment.getContentType();
            InputStream chunk = fileAttachment.getProxy().download().get();
            stream.add(chunk);
        }

        response.setContentType(contentType);
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + originalFilename + "\"");

        return outputStream -> {
            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];
            for (int i = 0; i < stream.size(); i++) {
                InputStream file = stream.get(i);
                while ((bytesRead = file.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

        };
    }
}
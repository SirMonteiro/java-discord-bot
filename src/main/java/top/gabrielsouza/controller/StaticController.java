package top.gabrielsouza.controller;

import jakarta.servlet.http.HttpServletResponse;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import top.gabrielsouza.botMain;
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
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/success")
    public String success() {
        return "success";
    }

    @PostMapping("/")
    public String fileUpload(@RequestParam("file")MultipartFile file, RedirectAttributes redirectAttributes, @RequestParam("channelID")String channelID) {

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
                    botMain.getJDA().getTextChannelById(channelID).sendFiles(upload).queue((message -> {
                        long messageID = message.getIdLong();
//                        botMain.getJDA().getTextChannelById(channelID).sendMessage(Long.toString(messageID)).queue();
                    }));

                }
//                botMain.getJDA().getTextChannelById(channelID).sendMessage("sending file...").addFiles(upload).queue();
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
    public StreamingResponseBody downloadFile(HttpServletResponse response, @PathVariable Long channelId, @PathVariable String originalFilename) throws ExecutionException, InterruptedException {

        System.out.println(originalFilename);

        List<Message> history = botMain.getJDA().getTextChannelById(channelId).getHistoryFromBeginning(100).complete().getRetrievedHistory();
        List<Long> messageIds = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            List<Message.Attachment> attachments = history.get(i).getAttachments();
            if (attachments.isEmpty()) continue;
            String fileName = attachments.get(0).getFileName();
            System.out.println(fileName);
            if (!fileName.contains(originalFilename)) continue;
            System.out.println("achou!");
            messageIds.add(history.get(i).getIdLong());
        }

        List<InputStream> stream = new ArrayList<>();
        String contentType = "";
        for (int i = 0; i < messageIds.size(); i++) {
            Long messageId = messageIds.get(i);
            CompletableFuture<Message> futureMessage = new CompletableFuture<>();
            botMain.getJDA().getTextChannelById(channelId).retrieveMessageById(messageId).queue(futureMessage::complete);
            Message.Attachment fileAttachment = futureMessage.get().getAttachments().get(0);
            if (i == 0) contentType = fileAttachment.getContentType();
            InputStream chunk = fileAttachment.getProxy().download().get();
            stream.add(chunk);
        }
//        InputStream result =
//        List<InputStream> file = stream;



//        CompletableFuture<Message> futureMessage = new CompletableFuture<>();
//        botMain.getJDA().getTextChannelById(channelId).retrieveMessageById(messageId).queue(futureMessage::complete);
//        Message.Attachment fileAttachment = futureMessage.get().getAttachments().get(0);
//        InputStream file = fileAttachment.getProxy().download().get();
//        String filename = fileAttachment.getFileName();
//        String contentType = fileAttachment.getContentType();

        response.setContentType(contentType);
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + originalFilename + "\"");

        return outputStream -> {
            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];
            for (int i = stream.size() - 1; i >= 0 ; i--) {
                InputStream file = stream.get(i);
                while ((bytesRead = file.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

        };
    }

//    // https://stackoverflow.com/a/60822304
//    @GetMapping("/get/{channelId}/{messageId}")
//    public StreamingResponseBody downloadFile(HttpServletResponse response, @PathVariable Long channelId, @PathVariable Long messageId) throws ExecutionException, InterruptedException {
//
//        CompletableFuture<Message> futureMessage = new CompletableFuture<>();
//        botMain.getJDA().getTextChannelById(channelId).retrieveMessageById(messageId).queue(futureMessage::complete);
//        Message.Attachment fileAttachment = futureMessage.get().getAttachments().get(0);
//        InputStream file = fileAttachment.getProxy().download().get();
//        String filename = fileAttachment.getFileName();
//        String contentType = fileAttachment.getContentType();
//
//        response.setContentType(contentType);
//        response.setHeader(
//                HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + filename + "\"");
//
//        return outputStream -> {
//            int bytesRead;
//            byte[] buffer = new byte[BUFFER_SIZE];
//            while ((bytesRead = file.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, bytesRead);
//            }
//        };
//    }

}
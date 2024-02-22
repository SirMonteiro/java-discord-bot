package top.gabrielsouza.utils;

import java.util.ArrayList;
import java.util.List;

public class fileSplitter {
    public static List<byte[]> getFileChunks(byte[] mainFile) {
        int sizeMB = 1024 * 1024 * 20;
        List<byte[]> chunks = new ArrayList<>();
        for (int i = 0; i < mainFile.length; ) {
            byte[] chunk = new byte[Math.min(sizeMB, mainFile.length - i)];
            for (int j = 0; j < chunk.length; j++, i++) {
                chunk[j] = mainFile[i];
            }
            chunks.add(chunk);
        }
        return chunks;
    }
}



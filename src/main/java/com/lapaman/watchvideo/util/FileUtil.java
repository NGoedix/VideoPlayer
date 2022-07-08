package com.lapaman.watchvideo.util;

import com.lapaman.watchvideo.WatchVideoMod;

import java.io.*;

public class FileUtil {

    public static byte[] toBytes(File file) {
        byte[] bytes = new byte[(int) file.length()];

        try(FileInputStream fis = new FileInputStream(file)){
            fis.read(bytes);
        } catch (IOException e) {
            WatchVideoMod.getWatchVideoMod().getLogger().info("Algo pasa al escribir bytes:");
            WatchVideoMod.getWatchVideoMod().getLogger().info(e.getMessage());
        }
        return bytes;
    }

    public static File fromBytes(byte[] array) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("lapaman", null);
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(array);
        } catch (IOException e) {
            WatchVideoMod.getWatchVideoMod().getLogger().info("Algo pasa al leer bytes:");
            WatchVideoMod.getWatchVideoMod().getLogger().info(e.getMessage());
        }
        return tempFile;
    }

}

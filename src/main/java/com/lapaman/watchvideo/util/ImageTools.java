package com.lapaman.watchvideo.util;

import net.minecraftforge.common.DimensionManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageTools {

    public static byte[] toBytes(BufferedImage image) throws IOException {
        ImageIO.setUseCache(false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush();
        byte[] data = baos.toByteArray();
        baos.close();
        return data;
    }

    public static BufferedImage fromBytes(byte[] data) throws IOException {
        ImageIO.setUseCache(false);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        BufferedImage image = ImageIO.read(bais);
        bais.close();
        return image;
    }

    public static File getImageFileFromFolder(String filename) {
        File imageFolder = new File(DimensionManager.getCurrentSaveRootDirectory(), "resourcesLapaman");
        return new File(imageFolder, filename + ".png");
    }

    public static BufferedImage loadImage(String filename) throws IOException {
        File image = ImageTools.getImageFileFromFolder(filename);

        FileInputStream fis = new FileInputStream(image);

        BufferedImage bufferedImage = ImageIO.read(fis);

        if(bufferedImage == null){
            throw new IOException("BufferedImage is null");
        }

        return bufferedImage;
    }
}

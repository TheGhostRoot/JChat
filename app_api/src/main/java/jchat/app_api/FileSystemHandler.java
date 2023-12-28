package jchat.app_api;

import org.apache.tomcat.util.codec.binary.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileSystemHandler {
    private final String dir = "attachments/";

    FileSystemHandler() {
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            if (!dirFile.mkdir()) {
                API.logger.info("can't make attachments folder!");
            }
        }
    }

    public String generateName(File folder) {
        if (folder.exists() && folder.isDirectory()) {
            List<String> names = new ArrayList<>();
            for (File file : folder.listFiles()) {
                if (file.exists() && file.isFile()) {
                    names.add(file.getName().split("\\.")[0]);
                }
            }
            int g = 1;
            while (names.contains(String.valueOf(g)) && g == 0) {
                g = API.random.nextInt();
            }
            return String.valueOf(g);
        }
        return null;
    }

    public boolean saveFile(long user_id, boolean video, String base64) {
        File user_folder = new File(dir + user_id);
        if (!user_folder.exists()) {
            if (!user_folder.mkdirs()) {
                API.logger.info("Can't create user's folder");
                return false;
            }
        }
        String generatedName = generateName(user_folder);
        if (generatedName == null) {
            return false;
        }

        File file = new File(user_folder.getAbsolutePath() + "/" + generatedName + (video ? ".mp4" : ".jpeg"));
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    API.logger.info("Can't create user's file");
                    return false;
                }
            } catch (Exception e) {
                API.logger.info("Can't create user's file");
                return false;
            }
        }

        try {
            new FileOutputStream(file.getAbsolutePath()).write(Base64.decodeBase64(base64));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteFile(long user_id, String name) {
        File user_folder = new File(dir + user_id);
        if (user_folder.exists()) {
            File toDelete = new File(user_folder.getAbsolutePath() + "/" + name);
            if (toDelete.exists() && toDelete.isFile()) {
                return toDelete.delete();
            }

            return false;
        }

        return false;
    }

}
